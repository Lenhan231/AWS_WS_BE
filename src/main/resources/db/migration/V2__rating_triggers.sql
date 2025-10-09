-- Maintain aggregate rating fields on offers whenever ratings change
CREATE OR REPLACE FUNCTION refresh_offer_rating()
RETURNS TRIGGER AS $$
DECLARE
    target_offer BIGINT;
    avg_rating NUMERIC(3,2);
    total_count INTEGER;
BEGIN
    target_offer := COALESCE(NEW.offer_id, OLD.offer_id);

    SELECT COALESCE(AVG(r.rating)::NUMERIC(3,2), 0),
           COUNT(*)
    INTO avg_rating, total_count
    FROM ratings r
    WHERE r.offer_id = target_offer;

    UPDATE offers
    SET average_rating = avg_rating,
        rating_count  = total_count,
        updated_at    = NOW()
    WHERE id = target_offer;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_refresh_offer_rating_ins ON ratings;
CREATE TRIGGER trg_refresh_offer_rating_ins
AFTER INSERT ON ratings
FOR EACH ROW
EXECUTE FUNCTION refresh_offer_rating();

DROP TRIGGER IF EXISTS trg_refresh_offer_rating_upd ON ratings;
CREATE TRIGGER trg_refresh_offer_rating_upd
AFTER UPDATE ON ratings
FOR EACH ROW
EXECUTE FUNCTION refresh_offer_rating();

DROP TRIGGER IF EXISTS trg_refresh_offer_rating_del ON ratings;
CREATE TRIGGER trg_refresh_offer_rating_del
AFTER DELETE ON ratings
FOR EACH ROW
EXECUTE FUNCTION refresh_offer_rating();
