package com.mryqr.core.app.query;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.report.ReportSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QOperationalApp {
    private String id;
    private String name;
    private UploadedFile icon;
    private boolean locked;
    private AppSetting setting;
    private ReportSetting reportSetting;
    private boolean groupSynced;
    private boolean canManageApp;
    private Map<String, String> groupFullNames;

    private Set<String> viewableGroupIds;
    private Set<String> viewablePageIds;

    private Set<String> managableGroupIds;
    private Set<String> managablePageIds;

    private Set<String> approvableGroupIds;
    private Set<String> approvablePageIds;
}
