CREATE TABLE recipients
(
    id                  UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    tenant_id           UUID         NOT NULL REFERENCES tenants (id),
    name                VARCHAR(255) NOT NULL,
    email               VARCHAR(255),
    phone               VARCHAR(50),
    channel_preferences JSONB        NOT NULL DEFAULT '[
      "EMAIL"
    ]',
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO recipients (tenant_id, name, email, channel_preferences)
SELECT id, 'Maria Silva', 'maria@escola.com', '["EMAIL"]'
FROM tenants
WHERE api_key = 'demo-api-key-001';