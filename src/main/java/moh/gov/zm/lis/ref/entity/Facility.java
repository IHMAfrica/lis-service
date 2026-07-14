package moh.gov.zm.lis.ref.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "ref", name = "facility")
public class Facility {
    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("district_id")
    private Long districtId;

    @Column("hmis_code")
    private String hmisCode;

    @Column("mfl_code")
    private String mflCode;

    @Column("is_active")
    private Boolean isActive;
}
