package moh.gov.zm.lis.lab.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PageMapper;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.exception.ConflictException;
import moh.gov.zm.lis.exception.ResourceNotFoundException;
import moh.gov.zm.lis.lab.dto.FacilityLaboratoryMapDTO;
import moh.gov.zm.lis.lab.entity.FacilityLaboratoryMap;
import moh.gov.zm.lis.lab.repository.FacilityLaboratoryMapRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacilityLaboratoryMapService {
    private final FacilityLaboratoryMapRepository facilityLaboratoryMapRepository;
    private final R2dbcEntityTemplate template;

    /**
     * Distinct lab codes a facility (by MFL code) has at least one active mapping to,
     * resolved facility → facility_laboratory_map → laboratory_test → laboratory.
     */
    public Flux<String> labCodesForFacility(String mflCode) {
        return template.getDatabaseClient().sql("""
                        SELECT DISTINCT l.lab_code
                        FROM lab.facility_laboratory_map flm
                        JOIN lab.laboratory_test lt ON lt.id = flm.laboratory_test_id
                        JOIN lab.laboratory l      ON l.id = lt.laboratory_id
                        JOIN ref.facility f        ON f.id = flm.facility_id
                        WHERE f.mfl_code = :mflCode AND flm.is_active = TRUE
                        ORDER BY l.lab_code
                        """)
                .bind("mflCode", mflCode)
                .map(row -> row.get("lab_code", String.class))
                .all();
    }

    public Mono<PagedResponse<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse>> list(
            int page, int size, Long facilityId, UUID laboratoryTestId, Boolean isActive) {
        List<Criteria> parts = new ArrayList<>();
        if (facilityId != null) {
            parts.add(Criteria.where("facilityId").is(facilityId));
        }
        if (laboratoryTestId != null) {
            parts.add(Criteria.where("laboratoryTestId").is(laboratoryTestId));
        }
        if (isActive != null) {
            parts.add(Criteria.where("isActive").is(isActive));
        }
        Query base = parts.isEmpty() ? Query.empty() : Query.query(Criteria.from(parts));
        Query paged = base.with(PageRequest.of(page, size, Sort.by("createdAt")));

        return template.select(paged, FacilityLaboratoryMap.class)
                .map(this::toResponse)
                .collectList()
                .zipWith(template.count(base, FacilityLaboratoryMap.class))
                .map(t -> PageMapper.of(t.getT1(), t.getT2(), page, size));
    }

    public Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> findById(UUID id) {
        return load(id).map(this::toResponse);
    }

    public Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> create(
            FacilityLaboratoryMapDTO.CreateFacilityLaboratoryMap request) {
        return facilityLaboratoryMapRepository
                .existsByFacilityIdAndLaboratoryTestId(request.getFacilityId(), request.getLaboratoryTestId())
                .flatMap(exists -> exists
                        ? Mono.error(new ConflictException(
                        "Facility %d is already mapped to laboratory-test %s"
                                .formatted(request.getFacilityId(), request.getLaboratoryTestId())))
                        : facilityLaboratoryMapRepository.save(FacilityLaboratoryMap.builder()
                        .facilityId(request.getFacilityId())
                        .laboratoryTestId(request.getLaboratoryTestId())
                        .isActive(request.getIsActive() == null || request.getIsActive())
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()))
                .map(this::toResponse);
    }

    public Mono<FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse> update(
            UUID id, FacilityLaboratoryMapDTO.UpdateFacilityLaboratoryMap request) {
        return load(id)
                .flatMap(existing -> {
                    if (request.getIsActive() != null) {
                        existing.setIsActive(request.getIsActive());
                    }
                    existing.setUpdatedAt(OffsetDateTime.now());
                    return facilityLaboratoryMapRepository.save(existing);
                })
                .map(this::toResponse);
    }

    public Mono<Void> delete(UUID id) {
        return load(id).flatMap(facilityLaboratoryMapRepository::delete);
    }

    private Mono<FacilityLaboratoryMap> load(UUID id) {
        return facilityLaboratoryMapRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("FacilityLaboratoryMap", String.valueOf(id))));
    }

    private FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse toResponse(FacilityLaboratoryMap e) {
        return FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse.builder()
                .id(e.getId())
                .facilityId(e.getFacilityId())
                .laboratoryTestId(e.getLaboratoryTestId())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
