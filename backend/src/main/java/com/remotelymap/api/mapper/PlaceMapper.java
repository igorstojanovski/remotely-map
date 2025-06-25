package com.remotelymap.api.mapper;

import com.remotelymap.api.dto.AddressResponse;
import com.remotelymap.api.dto.LocationResponse;
import com.remotelymap.api.dto.PlaceRequest;
import com.remotelymap.api.dto.PlaceResponse;
import com.remotelymap.api.model.Address;
import com.remotelymap.api.model.Location;
import com.remotelymap.api.model.Place;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlaceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "locationId", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "location", ignore = true)
    Place toEntity(PlaceRequest request);

    PlaceResponse toResponse(Place place);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "locationId", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "location", ignore = true)
    void updateEntityFromRequest(PlaceRequest request, @MappingTarget Place place);
    
    AddressResponse toAddressResponse(Address address);
    LocationResponse toLocationResponse(Location location);
} 