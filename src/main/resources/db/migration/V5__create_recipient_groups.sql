CREATE TABLE recipient_groups
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL REFERENCES tenants (id),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);