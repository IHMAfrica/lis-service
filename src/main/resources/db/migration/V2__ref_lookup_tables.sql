CREATE TABLE ref.outbound_event_type (
    id          SMALLINT     NOT NULL,
    code        VARCHAR(100) NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order  SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_outbound_event_type PRIMARY KEY (id),
    CONSTRAINT uq_outbound_event_type_code UNIQUE (code)
);

CREATE TABLE ref.inbound_event_type (
    id             SMALLINT     NOT NULL,
    code           VARCHAR(100) NOT NULL,
    name           VARCHAR(150) NOT NULL,
    source_service VARCHAR(100) NOT NULL,
    description    VARCHAR(500),
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order     SMALLINT     NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_inbound_event_type PRIMARY KEY (id),
    CONSTRAINT uq_inbound_event_type_code UNIQUE (code)
);

CREATE INDEX ix_inbound_event_type_source ON ref.inbound_event_type (source_service) WHERE is_active;

COMMENT ON TABLE ref.outbound_event_type IS 'Catalogue of domain events this service publishes via the outbox.';
COMMENT ON TABLE ref.inbound_event_type  IS 'Catalogue of external events this service consumes, keyed by originating service.';