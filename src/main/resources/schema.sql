CREATE TABLE IF NOT EXISTS cars (
    id UUID PRIMARY KEY,
    type VARCHAR(16) NOT NULL CHECK (type IN ('SUV', 'VAN', 'SEDAN')),
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    photo_url VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS reservations (
    id UUID PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    cancelled BOOLEAN NOT NULL DEFAULT FALSE,
    CHECK (end_date >= start_date)
);

CREATE TABLE IF NOT EXISTS reservation_cars (
    reservation_id UUID NOT NULL REFERENCES reservations (id) ON DELETE CASCADE,
    car_id UUID NOT NULL REFERENCES cars (id),
    PRIMARY KEY (reservation_id, car_id)
);

CREATE INDEX IF NOT EXISTS reservations_dates_idx
    ON reservations (start_date, end_date);

CREATE INDEX IF NOT EXISTS cars_type_idx ON cars (type);

-- OVERLAP GUARD
-- Rejects a second active reservation that overlaps the same car, and locks that
-- car so two concurrent inserts cannot both pass the check.

CREATE OR REPLACE FUNCTION reject_overlapping_active_assignment()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    new_start DATE;
    new_end DATE;
    new_cancelled BOOLEAN;
BEGIN
    SELECT start_date, end_date, cancelled
      INTO new_start, new_end, new_cancelled
      FROM reservations
     WHERE id = NEW.reservation_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Reservation % does not exist', NEW.reservation_id;
    END IF;

    IF new_cancelled THEN
        RETURN NEW;
    END IF;

    PERFORM 1 FROM cars WHERE id = NEW.car_id FOR UPDATE;

    IF EXISTS (
        SELECT 1
          FROM reservation_cars rc
          JOIN reservations r ON r.id = rc.reservation_id
         WHERE rc.car_id = NEW.car_id
           AND rc.reservation_id <> NEW.reservation_id
           AND r.cancelled = FALSE
           AND r.start_date <= new_end
           AND r.end_date >= new_start
    ) THEN
        RAISE EXCEPTION 'Car % is already booked for an overlapping period', NEW.car_id
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS reservation_cars_no_overlap ON reservation_cars;

CREATE TRIGGER reservation_cars_no_overlap
    BEFORE INSERT ON reservation_cars
    FOR EACH ROW
    EXECUTE FUNCTION reject_overlapping_active_assignment();

CREATE INDEX IF NOT EXISTS reservation_cars_car_id_idx ON reservation_cars (car_id);
