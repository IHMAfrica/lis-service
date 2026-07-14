-- -----------------------------------------------------------------------------
-- Geographic reference data: province -> district -> facility.
-- Read-only from the application's perspective (sourced from the national MFL).
-- -----------------------------------------------------------------------------
CREATE TABLE ref.province (
    id   SMALLSERIAL,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(3),
    CONSTRAINT pk_province      PRIMARY KEY (id),
    CONSTRAINT uq_province_code UNIQUE (code)
);

CREATE TABLE ref.district (
    id          BIGSERIAL,
    name        VARCHAR(50) NOT NULL,
    province_id SMALLINT    NOT NULL,
    code        VARCHAR(10),
    CONSTRAINT pk_district           PRIMARY KEY (id),
    CONSTRAINT fk_district_province  FOREIGN KEY (province_id) REFERENCES ref.province (id)
);

-- District codes are not guaranteed unique in the source data (a placeholder is
-- used where a code is unknown), so this is a plain lookup index, not a UNIQUE.
CREATE INDEX ix_district_province ON ref.district (province_id);
CREATE INDEX ix_district_code     ON ref.district (code) WHERE code IS NOT NULL;

CREATE TABLE ref.facility (
    id          BIGSERIAL,
    name        VARCHAR(256) NOT NULL,
    district_id BIGINT,
    hmis_code   VARCHAR(30),
    mfl_code    VARCHAR(30),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_facility          PRIMARY KEY (id),
    CONSTRAINT fk_facility_district FOREIGN KEY (district_id) REFERENCES ref.district (id)
);

CREATE INDEX ix_facility_district ON ref.facility (district_id);
CREATE INDEX ix_facility_hmis     ON ref.facility (hmis_code) WHERE hmis_code IS NOT NULL;
CREATE INDEX ix_facility_mfl      ON ref.facility (mfl_code)  WHERE mfl_code IS NOT NULL;

COMMENT ON TABLE ref.province IS 'Provinces (top-level administrative division).';
COMMENT ON TABLE ref.district IS 'Districts within a province.';
COMMENT ON TABLE ref.facility IS 'Health facilities that place lab orders / receive results.';
