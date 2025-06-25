-- Clear existing data
DELETE FROM place_photos;
DELETE FROM places;
DELETE FROM addresses;
DELETE FROM locations;

-- Insert sample addresses
INSERT INTO addresses (id, street, city, country, created_at, updated_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001', '123 Park Avenue', 'New York', 'USA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440002', '350 5th Avenue', 'New York', 'USA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440003', 'Times Square', 'New York', 'USA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440004', 'Brooklyn Bridge', 'New York', 'USA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440005', 'Liberty Island', 'New York', 'USA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440006', '1234 Market St', 'San Francisco', 'USA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440007', '567 Mission St', 'San Francisco', 'USA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample locations with PostGIS points
INSERT INTO locations (id, point, latitude, longitude, created_at, updated_at)
VALUES
    ('660e8400-e29b-41d4-a716-446655440001', ST_Point(-73.9654, 40.7829, 4326), 40.7829, -73.9654, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Central Park
    ('660e8400-e29b-41d4-a716-446655440002', ST_Point(-73.9857, 40.7484, 4326), 40.7484, -73.9857, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Empire State
    ('660e8400-e29b-41d4-a716-446655440003', ST_Point(-73.9857, 40.7580, 4326), 40.7580, -73.9857, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Times Square
    ('660e8400-e29b-41d4-a716-446655440004', ST_Point(-73.9969, 40.7061, 4326), 40.7061, -73.9969, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Brooklyn Bridge
    ('660e8400-e29b-41d4-a716-446655440005', ST_Point(-74.0445, 40.6892, 4326), 40.6892, -74.0445, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Statue of Liberty
    ('660e8400-e29b-41d4-a716-446655440006', ST_Point(-122.4194, 37.7749, 4326), 37.7749, -122.4194, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- SF Coffee Shop
    ('660e8400-e29b-41d4-a716-446655440007', ST_Point(-122.3965, 37.7849, 4326), 37.7849, -122.3965, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); -- SF Coworking

-- Insert sample places with references to addresses and locations
INSERT INTO places (id, name, description, address_id, location_id, rating, created_at, updated_at)
VALUES
    ('770e8400-e29b-41d4-a716-446655440001', 'Central Park Cafe', 'Great coffee spot with WiFi near Central Park, perfect for remote work', '550e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440001', 4.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('770e8400-e29b-41d4-a716-446655440002', 'Empire State Coworking', 'Professional coworking space with excellent internet and quiet environment', '550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440002', 4.8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('770e8400-e29b-41d4-a716-446655440003', 'Times Square Internet Cafe', 'Busy cafe with reliable WiFi and power outlets, open 24/7', '550e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440003', 4.3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('770e8400-e29b-41d4-a716-446655440004', 'Brooklyn Bridge View Library', 'Public library with free WiFi and amazing bridge views', '550e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440004', 4.7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('770e8400-e29b-41d4-a716-446655440005', 'Liberty Coffee Roasters', 'Quiet coffee shop with great WiFi and comfortable seating', '550e8400-e29b-41d4-a716-446655440005', '660e8400-e29b-41d4-a716-446655440005', 4.9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('770e8400-e29b-41d4-a716-446655440006', 'SF Remote Work Hub', 'Modern coworking space in downtown San Francisco with excellent amenities', '550e8400-e29b-41d4-a716-446655440006', '660e8400-e29b-41d4-a716-446655440006', 4.6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('770e8400-e29b-41d4-a716-446655440007', 'Mission District Cafe', 'Hip cafe with strong WiFi and laptop-friendly atmosphere', '550e8400-e29b-41d4-a716-446655440007', '660e8400-e29b-41d4-a716-446655440007', 4.4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Add some sample photos
INSERT INTO place_photos (place_id, photo_url)
VALUES
    ('770e8400-e29b-41d4-a716-446655440001', 'https://example.com/photos/central-park-cafe.jpg'),
    ('770e8400-e29b-41d4-a716-446655440002', 'https://example.com/photos/empire-state-coworking.jpg'),
    ('770e8400-e29b-41d4-a716-446655440003', 'https://example.com/photos/times-square-cafe.jpg'),
    ('770e8400-e29b-41d4-a716-446655440004', 'https://example.com/photos/brooklyn-bridge-library.jpg'),
    ('770e8400-e29b-41d4-a716-446655440005', 'https://example.com/photos/liberty-coffee.jpg'),
    ('770e8400-e29b-41d4-a716-446655440006', 'https://example.com/photos/sf-remote-hub.jpg'),
    ('770e8400-e29b-41d4-a716-446655440007', 'https://example.com/photos/mission-cafe.jpg');