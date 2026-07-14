package moh.gov.zm.lis.lab.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "lab", name = "laboratory")
public class Laboratory {
    @Id
    @Column("id")
    private Short id;

    @Column("lab_code")
    private String labCode;

    @Column("lab_name")
    private String labName;

    @Column("district_id")
    private Long districtId;

    @Column("comment")
    private String comment;

    @Column("lab_type_id")
    private Short labTypeId;

    @Column("is_active")
    private Boolean isActive;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
