package com.example.api.controller;

import com.example.api.dto.PaginatedResponse;
import com.example.api.dto.PhotoUploadResponse;
import com.example.api.dto.PlaceRequest;
import com.example.api.dto.PlaceResponse;
import com.example.api.mapper.PlaceMapper;
import com.example.api.model.Place;
import com.example.api.service.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;
    private final PlaceMapper placeMapper;

    @PostMapping
    public ResponseEntity<PlaceResponse> createPlace(@Valid @RequestBody PlaceRequest request) {
        Place place = placeMapper.toEntity(request);
        Place savedPlace = placeService.createPlace(place);
        return new ResponseEntity<>(placeMapper.toResponse(savedPlace), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<PlaceResponse>> getAllPlaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<Place> places = placeService.getAllPlaces(page, size);
        long totalElements = placeService.countPlaces();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        List<PlaceResponse> placeResponses = places.stream()
                .map(placeMapper::toResponse)
                .collect(Collectors.toList());
        
        PaginatedResponse<PlaceResponse> response = new PaginatedResponse<>(
            placeResponses,
            page,
            size,
            totalElements,
            totalPages,
            page < totalPages - 1,
            page > 0
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponse> getPlaceById(@PathVariable UUID id) {
        return placeService.getPlaceById(id)
                .map(place -> ResponseEntity.ok(placeMapper.toResponse(place)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaceResponse> updatePlace(
            @PathVariable UUID id,
            @Valid @RequestBody PlaceRequest request) {
        Place place = placeMapper.toEntity(request);
        return placeService.updatePlace(id, place)
                .map(updatedPlace -> ResponseEntity.ok(placeMapper.toResponse(updatedPlace)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable UUID id) {
        return placeService.deletePlace(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        return placeService.uploadPhoto(id, file)
                .map(photoUrl -> new ResponseEntity<>(
                        new PhotoUploadResponse(photoUrl, "Photo uploaded successfully"),
                        HttpStatus.CREATED
                ))
                .orElse(ResponseEntity.notFound().build());
    }
} 