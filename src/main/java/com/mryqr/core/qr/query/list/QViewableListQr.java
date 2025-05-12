package com.mryqr.core.qr.query.list;

import com.mryqr.common.domain.Geolocation;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.display.DisplayValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QViewableListQr {
    private final String id;
    private final String name;
    private final String plateId;
    private final String appId;
    private final String groupId;
    private final String circulationOptionId;
    private final boolean template;
    private final Instant createdAt;
    private final String createdBy;
    private final String creator;
    private final Geolocation geolocation;
    private final UploadedFile headerImage;
    private final Map<String, DisplayValue> attributeDisplayValues;
    private final String customId;
    private final boolean active;
}
