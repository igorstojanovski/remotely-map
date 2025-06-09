package com.example.api.repository;

import com.example.api.model.Place;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository {
    Place save(Place place);
    Optional<Place> findById(UUID id);
    List<Place> findAll(int page, int size);
    void deleteById(UUID id);
    long count();
    void update(Place place);
    void addPhoto(UUID placeId, String photoUrl);
}