CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS person (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    birth_date DATE NOT NULL,
    male BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by BIGINT NULL,
    updated_at TIMESTAMPTZ NULL DEFAULT now(),
    updated_by BIGINT NULL,
    deleted_at TIMESTAMPTZ NULL,
    deleted_by BIGINT NULL
);

CREATE INDEX IF NOT EXISTS person_uuid_idx ON person USING HASH (uuid);
