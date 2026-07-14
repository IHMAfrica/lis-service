CREATE TABLE messaging.outbound_event_outbox (
    id             UUID        NOT NULL DEFAULT gen_random_uuid(),
    event_type_id  SMALLINT     NOT NULL,
    topic          VARCHAR(255) NOT NULL,
    payload        TEXT         NOT NULL,
    correlation_id UUID         NOT NULL,
    is_published   BOOLEAN      NOT NULL DEFAULT FALSE,
    published_at   TIMESTAMPTZ,
    retry_count    SMALLINT     NOT NULL DEFAULT 0,
    last_error     TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     UUID,
    CONSTRAINT pk_outbound_event_outbox PRIMARY KEY (id),
    CONSTRAINT fk_outbound_outbox_event_type
        FOREIGN KEY (event_type_id) REFERENCES ref.outbound_event_type (id),
    CONSTRAINT ck_outbound_outbox_retry CHECK (retry_count >= 0)
);

CREATE INDEX ix_outbox_unpublished
    ON messaging.outbound_event_outbox (created_at)
    WHERE is_published = FALSE;

CREATE INDEX ix_outbox_correlation ON messaging.outbound_event_outbox (correlation_id);

COMMENT ON TABLE  messaging.outbound_event_outbox          IS 'Transactional outbox; rows are published to Kafka by the relay scheduler.';
COMMENT ON COLUMN messaging.outbound_event_outbox.payload  IS 'Raw serialized Kafka value (already a string), sent verbatim.';

CREATE TABLE messaging.inbound_event_log (
    id                UUID        NOT NULL DEFAULT gen_random_uuid(),
    message_id        VARCHAR(255) NOT NULL,
    topic             VARCHAR(255) NOT NULL,
    event_type_id     SMALLINT,
    event_type_code   VARCHAR(100),
    source_service    VARCHAR(100),
    correlation_id    UUID,
    payload           TEXT,
    processing_status VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    error_message     TEXT,
    received_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processed_at      TIMESTAMPTZ,
    CONSTRAINT pk_inbound_event_log PRIMARY KEY (id),
    CONSTRAINT uq_inbound_event_message_id UNIQUE (message_id),
    CONSTRAINT fk_inbound_log_event_type
        FOREIGN KEY (event_type_id) REFERENCES ref.inbound_event_type (id),
    CONSTRAINT ck_inbound_log_status
        CHECK (processing_status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'FAILED', 'SKIPPED', 'DEAD_LETTERED'))
);

-- Reprocessing / monitoring access paths.
CREATE INDEX ix_inbound_status_received
    ON messaging.inbound_event_log (processing_status, received_at);
CREATE INDEX ix_inbound_source_service ON messaging.inbound_event_log (source_service);
CREATE INDEX ix_inbound_event_type_code ON messaging.inbound_event_log (event_type_code);
CREATE INDEX ix_inbound_correlation    ON messaging.inbound_event_log (correlation_id);

COMMENT ON TABLE  messaging.inbound_event_log            IS 'Consumed-event log providing idempotency (unique message_id) and processing state.';
COMMENT ON COLUMN messaging.inbound_event_log.message_id IS 'Broker/business message id used for de-duplication.';
COMMENT ON COLUMN messaging.inbound_event_log.payload    IS 'Raw consumed message (HL7 v2.5 text), stored verbatim for audit / replay.';
