-- -----------------------------------------------------------------------------
-- Clinician review of unsolicited lab results.
--
-- Unsolicited results that carry valid observations are held PENDING_REVIEW: a
-- clinician at the ordering facility accepts (then the result is forwarded
-- downstream) or rejects (never forwarded). Auto-reconciled results are NONE.
-- -----------------------------------------------------------------------------
ALTER TABLE lab.lab_result ADD COLUMN review_status VARCHAR(20) NOT NULL DEFAULT 'NONE';
ALTER TABLE lab.lab_result ADD COLUMN reviewed_by   UUID;
ALTER TABLE lab.lab_result ADD COLUMN reviewed_at   TIMESTAMPTZ;
ALTER TABLE lab.lab_result ADD COLUMN review_note   TEXT;

-- Access path for a facility's pending-review queue.
CREATE INDEX ix_lab_result_pending_review
    ON lab.lab_result (ordering_mfl_code)
    WHERE review_status = 'PENDING_REVIEW' AND is_current = TRUE;

COMMENT ON COLUMN lab.lab_result.review_status IS 'NONE | PENDING_REVIEW | ACCEPTED | REJECTED (unsolicited results only).';
