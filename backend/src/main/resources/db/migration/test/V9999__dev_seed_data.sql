-- Clear existing data
DELETE FROM place_photos;
DELETE FROM places;

-- Insert sample places
INSERT INTO places (id, name, description, address, rating, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Central Park', 'A large urban park in Manhattan', '123 Park Avenue', 4.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Empire State Building', 'Iconic skyscraper in New York City', '350 5th Avenue', 4.8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Times Square', 'Major commercial intersection in Manhattan', 'Manhattan, NY 10036', 4.3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Brooklyn Bridge', 'Historic suspension bridge', 'Brooklyn Bridge, New York, NY', 4.7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Statue of Liberty', 'Iconic symbol of freedom', 'Liberty Island, New York, NY', 4.9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Add some sample photos
INSERT INTO place_photos (place_id, photo_url)
SELECT
    id,
    'https://example.com/photos/' || LOWER(REPLACE(name, ' ', '-')) || '.jpg'
FROM places;