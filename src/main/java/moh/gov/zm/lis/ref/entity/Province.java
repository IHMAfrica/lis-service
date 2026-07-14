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
@Table(schema = "ref", name = "province")
public class Province {
    @Id
    @Column("id")
    private Short id;

    @Column("name")
    private String name;

    @Column("code")
    private String code;
}
