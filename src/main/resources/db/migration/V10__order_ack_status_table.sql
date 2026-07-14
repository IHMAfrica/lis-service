-- -----------------------------------------------------------------------------
-- Lab-order acknowledgements consumed from the lab-orders-ack topic. Each HL7
-- ACK is recorded once (idempotent on its own MSH-10); ref_message_control_id
-- (MSA-2) points back to lab.order_status.message_control_id.
-- -----------------------------------------------------------------------------
CREATE TABLE lab.order_ack_status (
    id                          UUID        NOT NULL DEFAULT gen_random_uuid(),
    message_control_id          VARCHAR(64) NOT NULL,
    order_ack_date              DATE,
    order_ack_time              TIME,
    sending_facility_lab_code   VARCHAR(50),
    receiving_facility_mfl_code VARCHAR(30),
    ack_code                    VARCHAR(10),
    ref_message_control_id      VARCHAR(64),
    text_message                TEXT,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_order_ack_status                 PRIMARY KEY (id),
    CONSTRAINT uq_order_ack_status_message_control UNIQUE (message_control_id)
);

CREATE INDEX ix_order_ack_status_ref ON lab.order_ack_status (ref_message_control_id);

COMMENT ON TABLE  lab.order_ack_status                        IS 'Lab-order acknowledgements received from the laboratory (HL7 ACK).';
COMMENT ON COLUMN lab.order_ack_status.message_control_id     IS 'MSH-10 of the ACK message.';
COMMENT ON COLUMN lab.order_ack_status.sending_facility_lab_code   IS 'MSH-4.1 — the acknowledging laboratory.';
COMMENT ON COLUMN lab.order_ack_status.receiving_facility_mfl_code IS 'MSH-6.1 — the ordering facility (MFL code).';
COMMENT ON COLUMN lab.order_ack_status.ack_code               IS 'MSA-1 acknowledgement code (AA / AE / AR).';
COMMENT ON COLUMN lab.order_ack_status.ref_message_control_id IS 'MSA-2 — references lab.order_status.message_control_id.';
