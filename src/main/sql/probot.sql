CREATE TABLE direct_message_event (
                                      id VARCHAR(255) UNIQUE NOT NULL,
                                      json JSONB,
                                      PRIMARY KEY (id)
);

CREATE VIEW messages AS
SELECT
    id,
    json->'message_create'->'created_timestamp' AS timestamp,
    json->>'type' AS type,
    json->'message_create'->'message_data'->>'text' AS text,
    json->'message_create'->>'sender_id' AS sender_id,
    json->'message_create'->'target'->>'recipient_id' AS recipient_id
FROM direct_message_event;

