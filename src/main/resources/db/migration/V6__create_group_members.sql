CREATE TABLE group_members
(
    group_id     UUID NOT NULL REFERENCES recipient_groups (id),
    recipient_id UUID NOT NULL REFERENCES recipients (id),
    PRIMARY KEY (group_id, recipient_id)
);