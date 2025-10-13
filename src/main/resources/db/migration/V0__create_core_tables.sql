-- Schema bootstrap migrated from Docker init script
-- EasyBody relational schema & seed data for local development
-- Executed automatically when the Postgres container is created with an empty volume.


BEGIN;

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Utility trigger to bump updated_at on every modification
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Ensure locations always contain a Point geometry derived from lat/lon
CREATE OR REPLACE FUNCTION sync_location_geometry()
RETURNS TRIGGER AS $$
BEGIN
    NEW.coordinates := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    cognito_sub VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30),
    role VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    profile_image_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_users_set_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    coordinates geometry(Point,4326) NOT NULL,
    formatted_address TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_locations_sync_geometry
BEFORE INSERT OR UPDATE ON locations
FOR EACH ROW EXECUTE FUNCTION sync_location_geometry();

CREATE TRIGGER trg_locations_set_updated_at
BEFORE UPDATE ON locations
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS gyms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    logo_url TEXT,
    address VARCHAR(1000) NOT NULL,
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255),
    country VARCHAR(255),
    postal_code VARCHAR(50),
    phone_number VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    website VARCHAR(255),
    location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_gyms_set_updated_at
BEFORE UPDATE ON gyms
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS pt_users (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio VARCHAR(2000),
    specializations VARCHAR(1000),
    certifications VARCHAR(1000),
    years_of_experience INTEGER,
    profile_image_url TEXT,
    location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_pt_users_set_updated_at
BEFORE UPDATE ON pt_users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS client_users (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL,
    fitness_goals VARCHAR(1000),
    preferred_activities VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_client_users_set_updated_at
BEFORE UPDATE ON client_users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS gym_staff (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    gym_id BIGINT NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    position VARCHAR(255),
    can_manage_offers BOOLEAN NOT NULL DEFAULT FALSE,
    can_manage_pts BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_gym_staff_set_updated_at
BEFORE UPDATE ON gym_staff
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS gym_pt_associations (
    id BIGSERIAL PRIMARY KEY,
    gym_id BIGINT NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    pt_user_id BIGINT NOT NULL REFERENCES pt_users(id) ON DELETE CASCADE,
    approval_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(1000),
    approved_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_gym_pt_assoc_set_updated_at
BEFORE UPDATE ON gym_pt_associations
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS offers (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(3000) NOT NULL,
    offer_type VARCHAR(32) NOT NULL,
    gym_id BIGINT REFERENCES gyms(id) ON DELETE CASCADE,
    pt_user_id BIGINT REFERENCES pt_users(id) ON DELETE CASCADE,
    price NUMERIC(10,2) NOT NULL CHECK (price > 0),
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    duration_description VARCHAR(255),
    image_urls VARCHAR(1000),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    risk_score NUMERIC(5,2) NOT NULL DEFAULT 0,
    rejection_reason VARCHAR(1000),
    moderated_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    moderated_at TIMESTAMPTZ,
    average_rating NUMERIC(3,2) NOT NULL DEFAULT 0,
    rating_count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_offers_set_updated_at
BEFORE UPDATE ON offers
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS ratings (
    id BIGSERIAL PRIMARY KEY,
    offer_id BIGINT NOT NULL REFERENCES offers(id) ON DELETE CASCADE,
    client_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (offer_id, client_user_id)
);

CREATE TRIGGER trg_ratings_set_updated_at
BEFORE UPDATE ON ratings
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    reported_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    offer_id BIGINT REFERENCES offers(id) ON DELETE SET NULL,
    reported_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reason VARCHAR(1000) NOT NULL,
    details VARCHAR(2000),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reviewed_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMPTZ,
    review_notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_reports_set_updated_at
BEFORE UPDATE ON reports
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Spatial and lookup indexes
CREATE INDEX IF NOT EXISTS idx_locations_coordinates ON locations USING GIST (coordinates);
CREATE INDEX IF NOT EXISTS idx_offers_status ON offers(status);
CREATE INDEX IF NOT EXISTS idx_offers_offer_type ON offers(offer_type);
CREATE INDEX IF NOT EXISTS idx_gym_pt_assoc_status ON gym_pt_associations(approval_status);
CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);

-- Maintain offer aggregates when ratings change
CREATE OR REPLACE FUNCTION refresh_offer_rating()
RETURNS TRIGGER AS $$
DECLARE
    target_offer BIGINT;
    avg_rating NUMERIC(3,2);
    total_count INTEGER;
BEGIN
    IF TG_OP = 'DELETE' THEN
        target_offer := OLD.offer_id;
    ELSE
        target_offer := NEW.offer_id;
    END IF;

    SELECT COALESCE(AVG(r.rating)::NUMERIC(3,2), 0),
           COUNT(*)
    INTO avg_rating, total_count
    FROM ratings r
    WHERE r.offer_id = target_offer;

    UPDATE offers
    SET average_rating = avg_rating,
        rating_count = total_count
    WHERE id = target_offer;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_refresh_offer_rating ON ratings;
CREATE TRIGGER trg_refresh_offer_rating
AFTER INSERT OR UPDATE OR DELETE ON ratings
FOR EACH ROW EXECUTE FUNCTION refresh_offer_rating();

COMMIT;
