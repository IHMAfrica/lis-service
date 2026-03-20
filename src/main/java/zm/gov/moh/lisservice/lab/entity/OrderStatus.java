package zm.gov.moh.lisservice.lab.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "lab", name = "order_status")
public class OrderStatus {
    @Id
    private UUID id;
    private String orderId;
    private LocalDate orderDate;
    private LocalTime orderTime;
    private String mflCode;
    private String labCode;
}
