package com.mryqr.common.about;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAboutInfo {
    private final String deployTime;
    private final String environment;
}
