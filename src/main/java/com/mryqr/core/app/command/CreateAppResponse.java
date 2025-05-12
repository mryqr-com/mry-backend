package com.mryqr.core.app.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateAppResponse {
    private final String appId;
    private final String defaultGroupId;
    private final String homePageId;
}
