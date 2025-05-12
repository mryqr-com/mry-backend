package com.mryqr.core.inappnotification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QInAppNotificationCount {
    private final long count;
}
