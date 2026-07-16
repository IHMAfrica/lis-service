-- -----------------------------------------------------------------------------
-- Denormalised record of each lab order for the facility shipping list. The rich
-- DISA payload fields (patient name, age, sex, test type, requested / specimen
-- collected dates) are otherwise discarded after the HL7 OML^O21 is built, so
-- they are captured here at ingestion. The shipping list is scoped by facility
-- (mfl_code) and lab (lab_code) and bounded by the order-received time (created_at).
-- -----------------------------------------------------------------------------
CREATE TABLE lab.shipping_order (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    order_id                VARCHAR(100) NOT NULL,          -- shown as "Lab No"
    correlation_id          UUID,
    mfl_code                VARCHAR(30),                    -- sending facility ("From")
    lab_code                VARCHAR(5),                     -- receiving lab ("Refer To")
    full_name               VARCHAR(255),
    age                     INT,
    sex                     VARCHAR(10),                    -- M / F
    test_type               VARCHAR(255),                   -- DISA investigation test name
    requested_date          VARCHAR(50),                    -- DISA payload requestedDate (verbatim)
    specimen_collected_date VARCHAR(50),                    -- DISA payload specimenCollectedDate (verbatim)
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_shipping_order PRIMARY KEY (id)
);

-- Report access path: facility + lab, bounded by received time.
CREATE INDEX ix_shipping_order_scope ON lab.shipping_order (mfl_code, lab_code, created_at);

COMMENT ON TABLE  lab.shipping_order          IS 'Per-order data captured at ingestion for the facility shipping list.';
COMMENT ON COLUMN lab.shipping_order.order_id IS 'Order id; printed as the "Lab No" column.';
