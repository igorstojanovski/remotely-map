package com.remotelymap.api.service;

import com.remotelymap.api.model.Place;
import com.remotelymap.api.repository.JdbcPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final JdbcPlaceRepository placeRepository;

    @Transactional(readOnly = true)
    public List<Place> getAllPlaces(int page, int size) {
        return placeRepository.findAllWithAddressAndLocation(page, size);
    }

    @Transactional(readOnly = true)
    public List<Place> getPlacesNearby(double latitude, double longitude, double radiusKm, int page, int size) {
        double radiusMeters = radiusKm * 1000; // Convert km to meters
        return placeRepository.findByLocationNear(latitude, longitude, radiusMeters, page, size);
    }

    @Transactional(readOnly = true)
    public List<Place> searchPlaces(String query, String city, int page, int size) {
        return placeRepository.findByTextSearch(query, city, page, size);
    }

    @Transactional(readOnly = true)
    public Optional<Place> getPlaceById(UUID id) {
        return placeRepository.findById(id);
    }

    @Transactional
    public Place createPlace(Place place) {
        return placeRepository.save(place);
    }

    @Transactional
    public Optional<Place> updatePlace(UUID id, Place place) {
        return placeRepository.findById(id).map(existingPlace -> {
            place.setId(id);
            placeRepository.update(place);
            return placeRepository.findById(id).orElse(place);
        });
    }

    @Transactional
    public boolean deletePlace(UUID id) {
        if (placeRepository.findById(id).isPresent()) {
            placeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public long countPlaces() {
        return placeRepository.count();
    }

    @Transactional
    public Optional<String> uploadPhoto(UUID id, MultipartFile file) {
        return placeRepository.findById(id).map(place -> {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            if (!file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image");
            }
            
            String photoUrl = "/photos/" + id + "/" + file.getOriginalFilename();
            placeRepository.addPhoto(id, photoUrl);
            
            return photoUrl;
        });
    }
} 