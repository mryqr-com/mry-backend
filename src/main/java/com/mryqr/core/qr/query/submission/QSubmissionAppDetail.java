package com.mryqr.core.qr.query.submission;

import com.mryqr.core.app.domain.AppSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QSubmissionAppDetail {
    private final String id;
    private final String name;
    private final String version;
    private final AppSetting setting;
}
