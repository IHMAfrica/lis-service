-- -----------------------------------------------------------------------------
-- Lab results consumed from the lab-results topic (HL7 ORU^R01).
--
-- order_status gains the parameters needed to reconcile *unsolicited* results
-- (results whose placer order number does not match a known order): patient
-- identifier and test LOINC (both known at order time), plus the filler / lab
-- accession number captured from the first result seen for an order.
-- -----------------------------------------------------------------------------
ALTER TABLE lab.order_status ADD COLUMN patient_identifier  VARCHAR(100);
ALTER TABLE lab.order_status ADD COLUMN test_loinc          VARCHAR(20);
ALTER TABLE lab.order_status ADD COLUMN filler_order_number VARCHAR(64);

-- Secondary-reconciliation access path: patient + test + facility, filtered by
-- collection date within the observed turn-around window.
CREATE INDEX ix_order_status_reconcile
    ON lab.order_status (patient_identifier, test_loinc, mfl_code);

-- One row per received ORU message.
CREATE TABLE lab.lab_result (
    id                    UUID         NOT NULL DEFAULT gen_random_uuid(),
    message_control_id    VARCHAR(64)  NOT NULL,          -- ORU MSH-10
    placer_order_number   VARCHAR(64),                    -- ORC-2 / OBR-2 (our order id when solicited)
    filler_order_number   VARCHAR(64),                    -- ORC-3 / OBR-3 (lab accession)
    lab_code              VARCHAR(50),                    -- MSH-4.2
    ordering_mfl_code     VARCHAR(30),                    -- MSH-6.1
    ordering_hmis_code    VARCHAR(30),                    -- PV1-3.4
    patient_identifier    VARCHAR(100),                   -- PID-3.1
    patient_name          VARCHAR(255),                   -- PID-5
    patient_dob           DATE,                           -- PID-7
    patient_sex           VARCHAR(10),                    -- PID-8
    test_loinc            VARCHAR(20),                    -- OBR-4.1
    test_name             VARCHAR(255),                   -- OBR-4.2
    order_control         VARCHAR(10),                    -- ORC-1
    order_status_code     VARCHAR(10),                    -- ORC-5
    result_status         VARCHAR(10),                    -- OBR-25
    message_kind          VARCHAR(20)  NOT NULL,          -- RESULT | STATUS_UPDATE
    specimen_collected_at TIMESTAMPTZ,
    specimen_received_at  TIMESTAMPTZ,
    reconciliation_status VARCHAR(20)  NOT NULL,          -- RECONCILED | UNSOLICITED
    match_method          VARCHAR(20),                    -- PLACER | SECONDARY | NONE
    candidate_count       INT          NOT NULL DEFAULT 0,
    order_status_id       UUID,                           -- set when reconciled
    correlation_id        UUID,
    forward_status        VARCHAR(20)  NOT NULL DEFAULT 'NOT_APPLICABLE', -- NOT_APPLICABLE | PENDING | SENT | FAILED
    forward_attempts      SMALLINT     NOT NULL DEFAULT 0,
    forwarded_at          TIMESTAMPTZ,
    forward_error         TEXT,
    raw_message           TEXT,
    received_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_lab_result                 PRIMARY KEY (id),
    CONSTRAINT uq_lab_result_message_control UNIQUE (message_control_id),
    CONSTRAINT fk_lab_result_order           FOREIGN KEY (order_status_id) REFERENCES lab.order_status (id),
    CONSTRAINT ck_lab_result_forward_attempts CHECK (forward_attempts >= 0)
);

CREATE INDEX ix_lab_result_placer      ON lab.lab_result (placer_order_number);
CREATE INDEX ix_lab_result_patient     ON lab.lab_result (patient_identifier);
CREATE INDEX ix_lab_result_reconcile   ON lab.lab_result (reconciliation_status);
CREATE INDEX ix_lab_result_forward     ON lab.lab_result (forward_status) WHERE forward_status IN ('PENDING', 'FAILED');

-- One row per OBX observation.
CREATE TABLE lab.lab_result_observation (
    id                     UUID        NOT NULL DEFAULT gen_random_uuid(),
    lab_result_id          UUID        NOT NULL,
    set_id                 INT,                            -- OBX-1
    value_type             VARCHAR(10),                    -- OBX-2 (NM/ST/CE/DT/TM/TX)
    observation_loinc      VARCHAR(20),                    -- OBX-3.1
    observation_local_code VARCHAR(50),                    -- OBX-3.4
    observation_text       VARCHAR(255),                   -- OBX-3.2
    value                  TEXT,                           -- OBX-5
    numeric_value          NUMERIC,                        -- OBX-5 parsed when value_type = NM
    units                  VARCHAR(50),                    -- OBX-6
    reference_range        VARCHAR(100),                   -- OBX-7
    abnormal_flags         VARCHAR(20),                    -- OBX-8
    observation_status     VARCHAR(10),                    -- OBX-11
    observed_at            TIMESTAMPTZ,                     -- OBX-14
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_lab_result_observation PRIMARY KEY (id),
    CONSTRAINT fk_observation_result     FOREIGN KEY (lab_result_id) REFERENCES lab.lab_result (id) ON DELETE CASCADE
);

CREATE INDEX ix_lab_result_observation_result ON lab.lab_result_observation (lab_result_id);

COMMENT ON TABLE lab.lab_result             IS 'HL7 ORU^R01 lab results received from the laboratory.';
COMMENT ON TABLE lab.lab_result_observation IS 'Individual OBX observations belonging to a lab result.';
