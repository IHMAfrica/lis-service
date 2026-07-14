package moh.gov.zm.lis.iam.entity;

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
@Table(schema = "iam", name = "user_facility")
public class UserFacility {
    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private UUID userId;

    @Column("facility_id")
    private Long facilityId;

    @Column("is_active")
    private Boolean isActive;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
