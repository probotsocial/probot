
CREATE TABLE followers_ids (
                                id VARCHAR(255) UNIQUE NOT NULL,
                                ts TIMESTAMP NOT NULL DEFAULT NOW(),
                                PRIMARY KEY (id)
);

CREATE TABLE followers_json (
                                id VARCHAR(255) UNIQUE NOT NULL,
                                ts TIMESTAMP NOT NULL DEFAULT NOW(),
                                json JSONB,
                                PRIMARY KEY (id)
);

CREATE TABLE friends_json (
                                id VARCHAR(255) UNIQUE NOT NULL,
                                ts TIMESTAMP NOT NULL DEFAULT NOW(),
                                json JSONB,
                                PRIMARY KEY (id)
);

CREATE TABLE followers_usertimeline_json (
                                      id VARCHAR(255) UNIQUE NOT NULL,
                                      ts TIMESTAMP NOT NULL,
                                      json JSONB,
                                      PRIMARY KEY (id)
);

CREATE TABLE direct_message_events_json (
                                            id VARCHAR(255) UNIQUE NOT NULL,
                                            ts TIMESTAMP NOT NULL,
                                            json JSONB,
                                            PRIMARY KEY (id)
);

CREATE TABLE optins_json (
                                id VARCHAR(255) UNIQUE NOT NULL,
                                ts TIMESTAMP NOT NULL,
                                json JSONB,
                                PRIMARY KEY (id)
);

CREATE VIEW followers AS
SELECT
    id,
    ts,
    json->>'name' AS name,
    json->>'description' AS description,
    json->>'url' AS url,
    json->>'location' AS location,
    json->>'screen_name' AS screen_name,
    json->>'followers_count' AS followers_count,
    json->>'friends_count' AS friends_count,
    json->>'favourites_count' AS favourites_count,
    json->>'statuses_count' AS statuses_count,
    json->>'blocking' AS blocking,
    json->>'following' AS following,
    json->>'follow_request_sent' AS follow_request_sent
FROM followers_json;

CREATE VIEW messages AS
SELECT
    id,
    ts,
    json->>'type' AS type,
    json->'message_create'->'message_data'->>'text' AS text,
    json->'message_create'->>'sender_id' AS sender_id,
    json->'message_create'->'target'->>'recipient_id' AS recipient_id
FROM direct_message_events_json;

CREATE VIEW optins AS
SELECT
    id,
    ts,
    json->'message_create'->'message_data'->>'text' AS text,
    json->'message_create'->>'sender_id' AS sender_id,
    json->'email_address' AS email_address,
    json->'phone_number' AS phone_number
FROM optins_json;

