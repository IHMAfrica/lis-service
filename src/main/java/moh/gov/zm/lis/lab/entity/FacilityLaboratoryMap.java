package moh.gov.zm.lis.lab.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "lab", name = "facility_laboratory_map")
public class FacilityLaboratoryMap {
    @Id
    @Column("id")
    private UUID id;

    @Column("facility_id")
    private Long facilityId;

    @Column("laboratory_test_id")
    private UUID laboratoryTestId;

    @Column("is_active")
    private Boolean isActive;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
