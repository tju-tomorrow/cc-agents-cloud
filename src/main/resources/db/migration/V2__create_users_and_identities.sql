CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT users_email_unique UNIQUE (email)
);

CREATE TABLE identities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider VARCHAR(30) NOT NULL,
    provider_subject VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT identities_provider_subject_unique UNIQUE (provider, provider_subject),
    CONSTRAINT identities_password_hash_required CHECK (
        provider <> 'PASSWORD' OR password_hash IS NOT NULL
    )
);

CREATE INDEX identities_user_id_index ON identities (user_id);

