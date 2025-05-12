package com.mryqr.core.group.query;

import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QGroupQr {
    private final String id;
    private final String plateId;
    private final String name;
    private final String groupId;
    private final UploadedFile headerImage;
    private final Instant createdAt;
    private final boolean active;
}
