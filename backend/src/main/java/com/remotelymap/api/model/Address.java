package com.remotelymap.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Address(
    UUID id,
    String street,
    String city,
    String country,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
