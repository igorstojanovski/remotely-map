package com.remotelymap.api.model;

import org.locationtech.jts.geom.Point;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Location(
    UUID id,
    Point point,
    double latitude,
    double longitude,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
