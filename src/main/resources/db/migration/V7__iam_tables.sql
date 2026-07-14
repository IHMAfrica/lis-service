-- -----------------------------------------------------------------------------
-- Application users and their facility assignments. A user is keyed by the
-- external identity (user_id UUID, e.g. the IdP subject) and may serve several
-- facilities. Notifications are scoped to the users of a facility.
-- -----------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS iam;
COMMENT ON SCHEMA iam IS 'Application users and their facility assignments.';

CREATE TABLE iam.users (
    id         BIGSERIAL,
    user_id    UUID        NOT NULL,
    is_active  BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_users         PRIMARY KEY (id),
    CONSTRAINT uq_users_user_id UNIQUE (user_id)
);

CREATE TABLE iam.user_facility (
    id          BIGSERIAL,
    user_id     UUID        NOT NULL,
    facility_id BIGINT      NOT NULL,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_user_facility           PRIMARY KEY (id),
    CONSTRAINT uq_user_facility           UNIQUE (user_id, facility_id),
    CONSTRAINT fk_user_facility_user      FOREIGN KEY (user_id)     REFERENCES iam.users (user_id),
    CONSTRAINT fk_user_facility_facility  FOREIGN KEY (facility_id) REFERENCES ref.facility (id)
);

CREATE INDEX ix_user_facility_facility ON iam.user_facility (facility_id) WHERE is_active;
CREATE INDEX ix_user_facility_user     ON iam.user_facility (user_id);

COMMENT ON TABLE iam.users         IS 'Application users, keyed by external identity (user_id UUID).';
COMMENT ON TABLE iam.user_facility IS 'Facility assignments; a user may serve multiple facilities.';
