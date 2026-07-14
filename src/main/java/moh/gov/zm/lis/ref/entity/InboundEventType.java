package moh.gov.zm.lis.ref.entity;

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
@Table(schema = "ref", name = "inbound_event_type")
public class InboundEventType implements ReferenceData {
    @Id
    @Column("id")
    private Short id;

    @Column("code")
    private String code;

    @Column("name")
    private String name;

    @Column("source_service")
    private String sourceService;

    @Column("description")
    private String description;

    @Column("is_active")
    private boolean isActive;

    @Column("sort_order")
    private short sortOrder;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
