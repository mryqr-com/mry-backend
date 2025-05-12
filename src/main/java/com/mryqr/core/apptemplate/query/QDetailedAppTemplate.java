package com.mryqr.core.apptemplate.query;

import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.plan.domain.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QDetailedAppTemplate {
    private String id;
    private String name;
    private PlanType planType;
    private String cardDescription;
    private String detailDescription;
    private String introduction;
    private PlateSetting plateSetting;
    private QAppTemplateDemoQr demoQr;

    private int controlCount;
    private boolean geolocationEnabled;
    private boolean plateBatchEnabled;
    private boolean assignmentEnabled;
    private List<String> numberReports;
    private List<String> chartReports;
    private List<String> kanbans;
    private List<String> circulationStatuses;
    private List<String> pages;
    private List<String> fillablePages;
    private List<String> approvalPages;
    private List<String> notificationPages;
    private List<String> attributes;
    private List<String> operationMenus;
}
