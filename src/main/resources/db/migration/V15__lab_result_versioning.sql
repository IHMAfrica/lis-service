-- -----------------------------------------------------------------------------
-- Supersession / versioning for lab results.
--
-- Successive ORU messages for the same logical result (preliminary -> final ->
-- corrected) are chained by result_key. The newest is is_current = TRUE; older
-- versions are marked superseded. result_key groups by the reconciled order (or,
-- failing that, the lab accession) together with the test LOINC.
-- -----------------------------------------------------------------------------
ALTER TABLE lab.lab_result ADD COLUMN result_key    VARCHAR(200);
ALTER TABLE lab.lab_result ADD COLUMN version       INT         NOT NULL DEFAULT 1;
ALTER TABLE lab.lab_result ADD COLUMN is_current    BOOLEAN     NOT NULL DEFAULT TRUE;
ALTER TABLE lab.lab_result ADD COLUMN supersedes    UUID;   -- the version this row replaced
ALTER TABLE lab.lab_result ADD COLUMN superseded_by UUID;   -- the version that replaced this row
ALTER TABLE lab.lab_result ADD COLUMN superseded_at TIMESTAMPTZ;

CREATE INDEX ix_lab_result_key         ON lab.lab_result (result_key) WHERE result_key IS NOT NULL;
CREATE INDEX ix_lab_result_key_current ON lab.lab_result (result_key, is_current) WHERE is_current;

COMMENT ON COLUMN lab.lab_result.result_key IS 'Groups versions of one logical result: order (or lab accession) + test LOINC.';
COMMENT ON COLUMN lab.lab_result.is_current IS 'The latest version for its result_key; only current versions are forwarded / notified.';
