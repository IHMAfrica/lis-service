-- -----------------------------------------------------------------------------
-- Laboratory catalogue: lab types, tests, laboratories and the mappings that
-- describe which tests a lab offers and which labs a facility is served by.
-- -----------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS lab;
COMMENT ON SCHEMA lab IS 'Laboratory catalogue: labs, tests and their facility mappings.';

CREATE TABLE lab.lab_type (
    id         SMALLSERIAL,
    name       VARCHAR(50) NOT NULL,
    is_active  BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_lab_type      PRIMARY KEY (id),
    CONSTRAINT uq_lab_type_name UNIQUE (name)
);

CREATE TABLE lab.test (
    id                BIGSERIAL,
    name              VARCHAR(256) NOT NULL,
    loinc_code        VARCHAR(20)  NOT NULL,
    abbreviation      VARCHAR(50),
    short_title       VARCHAR(50),
    is_composite_test BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_test            PRIMARY KEY (id),
    CONSTRAINT uq_test_loinc_code UNIQUE (loinc_code)
);

COMMENT ON COLUMN lab.test.loinc_code IS 'LOINC code — the coding standard used for test ordering.';

CREATE TABLE lab.laboratory (
    id          SMALLSERIAL,
    lab_code    VARCHAR(5)   NOT NULL,
    lab_name    VARCHAR(100) NOT NULL,
    district_id BIGINT       NOT NULL,
    comment     VARCHAR(50),
    lab_type_id SMALLINT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_laboratory          PRIMARY KEY (id),
    CONSTRAINT uq_laboratory_lab_code UNIQUE (lab_code),
    CONSTRAINT fk_laboratory_district FOREIGN KEY (district_id) REFERENCES ref.district (id),
    CONSTRAINT fk_laboratory_lab_type FOREIGN KEY (lab_type_id) REFERENCES lab.lab_type (id)
);

CREATE INDEX ix_laboratory_district ON lab.laboratory (district_id);
CREATE INDEX ix_laboratory_lab_type ON lab.laboratory (lab_type_id);

CREATE TABLE lab.laboratory_test (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    laboratory_id SMALLINT    NOT NULL,
    test_id       BIGINT      NOT NULL,
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_laboratory_test      PRIMARY KEY (id),
    CONSTRAINT uq_laboratory_test      UNIQUE (laboratory_id, test_id),
    CONSTRAINT fk_laboratory_test_lab  FOREIGN KEY (laboratory_id) REFERENCES lab.laboratory (id),
    CONSTRAINT fk_laboratory_test_test FOREIGN KEY (test_id)       REFERENCES lab.test (id)
);

CREATE INDEX ix_laboratory_test_test ON lab.laboratory_test (test_id);

CREATE TABLE lab.facility_laboratory_map (
    id                 UUID        NOT NULL DEFAULT gen_random_uuid(),
    facility_id        BIGINT      NOT NULL,
    laboratory_test_id UUID        NOT NULL,
    is_active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_facility_laboratory_map  PRIMARY KEY (id),
    CONSTRAINT uq_facility_laboratory_test UNIQUE (facility_id, laboratory_test_id),
    CONSTRAINT fk_flm_facility             FOREIGN KEY (facility_id)        REFERENCES ref.facility (id),
    CONSTRAINT fk_flm_laboratory_test      FOREIGN KEY (laboratory_test_id) REFERENCES lab.laboratory_test (id)
);

CREATE INDEX ix_flm_laboratory_test ON lab.facility_laboratory_map (laboratory_test_id);

COMMENT ON TABLE lab.lab_type                IS 'Categories of laboratory (e.g. reference, hub, spoke).';
COMMENT ON TABLE lab.test                    IS 'Catalogue of orderable/reportable tests, keyed by LOINC.';
COMMENT ON TABLE lab.laboratory              IS 'Laboratories that fulfil orders and produce results.';
COMMENT ON TABLE lab.laboratory_test         IS 'Tests offered by a given laboratory.';
COMMENT ON TABLE lab.facility_laboratory_map IS 'Which lab-offered tests a facility is served by.';
