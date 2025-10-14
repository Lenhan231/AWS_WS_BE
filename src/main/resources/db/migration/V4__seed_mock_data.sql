DO $$
DECLARE
    admin_id BIGINT;
    i INT;
    gym_location_id BIGINT;
    pt_location_id BIGINT;
    client_location_id BIGINT;
    gym_id BIGINT;
    gym_staff_user_id BIGINT;
    pt_user_account_id BIGINT;
    pt_profile_id BIGINT;
    client_user_account_id BIGINT;
    v_offer_id BIGINT;
    association_status VARCHAR(32);
    base_created_at TIMESTAMPTZ;
BEGIN
    SELECT id INTO admin_id
    FROM users
    WHERE email = 'admin@easybody.com';

    FOR i IN 1..15 LOOP
        base_created_at := NOW() - (i || ' days')::INTERVAL;

        INSERT INTO locations (latitude, longitude, coordinates, formatted_address, created_at, updated_at)
        VALUES (
            10.700000 + (i * 0.01),
            106.700000 + (i * 0.01),
            ST_SetSRID(ST_MakePoint(106.700000 + (i * 0.01), 10.700000 + (i * 0.01)), 4326),
            FORMAT('Mock Gym Address #%s, District %s, HCMC', i, ((i - 1) % 10) + 1),
            base_created_at,
            base_created_at
        )
        RETURNING id INTO gym_location_id;

        INSERT INTO gyms (
            name,
            description,
            logo_url,
            address,
            city,
            state,
            country,
            postal_code,
            phone_number,
            email,
            website,
            location_id,
            active,
            verified,
            created_at,
            updated_at
        )
        VALUES (
            FORMAT('Mock Gym #%s', i),
            'Strength, conditioning and recovery zones for mock data.',
            FORMAT('https://picsum.photos/seed/mock-gym-%s/480/320', i),
            FORMAT('%s Mock Street', 10 + i),
            'TP. Hồ Chí Minh',
            'Hồ Chí Minh',
            'Việt Nam',
            FORMAT('70%03s', i),
            FORMAT('+8488800%04s', 1000 + i),
            FORMAT('mock.gym%s@easybody.vn', i),
            FORMAT('https://gym%s.easybody.vn', i),
            gym_location_id,
            TRUE,
            (i % 3 <> 0),
            base_created_at,
            base_created_at
        )
        RETURNING id INTO gym_id;

        INSERT INTO users (
            cognito_sub,
            email,
            first_name,
            last_name,
            phone_number,
            role,
            active,
            profile_image_url,
            created_at,
            updated_at
        )
        VALUES (
            FORMAT('mock-gymstaff-sub-%s', i),
            FORMAT('mock.staff%s@easybody.com', i),
            FORMAT('Staff%s', i),
            'Nguyen',
            FORMAT('+8488811%04s', 2000 + i),
            'GYM_STAFF',
            TRUE,
            FORMAT('https://picsum.photos/seed/mock-gymstaff-%s/200', i),
            base_created_at,
            base_created_at
        )
        RETURNING id INTO gym_staff_user_id;

        INSERT INTO gym_staff (
            user_id,
            gym_id,
            position,
            can_manage_offers,
            can_manage_pts,
            active,
            created_at,
            updated_at
        ) VALUES (
            gym_staff_user_id,
            gym_id,
            FORMAT('Manager #%s', i),
            (i % 2 = 0),
            (i % 3 = 0),
            TRUE,
            base_created_at,
            base_created_at
        );

        INSERT INTO locations (latitude, longitude, coordinates, formatted_address, created_at, updated_at)
        VALUES (
            10.710000 + (i * 0.01),
            106.710000 + (i * 0.01),
            ST_SetSRID(ST_MakePoint(106.710000 + (i * 0.01), 10.710000 + (i * 0.01)), 4326),
            FORMAT('Mock PT Address #%s, Bình Thạnh, HCMC', i),
            base_created_at,
            base_created_at
        )
        RETURNING id INTO pt_location_id;

        INSERT INTO users (
            cognito_sub,
            email,
            first_name,
            last_name,
            phone_number,
            role,
            active,
            profile_image_url,
            created_at,
            updated_at
        )
        VALUES (
            FORMAT('mock-pt-sub-%s', i),
            FORMAT('mock.pt%s@easybody.com', i),
            FORMAT('Trainer%s', i),
            'Pham',
            FORMAT('+8488822%04s', 3000 + i),
            'PT_USER',
            TRUE,
            FORMAT('https://picsum.photos/seed/mock-pt-%s/200', i),
            base_created_at,
            base_created_at
        )
        RETURNING id INTO pt_user_account_id;

        INSERT INTO pt_users (
            user_id,
            bio,
            specializations,
            certifications,
            years_of_experience,
            profile_image_url,
            location_id,
            active,
            verified,
            created_at,
            updated_at
        )
        VALUES (
            pt_user_account_id,
            'Mock PT providing personalised hybrid programmes.',
            'Strength, Conditioning, Mobility',
            'NASM CPT, Mock Nutrition',
            3 + (i % 7),
            FORMAT('https://picsum.photos/seed/mock-pt-profile-%s/320/320', i),
            pt_location_id,
            TRUE,
            (i % 4 <> 0),
            base_created_at,
            base_created_at
        )
        RETURNING id INTO pt_profile_id;

        association_status := CASE
            WHEN i % 3 = 0 THEN 'REJECTED'
            WHEN i % 3 = 1 THEN 'APPROVED'
            ELSE 'PENDING'
        END;

        INSERT INTO gym_pt_associations (
            gym_id,
            pt_user_id,
            approval_status,
            rejection_reason,
            approved_by_user_id,
            approved_at,
            created_at,
            updated_at
        ) VALUES (
            gym_id,
            pt_profile_id,
            association_status,
            CASE WHEN association_status = 'REJECTED' THEN 'Mock rejection reason' ELSE NULL END,
            CASE WHEN association_status = 'APPROVED' THEN gym_staff_user_id ELSE NULL END,
            CASE WHEN association_status = 'APPROVED' THEN base_created_at ELSE NULL END,
            base_created_at,
            base_created_at
        );

        INSERT INTO locations (latitude, longitude, coordinates, formatted_address, created_at, updated_at)
        VALUES (
            10.720000 + (i * 0.01),
            106.720000 + (i * 0.01),
            ST_SetSRID(ST_MakePoint(106.720000 + (i * 0.01), 10.720000 + (i * 0.01)), 4326),
            FORMAT('Mock Client Address #%s, Thủ Đức, HCMC', i),
            base_created_at,
            base_created_at
        )
        RETURNING id INTO client_location_id;

        INSERT INTO users (
            cognito_sub,
            email,
            first_name,
            last_name,
            phone_number,
            role,
            active,
            profile_image_url,
            created_at,
            updated_at
        )
        VALUES (
            FORMAT('mock-client-sub-%s', i),
            FORMAT('mock.client%s@easybody.com', i),
            FORMAT('Client%s', i),
            'Le',
            FORMAT('+8488833%04s', 4000 + i),
            'CLIENT_USER',
            TRUE,
            FORMAT('https://picsum.photos/seed/mock-client-%s/200', i),
            base_created_at,
            base_created_at
        )
        RETURNING id INTO client_user_account_id;

        INSERT INTO client_users (
            user_id,
            location_id,
            fitness_goals,
            preferred_activities,
            created_at,
            updated_at
        )
        VALUES (
            client_user_account_id,
            client_location_id,
            FORMAT('Goal set #%s: Gain lean muscle, improve endurance.', i),
            'Functional training, HIIT, Yoga',
            base_created_at,
            base_created_at
        );

        IF i % 2 = 0 THEN
            INSERT INTO offers (
                title,
                description,
                offer_type,
                gym_id,
                pt_user_id,
                price,
                currency,
                duration_description,
                image_urls,
                status,
                risk_score,
                rejection_reason,
                moderated_by_user_id,
                moderated_at,
                average_rating,
                rating_count,
                active,
                created_at,
                updated_at
            )
            VALUES (
                FORMAT('Mock Gym Offer #%s', i),
                'Unlimited access to mock facilities with recovery support.',
                'GYM_OFFER',
                gym_id,
                NULL,
                800000 + (i * 10000),
                'VND',
                FORMAT('%s-day pass', 10 + i),
                FORMAT('["https://picsum.photos/seed/mock-gym-offer-%s/640/360"]', i),
                'APPROVED',
                0,
                NULL,
                gym_staff_user_id,
                base_created_at,
                0,
                0,
                TRUE,
                base_created_at,
                base_created_at
            )
            RETURNING id INTO v_offer_id;
        ELSE
            INSERT INTO offers (
                title,
                description,
                offer_type,
                gym_id,
                pt_user_id,
                price,
                currency,
                duration_description,
                image_urls,
                status,
                risk_score,
                rejection_reason,
                moderated_by_user_id,
                moderated_at,
                average_rating,
                rating_count,
                active,
                created_at,
                updated_at
            )
            VALUES (
                FORMAT('Mock PT Offer #%s', i),
                'One-on-one coaching block focused on compound lifts and mobility.',
                'PT_OFFER',
                NULL,
                pt_profile_id,
                1500000 + (i * 20000),
                'VND',
                FORMAT('%s personal sessions', 5 + (i % 6)),
                FORMAT('["https://picsum.photos/seed/mock-pt-offer-%s/640/360"]', i),
                'APPROVED',
                0,
                NULL,
                gym_staff_user_id,
                base_created_at,
                0,
                0,
                TRUE,
                base_created_at,
                base_created_at
            )
            RETURNING id INTO v_offer_id;
        END IF;

        INSERT INTO ratings (
            v_offer_id,
            client_user_id,
            rating,
            comment,
            created_at,
            updated_at
        )
        VALUES (
            v_offer_id,
            client_user_account_id,
            ((i - 1) % 5) + 1,
            FORMAT('Mock feedback #%s - enjoyable sessions!', i),
            base_created_at,
            base_created_at
        )
        ON CONFLICT (offer_id, client_user_id) DO NOTHING;

        INSERT INTO reports (
            reported_by_user_id,
            v_offer_id,
            reported_user_id,
            reason,
            details,
            status,
            reviewed_by_user_id,
            reviewed_at,
            review_notes,
            created_at,
            updated_at
        )
        VALUES (
            client_user_account_id,
            v_offer_id,
            CASE WHEN i % 2 = 0 THEN pt_user_account_id ELSE gym_staff_user_id END,
            FORMAT('Mock report reason #%s', i),
            'Auto-generated mock report for QA scenarios.',
            CASE WHEN i % 4 = 0 THEN 'RESOLVED' ELSE 'PENDING' END,
            CASE WHEN i % 4 = 0 THEN admin_id ELSE NULL END,
            CASE WHEN i % 4 = 0 THEN base_created_at + INTERVAL '6 hours' ELSE NULL END,
            CASE WHEN i % 4 = 0 THEN 'Mock moderation note resolved.' ELSE NULL END,
            base_created_at,
            base_created_at
        );
    END LOOP;
END
$$;
