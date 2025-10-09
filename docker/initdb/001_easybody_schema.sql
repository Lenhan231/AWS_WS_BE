-- EasyBody relational schema & seed data for local development
-- Executed automatically when the Postgres container is created with an empty volume.

BEGIN;

CREATE EXTENSION IF NOT EXISTS postgis;

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

-- Seed baseline data -------------------------------------------------------

INSERT INTO users (id, cognito_sub, email, first_name, last_name, phone_number, role, profile_image_url)
VALUES
    (1, 'local-admin-sub', 'admin@easybody.com', 'System', 'Admin', '+84901112222', 'ADMIN', 'https://picsum.photos/seed/admin/200'),
    (2, 'local-gymstaff-sub', 'owner@easybody.com', 'Linh', 'Tran', '+84903334444', 'GYM_STAFF', 'https://picsum.photos/seed/owner/200'),
    (3, 'local-pt-sub', 'trainer@easybody.com', 'Minh', 'Nguyen', '+84905556666', 'PT_USER', 'https://picsum.photos/seed/trainer/200'),
    (4, 'local-client-sub', 'client@easybody.com', 'Anh', 'Pham', '+84907778888', 'CLIENT_USER', 'https://picsum.photos/seed/client/200')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('users','id'), GREATEST((SELECT MAX(id) FROM users), 1));

INSERT INTO locations (id, latitude, longitude, formatted_address)
VALUES
    (1, 21.0285, 105.8542, '123 Đường ABC, Quận Ba Đình, Hà Nội'),
    (2, 10.7769, 106.7009, '45 Nguyễn Thị Minh Khai, Quận 1, TP. Hồ Chí Minh'),
    (3, 21.0155, 105.8425, '89 Triệu Việt Vương, Hai Bà Trưng, Hà Nội'),
    (4, 21.0300, 105.8500, '22 Lý Thường Kiệt, Hoàn Kiếm, Hà Nội')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('locations','id'), GREATEST((SELECT MAX(id) FROM locations), 1));

INSERT INTO gyms (id, name, description, logo_url, address, city, state, country, postal_code, phone_number, email, website, location_id, active, verified)
VALUES
    (1, 'EasyBody Fitness Center', 'Modern gym with premium equipment and wellness services.', 'https://picsum.photos/seed/gym1/400/300', '123 Đường ABC, Quận Ba Đình', 'Hà Nội', NULL, 'Việt Nam', NULL, '+842839991000', 'contact@easybodyfitness.vn', 'https://easybodyfitness.vn', 1, TRUE, TRUE),
    (2, 'Saigon Iron Paradise', 'Strength and conditioning center with CrossFit classes.', 'https://picsum.photos/seed/gym2/400/300', '45 Nguyễn Thị Minh Khai, Quận 1', 'TP. Hồ Chí Minh', NULL, 'Việt Nam', NULL, '+842839992222', 'hello@ironparadise.vn', 'https://ironparadise.vn', 2, TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('gyms','id'), GREATEST((SELECT MAX(id) FROM gyms), 1));

INSERT INTO gym_staff (id, user_id, gym_id, position, can_manage_offers, can_manage_pts, active)
VALUES
    (1, 2, 1, 'General Manager', TRUE, TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('gym_staff','id'), GREATEST((SELECT MAX(id) FROM gym_staff), 1));

INSERT INTO pt_users (id, user_id, bio, specializations, certifications, years_of_experience, profile_image_url, location_id, active, verified)
VALUES
    (1, 3, 'Certified personal trainer focused on sustainable body recomposition.', 'Weight Loss, Hypertrophy, Mobility', 'ACE CPT, Precision Nutrition Level 1', 7, 'https://picsum.photos/seed/pt1/400/300', 3, TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('pt_users','id'), GREATEST((SELECT MAX(id) FROM pt_users), 1));

INSERT INTO client_users (id, user_id, location_id, fitness_goals, preferred_activities)
VALUES
    (1, 4, 4, 'Giảm mỡ, tăng sức bền', 'Cycling, Functional Training')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('client_users','id'), GREATEST((SELECT MAX(id) FROM client_users), 1));

INSERT INTO offers (id, title, description, offer_type, gym_id, pt_user_id, price, currency, duration_description, image_urls, status, average_rating, rating_count)
VALUES
    (1, '1-Month Premium Membership', 'Unlimited gym access, sauna, and 2 PT sessions.', 'GYM_OFFER', 1, NULL, 1200000, 'VND', '1 tháng', 'https://picsum.photos/seed/offer1/400/300', 'APPROVED', 0, 0),
    (2, 'Drop-in Day Pass', 'Perfect for travellers needing a one-day workout.', 'GYM_OFFER', 1, NULL, 150000, 'VND', '1 ngày', 'https://picsum.photos/seed/offer2/400/300', 'APPROVED', 0, 0),
    (3, 'CrossFit Foundations', 'Four fundamentals classes introducing Olympic lifts and WODs.', 'GYM_OFFER', 2, NULL, 1800000, 'VND', '4 buổi', 'https://picsum.photos/seed/offer3/400/300', 'PENDING', 0, 0),
    (4, '12-Session Personal Coaching', 'Tailored program with nutrition check-ins and mobility drills.', 'PT_OFFER', NULL, 1, 6000000, 'VND', '12 buổi', 'https://picsum.photos/seed/offer4/400/300', 'APPROVED', 0, 0)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('offers','id'), GREATEST((SELECT MAX(id) FROM offers), 1));

INSERT INTO ratings (id, offer_id, client_user_id, rating, comment)
VALUES
    (1, 1, 4, 5, 'Trang thiết bị rất mới và sạch sẽ.'),
    (2, 4, 4, 4, 'PT tận tâm, lịch tập linh hoạt.')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('ratings','id'), GREATEST((SELECT MAX(id) FROM ratings), 1));

INSERT INTO reports (id, reported_by_user_id, offer_id, reason, details, status)
VALUES
    (1, 4, 3, 'MISLEADING_INFORMATION', 'Giá niêm yết khác so với quảng cáo trên Facebook.', 'PENDING')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('reports','id'), GREATEST((SELECT MAX(id) FROM reports), 1));

-- Refresh aggregates for seeded ratings
UPDATE offers o
SET average_rating = sub.avg_rating,
    rating_count = sub.total_count
FROM (
    SELECT offer_id, AVG(rating)::NUMERIC(3,2) AS avg_rating, COUNT(*) AS total_count
    FROM ratings
    GROUP BY offer_id
) sub
WHERE o.id = sub.offer_id;

COMMIT;
