package com.remotelymap.api.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PlaceResponse {
    private UUID id;
    private String name;
    private String description;
    private String address;
    private Double rating;
    private List<String> photos;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
} 