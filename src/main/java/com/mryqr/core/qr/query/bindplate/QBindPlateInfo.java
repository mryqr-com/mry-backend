package com.mryqr.core.qr.query.bindplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QBindPlateInfo {
    private final String plateId;
    private final String memberId;
    private final String appId;
    private final String appName;
    private final String instanceDesignation;
    private final String groupDesignation;
    private final String homePageId;
    private final Map<String, String> selectableGroups;
}
