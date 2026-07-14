CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

CREATE SCHEMA IF NOT EXISTS ref;
CREATE SCHEMA IF NOT EXISTS messaging;

COMMENT ON SCHEMA ref       IS 'Reference / lookup data (seeded, mostly static).';
COMMENT ON SCHEMA messaging IS 'Transactional outbox and inbound event de-duplication log.';
