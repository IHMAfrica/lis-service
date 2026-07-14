-- -----------------------------------------------------------------------------
-- Record of lab orders successfully enqueued for publishing to the lab-orders
-- topic (written in the same transaction as the outbox insert). The HL7 message
-- control id (MSH-10) is retained so a future lab-order acknowledgement (MSA-2)
-- can be matched back to the originating order.
-- -----------------------------------------------------------------------------
CREATE TABLE lab.order_status (
    id                 UUID         NOT NULL DEFAULT gen_random_uuid(),
    message_control_id VARCHAR(64)  NOT NULL,
    order_id           VARCHAR(100) NOT NULL,
    order_date         DATE,
    order_time         TIME,
    mfl_code           VARCHAR(30),
    lab_code           VARCHAR(5),
    correlation_id     UUID         NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_order_status                 PRIMARY KEY (id),
    CONSTRAINT uq_order_status_message_control UNIQUE (message_control_id)
);

CREATE INDEX ix_order_status_order_id     ON lab.order_status (order_id);
CREATE INDEX ix_order_status_correlation  ON lab.order_status (correlation_id);

COMMENT ON TABLE  lab.order_status                    IS 'Lab orders successfully enqueued for publishing to the lab-orders topic.';
COMMENT ON COLUMN lab.order_status.message_control_id IS 'HL7 MSH-10 of the OML^O21; matched against MSA-2 of the lab-order acknowledgement.';
COMMENT ON COLUMN lab.order_status.correlation_id     IS 'System correlation id; also the outbox correlation / Kafka message key.';
