package com.remotelymap.api.testdata;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class InitTestDataTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration/prod,classpath:db/migration/test");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void testFlywayInsertedPlacesAndPhotos() {
        int placeCount = jdbc.queryForObject("SELECT COUNT(*) FROM places", Integer.class);
        int photoCount = jdbc.queryForObject("SELECT COUNT(*) FROM place_photos", Integer.class);

        // 5 places, 5 photos expected based on the SQL
        assertThat(placeCount).isEqualTo(5);
        assertThat(photoCount).isEqualTo(5);

        List<Map<String, Object>> rows = jdbc.queryForList("SELECT name, address FROM places");
        assertThat(rows).extracting(r -> r.get("name"))
                .containsExactlyInAnyOrder(
                        "Central Park",
                        "Empire State Building",
                        "Times Square",
                        "Brooklyn Bridge",
                        "Statue of Liberty"
                );
    }
}

