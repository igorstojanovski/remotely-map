package com.remotelymap.api.repository;

import com.remotelymap.api.model.Place;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository {
    Place save(Place place);
    Optional<Place> findById(UUID id);
    List<Place> findAll(int page, int size);
    List<Place> findAllWithAddressAndLocation(int page, int size);
    List<Place> findByLocationNear(double latitude, double longitude, double radiusMeters, int page, int size);
    List<Place> findByTextSearch(String query, String city, int page, int size);
    void deleteById(UUID id);
    long count();
    void update(Place place);
    void addPhoto(UUID placeId, String photoUrl);
}