package com.mryqr.core.app.report;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.UpdateAppReportSettingCommand;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.report.ReportSetting;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.ChartReportConfiguration;
import com.mryqr.core.app.domain.report.chart.ChartReportSetting;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeBarReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeDoughnutReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeNumberRangeSegmentReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributePieReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeTimeSegmentReport;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeCategorizedReportSetting;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeNumberRangeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeTimeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.control.ControlBarReport;
import com.mryqr.core.app.domain.report.chart.control.ControlDoughnutReport;
import com.mryqr.core.app.domain.report.chart.control.ControlNumberRangeSegmentReport;
import com.mryqr.core.app.domain.report.chart.control.ControlPieReport;
import com.mryqr.core.app.domain.report.chart.control.ControlTimeSegmentReport;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlCategorizedReportSetting;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlNumberRangeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlTimeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.style.BarReportStyle;
import com.mryqr.core.app.domain.report.chart.style.DoughnutReportStyle;
import com.mryqr.core.app.domain.report.chart.style.NumberRangeSegmentReportStyle;
import com.mryqr.core.app.domain.report.chart.style.PieReportStyle;
import com.mryqr.core.app.domain.report.chart.style.TimeSegmentReportStyle;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.app.domain.report.number.NumberReportConfiguration;
import com.mryqr.core.app.domain.report.number.NumberReportSetting;
import com.mryqr.core.app.domain.report.number.attribute.AttributeNumberReport;
import com.mryqr.core.app.domain.report.number.control.ControlNumberReport;
import com.mryqr.core.app.domain.report.number.instance.InstanceNumberReport;
import com.mryqr.core.app.domain.report.number.page.PageNumberReport;
import com.mryqr.core.common.domain.report.QrReportTimeBasedType;
import com.mryqr.core.common.utils.UuidGenerator;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.attribute.AttributeType.DIRECT_INPUT;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_SUBMIT_COUNT;
import static com.mryqr.core.app.domain.page.control.MultiLevelSelectionPrecisionType.LEVEL2;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.ATTRIBUTE_BAR_REPORT;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.ATTRIBUTE_DOUGHNUT_REPORT;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.ATTRIBUTE_PIE_REPORT;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.ATTRIBUTE_TIME_SEGMENT_REPORT;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.CONTROL_BAR_REPORT;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.CONTROL_DOUGHNUT_REPORT;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.CONTROL_NUMBER_RANGE_REPORT;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.CONTROL_PIE_REPORT;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.CONTROL_TIME_SEGMENT_REPORT;
import static com.mryqr.core.app.domain.report.number.NumberReportType.ATTRIBUTE_NUMBER_REPORT;
import static com.mryqr.core.app.domain.report.number.NumberReportType.CONTROL_NUMBER_REPORT;
import static com.mryqr.core.app.domain.report.number.NumberReportType.INSTANCE_NUMBER_REPORT;
import static com.mryqr.core.app.domain.report.number.NumberReportType.PAGE_NUMBER_REPORT;
import static com.mryqr.core.app.domain.report.number.instance.InstanceNumberReportType.INSTANCE_COUNT;
import static com.mryqr.core.app.domain.report.number.page.PageNumberReportType.PAGE_SUBMIT_COUNT;
import static com.mryqr.core.common.domain.AddressPrecisionType.CITY;
import static com.mryqr.core.common.domain.report.NumberAggregationType.AVG;
import static com.mryqr.core.common.domain.report.QrSegmentType.ATTRIBUTE_VALUE_MAX;
import static com.mryqr.core.common.domain.report.QrSegmentType.QR_COUNT_SUM;
import static com.mryqr.core.common.domain.report.ReportRange.NO_LIMIT;
import static com.mryqr.core.common.domain.report.SubmissionReportTimeBasedType.CREATED_AT;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.CONTROL_VALUE_AVG;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.CONTROL_VALUE_SUM;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.core.common.domain.report.TimeSegmentInterval.PER_MONTH;
import static com.mryqr.core.common.exception.ErrorCode.ATTRIBUTE_NOT_CATEGORIZED;
import static com.mryqr.core.common.exception.ErrorCode.ATTRIBUTE_NOT_NUMBER_VALUED;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_CATEGORIZED;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_NUMBERED;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_NUMBER_VALUED;
import static com.mryqr.core.common.exception.ErrorCode.REPORTING_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_ATTRIBUTE_NOT_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_CONTROL_NOT_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControl;
import static com.mryqr.utils.RandomTestFixture.defaultNumberInputControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static com.mryqr.utils.RandomTestFixture.rColor;
import static com.mryqr.utils.RandomTestFixture.rReportName;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppReportSettingApiTest extends BaseApiTest {

    @Test
    public void should_update_number_report_setting() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FNumberInputControl control = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(INSTANCE_SUBMIT_COUNT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        AttributeNumberReport attributeNumberReport = AttributeNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(ATTRIBUTE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .numberAggregationType(AVG)
                .attributeId(attribute.getId())
                .build();

        ControlNumberReport controlNumberReport = ControlNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_REPORT)
                .range(NO_LIMIT)
                .numberAggregationType(AVG)
                .pageId(response.getHomePageId())
                .controlId(control.getId())
                .build();

        InstanceNumberReport instanceNumberReport = InstanceNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(INSTANCE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .instanceNumberReportType(INSTANCE_COUNT)
                .build();

        PageNumberReport pageNumberReport = PageNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(PAGE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .pageNumberReportType(PAGE_SUBMIT_COUNT)
                .pageId(response.getHomePageId())
                .build();

        List<NumberReport> reports = newArrayList(attributeNumberReport, controlNumberReport, instanceNumberReport, pageNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        AppApi.updateAppReportSetting(response.getJwt(), response.getAppId(), command);

        App app = appRepository.byId(response.getAppId());
        assertEquals(command.getSetting(), app.getReportSetting());
    }

    @Test
    public void should_fail_update_report_setting_if_package_not_enough() {
        PreparedQrResponse response = setupApi.registerWithQr();

        InstanceNumberReport instanceNumberReport = InstanceNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(INSTANCE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .instanceNumberReportType(INSTANCE_COUNT)
                .build();

        List<NumberReport> reports = newArrayList(instanceNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), REPORTING_NOT_ALLOWED);
    }

    @Test
    public void should_fail_update_number_report_setting_if_referenced_attribute_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AttributeNumberReport attributeNumberReport = AttributeNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(ATTRIBUTE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .numberAggregationType(AVG)
                .attributeId(Attribute.newAttributeId())
                .build();

        List<NumberReport> reports = newArrayList(attributeNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_ATTRIBUTE_NOT_EXIST);
    }

    @Test
    public void should_fail_update_number_report_setting_if_referenced_page_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FNumberInputControl control = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        ControlNumberReport controlNumberReport = ControlNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_REPORT)
                .range(NO_LIMIT)
                .numberAggregationType(AVG)
                .pageId(Page.newPageId())
                .controlId(control.getId())
                .build();

        List<NumberReport> reports = newArrayList(controlNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();
        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_update_number_report_setting_if_referenced_control_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        ControlNumberReport controlNumberReport = ControlNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_REPORT)
                .range(NO_LIMIT)
                .numberAggregationType(AVG)
                .pageId(response.getHomePageId())
                .controlId(Control.newControlId())
                .build();

        List<NumberReport> reports = newArrayList(controlNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();
        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_update_number_report_setting_if_referenced_control_type_not_supported() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        ControlNumberReport controlNumberReport = ControlNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_REPORT)
                .range(NO_LIMIT)
                .numberAggregationType(AVG)
                .pageId(response.getHomePageId())
                .controlId(control.getId())
                .build();

        List<NumberReport> reports = newArrayList(controlNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();
        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), CONTROL_NOT_NUMBER_VALUED);
    }

    @Test
    public void should_fail_update_number_report_setting_if_referenced_page_not_exist_for_page_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        PageNumberReport pageNumberReport = PageNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(PAGE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .pageNumberReportType(PAGE_SUBMIT_COUNT)
                .pageId(Page.newPageId())
                .build();

        List<NumberReport> reports = newArrayList(pageNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();
        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_update_number_report_setting_if_attribute_type_not_supported() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(DIRECT_INPUT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        AttributeNumberReport attributeNumberReport = AttributeNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(ATTRIBUTE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .numberAggregationType(AVG)
                .attributeId(attribute.getId())
                .build();

        List<NumberReport> reports = newArrayList(attributeNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), ATTRIBUTE_NOT_NUMBER_VALUED);
    }

    @Test
    public void should_update_chart_report() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberRefAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxRefAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberRefAttribute, checkboxRefAttribute);

        ControlBarReport controlBarReport = ControlBarReport.builder()
                .id(newShortUuid())
                .type(CONTROL_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(SUBMIT_COUNT_SUM)
                        .pageId(response.getHomePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of())
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ControlPieReport controlPieReport = ControlPieReport.builder()
                .id(newShortUuid())
                .type(CONTROL_PIE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.getHomePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .range(NO_LIMIT)
                        .build())
                .style(PieReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        ControlDoughnutReport controlDoughnutReport = ControlDoughnutReport.builder()
                .id(newShortUuid())
                .type(CONTROL_DOUGHNUT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.getHomePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .range(NO_LIMIT)
                        .build())
                .style(DoughnutReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        ControlNumberRangeSegmentReport controlNumberRangeSegmentReport = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.getHomePageId())
                        .basedControlId(numberInputControl.getId())
                        .targetControlId(numberInputControl.getId())
                        .numberRangesString("10,20,30,40")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        ControlTimeSegmentReport controlTimeSegmentReport = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(
                                ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                        .id(newShortUuid())
                                        .name("分时报告")
                                        .segmentType(CONTROL_VALUE_SUM)
                                        .basedType(CREATED_AT)
                                        .pageId(response.getHomePageId())
                                        .targetControlId(numberInputControl.getId())
                                        .build()
                        ))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        AttributeBarReport attributeBarReport = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(QR_COUNT_SUM)
                        .basedAttributeId(checkboxRefAttribute.getId())
                        .targetAttributeIds(List.of())
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();


        AttributePieReport attributePieReport = AttributePieReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_PIE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .basedAttributeId(checkboxRefAttribute.getId())
                        .targetAttributeIds(List.of(numberRefAttribute.getId()))
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .range(NO_LIMIT)
                        .build())
                .style(PieReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        AttributeDoughnutReport attributeDoughnutReport = AttributeDoughnutReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_DOUGHNUT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .basedAttributeId(checkboxRefAttribute.getId())
                        .targetAttributeIds(List.of(numberRefAttribute.getId()))
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .range(NO_LIMIT)
                        .build())
                .style(DoughnutReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();


        AttributeNumberRangeSegmentReport attributeNumberRangeSegmentReport = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .basedAttributeId(numberRefAttribute.getId())
                        .targetAttributeId(numberRefAttribute.getId())
                        .numberRangesString("10,20,30,40")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();


        AttributeTimeSegmentReport attributeTimeSegmentReport = AttributeTimeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_TIME_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(AttributeTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name(rReportName())
                                .segmentType(ATTRIBUTE_VALUE_MAX)
                                .basedType(QrReportTimeBasedType.CREATED_AT)
                                .targetAttributeId(numberRefAttribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(controlBarReport, controlPieReport, controlDoughnutReport, controlNumberRangeSegmentReport, controlTimeSegmentReport,
                attributeBarReport, attributeDoughnutReport, attributePieReport, attributeNumberRangeSegmentReport, attributeTimeSegmentReport
        );

        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        AppApi.updateAppReportSetting(response.getJwt(), response.getAppId(), command);
        App app = appRepository.byId(response.getAppId());
        assertEquals(command.getSetting(), app.getReportSetting());
    }

    @Test
    public void should_fail_create_categorized_control_chart_report_if_referenced_page_not_exists() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlPieReport report = ControlPieReport.builder()
                .id(newShortUuid())
                .type(CONTROL_PIE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_AVG)
                        .pageId(Page.newPageId())
                        .basedControlId(checkboxControl.getId())
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(PieReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_categorized_control_chart_report_if_referenced_based_control_not_exists() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlDoughnutReport report = ControlDoughnutReport.builder()
                .id(newShortUuid())
                .type(CONTROL_DOUGHNUT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_AVG)
                        .pageId(response.getHomePageId())
                        .basedControlId(Control.newControlId())
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(DoughnutReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_CONTROL_NOT_EXIST);
    }


    @Test
    public void should_fail_create_categorized_control_chart_report_if_referenced_based_control_not_support_categorized() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlBarReport controlBarReport = ControlBarReport.builder()
                .id(newShortUuid())
                .type(CONTROL_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_AVG)
                        .pageId(response.getHomePageId())
                        .basedControlId(numberInputControl.getId())
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(controlBarReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), CONTROL_NOT_CATEGORIZED);
    }

    @Test
    public void should_fail_create_categorized_control_chart_report_if_referenced_target_control_not_exits() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlBarReport controlBarReport = ControlBarReport.builder()
                .id(newShortUuid())
                .type(CONTROL_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.getHomePageId())
                        .basedControlId(checkboxControl.getId())
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .targetControlIds(List.of(Control.newControlId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(controlBarReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_categorized_control_chart_report_if_referenced_target_control_not_support_numbered() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlBarReport controlBarReport = ControlBarReport.builder()
                .id(newShortUuid())
                .type(CONTROL_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.getHomePageId())
                        .basedControlId(checkboxControl.getId())
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .targetControlIds(List.of(checkboxControl.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(controlBarReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), CONTROL_NOT_NUMBERED);
    }

    @Test
    public void should_fail_create_number_range_control_chart_report_if_referenced_page_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(Page.newPageId())
                        .basedControlId(numberInputControl.getId())
                        .targetControlId(numberInputControl.getId())
                        .numberRangesString("10,20,30")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_number_range_control_chart_report_if_referenced_based_control_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.getHomePageId())
                        .basedControlId(Control.newControlId())
                        .targetControlId(numberInputControl.getId())
                        .numberRangesString("10,20,30")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_number_range_control_chart_report_if_referenced_based_control_not_numbered() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.getHomePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlId(numberInputControl.getId())
                        .numberRangesString("10,20,30")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), CONTROL_NOT_NUMBERED);
    }

    @Test
    public void should_fail_create_number_range_control_chart_report_if_referenced_target_control_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.getHomePageId())
                        .basedControlId(numberInputControl.getId())
                        .targetControlId(Control.newControlId())
                        .numberRangesString("10,20,30")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_number_range_control_chart_report_if_referenced_target_control_not_numbered() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.getHomePageId())
                        .basedControlId(numberInputControl.getId())
                        .targetControlId(checkboxControl.getId())
                        .numberRangesString("10,20,30")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), CONTROL_NOT_NUMBERED);
    }

    @Test
    public void should_fail_create_time_segment_control_chart_report_if_referenced_page_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(
                                ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                        .id(UuidGenerator.newShortUuid())
                                        .name("分时报告")
                                        .segmentType(CONTROL_VALUE_SUM)
                                        .basedType(CREATED_AT)
                                        .pageId(Page.newPageId())
                                        .targetControlId(checkboxControl.getId())
                                        .build()
                        ))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_time_segment_control_chart_report_if_referenced_control_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时报告")
                                .segmentType(CONTROL_VALUE_SUM)
                                .basedType(CREATED_AT)
                                .pageId(response.getHomePageId())
                                .targetControlId(Control.newControlId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_time_segment_control_chart_report_if_referenced_control_not_numbered() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时报告")
                                .segmentType(CONTROL_VALUE_SUM)
                                .basedType(CREATED_AT)
                                .pageId(response.getHomePageId())
                                .targetControlId(checkboxControl.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), CONTROL_NOT_NUMBERED);
    }

    @Test
    public void should_fail_create_categorized_attribute_chart_report_if_referenced_based_attribute_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberAttribute, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .basedAttributeId(Attribute.newAttributeId())
                        .targetAttributeIds(List.of(numberAttribute.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_ATTRIBUTE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_categorized_attribute_chart_report_if_referenced_based_attribute_not_categorized() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberAttribute, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .basedAttributeId(numberAttribute.getId())
                        .targetAttributeIds(List.of(numberAttribute.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), ATTRIBUTE_NOT_CATEGORIZED);
    }

    @Test
    public void should_fail_create_categorized_attribute_chart_report_if_referenced_target_attribute_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberAttribute, checkboxAttribute);

        AttributeDoughnutReport report = AttributeDoughnutReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_DOUGHNUT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of(Attribute.newAttributeId()))
                        .range(NO_LIMIT)
                        .build())
                .style(DoughnutReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_ATTRIBUTE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_categorized_attribute_chart_report_if_referenced_target_attribute_not_numbered() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberAttribute, checkboxAttribute);

        AttributePieReport report = AttributePieReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_PIE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .addressPrecisionType(CITY)
                        .multiLevelSelectionPrecisionType(LEVEL2)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of(checkboxAttribute.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(PieReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), ATTRIBUTE_NOT_NUMBER_VALUED);
    }

    @Test
    public void should_fail_create_number_range_attribute_chart_report_if_referenced_based_attribute_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberAttribute, checkboxAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .basedAttributeId(Attribute.newAttributeId())
                        .targetAttributeId(numberAttribute.getId())
                        .numberRangesString("10,20,30")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_ATTRIBUTE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_number_range_attribute_chart_report_if_referenced_based_attribute_not_numbered() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberAttribute, checkboxAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeId(numberAttribute.getId())
                        .numberRangesString("10,20,30")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), ATTRIBUTE_NOT_NUMBER_VALUED);
    }

    @Test
    public void should_fail_create_number_range_attribute_chart_report_if_referenced_target_attribute_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberAttribute, checkboxAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .basedAttributeId(numberAttribute.getId())
                        .targetAttributeId(Attribute.newAttributeId())
                        .numberRangesString("10,20,30")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_ATTRIBUTE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_number_range_attribute_chart_report_if_referenced_target_attribute_not_numbered() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberAttribute, checkboxAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .basedAttributeId(numberAttribute.getId())
                        .targetAttributeId(checkboxAttribute.getId())
                        .numberRangesString("10,20,30")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), ATTRIBUTE_NOT_NUMBER_VALUED);
    }

    @Test
    public void should_fail_create_time_attribute_chart_report_if_referenced_target_attribute_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberAttribute, checkboxAttribute);

        AttributeTimeSegmentReport report = AttributeTimeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_TIME_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(AttributeTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name(rReportName())
                                .segmentType(ATTRIBUTE_VALUE_MAX)
                                .basedType(QrReportTimeBasedType.CREATED_AT)
                                .targetAttributeId(newAttributeId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), VALIDATION_ATTRIBUTE_NOT_EXIST);
    }


    @Test
    public void should_fail_create_time_attribute_chart_report_if_referenced_target_attribute_not_numbered() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, checkboxControl);

        Attribute numberAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), numberAttribute, checkboxAttribute);

        AttributeTimeSegmentReport report = AttributeTimeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_TIME_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(AttributeTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name(rReportName())
                                .segmentType(ATTRIBUTE_VALUE_MAX)
                                .basedType(QrReportTimeBasedType.CREATED_AT)
                                .targetAttributeId(checkboxAttribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList()).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        assertError(() -> AppApi.updateAppReportSettingRaw(response.getJwt(), response.getAppId(), command), ATTRIBUTE_NOT_NUMBER_VALUED);
    }
}
