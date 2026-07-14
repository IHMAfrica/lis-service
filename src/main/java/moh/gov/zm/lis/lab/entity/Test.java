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
@Table(schema = "lab", name = "test")
public class Test {
    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("loinc_code")
    private String loincCode;

    @Column("abbreviation")
    private String abbreviation;

    @Column("short_title")
    private String shortTitle;

    @Column("is_composite_test")
    private Boolean isCompositeTest;

    @Column("is_active")
    private Boolean isActive;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
