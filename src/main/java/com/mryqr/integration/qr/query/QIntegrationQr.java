package com.mryqr.integration.qr.query;

import com.mryqr.core.common.domain.Geolocation;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QIntegrationQr {
    private final String id;
    private final String name;
    private final String plateId;
    private final String appId;
    private final String groupId;
    private final boolean template;
    private final UploadedFile headerImage;
    private final String description;
    private final Map<String, AttributeValue> attributeValues;
    private final int accessCount;
    private final Instant lastAccessedAt;
    private final Geolocation geolocation;
    private final String customId;
    private final Instant createdAt;
    private final String createdBy;
    private final boolean active;
}
