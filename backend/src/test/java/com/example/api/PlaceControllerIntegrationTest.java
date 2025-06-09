package com.example.api;

import com.example.api.dto.PhotoUploadResponse;
import com.example.api.dto.PlaceRequest;
import com.example.api.dto.PlaceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "logging.level.org.springframework=DEBUG",
    "logging.level.com.example.api=DEBUG"
})
class PlaceControllerIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(PlaceControllerIntegrationTest.class);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        log.info("Configuring database properties for test");
        log.info("Database URL: {}", postgres.getJdbcUrl());
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
    }

    @Test
    void shouldCreateAndRetrievePlace() throws Exception {
        // Create place request
        PlaceRequest request = new PlaceRequest();
        request.setName("Test Place");
        request.setDescription("A test place");
        request.setAddress("123 Test St");
        request.setRating(4.5);

        // Create place
        MvcResult createResult = mockMvc.perform(post("/api/places")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        PlaceResponse createdPlace = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            PlaceResponse.class
        );

        // Assert created place
        assertThat(createdPlace)
            .isNotNull()
            .satisfies(place -> {
                assertThat(place.getId()).isNotNull();
                assertThat(place.getName()).isEqualTo(request.getName());
                assertThat(place.getDescription()).isEqualTo(request.getDescription());
                assertThat(place.getAddress()).isEqualTo(request.getAddress());
                assertThat(place.getRating()).isEqualTo(request.getRating());
                assertThat(place.getCreatedAt()).isNotNull();
                assertThat(place.getUpdatedAt()).isNotNull();
            });

        // Retrieve place
        MvcResult getResult = mockMvc.perform(get("/api/places/" + createdPlace.getId()))
                .andExpect(status().isOk())
                .andReturn();

        PlaceResponse retrievedPlace = objectMapper.readValue(
            getResult.getResponse().getContentAsString(),
            PlaceResponse.class
        );

        // Assert retrieved place matches created place
        assertThat(retrievedPlace)
            .isNotNull()
            .isEqualTo(createdPlace);
    }

    @Test
    void shouldUpdatePlace() throws Exception {
        // Create initial place
        PlaceRequest createRequest = new PlaceRequest();
        createRequest.setName("Initial Place");
        createRequest.setDescription("Initial description");
        createRequest.setAddress("Initial address");
        createRequest.setRating(3.0);

        MvcResult createResult = mockMvc.perform(post("/api/places")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        PlaceResponse createdPlace = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            PlaceResponse.class
        );

        // Update place
        PlaceRequest updateRequest = new PlaceRequest();
        updateRequest.setName("Updated Place");
        updateRequest.setDescription("Updated description");
        updateRequest.setAddress("Updated address");
        updateRequest.setRating(4.5);

        MvcResult updateResult = mockMvc.perform(put("/api/places/" + createdPlace.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andReturn();

        PlaceResponse updatedPlace = objectMapper.readValue(
            updateResult.getResponse().getContentAsString(),
            PlaceResponse.class
        );

        // Assert updated place
        assertThat(updatedPlace)
            .isNotNull()
            .satisfies(place -> {
                assertThat(place.getId()).isEqualTo(createdPlace.getId());
                assertThat(place.getName()).isEqualTo(updateRequest.getName());
                assertThat(place.getDescription()).isEqualTo(updateRequest.getDescription());
                assertThat(place.getAddress()).isEqualTo(updateRequest.getAddress());
                assertThat(place.getRating()).isEqualTo(updateRequest.getRating());
                assertThat(place.getUpdatedAt()).isAfter(place.getCreatedAt());
            });
    }

    @Test
    void shouldDeletePlace() throws Exception {
        // Create place
        PlaceRequest request = new PlaceRequest();
        request.setName("Place to Delete");
        request.setDescription("Will be deleted");
        request.setAddress("Delete St");
        request.setRating(3.0);

        MvcResult createResult = mockMvc.perform(post("/api/places")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        PlaceResponse createdPlace = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            PlaceResponse.class
        );

        // Delete place
        mockMvc.perform(delete("/api/places/" + createdPlace.getId()))
                .andExpect(status().isNoContent());

        // Verify place is deleted
        MvcResult getResult = mockMvc.perform(get("/api/places/" + createdPlace.getId()))
                .andExpect(status().isNotFound())
                .andReturn();

        assertThat(getResult.getResponse().getContentAsString()).isEmpty();
    }

    @Test
    void shouldUploadPhoto() throws Exception {
        // Create place
        PlaceRequest request = new PlaceRequest();
        request.setName("Place with Photo");
        request.setDescription("Will have a photo");
        request.setAddress("Photo St");
        request.setRating(4.0);

        MvcResult createResult = mockMvc.perform(post("/api/places")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        PlaceResponse createdPlace = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            PlaceResponse.class
        );

        // Upload photo
        MockMultipartFile photo = new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/places/" + createdPlace.getId() + "/photos")
                .file(photo))
                .andExpect(status().isCreated())
                .andReturn();

        PhotoUploadResponse uploadResponse = objectMapper.readValue(
            uploadResult.getResponse().getContentAsString(),
            PhotoUploadResponse.class
        );

        // Assert photo upload response
        assertThat(uploadResponse)
            .isNotNull()
            .satisfies(response -> {
                assertThat(response.getPhotoUrl()).isNotNull().isNotEmpty();
                assertThat(response.getMessage()).isEqualTo("Photo uploaded successfully");
            });
    }

    @Test
    void shouldReturnNotFoundForNonExistentPlace() throws Exception {
        mockMvc.perform(get("/api/places/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    void cleanup() {
        log.info("Cleaning up test data");
        // Delete in correct order due to foreign key constraints
        jdbcTemplate.execute("DELETE FROM place_photos");
        jdbcTemplate.execute("DELETE FROM places");
    }
} 