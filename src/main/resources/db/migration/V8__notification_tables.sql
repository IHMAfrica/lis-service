-- -----------------------------------------------------------------------------
-- Notifications. A notification is a single event (optionally scoped to a
-- facility); notification_recipient holds each user's own delivery + read /
-- deleted state, so read/unread/delete are managed per user.
-- -----------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS notify;
COMMENT ON SCHEMA notify IS 'User notifications delivered over SSE.';

CREATE TABLE notify.notification (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    type           VARCHAR(100) NOT NULL,
    title          VARCHAR(255),
    body           TEXT,
    data           TEXT,
    facility_id    BIGINT,
    correlation_id UUID,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_notification          PRIMARY KEY (id),
    CONSTRAINT fk_notification_facility FOREIGN KEY (facility_id) REFERENCES ref.facility (id)
);

CREATE INDEX ix_notification_facility    ON notify.notification (facility_id);
CREATE INDEX ix_notification_correlation ON notify.notification (correlation_id);

COMMENT ON TABLE  notify.notification      IS 'A notification event, optionally scoped to a facility.';
COMMENT ON COLUMN notify.notification.type IS 'Notification kind, e.g. LAB_ORDER_ACK, LAB_RESULT (free-form until the events are wired).';
COMMENT ON COLUMN notify.notification.data IS 'Optional JSON payload (stored as text), e.g. order / result identifiers.';

CREATE TABLE notify.notification_recipient (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    notification_id UUID        NOT NULL,
    user_id         UUID        NOT NULL,
    read_at         TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_notification_recipient       PRIMARY KEY (id),
    CONSTRAINT uq_notification_recipient       UNIQUE (notification_id, user_id),
    CONSTRAINT fk_notif_recipient_notification FOREIGN KEY (notification_id) REFERENCES notify.notification (id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_recipient_user         FOREIGN KEY (user_id)         REFERENCES iam.users (user_id)
);

-- Inbox access paths: a user's undeleted notifications by recency, and unread only.
CREATE INDEX ix_notif_recipient_inbox  ON notify.notification_recipient (user_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_notif_recipient_unread ON notify.notification_recipient (user_id) WHERE deleted_at IS NULL AND read_at IS NULL;

COMMENT ON TABLE notify.notification_recipient IS 'Per-user delivery + read / deleted state for a notification.';
