CREATE TABLE notifications
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    tenant_id   UUID        NOT NULL REFERENCES tenants (id),
    template_id UUID,
    group_id    UUID,
    payload     JSONB       NOT NULL,
    status      VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);