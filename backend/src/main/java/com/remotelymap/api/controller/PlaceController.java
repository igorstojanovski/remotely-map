package com.remotelymap.api.controller;

import com.remotelymap.api.dto.PaginatedResponse;
import com.remotelymap.api.dto.PhotoUploadResponse;
import com.remotelymap.api.dto.PlaceRequest;
import com.remotelymap.api.dto.PlaceResponse;
import com.remotelymap.api.mapper.PlaceMapper;
import com.remotelymap.api.model.Place;
import com.remotelymap.api.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Places", description = "Places management APIs")
public class PlaceController {
    private final PlaceService placeService;
    private final PlaceMapper placeMapper;

    @Operation(
        summary = "Create a new place",
        description = "Creates a new place with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Place created successfully",
            content = @Content(schema = @Schema(implementation = PlaceResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input"
        )
    })
    @PostMapping
    public ResponseEntity<PlaceResponse> createPlace(@Valid @RequestBody PlaceRequest request) {
        Place place = placeMapper.toEntity(request);
        Place savedPlace = placeService.createPlace(place);
        return new ResponseEntity<>(placeMapper.toResponse(savedPlace), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Get all places",
        description = "Retrieves a paginated list of all places"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved places",
            content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
        )
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<PlaceResponse>> getAllPlaces(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") 
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

    @Operation(
        summary = "Get place by ID",
        description = "Retrieves a specific place by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Place found",
            content = @Content(schema = @Schema(implementation = PlaceResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Place not found"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponse> getPlaceById(
            @Parameter(description = "Place ID") 
            @PathVariable UUID id) {
        return placeService.getPlaceById(id)
                .map(place -> ResponseEntity.ok(placeMapper.toResponse(place)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Update a place",
        description = "Updates an existing place with new details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Place updated successfully",
            content = @Content(schema = @Schema(implementation = PlaceResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Place not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<PlaceResponse> updatePlace(
            @Parameter(description = "Place ID") 
            @PathVariable UUID id,
            @Valid @RequestBody PlaceRequest request) {
        Place place = placeMapper.toEntity(request);
        return placeService.updatePlace(id, place)
                .map(updatedPlace -> ResponseEntity.ok(placeMapper.toResponse(updatedPlace)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Delete a place",
        description = "Deletes a place by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Place deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Place not found"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(
            @Parameter(description = "Place ID") 
            @PathVariable UUID id) {
        return placeService.deletePlace(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Upload a photo",
        description = "Uploads a photo for a specific place"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Photo uploaded successfully",
            content = @Content(schema = @Schema(implementation = PhotoUploadResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Place not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file"
        )
    })
    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(
            @Parameter(description = "Place ID") 
            @PathVariable UUID id,
            @Parameter(description = "Photo file") 
            @RequestParam("file") MultipartFile file) {
        return placeService.uploadPhoto(id, file)
                .map(photoUrl -> new ResponseEntity<>(
                        new PhotoUploadResponse(photoUrl, "Photo uploaded successfully"),
                        HttpStatus.CREATED
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Search places nearby",
        description = "Finds places within a specified radius of given coordinates"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved nearby places",
            content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
        )
    })
    @GetMapping("/nearby")
    public ResponseEntity<PaginatedResponse<PlaceResponse>> getPlacesNearby(
            @Parameter(description = "Latitude") 
            @RequestParam double lat,
            @Parameter(description = "Longitude") 
            @RequestParam double lng,
            @Parameter(description = "Search radius in kilometers") 
            @RequestParam(defaultValue = "5.0") double radius,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") 
            @RequestParam(defaultValue = "10") int size) {

        List<Place> places = placeService.getPlacesNearby(lat, lng, radius, page, size);
        long totalElements = placeService.countPlaces(); // Simplified for now
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

    @Operation(
        summary = "Search places by text",
        description = "Searches places by name, description, or city"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved search results",
            content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
        )
    })
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<PlaceResponse>> searchPlaces(
            @Parameter(description = "Search query for name or description") 
            @RequestParam(required = false) String q,
            @Parameter(description = "Filter by city") 
            @RequestParam(required = false) String city,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") 
            @RequestParam(defaultValue = "10") int size) {

        List<Place> places = placeService.searchPlaces(q, city, page, size);
        long totalElements = placeService.countPlaces(); // Simplified for now
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
} 