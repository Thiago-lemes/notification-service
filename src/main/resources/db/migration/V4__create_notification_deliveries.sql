CREATE TABLE notification_deliveries
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    notification_id UUID        NOT NULL REFERENCES notifications (id),
    recipient_id    UUID        NOT NULL REFERENCES recipients (id),
    channel         VARCHAR(50) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    attempt_count   INT         NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP,
    error_message   TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);