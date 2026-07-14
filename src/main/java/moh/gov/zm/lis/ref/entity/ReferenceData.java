package moh.gov.zm.lis.ref.entity;

import java.time.OffsetDateTime;

/**
 * Common contract implemented by every {@code ref} lookup entity. It lets the
 * generic reference-data CRUD layer (service + controller) operate over any
 * lookup table without per-entity duplication.
 */
public interface ReferenceData {
    Short getId();

    void setId(Short id);

    String getCode();

    void setCode(String code);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    boolean isActive();

    void setActive(boolean active);

    short getSortOrder();

    void setSortOrder(short sortOrder);

    OffsetDateTime getCreatedAt();

    void setCreatedAt(OffsetDateTime createdAt);
}
