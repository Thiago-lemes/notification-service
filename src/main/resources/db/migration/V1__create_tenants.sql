CREATE TABLE tenants
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    api_key    VARCHAR(255) NOT NULL UNIQUE,
    status     VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO tenants (name, api_key)
VALUES ('EduManager Demo', 'demo-api-key-001');

