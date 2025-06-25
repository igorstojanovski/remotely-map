-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create Address table
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create Location table with PostGIS geometry
CREATE TABLE locations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    point GEOMETRY(POINT, 4326) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create spatial index for efficient location queries
CREATE INDEX idx_locations_point ON locations USING GIST (point);

-- Add foreign key columns to places table
ALTER TABLE places 
ADD COLUMN address_id UUID REFERENCES addresses(id),
ADD COLUMN location_id UUID REFERENCES locations(id);

-- Remove old address column since it's now in separate table
ALTER TABLE places DROP COLUMN IF EXISTS address;

-- Create indexes for foreign keys
CREATE INDEX idx_places_address_id ON places(address_id);
CREATE INDEX idx_places_location_id ON places(location_id);