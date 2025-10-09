-- Maintain aggregate rating fields on offers whenever ratings change
CREATE OR REPLACE FUNCTION update_offer_rating()
RETURNS TRIGGER AS $$
DECLARE
    target_offer BIGINT;
BEGIN
    target_offer := COALESCE(NEW.offer_id, OLD.offer_id);

    UPDATE offers
    SET average_rating = COALESCE((
            SELECT AVG(r.rating)::NUMERIC(3,2) FROM ratings r WHERE r.offer_id = target_offer
        ), 0),
        rating_count = (
            SELECT COUNT(*) FROM ratings r WHERE r.offer_id = target_offer
        )
    WHERE id = target_offer;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_offer_rating_ins ON ratings;
CREATE TRIGGER trg_update_offer_rating_ins
AFTER INSERT ON ratings
FOR EACH ROW
EXECUTE FUNCTION update_offer_rating();

DROP TRIGGER IF EXISTS trg_update_offer_rating_upd ON ratings;
CREATE TRIGGER trg_update_offer_rating_upd
AFTER UPDATE ON ratings
FOR EACH ROW
EXECUTE FUNCTION update_offer_rating();

DROP TRIGGER IF EXISTS trg_update_offer_rating_del ON ratings;
CREATE TRIGGER trg_update_offer_rating_del
AFTER DELETE ON ratings
FOR EACH ROW
EXECUTE FUNCTION update_offer_rating();
