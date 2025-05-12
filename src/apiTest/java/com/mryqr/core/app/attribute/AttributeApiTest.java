package com.mryqr.core.app.attribute;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.Geolocation;
import com.mryqr.common.domain.TextOption;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.common.domain.report.QrReportTimeBasedType;
import com.mryqr.common.domain.report.ReportRange;
import com.mryqr.common.utils.UuidGenerator;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.UpdateAppReportSettingCommand;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeInfo;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.event.AppAttributesCreatedEvent;
import com.mryqr.core.app.domain.event.AppAttributesDeletedEvent;
import com.mryqr.core.app.domain.event.DeletedAttributeInfo;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.report.ReportSetting;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.ChartReportConfiguration;
import com.mryqr.core.app.domain.report.chart.ChartReportSetting;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeTimeSegmentReport;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeTimeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeTimeSegmentReportSetting.TimeSegmentSetting;
import com.mryqr.core.app.domain.report.chart.style.TimeSegmentReportStyle;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.app.domain.report.number.NumberReportConfiguration;
import com.mryqr.core.app.domain.report.number.NumberReportSetting;
import com.mryqr.core.app.domain.report.number.attribute.AttributeNumberReport;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.command.UpdateQrBaseSettingCommand;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.*;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.domain.Address.joinAddress;
import static com.mryqr.common.domain.ValueType.*;
import static com.mryqr.common.domain.report.NumberAggregationType.AVG;
import static com.mryqr.common.domain.report.QrSegmentType.ATTRIBUTE_VALUE_MAX;
import static com.mryqr.common.domain.report.TimeSegmentInterval.PER_MONTH;
import static com.mryqr.common.event.DomainEventType.APP_ATTRIBUTES_CREATED;
import static com.mryqr.common.event.DomainEventType.APP_ATTRIBUTES_DELETED;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.*;
import static com.mryqr.core.app.domain.page.Page.newPageId;
import static com.mryqr.core.app.domain.page.control.Control.newControlId;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.ATTRIBUTE_TIME_SEGMENT_REPORT;
import static com.mryqr.core.app.domain.report.number.NumberReportType.ATTRIBUTE_NUMBER_REPORT;
import static com.mryqr.core.qr.domain.attribute.BooleanAttributeValue.FALSE;
import static com.mryqr.core.qr.domain.attribute.BooleanAttributeValue.TRUE;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.time.LocalDate.ofInstant;
import static java.time.LocalDate.parse;
import static java.time.ZoneId.systemDefault;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;

public class AttributeApiTest extends BaseApiTest {

    @Test
    public void should_update_app_setting_with_custom_attributes() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        Page page = setting.homePage();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        page.getControls().clear();
        page.getControls().add(control);

        Attribute fixValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(FIXED).fixedValue("whatever")
                .build();
        Attribute fillableValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(DIRECT_INPUT).build();
        Attribute instanceRefValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(INSTANCE_CREATE_TIME)
                .pcListEligible(false).build();
        Attribute pageRefValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(PAGE_LAST_SUBMITTED_TIME)
                .pageId(page.getId()).build();
        Attribute controlRefValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(CONTROL_LAST)
                .pageId(page.getId()).controlId(control.getId()).build();
        List<Attribute> attributes = newArrayList(fixValue, fillableValue, instanceRefValue, pageRefValue, controlRefValue);
        setting.getAttributes().clear();
        setting.getAttributes().addAll(attributes);
        AppApi.updateAppSetting(response.getJwt(), appId, setting);

