package zm.gov.moh.lisservice.ref.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "ref", name = "facility")
public class Facility {
    @Id
    private Long id;
    private String name;
    private Long districtId;
    private String hmisCode;
    private String mflCode;
    private Boolean isActive;
}
