package com.mryqr.core.inappnotification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QInAppNotification {
    private final String id;
    private final String memberId;
    private final boolean viewed;
    private final String pcUrl;
    private final String mobileUrl;
    private final String content;
    private final Instant createdAt;
}
