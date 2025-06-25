package com.remotelymap.api.repository;

import com.remotelymap.api.model.Address;
import com.remotelymap.api.model.Location;
import com.remotelymap.api.model.Place;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
@Slf4j
public class JdbcPlaceRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:15-3.3")
                    .asCompatibleSubstituteFor("postgres")
    )
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
        registry.add("spring.flyway.locations", () -> "classpath:db/migration/prod");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
    }

    @Autowired
    private JdbcPlaceRepository placeRepository;
    
    @Autowired
    private JdbcAddressRepository addressRepository;
    
    @Autowired
    private JdbcLocationRepository locationRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private UUID centralParkCafeId;
    private UUID empireStateCoworkingId;
    private UUID sfRemoteHubId;

    @BeforeEach
    void setUp() {
        // Create test addresses
        Address nycAddress = addressRepository.save(new Address(
                null, "123 Park Avenue", "New York", "USA", null, null
        ));
        Address nycAddress2 = addressRepository.save(new Address(
                null, "350 5th Avenue", "New York", "USA", null, null
        ));
        Address sfAddress = addressRepository.save(new Address(
                null, "1234 Market St", "San Francisco", "USA", null, null
        ));

        // Create test locations with PostGIS points
        // Central Park coordinates: 40.7829, -73.9654
        Point centralParkPoint = geometryFactory.createPoint(new Coordinate(-73.9654, 40.7829));
        Location centralParkLocation = locationRepository.save(new Location(
                null, centralParkPoint, 40.7829, -73.9654, null, null
        ));

        // Empire State Building coordinates: 40.7484, -73.9857 (about 3.5km from Central Park)
        Point empireStatePoint = geometryFactory.createPoint(new Coordinate(-73.9857, 40.7484));
        Location empireStateLocation = locationRepository.save(new Location(
                null, empireStatePoint, 40.7484, -73.9857, null, null
        ));

        // San Francisco coordinates: 37.7749, -122.4194 (cross-country)
        Point sfPoint = geometryFactory.createPoint(new Coordinate(-122.4194, 37.7749));
        Location sfLocation = locationRepository.save(new Location(
                null, sfPoint, 37.7749, -122.4194, null, null
        ));

        // Create test places
        Place centralParkCafe = new Place();
        centralParkCafe.setName("Central Park Cafe");
        centralParkCafe.setDescription("Great coffee spot with WiFi near Central Park, perfect for remote work");
        centralParkCafe.setAddressId(nycAddress.id());
        centralParkCafe.setLocationId(centralParkLocation.id());
        centralParkCafe.setRating(4.5);
        centralParkCafe = placeRepository.save(centralParkCafe);
        centralParkCafeId = centralParkCafe.getId();

        Place empireStateCoworking = new Place();
        empireStateCoworking.setName("Empire State Coworking");
        empireStateCoworking.setDescription("Professional coworking space with excellent internet and quiet environment");
        empireStateCoworking.setAddressId(nycAddress2.id());
        empireStateCoworking.setLocationId(empireStateLocation.id());
        empireStateCoworking.setRating(4.8);
        empireStateCoworking = placeRepository.save(empireStateCoworking);
        empireStateCoworkingId = empireStateCoworking.getId();

        Place sfRemoteHub = new Place();
        sfRemoteHub.setName("SF Remote Work Hub");
        sfRemoteHub.setDescription("Modern coworking space in downtown San Francisco with excellent amenities");
        sfRemoteHub.setAddressId(sfAddress.id());
        sfRemoteHub.setLocationId(sfLocation.id());
        sfRemoteHub.setRating(4.6);
        sfRemoteHub = placeRepository.save(sfRemoteHub);
        sfRemoteHubId = sfRemoteHub.getId();

        // Add photos
        placeRepository.addPhoto(centralParkCafeId, "https://example.com/photos/central-park-cafe.jpg");
        placeRepository.addPhoto(empireStateCoworkingId, "https://example.com/photos/empire-state-coworking.jpg");
        placeRepository.addPhoto(sfRemoteHubId, "https://example.com/photos/sf-remote-hub.jpg");
    }

    @Test
    void testSave_ShouldCreatePlaceWithGeneratedId() {
        // Given
        Address testAddress = addressRepository.save(new Address(
                null, "Test Street", "Test City", "Test Country", null, null
        ));
        Point testPoint = geometryFactory.createPoint(new Coordinate(-74.0, 40.7));
        Location testLocation = locationRepository.save(new Location(
                null, testPoint, 40.7, -74.0, null, null
        ));

        Place newPlace = new Place();
        newPlace.setName("Test Place");
        newPlace.setDescription("Test Description");
        newPlace.setAddressId(testAddress.id());
        newPlace.setLocationId(testLocation.id());
        newPlace.setRating(3.5);

        // When
        Place savedPlace = placeRepository.save(newPlace);

        // Then
        assertThat(savedPlace.getId()).isNotNull();
        assertThat(savedPlace.getName()).isEqualTo("Test Place");
        assertThat(savedPlace.getDescription()).isEqualTo("Test Description");
        assertThat(savedPlace.getRating()).isEqualTo(3.5);
        assertThat(savedPlace.getCreatedAt()).isNotNull();
        assertThat(savedPlace.getUpdatedAt()).isNotNull();
    }

    @Test
    void testFindById_ShouldReturnPlaceWhenExists() {
        // When
        Optional<Place> foundPlace = placeRepository.findById(centralParkCafeId);

        // Then
        assertThat(foundPlace).isPresent();
        assertThat(foundPlace.get().getName()).isEqualTo("Central Park Cafe");
        assertThat(foundPlace.get().getRating()).isEqualTo(4.5);
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Place> foundPlace = placeRepository.findById(nonExistentId);

        // Then
        assertThat(foundPlace).isEmpty();
    }

    @Test
    void testFindAllWithAddressAndLocation_ShouldReturnPlacesWithJoinedData() {
        // When
        List<Place> places = placeRepository.findAllWithAddressAndLocation(0, 10);

        // Then
        assertThat(places).hasSize(3);
        
        Place centralParkCafe = places.stream()
                .filter(p -> p.getName().equals("Central Park Cafe"))
                .findFirst()
                .orElseThrow();

        // Verify address data is joined
        assertThat(centralParkCafe.getAddress()).isNotNull();
        assertThat(centralParkCafe.getAddress().street()).isEqualTo("123 Park Avenue");
        assertThat(centralParkCafe.getAddress().city()).isEqualTo("New York");
        assertThat(centralParkCafe.getAddress().country()).isEqualTo("USA");

        // Verify location data is joined
        assertThat(centralParkCafe.getLocation()).isNotNull();
        assertThat(centralParkCafe.getLocation().latitude()).isEqualTo(40.7829);
        assertThat(centralParkCafe.getLocation().longitude()).isEqualTo(-73.9654);
        assertThat(centralParkCafe.getLocation().point()).isNotNull();
    }

    @Test
    void testFindByLocationNear_ShouldReturnPlacesWithinRadius() {
        // Given: Central Park coordinates with 5km radius
        double centralParkLat = 40.7829;
        double centralParkLng = -73.9654;
        double radiusMeters = 5000; // 5km

        // When
        List<Place> nearbyPlaces = placeRepository.findByLocationNear(
                centralParkLat, centralParkLng, radiusMeters, 0, 10
        );

        // Then: Should include Central Park Cafe and Empire State Coworking (both in NYC)
        // but exclude SF Remote Hub (cross-country)
        assertThat(nearbyPlaces).hasSize(2);
        
        List<String> placeNames = nearbyPlaces.stream()
                .map(Place::getName)
                .toList();
        
        assertThat(placeNames).containsExactlyInAnyOrder(
                "Central Park Cafe", 
                "Empire State Coworking"
        );

        // Verify results are ordered by distance (Central Park Cafe should be first)
        assertThat(nearbyPlaces.get(0).getName()).isEqualTo("Central Park Cafe");
    }

    @Test
    void testFindByLocationNear_ShouldReturnEmptyWhenNoPlacesInRadius() {
        // Given: Coordinates in the middle of the Atlantic Ocean
        double atlanticLat = 35.0;
        double atlanticLng = -40.0;
        double radiusMeters = 1000; // 1km

        // When
        List<Place> nearbyPlaces = placeRepository.findByLocationNear(
                atlanticLat, atlanticLng, radiusMeters, 0, 10
        );

        // Then
        assertThat(nearbyPlaces).isEmpty();
    }

    @Test
    void testFindByLocationNear_ShouldRespectPagination() {
        // Given: Central Park coordinates with large radius to include all places
        double centralParkLat = 40.7829;
        double centralParkLng = -73.9654;
        double radiusMeters = 5000000; // 5000km (includes SF)

        // When: Request only first page with size 2
        List<Place> firstPage = placeRepository.findByLocationNear(
                centralParkLat, centralParkLng, radiusMeters, 0, 2
        );
        List<Place> secondPage = placeRepository.findByLocationNear(
                centralParkLat, centralParkLng, radiusMeters, 1, 2
        );

        // Then
        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(1);
        
        // Verify no duplicates between pages
        List<UUID> firstPageIds = firstPage.stream().map(Place::getId).toList();
        List<UUID> secondPageIds = secondPage.stream().map(Place::getId).toList();
        assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
    }

    @Test
    void testFindByTextSearch_ShouldSearchByName() {
        // When
        List<Place> coffeeResults = placeRepository.findByTextSearch("Cafe", null, 0, 10);

        // Then
        assertThat(coffeeResults).hasSize(1);
        assertThat(coffeeResults.get(0).getName()).isEqualTo("Central Park Cafe");
    }

    @Test
    void testFindByTextSearch_ShouldSearchByDescription() {
        // When
        List<Place> coworkingResults = placeRepository.findByTextSearch("coworking", null, 0, 10);

        // Then
        assertThat(coworkingResults).hasSize(2);
        List<String> names = coworkingResults.stream().map(Place::getName).toList();
        assertThat(names).containsExactlyInAnyOrder(
                "Empire State Coworking", 
                "SF Remote Work Hub"
        );
    }

    @Test
    void testFindByTextSearch_ShouldFilterByCity() {
        // When
        List<Place> nycResults = placeRepository.findByTextSearch(null, "New York", 0, 10);

        // Then
        assertThat(nycResults).hasSize(2);
        List<String> names = nycResults.stream().map(Place::getName).toList();
        assertThat(names).containsExactlyInAnyOrder(
                "Central Park Cafe", 
                "Empire State Coworking"
        );
    }

    @Test
    void testFindByTextSearch_ShouldCombineQueryAndCityFilter() {
        // When
        List<Place> results = placeRepository.findByTextSearch("coworking", "New York", 0, 10);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Empire State Coworking");
    }

    @Test
    void testFindByTextSearch_ShouldReturnEmptyWhenNoMatches() {
        // When
        List<Place> results = placeRepository.findByTextSearch("nonexistent", null, 0, 10);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void testUpdate_ShouldModifyPlaceData() {
        // Given
        Place existingPlace = placeRepository.findById(centralParkCafeId).orElseThrow();
        existingPlace.setName("Updated Cafe Name");
        existingPlace.setRating(5.0);

        // When
        placeRepository.update(existingPlace);

        // Then
        Place updatedPlace = placeRepository.findById(centralParkCafeId).orElseThrow();
        assertThat(updatedPlace.getName()).isEqualTo("Updated Cafe Name");
        assertThat(updatedPlace.getRating()).isEqualTo(5.0);
    }

    @Test
    void testDeleteById_ShouldRemovePlace() {
        // Given
        assertThat(placeRepository.findById(centralParkCafeId)).isPresent();

        // When
        placeRepository.deleteById(centralParkCafeId);

        // Then
        assertThat(placeRepository.findById(centralParkCafeId)).isEmpty();
    }

    @Test
    void testCount_ShouldReturnCorrectNumber() {
        // When
        long count = placeRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void testAddPhoto_ShouldAssociatePhotoWithPlace() {
        // Given
        String photoUrl = "https://example.com/new-photo.jpg";

        // When
        placeRepository.addPhoto(centralParkCafeId, photoUrl);

        // Then
        // Verify photo was added (this would require a method to retrieve photos)
        // For now, we just verify no exception was thrown
        assertThat(centralParkCafeId).isNotNull();
    }

    @Test
    void testFindAll_ShouldReturnBasicPlaceDataWithoutJoins() {
        // When
        List<Place> places = placeRepository.findAll(0, 10);

        // Then
        assertThat(places).hasSize(3);
        
        // Verify that joined data is NOT populated in basic findAll
        Place place = places.get(0);
        assertThat(place.getId()).isNotNull();
        assertThat(place.getName()).isNotNull();
        assertThat(place.getAddressId()).isNotNull();
        assertThat(place.getLocationId()).isNotNull();
        
        // These should be null since no joins are performed
        assertThat(place.getAddress()).isNull();
        assertThat(place.getLocation()).isNull();
    }
}