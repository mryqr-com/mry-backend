package com.mryqr.core.apptemplate.query;

import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAppTemplateDemoQr {
    private final String id;
    private final String name;
    private final UploadedFile headerImage;
    private final String customId;
    private final String plateId;
    private final Map<String, String> attributeValues;
    private final String appName;
    private final String tenantName;
    private final String groupId;
    private final String groupName;
}
