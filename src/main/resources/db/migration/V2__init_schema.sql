BEGIN;

CREATE SCHEMA IF NOT EXISTS ref;
CREATE SCHEMA IF NOT EXISTS lab;
CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE IF NOT EXISTS ref.province (
    id SMALLSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    code VARCHAR(3)
);

CREATE TABLE IF NOT EXISTS ref.district (
    id BIGSERIAL PRIMARY KEY,
    name  VARCHAR(50) NOT NULL,
    province_id SMALLINT REFERENCES ref.province(id) NOT NULL,
    code  VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS ref.facility (
    id BIGSERIAL PRIMARY KEY,
    name  VARCHAR(256) NOT NULL,
    district_id BIGINT REFERENCES ref.district(id),
    hmis_code   VARCHAR(30),
    mfl_code    VARCHAR(30),

    is_active   BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS lab.lab_type (
    id SMALLSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,

    is_active         BOOLEAN DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lab.test (
    id BIGSERIAL PRIMARY KEY,
    name  VARCHAR(256) NOT NULL,
    loinc_code  VARCHAR(20) UNIQUE NOT NULL,
    abbreviation  VARCHAR(50),
    short_title   VARCHAR(50),
    is_composite_test   BOOLEAN DEFAULT FALSE,

    is_active         BOOLEAN DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lab.laboratory (
    id          SMALLSERIAL PRIMARY KEY,
    lab_code    VARCHAR(5) UNIQUE NOT NULL,
    lab_name    VARCHAR(100) NOT NULL,
    district_id BIGINT REFERENCES ref.district(id) NOT NULL,
    comment     VARCHAR(50),
    lab_type_id SMALLINT REFERENCES lab.lab_type(id),

    is_active         BOOLEAN DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lab.laboratory_test (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    laboratory_id SMALLINT REFERENCES lab.laboratory(id) NOT NULL,
    test_id BIGINT REFERENCES lab.test(id) NOT NULL,

    is_active         BOOLEAN DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_lab_id_test_id UNIQUE (laboratory_id, test_id)
);

CREATE TABLE IF NOT EXISTS lab.facility_laboratory_map (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    facility_id BIGINT REFERENCES ref.facility(id) NOT NULL,
    laboratory_test_id UUID REFERENCES lab.laboratory_test(id) NOT NULL,

    is_active         BOOLEAN DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_facility_id_lab_test_id UNIQUE (facility_id, laboratory_test_id)
);

CREATE TABLE IF NOT EXISTS lab.order_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    VARCHAR(50) NOT NULL,
    order_date  DATE NOT NULL,
    order_time  TIME NOT NULL,
    mfl_code    VARCHAR(30) NOT NULL,
    lab_code    VARCHAR(10) NOT NULL
);

CREATE TABLE audit.audit_logs (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_user_id  VARCHAR(255),
    entity_type       VARCHAR(100) NOT NULL,
    action            VARCHAR(50)  NOT NULL,
    changes           JSONB,
    reason            TEXT,
    ip_address        VARCHAR(45),
    user_agent        TEXT,
    performed_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMIT;