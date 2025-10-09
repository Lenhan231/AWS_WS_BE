DO $$
DECLARE
    admin_user_id BIGINT;
    gym_staff_user_id BIGINT;
    pt_user_id BIGINT;
    gym_location_id BIGINT;
    pt_location_id BIGINT;
    gym_record_id BIGINT;
    pt_profile_id BIGINT;
BEGIN
    INSERT INTO users (cognito_sub, email, first_name, last_name, phone_number, role, active, profile_image_url, created_at, updated_at)
    VALUES ('seed-admin-sub', 'admin@easybody.com', 'System', 'Admin', '+8488880001', 'ADMIN', TRUE, NULL, NOW(), NOW())
    ON CONFLICT (email) DO UPDATE SET
        first_name = EXCLUDED.first_name,
        last_name = EXCLUDED.last_name,
        phone_number = EXCLUDED.phone_number,
        role = EXCLUDED.role,
        active = EXCLUDED.active,
        profile_image_url = EXCLUDED.profile_image_url,
        updated_at = NOW();
    SELECT id INTO admin_user_id FROM users WHERE email = 'admin@easybody.com';

    INSERT INTO users (cognito_sub, email, first_name, last_name, phone_number, role, active, profile_image_url, created_at, updated_at)
    VALUES ('seed-gymstaff-sub', 'owner@easybody.com', 'Linh', 'Tran', '+8488880002', 'GYM_STAFF', TRUE, 'https://picsum.photos/seed/gym-owner/200', NOW(), NOW())
    ON CONFLICT (email) DO UPDATE SET
        first_name = EXCLUDED.first_name,
        last_name = EXCLUDED.last_name,
        phone_number = EXCLUDED.phone_number,
        role = EXCLUDED.role,
        active = EXCLUDED.active,
        profile_image_url = EXCLUDED.profile_image_url,
        updated_at = NOW();
    SELECT id INTO gym_staff_user_id FROM users WHERE email = 'owner@easybody.com';

    INSERT INTO users (cognito_sub, email, first_name, last_name, phone_number, role, active, profile_image_url, created_at, updated_at)
    VALUES ('seed-pt-sub', 'trainer@easybody.com', 'Minh', 'Nguyen', '+8488880003', 'PT_USER', TRUE, 'https://picsum.photos/seed/pt-user/200', NOW(), NOW())
    ON CONFLICT (email) DO UPDATE SET
        first_name = EXCLUDED.first_name,
        last_name = EXCLUDED.last_name,
        phone_number = EXCLUDED.phone_number,
        role = EXCLUDED.role,
        active = EXCLUDED.active,
        profile_image_url = EXCLUDED.profile_image_url,
        updated_at = NOW();
    SELECT id INTO pt_user_id FROM users WHERE email = 'trainer@easybody.com';

    SELECT id INTO gym_location_id FROM locations WHERE formatted_address = '45 Nguyễn Thị Minh Khai, Quận 1, TP. Hồ Chí Minh';
    IF gym_location_id IS NULL THEN
        INSERT INTO locations (latitude, longitude, coordinates, formatted_address, created_at, updated_at)
        VALUES (10.7769, 106.7009, ST_SetSRID(ST_MakePoint(106.7009, 10.7769), 4326), '45 Nguyễn Thị Minh Khai, Quận 1, TP. Hồ Chí Minh', NOW(), NOW())
        RETURNING id INTO gym_location_id;
    END IF;

    SELECT id INTO pt_location_id FROM locations WHERE formatted_address = '72 Lê Thánh Tôn, Quận 1, TP. Hồ Chí Minh';
    IF pt_location_id IS NULL THEN
        INSERT INTO locations (latitude, longitude, coordinates, formatted_address, created_at, updated_at)
        VALUES (10.7801, 106.6997, ST_SetSRID(ST_MakePoint(106.6997, 10.7801), 4326), '72 Lê Thánh Tôn, Quận 1, TP. Hồ Chí Minh', NOW(), NOW())
        RETURNING id INTO pt_location_id;
    END IF;

    SELECT id INTO gym_record_id FROM gyms WHERE name = 'EasyBody Downtown Club';
    IF gym_record_id IS NULL THEN
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
            gym_location_id,
            TRUE,
            TRUE,
            NOW(),
            NOW()
        ) RETURNING id INTO gym_record_id;
    ELSE
        UPDATE gyms
        SET description = 'Premium strength & conditioning club with sauna and recovery zone.',
            logo_url = 'https://picsum.photos/seed/easybody-gym/480/320',
            address = '45 Nguyễn Thị Minh Khai',
            city = 'TP. Hồ Chí Minh',
            state = 'Hồ Chí Minh',
            country = 'Việt Nam',
            postal_code = '700000',
            phone_number = '+842839992222',
            email = 'hello@easybody.vn',
            website = 'https://easybody.vn',
            location_id = gym_location_id,
            active = TRUE,
            verified = TRUE,
            updated_at = NOW()
        WHERE id = gym_record_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM gym_staff WHERE user_id = gym_staff_user_id AND gym_id = gym_record_id) THEN
        INSERT INTO gym_staff (user_id, gym_id, position, can_manage_offers, can_manage_pts, active, created_at, updated_at)
        VALUES (gym_staff_user_id, gym_record_id, 'General Manager', TRUE, TRUE, TRUE, NOW(), NOW());
    END IF;

    SELECT id INTO pt_profile_id FROM pt_users WHERE user_id = pt_user_id;
    IF pt_profile_id IS NULL THEN
        INSERT INTO pt_users (user_id, bio, specializations, certifications, years_of_experience, profile_image_url, location_id, active, verified, created_at, updated_at)
        VALUES (
            pt_user_id,
            'Certified personal trainer focused on sustainable transformation plans.',
            'Strength Coaching, Fat Loss, Mobility',
            'ACE CPT, Precision Nutrition',
            6,
            'https://picsum.photos/seed/easybody-pt/480/320',
            pt_location_id,
            TRUE,
            TRUE,
            NOW(),
            NOW()
        ) RETURNING id INTO pt_profile_id;
    ELSE
        UPDATE pt_users
        SET bio = 'Certified personal trainer focused on sustainable transformation plans.',
            specializations = 'Strength Coaching, Fat Loss, Mobility',
            certifications = 'ACE CPT, Precision Nutrition',
            years_of_experience = 6,
            profile_image_url = 'https://picsum.photos/seed/easybody-pt/480/320',
            location_id = pt_location_id,
            active = TRUE,
            verified = TRUE,
            updated_at = NOW()
        WHERE id = pt_profile_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM offers WHERE title = '1-Month Premium Gym Access') THEN
        INSERT INTO offers (title, description, offer_type, gym_id, pt_user_id, price, currency, duration_description, image_urls, status, risk_score, average_rating, rating_count, active, created_at, updated_at)
        VALUES (
            '1-Month Premium Gym Access',
            'Unlimited access to all equipment, group classes, and wellness amenities.',
            'GYM_OFFER',
            gym_record_id,
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
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM offers WHERE title = '12-Session Personal Coaching') THEN
        INSERT INTO offers (title, description, offer_type, gym_id, pt_user_id, price, currency, duration_description, image_urls, status, risk_score, average_rating, rating_count, active, created_at, updated_at)
        VALUES (
            '12-Session Personal Coaching',
            'Tailored one-on-one training programme with progress tracking and nutrition check-ins.',
            'PT_OFFER',
            NULL,
            pt_profile_id,
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
    END IF;
END
$$;
