package com.mryqr.common.about;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAboutInfo {
    private final String buildTime;
    private final String deployTime;
    private final String gitRevision;
    private final String gitBranch;
    private final String environment;
}
