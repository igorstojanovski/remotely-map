package com.remotelymap.api.repository;

import com.remotelymap.api.model.Address;
import com.remotelymap.api.model.Location;
import com.remotelymap.api.model.Place;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcPlaceRepository implements PlaceRepository {
    private final JdbcTemplate jdbcTemplate;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    
    private final RowMapper<Place> placeRowMapper = (rs, rowNum) -> {
        Place place = new Place();
        place.setId(UUID.fromString(rs.getString("id")));
        place.setName(rs.getString("name"));
        place.setDescription(rs.getString("description"));
        place.setAddressId(rs.getString("address_id") != null ? UUID.fromString(rs.getString("address_id")) : null);
        place.setLocationId(rs.getString("location_id") != null ? UUID.fromString(rs.getString("location_id")) : null);
        place.setRating(rs.getDouble("rating"));
        place.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(OffsetDateTime.now().getOffset()));
        place.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().atOffset(OffsetDateTime.now().getOffset()));
        return place;
    };
    
    private final RowMapper<Place> placeWithJoinRowMapper = (rs, rowNum) -> {
        Place place = new Place();
        place.setId(UUID.fromString(rs.getString("p_id")));
        place.setName(rs.getString("p_name"));
        place.setDescription(rs.getString("p_description"));
        place.setAddressId(rs.getString("p_address_id") != null ? UUID.fromString(rs.getString("p_address_id")) : null);
        place.setLocationId(rs.getString("p_location_id") != null ? UUID.fromString(rs.getString("p_location_id")) : null);
        place.setRating(rs.getDouble("p_rating"));
        place.setCreatedAt(rs.getTimestamp("p_created_at").toInstant().atOffset(OffsetDateTime.now().getOffset()));
        place.setUpdatedAt(rs.getTimestamp("p_updated_at").toInstant().atOffset(OffsetDateTime.now().getOffset()));
        
        // Add address if present
        if (rs.getString("a_id") != null) {
            Address address = new Address(
                UUID.fromString(rs.getString("a_id")),
                rs.getString("a_street"),
                rs.getString("a_city"),
                rs.getString("a_country"),
                rs.getTimestamp("a_created_at").toInstant().atOffset(OffsetDateTime.now().getOffset()),
                rs.getTimestamp("a_updated_at").toInstant().atOffset(OffsetDateTime.now().getOffset())
            );
            place.setAddress(address);
        }
        
        // Add location if present
        if (rs.getString("l_id") != null) {
            double lat = rs.getDouble("l_latitude");
            double lng = rs.getDouble("l_longitude");
            Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
            
            Location location = new Location(
                UUID.fromString(rs.getString("l_id")),
                point,
                lat,
                lng,
                rs.getTimestamp("l_created_at").toInstant().atOffset(OffsetDateTime.now().getOffset()),
                rs.getTimestamp("l_updated_at").toInstant().atOffset(OffsetDateTime.now().getOffset())
            );
            place.setLocation(location);
        }
        
        return place;
    };

    @Override
    public Place save(Place place) {
        String sql = """
        INSERT INTO places (name, description, address_id, location_id, rating, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        RETURNING id
        """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, place.getName());
            ps.setString(2, place.getDescription());
            ps.setObject(3, place.getAddressId());
            ps.setObject(4, place.getLocationId());
            ps.setDouble(5, place.getRating());
            return ps;
        }, keyHolder);

        UUID id = keyHolder.getKeyAs(UUID.class);
        String selectSql = "SELECT * FROM places WHERE id = ?";
        return jdbcTemplate.queryForObject(selectSql, placeRowMapper, id);
    }

    @Override
    public Optional<Place> findById(UUID id) {
        String sql = "SELECT * FROM places WHERE id = ?";
        try {
            Place place = jdbcTemplate.queryForObject(sql, placeRowMapper, id);
            return Optional.ofNullable(place);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Place> findAll(int page, int size) {
        String sql = "SELECT * FROM places ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, placeRowMapper, size, page * size);
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM places WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM places";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    @Override
    public void update(Place place) {
        String sql = """
            UPDATE places 
            SET name = ?, description = ?, address_id = ?, location_id = ?, rating = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        jdbcTemplate.update(sql,
            place.getName(),
            place.getDescription(),
            place.getAddressId(),
            place.getLocationId(),
            place.getRating(),
            place.getId()
        );
    }

    @Override
    public void addPhoto(UUID placeId, String photoUrl) {
        String sql = "INSERT INTO place_photos (place_id, photo_url) VALUES (?, ?)";
        jdbcTemplate.update(sql, placeId, photoUrl);
    }
    
    @Override
    public List<Place> findAllWithAddressAndLocation(int page, int size) {
        String sql = """
        SELECT 
            p.id as p_id, p.name as p_name, p.description as p_description, 
            p.address_id as p_address_id, p.location_id as p_location_id, 
            p.rating as p_rating, p.created_at as p_created_at, p.updated_at as p_updated_at,
            a.id as a_id, a.street as a_street, a.city as a_city, a.country as a_country,
            a.created_at as a_created_at, a.updated_at as a_updated_at,
            l.id as l_id, l.latitude as l_latitude, l.longitude as l_longitude,
            l.created_at as l_created_at, l.updated_at as l_updated_at
        FROM places p
        LEFT JOIN addresses a ON p.address_id = a.id
        LEFT JOIN locations l ON p.location_id = l.id
        ORDER BY p.created_at DESC 
        LIMIT ? OFFSET ?
        """;
        return jdbcTemplate.query(sql, placeWithJoinRowMapper, size, page * size);
    }

    @Override
    public List<Place> findByLocationNear(double latitude, double longitude, double radiusMeters, int page, int size) {
        String sql = """
            SELECT
              p.id          AS p_id,
              p.name        AS p_name,
              p.description AS p_description,
              p.address_id  AS p_address_id,
              p.location_id AS p_location_id,
              p.rating      AS p_rating,
              p.created_at  AS p_created_at,
              p.updated_at  AS p_updated_at,
              a.id          AS a_id,
              a.street      AS a_street,
              a.city        AS a_city,
              a.country     AS a_country,
              a.created_at  AS a_created_at,
              a.updated_at  AS a_updated_at,
              l.id          AS l_id,
              l.latitude    AS l_latitude,
              l.longitude   AS l_longitude,
              l.created_at  AS l_created_at,
              l.updated_at  AS l_updated_at
            FROM places p
            LEFT JOIN addresses  a ON p.address_id  = a.id
            JOIN       locations l ON p.location_id = l.id
            WHERE ST_DWithin(
                    l.point::geography,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                    ?                          -- radius in metres
                  )
            ORDER BY l.point <-> ST_SetSRID(ST_MakePoint(?, ?), 4326)  -- same lon/lat pair
            LIMIT  ? OFFSET ?;

        """;
        return jdbcTemplate.query(sql, placeWithJoinRowMapper, 
            longitude, latitude, radiusMeters,
            longitude, latitude,
            size, page * size);
    }

    @Override
    public List<Place> findByTextSearch(String query, String city, int page, int size) {
        StringBuilder sqlBuilder = new StringBuilder("""
        SELECT 
            p.id as p_id, p.name as p_name, p.description as p_description, 
            p.address_id as p_address_id, p.location_id as p_location_id, 
            p.rating as p_rating, p.created_at as p_created_at, p.updated_at as p_updated_at,
            a.id as a_id, a.street as a_street, a.city as a_city, a.country as a_country,
            a.created_at as a_created_at, a.updated_at as a_updated_at,
            l.id as l_id, l.latitude as l_latitude, l.longitude as l_longitude,
            l.created_at as l_created_at, l.updated_at as l_updated_at
        FROM places p
        LEFT JOIN addresses a ON p.address_id = a.id
        LEFT JOIN locations l ON p.location_id = l.id
        WHERE 1=1
        """);
        
        java.util.List<Object> params = new java.util.ArrayList<>();
        
        if (query != null && !query.trim().isEmpty()) {
            sqlBuilder.append(" AND (p.name ILIKE ? OR p.description ILIKE ?)");
            String searchPattern = "%" + query + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (city != null && !city.trim().isEmpty()) {
            sqlBuilder.append(" AND a.city ILIKE ?");
            params.add("%" + city + "%");
        }
        
        sqlBuilder.append(" ORDER BY p.created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);
        
        return jdbcTemplate.query(sqlBuilder.toString(), placeWithJoinRowMapper, params.toArray());
    }
}