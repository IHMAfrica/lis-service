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
@Table(schema = "ref", name = "district")
public class District {
    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("province_id")
    private Short provinceId;

    @Column("code")
    private String code;
}
