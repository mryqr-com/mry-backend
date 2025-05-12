package com.mryqr.core.qr.query.submission;

import com.mryqr.core.common.domain.Geolocation;
import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QSubmissionQrDetail {
    private final String id;
    private final String plateId;
    private final String name;
    private final String appId;
    private final String groupId;
    private final String tenantId;
    private final String circulationOptionId;
    private final boolean template;
    private final UploadedFile headerImage;
    private final Geolocation geolocation;
    private final String description;
}
