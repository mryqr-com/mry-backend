package com.mryqr.core.qr.query;

import com.mryqr.core.common.domain.Geolocation;
import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QQrBaseSetting {
    private String id;
    private String name;
    private String description;
    private UploadedFile headerImage;
    private Map<String, String> manualAttributeValues;
    private Geolocation geolocation;
    private String customId;
}
