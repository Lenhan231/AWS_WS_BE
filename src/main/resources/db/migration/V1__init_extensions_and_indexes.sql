-- Flyway migration: enable extensions and create spatial indexes when columns exist
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'locations'
          AND column_name = 'coordinates'
    ) THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_locations_coordinates ON public.locations USING GIST (coordinates)';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'gyms'
          AND column_name = 'location_id'
    ) THEN
        -- For legacy schema using geography column directly, adjust before running
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_gyms_location_id ON public.gyms (location_id)';
    END IF;
END
$$;
