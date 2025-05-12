package com.mryqr.core.assignment.query;

import com.mryqr.core.common.domain.Geolocation;
import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAssignmentListQr {
    private final String id;
    private final String name;
    private final String plateId;
    private final boolean finished;
    private final String submissionId;
    private final String operatorId;
    private final String operatorName;
    private final Instant finishedAt;
    private final UploadedFile headerImage;
    private final Geolocation geolocation;
}
