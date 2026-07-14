package moh.gov.zm.lis.disa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.lab.entity.Test;
import moh.gov.zm.lis.lab.service.TestService;
import moh.gov.zm.lis.ref.entity.Facility;
import moh.gov.zm.lis.ref.service.FacilityService;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Resolves the authoritative lab code, LOINC code and MFL code for a DISA lab
 * order from this application's own data — never trusting the codes carried in
 * the payload.
 *
 * <ul>
 *   <li>{@code mflCode}   — {@code ref.facility.mfl_code}, matched on {@code hmisCode}.</li>
 *   <li>{@code loincCode} — {@code lab.test.loinc_code}, matched on {@code investigationTestName}.</li>
 *   <li>{@code labCode}   — the {@code lab.laboratory} that offers <em>this test</em> to <em>this
 *       facility</em> (facility_laboratory_map → laboratory_test → laboratory), so the order is
 *       routed to a lab that actually runs it.</li>
 * </ul>
 *
 * <p>Facility and test are read from the Redis-cached reference snapshots; the
 * mutable facility_laboratory_map / laboratory_test rows are joined fresh in the DB.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DisaLogResolutionService {
    private final FacilityService facilityService;
    private final TestService testService;
    private final DatabaseClient databaseClient;

    public Mono<ResolvedDisaLog> resolve(DisaLogPayload payload) {
        Mono<Facility> facility = resolveFacility(payload.getHmisCode());
        Mono<Test> test = resolveTest(payload.getInvestigationTestName());

        return Mono.zip(facility, test).flatMap(t -> {
            Facility f = t.getT1();
            Test te = t.getT2();
            String mflCode = f.getMflCode() != null ? f.getMflCode() : "";

            return resolveLabCode(f.getId(), te.getId(), payload.getHmisCode(), payload.getInvestigationTestName())
                    .doOnNext(labCode -> log.debug("Resolved order: hmis='{}' → mfl='{}', test='{}' → loinc='{}', lab='{}'",
                            payload.getHmisCode(), mflCode, te.getName(), te.getLoincCode(), labCode))
                    .map(labCode -> new ResolvedDisaLog(payload, labCode, te.getLoincCode(), mflCode));
        });
    }

    private Mono<Facility> resolveFacility(String hmisCode) {
        return facilityService.cachedAll()
                .flatMap(facilities -> facilities.stream()
                        .filter(f -> Boolean.TRUE.equals(f.getIsActive()))
                        .filter(f -> hmisCode != null && hmisCode.equals(f.getHmisCode()))
                        .findFirst()
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new IllegalArgumentException(
                                "No active facility found for HMIS code: " + hmisCode))));
    }

    private Mono<Test> resolveTest(String testName) {
        return testService.cachedAll()
                .flatMap(tests -> tests.stream()
                        .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
                        .filter(t -> testName != null && testName.equalsIgnoreCase(t.getName()))
                        .findFirst()
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new IllegalArgumentException(
                                "No active test found with name: " + testName))));
    }

    private Mono<String> resolveLabCode(Long facilityId, Long testId, String hmisCode, String testName) {
        return databaseClient.sql("""
                        SELECT l.lab_code
                        FROM lab.facility_laboratory_map flm
                        JOIN lab.laboratory_test lt ON lt.id = flm.laboratory_test_id
                        JOIN lab.laboratory     l  ON l.id  = lt.laboratory_id
                        WHERE flm.facility_id = :facilityId
                          AND lt.test_id = :testId
                          AND flm.is_active AND lt.is_active AND l.is_active
                        ORDER BY l.lab_code
                        LIMIT 1
                        """)
                .bind("facilityId", facilityId)
                .bind("testId", testId)
                .map(row -> Objects.requireNonNull(row.get("lab_code", String.class)))
                .one()
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No active laboratory offers test '" + testName + "' for facility with HMIS code: " + hmisCode)));
    }
}
