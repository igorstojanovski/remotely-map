package com.example.api.mapper;

import com.example.api.dto.PlaceRequest;
import com.example.api.dto.PlaceResponse;
import com.example.api.model.Place;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlaceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "photos", ignore = true)
    Place toEntity(PlaceRequest request);

    PlaceResponse toResponse(Place place);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "photos", ignore = true)
    void updateEntityFromRequest(PlaceRequest request, @MappingTarget Place place);
} 