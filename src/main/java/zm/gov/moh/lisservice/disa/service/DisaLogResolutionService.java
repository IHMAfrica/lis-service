package zm.gov.moh.lisservice.disa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.lab.entity.Laboratory;
import zm.gov.moh.lisservice.lab.entity.Test;
import zm.gov.moh.lisservice.lab.repository.FacilityLaboratoryMapRepository;
import zm.gov.moh.lisservice.lab.repository.LaboratoryRepository;
import zm.gov.moh.lisservice.lab.repository.LaboratoryTestRepository;
import zm.gov.moh.lisservice.lab.repository.TestRepository;
import zm.gov.moh.lisservice.ref.entity.Facility;
import zm.gov.moh.lisservice.ref.repository.FacilityRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisaLogResolutionService {

    private final FacilityRepository facilityRepository;
    private final FacilityLaboratoryMapRepository facilityLaboratoryMapRepository;
    private final LaboratoryTestRepository laboratoryTestRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final TestRepository testRepository;

    /**
     * Resolves a single {@link DisaLogPayload} into a {@link ResolvedDisaLog} by
     * looking up the authoritative lab code, MFL code, and LOINC code from this
     * application's reference data.
     *
     * <p>Resolution chain for lab / MFL code:
     * hmisCode → ref.facility (→ mfl_code captured here) → facility_laboratory_map
     *          → laboratory_test → laboratory.lab_code
     *
     * <p>LOINC code is resolved by matching investigationTestName against lab.test.name.
     */
    public Mono<ResolvedDisaLog> resolve(DisaLogPayload payload) {
        return resolveFacility(payload.getHmisCode())
                .flatMap(facility -> {
                    String mflCode = facility.getMflCode() != null ? facility.getMflCode() : "";
                    log.debug("Resolved facility '{}' (mfl='{}') for HMIS '{}'",
                            facility.getId(), mflCode, payload.getHmisCode());

                    Mono<String> labCodeMono = resolveLabCode(facility, payload.getHmisCode());
                    Mono<String> loincMono = resolveLoincCode(payload.getInvestigationTestName());

                    return Mono.zip(labCodeMono, loincMono)
                            .map(tuple -> new ResolvedDisaLog(payload, tuple.getT1(), tuple.getT2(), mflCode));
                });
    }

    // -----------------------------------------------------------------------
    // Facility: hmisCode → ref.facility
    // -----------------------------------------------------------------------

    private Mono<Facility> resolveFacility(String hmisCode) {
        return facilityRepository.findByHmisCodeAndIsActiveTrue(hmisCode)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No active facility found for HMIS code: " + hmisCode)));
    }

    // -----------------------------------------------------------------------
    // Lab code: facility → facility_laboratory_map → laboratory_test → laboratory.lab_code
    // -----------------------------------------------------------------------

    private Mono<String> resolveLabCode(Facility facility, String hmisCode) {
        return facilityLaboratoryMapRepository
                .findFirstByFacilityIdAndIsActiveTrue(facility.getId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No active facility-laboratory mapping found for HMIS code: " + hmisCode)))
                .flatMap(map -> laboratoryTestRepository.findByIdAndIsActiveTrue(map.getLaboratoryTestId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No active laboratory-test entry found for HMIS code: " + hmisCode)))
                .flatMap(lt -> laboratoryRepository.findByIdAndIsActiveTrue(lt.getLaboratoryId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Laboratory is inactive or not found for HMIS code: " + hmisCode)))
                .map(Laboratory::getLabCode);
    }

    // -----------------------------------------------------------------------
    // LOINC code: test_name (case-insensitive) → lab.test.loinc_code
    // -----------------------------------------------------------------------

    private Mono<String> resolveLoincCode(String testName) {
        return testRepository.findFirstByNameIgnoreCaseAndIsActiveTrue(testName)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No active test found with name: " + testName)))
                .map(Test::getLoincCode);
    }
}