        App updatedApp = appRepository.byId(appId);
        List<Attribute> updatedAttributes = updatedApp.getSetting().getAttributes();
        assertEquals(attributes.stream().map(Attribute::getId).collect(toSet()),
                updatedAttributes.stream().map(Attribute::getId).collect(toSet()));
        assertEquals(TEXT_VALUE, updatedAttributes.get(0).getValueType());
        assertEquals(TEXT_VALUE, updatedAttributes.get(1).getValueType());
        assertEquals(TIMESTAMP_VALUE, updatedAttributes.get(2).getValueType());
        assertEquals(TIMESTAMP_VALUE, updatedAttributes.get(3).getValueType());
        assertEquals(control.getType().getAnswerValueType(), updatedAttributes.get(4).getValueType());
    }

    @Test
    public void should_calculate_suffix_and_precision_for_number_input_control() {
        PreparedAppResponse response = setupApi.registerWithApp();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().suffix("m").precision(2).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(CONTROL_LAST)
                .pageId(response.getHomePageId()).controlId(numberInputControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App updatedApp = appRepository.byId(response.getAppId());
        Attribute updatedAttribute = updatedApp.getSetting().getAttributes().get(0);
        assertEquals("m", updatedAttribute.getSuffix());
        assertEquals(2, updatedAttribute.getPrecision());
    }

    @Test
    public void should_fail_update_app_setting_if_attribute_ref_page_is_null() {
        PreparedAppResponse response = setupApi.registerWithApp();
        App app = appRepository.byId(response.getAppId());

        Attribute invalidPageRefValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT)
                .type(PAGE_LAST_SUBMITTED_TIME).pageId(null).build();
        AppSetting setting = app.getSetting();
        setting.getAttributes().add(invalidPageRefValue);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                EMPTY_ATTRIBUTE_REF_PAGE_ID);
    }

    @Test
    public void should_fail_update_app_setting_if_ref_page_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        App app = appRepository.byId(response.getAppId());

        Attribute invalidPageRefValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT)
                .type(PAGE_LAST_SUBMITTED_TIME).pageId(newPageId()).build();
        AppSetting setting = app.getSetting();
        setting.getAttributes().add(invalidPageRefValue);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_ATTRIBUTE_REF_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_update_app_setting_if_ref_control_is_null() {
        PreparedAppResponse response = setupApi.registerWithApp();
        App app = appRepository.byId(response.getAppId());

        Attribute invalidControlRefValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(CONTROL_LAST)
                .pageId(response.getHomePageId()).controlId(null).build();
        AppSetting setting = app.getSetting();
        setting.getAttributes().add(invalidControlRefValue);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                EMPTY_ATTRIBUTE_REF_CONTROL_ID);
    }

    @Test
    public void should_fail_update_app_setting_if_ref_control_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        App app = appRepository.byId(response.getAppId());

        Attribute invalidControlRefValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(CONTROL_LAST)
                .pageId(response.getHomePageId()).controlId(newControlId()).build();
        AppSetting setting = app.getSetting();
        setting.getAttributes().add(invalidControlRefValue);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_ATTRIBUTE_REF_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_update_app_setting_if_attribute_reference_wrong_control_type_for_numbered_control() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        Page page = setting.homePage();
        Control control = defaultSingleLineTextControl();
        page.getControls().add(control);

        Attribute controlRefAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(CONTROL_AVERAGE)
                .pageId(page.getId()).controlId(control.getId()).build();
        setting.getAttributes().add(controlRefAttribute);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), WRONG_ATTRIBUTE_REF_CONTROL_TYPE);
    }

    @Test
    public void should_fail_update_app_setting_if_attribute_reference_wrong_control_type_for_non_numbered_control() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        Page page = setting.homePage();
        Control control = defaultSectionTitleControl();
        page.getControls().add(control);

        Attribute controlRefAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(CONTROL_LAST)
                .pageId(page.getId()).controlId(control.getId()).build();
        setting.getAttributes().add(controlRefAttribute);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), WRONG_ATTRIBUTE_REF_CONTROL_TYPE);
    }

    @Test
    public void should_fail_update_app_setting_if_fixed_attribute_has_no_value() {
        PreparedAppResponse response = setupApi.registerWithApp();
        App app = appRepository.byId(response.getAppId());

        Attribute invalidControlRefValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(FIXED).fixedValue(null).build();
        AppSetting setting = app.getSetting();
        setting.getAttributes().add(invalidControlRefValue);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                EMPTY_ATTRIBUTE_FIXED_VALUE);
    }

    @Test
    public void should_fail_update_app_setting_if_range_not_provided_but_required() {
        PreparedAppResponse response = setupApi.registerWithApp();
        App app = appRepository.byId(response.getAppId());

        Attribute invalidControlRefValue = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_SUBMIT_COUNT)
                .range(null).build();
        AppSetting setting = app.getSetting();
        setting.getAttributes().add(invalidControlRefValue);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                ATTRIBUTE_RANGE_SHOULD_NOT_NULL);
    }

    @Test
    public void create_attribute_should_raise_event() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.getAppId();
        CreateQrResponse qrResponse1 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CREATE_TIME).build();
        AppApi.updateAppAttributes(response.getJwt(), appId, attribute);

        AppAttributesCreatedEvent attributesCreatedEvent = latestEventFor(appId, APP_ATTRIBUTES_CREATED, AppAttributesCreatedEvent.class);
        assertEquals(1, attributesCreatedEvent.getAttributes().size());
        assertTrue(
                attributesCreatedEvent.getAttributes().stream().map(AttributeInfo::getAttributeId).collect(toSet()).contains(attribute.getId()));
        App app = appRepository.byId(appId);
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR updatedQr1 = qrRepository.byId(qrResponse1.getQrId());
        TimestampAttributeValue attributeValue = (TimestampAttributeValue) updatedQr1.getAttributeValues().get(attributeId);
        assertEquals(attributeValue.getTimestamp(), updatedQr1.getCreatedAt());
        IndexedValue indexedValue = updatedQr1.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertEquals(updatedQr1.getCreatedAt().toEpochMilli(), indexedValue.getSv());
        QR updatedQr2 = qrRepository.byId(qrResponse1.getQrId());
        TimestampAttributeValue attributeValue2 = (TimestampAttributeValue) updatedQr2.getAttributeValues().get(attributeId);
        assertEquals(attributeValue2.getTimestamp(), updatedQr2.getCreatedAt());
        IndexedValue indexedValue2 = updatedQr2.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue2.getRid());
        assertEquals(updatedQr2.getCreatedAt().toEpochMilli(), indexedValue2.getSv());
    }

    @Test
    public void delete_attribute_should_raise_event() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.getAppId();
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CREATE_TIME).build();
        AppApi.updateAppAttributes(response.getJwt(), appId, attribute);
        App app = appRepository.byId(appId);
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(qrResponse.getQrId());
        TimestampAttributeValue attributeValue = (TimestampAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeValue.getTimestamp(), qr.getCreatedAt());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertEquals(qr.getCreatedAt().toEpochMilli(), indexedValue.getSv());

        AppApi.updateAppAttributes(response.getJwt(), appId);

        AppAttributesDeletedEvent attributesDeletedEvent = latestEventFor(appId, APP_ATTRIBUTES_DELETED, AppAttributesDeletedEvent.class);
        assertEquals(1, attributesDeletedEvent.getAttributes().size());
        assertTrue(attributesDeletedEvent.getAttributes().stream().map(DeletedAttributeInfo::getAttributeId).collect(toSet())
                .contains(attribute.getId()));
        QR updatedQr = qrRepository.byId(qrResponse.getQrId());
        assertNull(updatedQr.getAttributeValues().get(attributeId));
        assertNull(updatedQr.getIndexedValues().valueOf(indexedField));
    }

    @Test
    public void delete_attribute_with_override_index_field_should_raise_event() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.getAppId();
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CREATE_TIME).build();
        AppApi.updateAppAttributes(response.getJwt(), appId, attribute);
        App app = appRepository.byId(appId);
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(qrResponse.getQrId());
        TimestampAttributeValue attributeValue = (TimestampAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeValue.getTimestamp(), qr.getCreatedAt());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertEquals(qr.getCreatedAt().toEpochMilli(), indexedValue.getSv());

        List<Attribute> newAttributes = IntStream.range(0, 20)
                .mapToObj(value -> Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CREATE_TIME).build())
                .collect(Collectors.toList());
        AppApi.updateAppAttributes(response.getJwt(), appId, newAttributes);

        AppAttributesDeletedEvent attributesDeletedEvent = latestEventFor(appId, APP_ATTRIBUTES_DELETED, AppAttributesDeletedEvent.class);
        assertEquals(1, attributesDeletedEvent.getAttributes().size());
        assertTrue(attributesDeletedEvent.getAttributes().stream().map(DeletedAttributeInfo::getAttributeId).collect(toSet())
                .contains(attribute.getId()));
        QR updatedQr = qrRepository.byId(qrResponse.getQrId());
        assertNull(updatedQr.getAttributeValues().get(attributeId));
        assertNotEquals(attributeId, updatedQr.getIndexedValues().valueOf(indexedField).getRid());
        assertNotNull(updatedQr.getIndexedValues().valueOf(indexedField).getSv());
    }

    @Test
    public void delete_attribute_should_also_delete_attribute_aware_number_reports() {
        PreparedAppResponse response = setupApi.registerWithApp();

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT)
                .type(INSTANCE_SUBMIT_COUNT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        AttributeNumberReport attributeNumberReport = AttributeNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(ATTRIBUTE_NUMBER_REPORT)
                .range(ReportRange.NO_LIMIT)
                .numberAggregationType(AVG)
                .attributeId(attribute.getId())
                .build();

        List<NumberReport> reports = newArrayList(attributeNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(
                                ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build())
                                        .build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports)
                                .configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();
        AppApi.updateAppReportSetting(response.getJwt(), response.getAppId(), command);
        assertEquals(1, appRepository.byId(response.getAppId())
                .getReportSetting().getNumberReportSetting().getReports().size());

        AppApi.updateAppAttributes(response.getJwt(), response.getAppId());
        assertEquals(0, appRepository.byId(response.getAppId())
                .getReportSetting().getNumberReportSetting().getReports().size());
    }

    @Test
    public void delete_attribute_should_also_delete_attribute_aware_chart_reports() {
        PreparedAppResponse response = setupApi.registerWithApp();

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT)
                .type(INSTANCE_SUBMIT_COUNT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        AttributeTimeSegmentReport report = AttributeTimeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_TIME_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name(rReportName())
                                .segmentType(ATTRIBUTE_VALUE_MAX)
                                .basedType(QrReportTimeBasedType.CREATED_AT)
                                .targetAttributeId(attribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        List<ChartReport> reports = newArrayList(report);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(
                                ChartReportSetting.builder().reports(reports).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(newArrayList())
                                .configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();
        AppApi.updateAppReportSetting(response.getJwt(), response.getAppId(), command);
        List<ChartReport> updatedReports = appRepository.byId(response.getAppId())
                .getReportSetting().getChartReportSetting().getReports();
        assertEquals(1, updatedReports.size());
        assertEquals(report.getId(), updatedReports.get(0).getId());

        AppApi.updateAppAttributes(response.getJwt(), response.getAppId());
        assertEquals(0, appRepository.byId(response.getAppId())
                .getReportSetting().getChartReportSetting().getReports().size());
    }

    @Test
    public void should_fail_update_app_setting_if_attribute_id_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();

        String attributeId = newAttributeId();
        Attribute pageRefAttribute1 = Attribute.builder().id(attributeId).name(rAttributeName()).range(NO_LIMIT).type(PAGE_LAST_SUBMITTER)
                .pageId(setting.homePageId()).build();
        Attribute pageRefAttribute2 = Attribute.builder().id(attributeId).name(rAttributeName()).range(NO_LIMIT).type(PAGE_LAST_SUBMITTER)
                .pageId(setting.homePageId()).build();
        List<Attribute> attributes = newArrayList(pageRefAttribute1, pageRefAttribute2);
        setting.getAttributes().addAll(attributes);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), ATTRIBUTE_ID_DUPLICATED);
    }

    @Test
    public void should_fail_update_app_setting_if_attribute_name_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();

        String attributeName = rAttributeName();
        Attribute pageRefAttribute1 = Attribute.builder().id(newAttributeId()).name(attributeName).range(NO_LIMIT).type(PAGE_LAST_SUBMITTER)
                .pageId(setting.homePageId()).build();
        Attribute pageRefAttribute2 = Attribute.builder().id(newAttributeId()).name(attributeName).range(NO_LIMIT).type(PAGE_LAST_SUBMITTER)
                .pageId(setting.homePageId()).build();
        List<Attribute> attributes = newArrayList(pageRefAttribute1, pageRefAttribute2);
        setting.getAttributes().addAll(attributes);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), ATTRIBUTE_NAME_DUPLICATED);
    }

    @Test
    public void should_fail_update_app_setting_if_attribute_schema_changed() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        String attributeId = newAttributeId();
        Attribute oldAttribute = Attribute.builder().id(attributeId).name(rAttributeName()).range(NO_LIMIT).type(FIXED).fixedValue("whatever")
                .build();
        AppApi.updateAppAttributes(response.getJwt(), appId, oldAttribute);

        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        Attribute updatedAttribute = Attribute.builder().id(attributeId).name(rAttributeName()).range(NO_LIMIT).type(DIRECT_INPUT).build();
        setting.getAttributes().clear();
        setting.getAttributes().add(updatedAttribute);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, setting), ATTRIBUTE_SCHEMA_CANNOT_MODIFIED);
    }

    @Test
    public void should_sync_to_empty_for_non_filled_attribute_values() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CUSTOM_ID).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        QR qr = qrRepository.byId(response.getQrId());
        assertTrue(qr.getAttributeValues().isEmpty());
        assertNull(qr.getIndexedValues());
    }

    @Test
    public void should_sync_instance_name_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_NAME).build();

        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        QR qr = qrRepository.byId(response.getQrId());
        TextAttributeValue attributeValue = (TextAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_NAME, attributeValue.getAttributeType());
        assertEquals(TEXT_VALUE, attributeValue.getValueType());
        assertEquals(qr.getName(), attributeValue.getText());
        assertNull(qr.getIndexedValues());
    }

    @Test
    public void should_sync_instance_active_status_attribute_value() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_ACTIVE_STATUS).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        QrApi.deactivate(response.getJwt(), qrResponse.getQrId());

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertNull(qr.getSearchableValues());
        BooleanAttributeValue attributeValue = (BooleanAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_ACTIVE_STATUS, attributeValue.getAttributeType());
        assertEquals(BOOLEAN_VALUE, attributeValue.getValueType());
        assertFalse(attributeValue.isYes());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(FALSE));
        assertNull(indexedValue.getSv());

        QrApi.activate(response.getJwt(), qrResponse.getQrId());
        QR updatedQr = qrRepository.byId(qrResponse.getQrId());
        BooleanAttributeValue updatedAttributeValue = (BooleanAttributeValue) updatedQr.getAttributeValues().get(attributeId);
        assertTrue(updatedAttributeValue.isYes());
        IndexedValue updatedIndexedValue = updatedQr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, updatedIndexedValue.getRid());
        assertTrue(updatedIndexedValue.getTv().contains(TRUE));
        assertNull(updatedIndexedValue.getSv());
    }

    @Test
    public void should_sync_instance_template_status_attribute_value() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_TEMPLATE_STATUS).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        QrApi.markTemplate(response.getJwt(), qrResponse.getQrId());

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertNull(qr.getSearchableValues());
        BooleanAttributeValue attributeValue = (BooleanAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_TEMPLATE_STATUS, attributeValue.getAttributeType());
        assertEquals(BOOLEAN_VALUE, attributeValue.getValueType());
        assertTrue(attributeValue.isYes());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(TRUE));
        assertNull(indexedValue.getSv());

        QrApi.unmarkTemplate(response.getJwt(), qrResponse.getQrId());
        QR updatedQr = qrRepository.byId(qrResponse.getQrId());
        BooleanAttributeValue updatedAttributeValue = (BooleanAttributeValue) updatedQr.getAttributeValues().get(attributeId);
        assertFalse(updatedAttributeValue.isYes());
        IndexedValue updatedIndexedValue = updatedQr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, updatedIndexedValue.getRid());
        assertTrue(updatedIndexedValue.getTv().contains(FALSE));
        assertNull(updatedIndexedValue.getSv());
    }

    @Test
    public void should_sync_instance_plate_id_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_PLATE_ID).build();

        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        IdentifierAttributeValue attributeValue = (IdentifierAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_PLATE_ID, attributeValue.getAttributeType());
        assertEquals(IDENTIFIER_VALUE, attributeValue.getValueType());
        assertEquals(qr.getPlateId(), attributeValue.getContent());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(qr.getPlateId()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_instance_custom_id_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String customId = rCustomId();
        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(),
                UpdateQrBaseSettingCommand.builder().name(rQrName()).customId(customId).build());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CUSTOM_ID).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        IdentifierAttributeValue attributeValue = (IdentifierAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_CUSTOM_ID, attributeValue.getAttributeType());
        assertEquals(IDENTIFIER_VALUE, attributeValue.getValueType());
        assertEquals(customId, attributeValue.getContent());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(customId));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_instance_circulation_attribute_value() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(option1.getId())
                .statusAfterSubmissions(List.of())
                .statusPermissions(List.of())
                .build();
        AppApi.updateCirculationStatusSetting(response.getJwt(), response.getAppId(), setting);

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CIRCULATION_STATUS).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(qrResponse.getQrId());
        CirculationStatusAttributeValue attributeValue = (CirculationStatusAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_CIRCULATION_STATUS, attributeValue.getAttributeType());
        assertEquals(CIRCULATION_STATUS_VALUE, attributeValue.getValueType());
        assertEquals(option1.getId(), attributeValue.getOptionId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(option1.getId()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_instance_geolocation_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.enableAppPosition(response.getJwt(), response.getAppId());
        Geolocation geolocation = rGeolocation();
        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(),
                UpdateQrBaseSettingCommand.builder().name(rQrName()).geolocation(geolocation).build());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_GEOLOCATION).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        GeolocationAttributeValue attributeValue = (GeolocationAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_GEOLOCATION, attributeValue.getAttributeType());
        assertEquals(GEOLOCATION_VALUE, attributeValue.getValueType());
        assertEquals(geolocation, attributeValue.getGeolocation());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(geolocation.getAddress().getProvince()));
        assertTrue(indexedValue.getTv().contains(joinAddress(geolocation.getAddress().getProvince(), geolocation.getAddress().getCity())));
        assertTrue(indexedValue.getTv().contains(
                joinAddress(geolocation.getAddress().getProvince(), geolocation.getAddress().getCity(), geolocation.getAddress().getDistrict())));
    }

    @Test
    public void should_sync_instance_created_time_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CREATE_TIME).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        TimestampAttributeValue attributeValue = (TimestampAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_CREATE_TIME, attributeValue.getAttributeType());
        assertEquals(TIMESTAMP_VALUE, attributeValue.getValueType());
        assertEquals(qr.getCreatedAt(), attributeValue.getTimestamp());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertEquals(qr.getCreatedAt().toEpochMilli(), indexedValue.getSv());
        assertNull(indexedValue.getTv());
    }

    @Test
    public void should_sync_instance_create_date_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CREATE_DATE).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        LocalDateAttributeValue attributeValue = (LocalDateAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_CREATE_DATE, attributeValue.getAttributeType());
        assertEquals(LOCAL_DATE_VALUE, attributeValue.getValueType());
        String expectedDate = ofInstant(qr.getCreatedAt(), systemDefault()).toString();
        assertEquals(expectedDate, attributeValue.getDate());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertEquals((double) parse(expectedDate).atStartOfDay(systemDefault()).toInstant().toEpochMilli(), indexedValue.getSv());
        assertNull(indexedValue.getTv());
    }

    @Test
    public void should_sync_instance_creator_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CREATOR).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        MemberAttributeValue attributeValue = (MemberAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_CREATOR, attributeValue.getAttributeType());
        assertEquals(MEMBER_VALUE, attributeValue.getValueType());
        assertEquals(qr.getCreatedBy(), attributeValue.getMemberId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(qr.getCreatedBy()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_instance_creator_and_mobile_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CREATOR_AND_MOBILE).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        MemberMobileAttributeValue attributeValue = (MemberMobileAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_CREATOR_AND_MOBILE, attributeValue.getAttributeType());
        assertEquals(MEMBER_MOBILE_VALUE, attributeValue.getValueType());
        assertEquals(qr.getCreatedBy(), attributeValue.getMemberId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(qr.getCreatedBy()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_instance_creator_and_email_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CREATOR_AND_EMAIL).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        MemberEmailAttributeValue attributeValue = (MemberEmailAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_CREATOR_AND_EMAIL, attributeValue.getAttributeType());
        assertEquals(MEMBER_EMAIL_VALUE, attributeValue.getValueType());
        assertEquals(qr.getCreatedBy(), attributeValue.getMemberId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(qr.getCreatedBy()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_instance_submit_count_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).range(NO_LIMIT).type(INSTANCE_SUBMIT_COUNT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        IntegerAttributeValue attributeValue = (IntegerAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_SUBMIT_COUNT, attributeValue.getAttributeType());
        assertEquals(INTEGER_VALUE, attributeValue.getValueType());
        assertEquals(1, attributeValue.getNumber());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertEquals(1, indexedValue.getSv());
        assertNull(indexedValue.getTv());
    }

    @Test
    public void should_sync_instance_access_count_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        QrApi.fetchSubmissionQr(response.getJwt(), response.getPlateId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_ACCESS_COUNT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        IntegerAttributeValue attributeValue = (IntegerAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_ACCESS_COUNT, attributeValue.getAttributeType());
        assertEquals(INTEGER_VALUE, attributeValue.getValueType());
        assertEquals(1, attributeValue.getNumber());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertEquals(1, indexedValue.getSv());
        assertNull(indexedValue.getTv());
    }

    @Test
    public void should_sync_instance_group_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_GROUP).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        GroupAttributeValue attributeValue = (GroupAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_GROUP, attributeValue.getAttributeType());
        assertEquals(GROUP_VALUE, attributeValue.getValueType());
        assertEquals(response.getDefaultGroupId(), attributeValue.getGroupId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(response.getDefaultGroupId()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_instance_group_managers_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String memberId = MemberApi.createMember(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberId);

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_GROUP_MANAGERS).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        MembersAttributeValue attributeValue = (MembersAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_GROUP_MANAGERS, attributeValue.getAttributeType());
        assertEquals(MEMBERS_VALUE, attributeValue.getValueType());
        assertTrue(attributeValue.getMemberIds().contains(memberId));
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(memberId));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_instance_group_managers_and_mobile_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String memberId = MemberApi.createMember(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberId);

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_GROUP_MANAGERS_AND_MOBILE).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        MembersMobileAttributeValue attributeValue = (MembersMobileAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_GROUP_MANAGERS_AND_MOBILE, attributeValue.getAttributeType());
        assertEquals(MEMBERS_MOBILE_VALUE, attributeValue.getValueType());
        assertTrue(attributeValue.getMemberIds().contains(memberId));
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(memberId));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_instance_group_managers_and_email_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String memberId = MemberApi.createMember(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberId);

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_GROUP_MANAGERS_AND_EMAIL).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        MembersEmailAttributeValue attributeValue = (MembersEmailAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_GROUP_MANAGERS_AND_EMAIL, attributeValue.getAttributeType());
        assertEquals(MEMBERS_EMAIL_VALUE, attributeValue.getValueType());
        assertTrue(attributeValue.getMemberIds().contains(memberId));
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(memberId));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_page_submit_account_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_SUBMIT_COUNT).range(NO_LIMIT)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        IntegerAttributeValue attributeValue = (IntegerAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_SUBMIT_COUNT, attributeValue.getAttributeType());
        assertEquals(INTEGER_VALUE, attributeValue.getValueType());
        assertEquals(1, attributeValue.getNumber());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertNull(indexedValue.getTv());
        assertEquals(1, indexedValue.getSv());
    }

    @Test
    public void should_sync_page_first_submitted_time_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_FIRST_SUBMITTED_TIME).range(NO_LIMIT)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        TimestampAttributeValue attributeValue = (TimestampAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_FIRST_SUBMITTED_TIME, attributeValue.getAttributeType());
        assertEquals(TIMESTAMP_VALUE, attributeValue.getValueType());
        assertEquals(submission.getCreatedAt(), attributeValue.getTimestamp());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertNull(indexedValue.getTv());
        assertEquals(submission.getCreatedAt().toEpochMilli(), indexedValue.getSv());
    }

    @Test
    public void should_sync_page_first_submitted_date_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_FIRST_SUBMITTED_DATE).range(NO_LIMIT)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        LocalDateAttributeValue attributeValue = (LocalDateAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_FIRST_SUBMITTED_DATE, attributeValue.getAttributeType());
        assertEquals(LOCAL_DATE_VALUE, attributeValue.getValueType());
        String expectedDate = ofInstant(submission.getCreatedAt(), systemDefault()).toString();
        assertEquals(expectedDate, attributeValue.getDate());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertEquals((double) parse(expectedDate).atStartOfDay(systemDefault()).toInstant().toEpochMilli(), indexedValue.getSv());
        assertNull(indexedValue.getTv());
    }

    @Test
    public void should_sync_page_first_submitter_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_FIRST_SUBMITTER)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        MemberAttributeValue attributeValue = (MemberAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_FIRST_SUBMITTER, attributeValue.getAttributeType());
        assertEquals(MEMBER_VALUE, attributeValue.getValueType());
        assertEquals(submission.getCreatedBy(), attributeValue.getMemberId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(submission.getCreatedBy()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_page_first_submitter_and_mobile_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_FIRST_SUBMITTER_AND_MOBILE)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        MemberMobileAttributeValue attributeValue = (MemberMobileAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_FIRST_SUBMITTER_AND_MOBILE, attributeValue.getAttributeType());
        assertEquals(MEMBER_MOBILE_VALUE, attributeValue.getValueType());
        assertEquals(submission.getCreatedBy(), attributeValue.getMemberId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(submission.getCreatedBy()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_page_first_submitter_and_email_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_FIRST_SUBMITTER_AND_EMAIL)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        MemberEmailAttributeValue attributeValue = (MemberEmailAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_FIRST_SUBMITTER_AND_EMAIL, attributeValue.getAttributeType());
        assertEquals(MEMBER_EMAIL_VALUE, attributeValue.getValueType());
        assertEquals(submission.getCreatedBy(), attributeValue.getMemberId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(submission.getCreatedBy()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_page_last_submitted_time_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_LAST_SUBMITTED_TIME).range(NO_LIMIT)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        TimestampAttributeValue attributeValue = (TimestampAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_LAST_SUBMITTED_TIME, attributeValue.getAttributeType());
        assertEquals(TIMESTAMP_VALUE, attributeValue.getValueType());
        assertEquals(submission.getCreatedAt(), attributeValue.getTimestamp());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertNull(indexedValue.getTv());
        assertEquals(submission.getCreatedAt().toEpochMilli(), indexedValue.getSv());
    }

    @Test
    public void should_sync_page_last_submitted_date_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_LAST_SUBMITTED_DATE).range(NO_LIMIT)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        LocalDateAttributeValue attributeValue = (LocalDateAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_LAST_SUBMITTED_DATE, attributeValue.getAttributeType());
        assertEquals(LOCAL_DATE_VALUE, attributeValue.getValueType());
        String expectedDate = ofInstant(submission.getCreatedAt(), systemDefault()).toString();
        assertEquals(expectedDate, attributeValue.getDate());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertEquals((double) parse(expectedDate).atStartOfDay(systemDefault()).toInstant().toEpochMilli(), indexedValue.getSv());
        assertNull(indexedValue.getTv());
    }

    @Test
    public void should_sync_page_last_updated_time_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        SubmissionApi.updateSubmission(response.getJwt(), submissionId);

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_LAST_SUBMISSION_UPDATED_TIME).range(NO_LIMIT)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        TimestampAttributeValue attributeValue = (TimestampAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_LAST_SUBMISSION_UPDATED_TIME, attributeValue.getAttributeType());
        assertEquals(TIMESTAMP_VALUE, attributeValue.getValueType());
        assertEquals(submission.getUpdatedAt(), attributeValue.getTimestamp());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertNull(indexedValue.getTv());
        assertEquals(submission.getUpdatedAt().toEpochMilli(), indexedValue.getSv());
    }

    @Test
    public void should_sync_page_last_update_date_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        SubmissionApi.updateSubmission(response.getJwt(), submissionId);

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_LAST_SUBMISSION_UPDATE_DATE).range(NO_LIMIT)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        LocalDateAttributeValue attributeValue = (LocalDateAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_LAST_SUBMISSION_UPDATE_DATE, attributeValue.getAttributeType());
        assertEquals(LOCAL_DATE_VALUE, attributeValue.getValueType());
        String expectedDate = ofInstant(submission.getUpdatedAt(), systemDefault()).toString();
        assertEquals(expectedDate, attributeValue.getDate());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertEquals((double) parse(expectedDate).atStartOfDay(systemDefault()).toInstant().toEpochMilli(), indexedValue.getSv());
        assertNull(indexedValue.getTv());
    }

    @Test
    public void should_sync_page_last_submitter_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_LAST_SUBMITTER)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        MemberAttributeValue attributeValue = (MemberAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_LAST_SUBMITTER, attributeValue.getAttributeType());
        assertEquals(MEMBER_VALUE, attributeValue.getValueType());
        assertEquals(submission.getCreatedBy(), attributeValue.getMemberId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(submission.getCreatedBy()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_page_last_submission_updater_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        SubmissionApi.updateSubmission(response.getJwt(), submissionId);

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_LAST_SUBMISSION_UPDATER).range(NO_LIMIT)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        QR qr = qrRepository.byId(response.getQrId());
        MemberAttributeValue attributeValue = (MemberAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_LAST_SUBMISSION_UPDATER, attributeValue.getAttributeType());
        assertEquals(MEMBER_VALUE, attributeValue.getValueType());
        assertEquals(response.getMemberId(), attributeValue.getMemberId());
    }

    @Test
    public void should_sync_page_last_submitter_and_mobile_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_LAST_SUBMITTER_AND_MOBILE)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        MemberMobileAttributeValue attributeValue = (MemberMobileAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_LAST_SUBMITTER_AND_MOBILE, attributeValue.getAttributeType());
        assertEquals(MEMBER_MOBILE_VALUE, attributeValue.getValueType());
        assertEquals(submission.getCreatedBy(), attributeValue.getMemberId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(submission.getCreatedBy()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_page_last_submitter_and_email_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_LAST_SUBMITTER_AND_EMAIL)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attributeId).get();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);
        MemberEmailAttributeValue attributeValue = (MemberEmailAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(PAGE_LAST_SUBMITTER_AND_EMAIL, attributeValue.getAttributeType());
        assertEquals(MEMBER_EMAIL_VALUE, attributeValue.getValueType());
        assertEquals(submission.getCreatedBy(), attributeValue.getMemberId());
        IndexedValue indexedValue = qr.getIndexedValues().valueOf(indexedField);
        assertEquals(attributeId, indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(submission.getCreatedBy()));
        assertNull(indexedValue.getSv());
    }

    @Test
    public void should_sync_page_submission_exists() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(PAGE_SUBMISSION_EXISTS)
                .pageId(response.getHomePageId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        assertFalse(((BooleanAttributeValue) qrRepository.byId(response.getQrId()).getAttributeValues().get(attributeId)).isYes());

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        assertTrue(((BooleanAttributeValue) qrRepository.byId(response.getQrId()).getAttributeValues().get(attributeId)).isYes());
    }

    @Test
    public void last_empty_answer_should_override_existing_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl control = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        CheckboxAnswer answer = rAnswer(control);
        List<String> optionIds = answer.getOptionIds();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        CheckboxAttributeValue attributeValue = (CheckboxAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(control.getId(), attributeValue.getControlId());
        assertEquals(optionIds, attributeValue.getOptionIds());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.containsAll(optionIds));

        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        QR updatedQr = qrRepository.byId(response.getQrId());
        assertNull(updatedQr.getAttributeValues().get(attribute.getId()));
    }
}
