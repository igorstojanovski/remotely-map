package com.remotelymap.api.repository;

import com.remotelymap.api.model.Place;
import lombok.RequiredArgsConstructor;
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
    
    private final RowMapper<Place> placeRowMapper = (rs, rowNum) -> {
        Place place = new Place();
        place.setId(UUID.fromString(rs.getString("id")));
        place.setName(rs.getString("name"));
        place.setDescription(rs.getString("description"));
        place.setAddress(rs.getString("address"));
        place.setRating(rs.getDouble("rating"));
        place.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(OffsetDateTime.now().getOffset()));
        place.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().atOffset(OffsetDateTime.now().getOffset()));
        return place;
    };

    @Override
    public Place save(Place place) {
        String sql = """
        INSERT INTO places (name, description, address, rating, created_at, updated_at)
        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        RETURNING id
        """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, place.getName());
            ps.setString(2, place.getDescription());
            ps.setString(3, place.getAddress());
            ps.setDouble(4, place.getRating());
            return ps;
        }, keyHolder);

        // Get the UUID from the key holder
        UUID id = keyHolder.getKeyAs(UUID.class);

        // Fetch the complete saved entity
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
            SET name = ?, description = ?, address = ?, rating = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        jdbcTemplate.update(sql,
            place.getName(),
            place.getDescription(),
            place.getAddress(),
            place.getRating(),
            place.getId()
        );
    }

    @Override
    public void addPhoto(UUID placeId, String photoUrl) {
        String sql = "INSERT INTO place_photos (place_id, photo_url) VALUES (?, ?)";
        jdbcTemplate.update(sql, placeId, photoUrl);
    }
}