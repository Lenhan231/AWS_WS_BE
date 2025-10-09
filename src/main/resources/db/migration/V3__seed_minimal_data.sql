-- Seed minimal data set for local integration testing
WITH admin_user AS (
    INSERT INTO users (cognito_sub, email, first_name, last_name, phone_number, role, active, profile_image_url, created_at, updated_at)
    VALUES ('seed-admin-sub', 'admin@easybody.com', 'System', 'Admin', '+8488880001', 'ADMIN', TRUE, NULL, NOW(), NOW())
    RETURNING id
),
gym_staff_user AS (
    INSERT INTO users (cognito_sub, email, first_name, last_name, phone_number, role, active, profile_image_url, created_at, updated_at)
    VALUES ('seed-gymstaff-sub', 'owner@easybody.com', 'Linh', 'Tran', '+8488880002', 'GYM_STAFF', TRUE, 'https://picsum.photos/seed/gym-owner/200', NOW(), NOW())
    RETURNING id
),
pt_account AS (
    INSERT INTO users (cognito_sub, email, first_name, last_name, phone_number, role, active, profile_image_url, created_at, updated_at)
    VALUES ('seed-pt-sub', 'trainer@easybody.com', 'Minh', 'Nguyen', '+8488880003', 'PT_USER', TRUE, 'https://picsum.photos/seed/pt-user/200', NOW(), NOW())
    RETURNING id
),
gym_location AS (
    INSERT INTO locations (latitude, longitude, coordinates, formatted_address, created_at, updated_at)
    VALUES (10.7769, 106.7009, ST_SetSRID(ST_MakePoint(106.7009, 10.7769), 4326), '45 Nguyễn Thị Minh Khai, Quận 1, TP. Hồ Chí Minh', NOW(), NOW())
    RETURNING id
),
pt_location AS (
    INSERT INTO locations (latitude, longitude, coordinates, formatted_address, created_at, updated_at)
    VALUES (10.7801, 106.6997, ST_SetSRID(ST_MakePoint(106.6997, 10.7801), 4326), '72 Lê Thánh Tôn, Quận 1, TP. Hồ Chí Minh', NOW(), NOW())
    RETURNING id
),
gym_record AS (
    INSERT INTO gyms (name, description, logo_url, address, city, state, country, postal_code, phone_number, email, website, location_id, active, verified, created_at, updated_at)
    VALUES (
        'EasyBody Downtown Club',
        'Premium strength & conditioning club with sauna and recovery zone.',
        'https://picsum.photos/seed/easybody-gym/480/320',
        '45 Nguyễn Thị Minh Khai',
        'TP. Hồ Chí Minh',
        'Hồ Chí Minh',
        'Việt Nam',
        '700000',
        '+842839992222',
        'hello@easybody.vn',
        'https://easybody.vn',
        (SELECT id FROM gym_location),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    )
    RETURNING id
),
gym_staff_assignment AS (
    INSERT INTO gym_staff (user_id, gym_id, position, can_manage_offers, can_manage_pts, active, created_at, updated_at)
    VALUES (
        (SELECT id FROM gym_staff_user),
        (SELECT id FROM gym_record),
        'General Manager',
        TRUE,
        TRUE,
        TRUE,
        NOW(),
        NOW()
    )
    RETURNING gym_id
),
pt_profile AS (
    INSERT INTO pt_users (user_id, bio, specializations, certifications, years_of_experience, profile_image_url, location_id, active, verified, created_at, updated_at)
    VALUES (
        (SELECT id FROM pt_account),
        'Certified personal trainer focused on sustainable transformation plans.',
        'Strength Coaching, Fat Loss, Mobility',
        'ACE CPT, Precision Nutrition',
        6,
        'https://picsum.photos/seed/easybody-pt/480/320',
        (SELECT id FROM pt_location),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    )
    RETURNING id
)
INSERT INTO offers (title, description, offer_type, gym_id, pt_user_id, price, currency, duration_description, image_urls, status, risk_score, average_rating, rating_count, active, created_at, updated_at)
VALUES
    (
        '1-Month Premium Gym Access',
        'Unlimited access to all equipment, group classes, and wellness amenities.',
        'GYM_OFFER',
        (SELECT id FROM gym_record),
        NULL,
        1200000.00,
        'VND',
        '1 month membership',
        '["https://picsum.photos/seed/gym-offer/640/360"]',
        'APPROVED',
        0,
        0,
        0,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        '12-Session Personal Coaching',
        'Tailored one-on-one training programme with progress tracking and nutrition check-ins.',
        'PT_OFFER',
        NULL,
        (SELECT id FROM pt_profile),
        6000000.00,
        'VND',
        '12 sessions package',
        '["https://picsum.photos/seed/pt-offer/640/360"]',
        'APPROVED',
        0,
        0,
        0,
        TRUE,
        NOW(),
        NOW()
    );
