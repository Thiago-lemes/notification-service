CREATE TABLE templates
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    tenant_id  UUID         NOT NULL REFERENCES tenants (id),
    name       VARCHAR(255) NOT NULL,
    channel    VARCHAR(50)  NOT NULL,
    subject    VARCHAR(500),
    body       TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);