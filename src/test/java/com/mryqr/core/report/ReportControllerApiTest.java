package com.mryqr.core.report;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.stat.CategorizedOptionSegment;
import com.mryqr.common.domain.stat.NumberRangeSegment;
import com.mryqr.common.domain.stat.QrTimeBasedType;
import com.mryqr.common.utils.UuidGenerator;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import com.mryqr.core.app.domain.attribute.AttributeType;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FDateControl;
import com.mryqr.core.app.domain.page.control.FDateTimeControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.report.chart.attribute.*;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeCategorizedReportSetting;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeNumberRangeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeTimeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.control.*;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlCategorizedReportSetting;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlNumberRangeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlTimeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.style.*;
import com.mryqr.core.app.domain.report.number.attribute.AttributeNumberReport;
import com.mryqr.core.app.domain.report.number.control.ControlNumberReport;
import com.mryqr.core.app.domain.report.number.instance.InstanceNumberReport;
import com.mryqr.core.app.domain.report.number.page.PageNumberReport;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.report.query.chart.ChartReportQuery;
import com.mryqr.core.report.query.chart.QCategorizedOptionSegmentReport;
import com.mryqr.core.report.query.chart.QNumberRangeSegmentReport;
import com.mryqr.core.report.query.chart.QTimeSegmentReport;
import com.mryqr.core.report.query.number.NumberReportQuery;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static com.mryqr.common.domain.stat.NumberAggregationType.*;
import static com.mryqr.common.domain.stat.QrSegmentType.*;
import static com.mryqr.common.domain.stat.StatRange.*;
import static com.mryqr.common.domain.stat.SubmissionSegmentType.*;
import static com.mryqr.common.domain.stat.SubmissionTimeBasedType.CREATED_AT;
import static com.mryqr.common.domain.stat.SubmissionTimeBasedType.DATE_CONTROL;
import static com.mryqr.common.domain.stat.TimeSegmentInterval.*;
import static com.mryqr.common.exception.ErrorCode.ACCESS_DENIED;
import static com.mryqr.common.exception.ErrorCode.REPORTING_NOT_ALLOWED;
import static com.mryqr.common.utils.CommonUtils.*;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.report.chart.ChartReportType.*;
import static com.mryqr.core.app.domain.report.number.NumberReportType.*;
import static com.mryqr.core.app.domain.report.number.instance.InstanceNumberReportType.*;
import static com.mryqr.core.app.domain.report.number.page.PageNumberReportType.PAGE_SUBMIT_COUNT;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReportControllerApiTest extends BaseApiTest {

    @Test
    public void should_fetch_instance_count_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        InstanceNumberReport report = InstanceNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(INSTANCE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .instanceNumberReportType(INSTANCE_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());

        QrApi.createQr(response.jwt(), response.defaultGroupId());
        assertEquals(2, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fail_fetch_number_report_if_plan_not_enough() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant theTenant = tenantRepository.byId(response.tenantId());
        setupApi.updateTenantPlan(theTenant, theTenant.currentPlan().withReportingAllowed(false));

        InstanceNumberReport report = InstanceNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(INSTANCE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .instanceNumberReportType(INSTANCE_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertError(() -> ReportApi.fetchNumberReportRaw(response.jwt(), query), REPORTING_NOT_ALLOWED);
    }

    @Test
    public void should_fetch_number_report_for_specific_group() {
        PreparedQrResponse response = setupApi.registerWithQr();

        InstanceNumberReport report = InstanceNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(INSTANCE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .instanceNumberReportType(INSTANCE_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .groupId(response.defaultGroupId())
                .report(report)
                .build();

        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());

        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        QrApi.createQr(response.jwt(), groupId);
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_number_report_based_on_range() {
        PreparedQrResponse response = setupApi.registerWithQr();

        InstanceNumberReport report = InstanceNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(INSTANCE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .instanceNumberReportType(INSTANCE_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .groupId(response.defaultGroupId())
                .report(report)
                .build();

        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", Instant.now().minus(10, DAYS));
        qrRepository.save(qr);

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fail_fetch_number_report_if_not_app_manager() {
        PreparedQrResponse response = setupApi.registerWithQr();

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.jwt());

        InstanceNumberReport report = InstanceNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(INSTANCE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .instanceNumberReportType(INSTANCE_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertError(() -> ReportApi.fetchNumberReportRaw(memberResponse.getJwt(), query), ACCESS_DENIED);

        GroupApi.addGroupManagers(response.jwt(), response.defaultGroupId(), memberResponse.getMemberId());
        assertError(() -> ReportApi.fetchNumberReportRaw(memberResponse.getJwt(), query), ACCESS_DENIED);

        AppApi.setAppManagers(response.jwt(), response.appId(), memberResponse.getMemberId());
        assertEquals(1, ReportApi.fetchNumberReport(memberResponse.getJwt(), query).getNumber());
    }

    @Test
    public void should_fail_fetch_number_report_if_not_group_manager() {
        PreparedQrResponse response = setupApi.registerWithQr();

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.jwt());

        InstanceNumberReport report = InstanceNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(INSTANCE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .instanceNumberReportType(INSTANCE_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .groupId(response.defaultGroupId())
                .report(report)
                .build();

        assertError(() -> ReportApi.fetchNumberReportRaw(memberResponse.getJwt(), query), ACCESS_DENIED);

        GroupApi.addGroupManagers(response.jwt(), response.defaultGroupId(), memberResponse.getMemberId());
        assertEquals(1, ReportApi.fetchNumberReport(memberResponse.getJwt(), query).getNumber());
    }

    @Test
    public void should_fetch_instance_submission_count_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        InstanceNumberReport report = InstanceNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(INSTANCE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .instanceNumberReportType(INSTANCE_SUBMIT_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_instance_accessed_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        InstanceNumberReport report = InstanceNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(INSTANCE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .instanceNumberReportType(ACCESSED_INSTANCE_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        QR qr = qrRepository.byId(response.qrId());
        qr.access();
        qrRepository.save(qr);
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_sum_attribute_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT)
                .type(AttributeType.INSTANCE_SUBMIT_COUNT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        AttributeNumberReport report = AttributeNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(ATTRIBUTE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .attributeId(attribute.getId())
                .numberAggregationType(SUM)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_avg_attribute_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT)
                .type(AttributeType.INSTANCE_SUBMIT_COUNT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        AttributeNumberReport report = AttributeNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(ATTRIBUTE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .attributeId(attribute.getId())
                .numberAggregationType(AVG)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());

        QrApi.createQr(response.jwt(), response.defaultGroupId());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(2, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_max_attribute_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT)
                .type(AttributeType.INSTANCE_SUBMIT_COUNT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        AttributeNumberReport report = AttributeNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(ATTRIBUTE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .attributeId(attribute.getId())
                .numberAggregationType(MAX)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());

        CreateQrResponse qrResponse = QrApi.createQr(response.jwt(), response.defaultGroupId());
        SubmissionApi.newSubmission(response.jwt(), qrResponse.getQrId(), response.homePageId());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(3, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_min_attribute_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT)
                .type(AttributeType.INSTANCE_SUBMIT_COUNT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        AttributeNumberReport report = AttributeNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(ATTRIBUTE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .attributeId(attribute.getId())
                .numberAggregationType(MIN)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());

        CreateQrResponse qrResponse = QrApi.createQr(response.jwt(), response.defaultGroupId());
        SubmissionApi.newSubmission(response.jwt(), qrResponse.getQrId(), response.homePageId());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_attribute_number_report_for_range() {
        PreparedQrResponse response = setupApi.registerWithQr();

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT)
                .type(AttributeType.INSTANCE_SUBMIT_COUNT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        AttributeNumberReport report = AttributeNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(ATTRIBUTE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .attributeId(attribute.getId())
                .numberAggregationType(SUM)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", Instant.now().minus(10, DAYS));
        qrRepository.save(qr);
        assertNull(ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_attribute_number_report_for_specific_group() {
        PreparedQrResponse response = setupApi.registerWithQr();

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(AttributeStatisticRange.NO_LIMIT)
                .type(AttributeType.INSTANCE_SUBMIT_COUNT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        AttributeNumberReport report = AttributeNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(ATTRIBUTE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .attributeId(attribute.getId())
                .numberAggregationType(SUM)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .groupId(response.defaultGroupId())
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());

        String newGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateQrResponse qrResponse = QrApi.createQr(response.jwt(), newGroupId);
        SubmissionApi.newSubmission(response.jwt(), qrResponse.getQrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_page_submit_count_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        PageNumberReport report = PageNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(PAGE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .pageId(response.homePageId())
                .pageNumberReportType(PAGE_SUBMIT_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_page_number_report_for_specific_group() {
        PreparedQrResponse response = setupApi.registerWithQr();

        PageNumberReport report = PageNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(PAGE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .pageId(response.homePageId())
                .pageNumberReportType(PAGE_SUBMIT_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .groupId(response.defaultGroupId())
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());

        String newGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateQrResponse qrResponse = QrApi.createQr(response.jwt(), newGroupId);
        SubmissionApi.newSubmission(response.jwt(), qrResponse.getQrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_page_number_report_for_sub_groups() {
        PreparedQrResponse response = setupApi.registerWithQr();

        PageNumberReport report = PageNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(PAGE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .pageId(response.homePageId())
                .pageNumberReportType(PAGE_SUBMIT_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .groupId(response.defaultGroupId())
                .build();

        String newGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), response.defaultGroupId());
        CreateQrResponse newQr = QrApi.createQr(response.jwt(), newGroupId);

        SubmissionApi.newSubmission(response.jwt(), newQr.getQrId(), response.homePageId());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(2, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_page_number_report_for_range() {
        PreparedQrResponse response = setupApi.registerWithQr();

        PageNumberReport report = PageNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(PAGE_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .pageId(response.homePageId())
                .pageNumberReportType(PAGE_SUBMIT_COUNT)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());

        Submission submission = submissionRepository.byId(submissionId);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(10, DAYS));
        submissionRepository.save(submission);
        assertEquals(0, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_sum_control_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        ControlNumberReport report = ControlNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .pageId(response.homePageId())
                .controlId(control.getId())
                .numberAggregationType(SUM)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertNull(ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(control).number(2D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(control).number(1D).build());
        assertEquals(3, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_avg_control_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        ControlNumberReport report = ControlNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .pageId(response.homePageId())
                .controlId(control.getId())
                .numberAggregationType(AVG)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertNull(ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(control).number(3D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(control).number(1D).build());
        assertEquals(2, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_max_control_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        ControlNumberReport report = ControlNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .pageId(response.homePageId())
                .controlId(control.getId())
                .numberAggregationType(MAX)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertNull(ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(control).number(3D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(control).number(1D).build());
        assertEquals(3, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_min_control_number_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        ControlNumberReport report = ControlNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .pageId(response.homePageId())
                .controlId(control.getId())
                .numberAggregationType(MIN)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        assertNull(ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(control).number(3D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(control).number(1D).build());
        assertEquals(1, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_control_number_report_for_range() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        ControlNumberReport report = ControlNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_REPORT)
                .range(LAST_7_DAYS)
                .pageId(response.homePageId())
                .controlId(control.getId())
                .numberAggregationType(MAX)
                .build();

        NumberReportQuery query = NumberReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(control).number(3D).build());
        assertEquals(3, ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
        Submission submission = submissionRepository.byId(submissionId);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(10, DAYS));
        submissionRepository.save(submission);
        assertNull(ReportApi.fetchNumberReport(response.jwt(), query).getNumber());
    }

    @Test
    public void should_fetch_control_bar_report_for_submission_count() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);

        ControlBarReport report = ControlBarReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_BAR_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(SUBMIT_COUNT_SUM)
                        .pageId(response.homePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of())
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1, optionId2)).build());
        QCategorizedOptionSegmentReport qChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = qChartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(1, segment1.getValue());

        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(1, segment2.getValue());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build());
        QCategorizedOptionSegmentReport updatedReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> updatedSegments = updatedReport.getSegmentsData().get(0);
        CategorizedOptionSegment updatedSegment1 = updatedSegments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst()
                .get();
        assertEquals(2, updatedSegment1.getValue());
    }

    @Test
    public void should_fetch_control_bar_report_for_answer_sum() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);

        ControlBarReport report = ControlBarReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_BAR_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.homePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1, optionId2)).build(),
                rAnswerBuilder(numberInputControl).number(5D).build());

        QCategorizedOptionSegmentReport qChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = qChartReport.getSegmentsData().get(0);
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(10, segment1.getValue());
        assertEquals(5, segment2.getValue());
    }

    @Test
    public void should_fetch_control_bar_report_for_answer_avg() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);

        ControlBarReport report = ControlBarReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_BAR_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_AVG)
                        .pageId(response.homePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(4D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1, optionId2)).build(),
                rAnswerBuilder(numberInputControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl).number(8D).build());

        QCategorizedOptionSegmentReport qChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = qChartReport.getSegmentsData().get(0);
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(5, segment1.getValue());
        assertEquals(7, segment2.getValue());
    }

    @Test
    public void should_fetch_control_bar_report_for_answer_max() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);

        ControlBarReport report = ControlBarReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_BAR_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_MAX)
                        .pageId(response.homePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(4D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1, optionId2)).build(),
                rAnswerBuilder(numberInputControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl).number(8D).build());

        QCategorizedOptionSegmentReport qChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = qChartReport.getSegmentsData().get(0);
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(6, segment1.getValue());
        assertEquals(8, segment2.getValue());
    }

    @Test
    public void should_fetch_control_bar_report_for_answer_min() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);

        ControlBarReport report = ControlBarReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_BAR_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_MIN)
                        .pageId(response.homePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(4D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1, optionId2)).build(),
                rAnswerBuilder(numberInputControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl).number(8D).build());

        QCategorizedOptionSegmentReport qChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = qChartReport.getSegmentsData().get(0);
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(4, segment1.getValue());
        assertEquals(6, segment2.getValue());
    }

    @Test
    public void should_fetch_control_bar_report_for_given_range() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);

        ControlBarReport report = ControlBarReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_BAR_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_MIN)
                        .pageId(response.homePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .range(THIS_MONTH)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String submission1Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(4D).build());
        String submission2Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(6D).build());

        QCategorizedOptionSegmentReport qChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(4, qChartReport.getSegmentsData().get(0).get(0).getValue());

        Submission submission = submissionRepository.byId(submission1Id);
        ReflectionTestUtils.setField(submission, "createdAt", startOfLastMonth());
        submissionRepository.save(submission);

        QCategorizedOptionSegmentReport updatedChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(),
                query);
        assertEquals(6, updatedChartReport.getSegmentsData().get(0).get(0).getValue());
    }

    @Test
    public void should_fetch_control_bar_report_for_given_group() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);

        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateQrResponse anotherQr = QrApi.createQr(response.jwt(), anotherGroupId);

        ControlBarReport report = ControlBarReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_BAR_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_MIN)
                        .pageId(response.homePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of(numberInputControl.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(4D).build());
        SubmissionApi.newSubmission(response.jwt(), anotherQr.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(3D).build());

        ChartReportQuery unGroupedQuery = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();
        assertEquals(3,
                ((QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), unGroupedQuery)).getSegmentsData().get(0).get(0)
                        .getValue());

        ChartReportQuery groupedQuery = ChartReportQuery.builder()
                .appId(response.appId())
                .groupId(response.defaultGroupId())
                .report(report)
                .build();
        assertEquals(4,
                ((QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), groupedQuery)).getSegmentsData().get(0).get(0)
                        .getValue());
    }

    @Test
    public void should_fetch_control_pie_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);

        ControlPieReport report = ControlPieReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_PIE_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(SUBMIT_COUNT_SUM)
                        .pageId(response.homePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of())
                        .range(NO_LIMIT)
                        .build())
                .style(PieReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1, optionId2)).build());
        QCategorizedOptionSegmentReport qChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = qChartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(1, segment1.getValue());

        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(1, segment2.getValue());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build());
        QCategorizedOptionSegmentReport updatedReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> updatedSegments = updatedReport.getSegmentsData().get(0);
        CategorizedOptionSegment updatedSegment1 = updatedSegments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst()
                .get();
        assertEquals(2, updatedSegment1.getValue());
    }

    @Test
    public void should_fetch_control_doughnut_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);

        ControlDoughnutReport report = ControlDoughnutReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_DOUGHNUT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(SUBMIT_COUNT_SUM)
                        .pageId(response.homePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of())
                        .range(NO_LIMIT)
                        .build())
                .style(DoughnutReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1, optionId2)).build());
        QCategorizedOptionSegmentReport qChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = qChartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(1, segment1.getValue());

        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(1, segment2.getValue());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build());
        QCategorizedOptionSegmentReport updatedReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> updatedSegments = updatedReport.getSegmentsData().get(0);
        CategorizedOptionSegment updatedSegment1 = updatedSegments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst()
                .get();
        assertEquals(2, updatedSegment1.getValue());
    }

    @Test
    public void should_fetch_control_number_range_report_for_submit_count() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(SUBMIT_COUNT_SUM)
                        .pageId(response.homePageId())
                        .basedControlId(basedControl.getId())
                        .numberRangesString("10,20,30,40")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(11D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build());
        QNumberRangeSegmentReport qChartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(List.of(10D, 20D, 30D, 40D), qChartReport.getNumberRanges());
        List<NumberRangeSegment> segments = qChartReport.getSegments();
        assertEquals(3, segments.size());
        assertEquals(2, segments.get(0).getValue());
        assertEquals(10, segments.get(0).getSegment());
        assertEquals(1, segments.get(1).getValue());
        assertEquals(20, segments.get(1).getSegment());
        assertEquals(0, segments.get(2).getValue());//无数据时默认为0
        assertEquals(30, segments.get(2).getSegment());
    }

    @Test
    public void should_fetch_control_number_range_report_for_answer_sum() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.homePageId())
                        .basedControlId(basedControl.getId())
                        .numberRangesString("10,20,30")
                        .targetControlId(targetControl.getId())
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(11D).build(), rAnswerBuilder(targetControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(2D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(5D).build());
        QNumberRangeSegmentReport qChartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(List.of(10D, 20D, 30D), qChartReport.getNumberRanges());
        List<NumberRangeSegment> segments = qChartReport.getSegments();
        assertEquals(3, segments.get(0).getValue());
        assertEquals(10, segments.get(0).getSegment());
        assertEquals(5, segments.get(1).getValue());
        assertEquals(20, segments.get(1).getSegment());
    }

    @Test
    public void should_fetch_control_number_range_report_for_answer_avg() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_AVG)
                        .pageId(response.homePageId())
                        .basedControlId(basedControl.getId())
                        .numberRangesString("10,20,30")
                        .targetControlId(targetControl.getId())
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(11D).build(), rAnswerBuilder(targetControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(3D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(5D).build());
        QNumberRangeSegmentReport qChartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(List.of(10D, 20D, 30D), qChartReport.getNumberRanges());
        List<NumberRangeSegment> segments = qChartReport.getSegments();
        assertEquals(2, segments.get(0).getValue());
        assertEquals(10, segments.get(0).getSegment());
        assertEquals(5, segments.get(1).getValue());
        assertEquals(20, segments.get(1).getSegment());
    }

    @Test
    public void should_fetch_control_number_range_report_for_answer_max() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_MAX)
                        .pageId(response.homePageId())
                        .basedControlId(basedControl.getId())
                        .numberRangesString("10,20,30")
                        .targetControlId(targetControl.getId())
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(11D).build(), rAnswerBuilder(targetControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(3D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(5D).build());
        QNumberRangeSegmentReport qChartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(List.of(10D, 20D, 30D), qChartReport.getNumberRanges());
        List<NumberRangeSegment> segments = qChartReport.getSegments();
        assertEquals(3, segments.get(0).getValue());
        assertEquals(10, segments.get(0).getSegment());
        assertEquals(5, segments.get(1).getValue());
        assertEquals(20, segments.get(1).getSegment());
    }

    @Test
    public void should_fetch_control_number_range_report_for_answer_min() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_MIN)
                        .pageId(response.homePageId())
                        .basedControlId(basedControl.getId())
                        .numberRangesString("10,20,30")
                        .targetControlId(targetControl.getId())
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(11D).build(), rAnswerBuilder(targetControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(3D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(5D).build());
        QNumberRangeSegmentReport qChartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(List.of(10D, 20D, 30D), qChartReport.getNumberRanges());
        List<NumberRangeSegment> segments = qChartReport.getSegments();
        assertEquals(1, segments.get(0).getValue());
        assertEquals(10, segments.get(0).getSegment());
        assertEquals(5, segments.get(1).getValue());
        assertEquals(20, segments.get(1).getSegment());
    }

    @Test
    public void should_fetch_control_number_range_report_for_given_range() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_MIN)
                        .pageId(response.homePageId())
                        .basedControlId(basedControl.getId())
                        .numberRangesString("10,20,30")
                        .targetControlId(targetControl.getId())
                        .range(THIS_MONTH)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String submission1Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(11D).build(), rAnswerBuilder(targetControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(3D).build());
        assertEquals(1, ((QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query)).getSegments().get(0).getValue());

        Submission submission = submissionRepository.byId(submission1Id);
        ReflectionTestUtils.setField(submission, "createdAt", startOfLastMonth());
        submissionRepository.save(submission);
        assertEquals(3, ((QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query)).getSegments().get(0).getValue());
    }

    @Test
    public void should_fetch_control_number_range_report_for_given_group() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);

        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateQrResponse anotherQr = QrApi.createQr(response.jwt(), groupId);

        ControlNumberRangeSegmentReport report = ControlNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_RANGE_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlNumberRangeSegmentReportSetting.builder()
                        .segmentType(CONTROL_VALUE_MIN)
                        .pageId(response.homePageId())
                        .basedControlId(basedControl.getId())
                        .numberRangesString("10,20,30")
                        .targetControlId(targetControl.getId())
                        .range(THIS_MONTH)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(11D).build(), rAnswerBuilder(targetControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), anotherQr.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(3D).build());

        ChartReportQuery noGroupQuery = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();
        assertEquals(1,
                ((QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), noGroupQuery)).getSegments().get(0).getValue());

        ChartReportQuery groupQuery = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .groupId(anotherQr.getGroupId())
                .build();
        assertEquals(3,
                ((QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), groupQuery)).getSegments().get(0).getValue());
    }

    @Test
    public void should_fetch_control_time_segment_report_for_submit_count() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时报告")
                                .segmentType(SUBMIT_COUNT_SUM)
                                .basedType(CREATED_AT)
                                .pageId(response.homePageId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String submission1Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(1D).build());
        String submission2Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(1D).build());
        String submission3Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(1D).build());

        Submission submission1 = submissionRepository.byId(submission1Id);
        ReflectionTestUtils.setField(submission1, "createdAt", startOfLastMonth());
        submissionRepository.save(submission1);

        QTimeSegmentReport qTimeSegmentReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(PER_MONTH, qTimeSegmentReport.getInterval());
        assertEquals(2, qTimeSegmentReport.getSegmentsData().get(0).size());
        assertEquals(1, qTimeSegmentReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(2, qTimeSegmentReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_control_time_segment_report_for_answer_sum() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时统计")
                                .segmentType(CONTROL_VALUE_SUM)
                                .basedType(CREATED_AT)
                                .pageId(response.homePageId())
                                .targetControlId(targetControl.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String submission1Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        String submission2Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());
        String submission3Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(20D).build());

        Submission submission1 = submissionRepository.byId(submission1Id);
        ReflectionTestUtils.setField(submission1, "createdAt", startOfLastMonth());
        submissionRepository.save(submission1);

        QTimeSegmentReport qTimeSegmentReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(5, qTimeSegmentReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(30, qTimeSegmentReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_control_time_segment_report_for_answer_avg() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时统计")
                                .segmentType(CONTROL_VALUE_AVG)
                                .basedType(CREATED_AT)
                                .pageId(response.homePageId())
                                .targetControlId(targetControl.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String submission1Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        String submission2Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());
        String submission3Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(20D).build());

        Submission submission1 = submissionRepository.byId(submission1Id);
        ReflectionTestUtils.setField(submission1, "createdAt", startOfLastMonth());
        submissionRepository.save(submission1);

        QTimeSegmentReport qTimeSegmentReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(5, qTimeSegmentReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(15, qTimeSegmentReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_control_time_segment_report_for_answer_max() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时统计")
                                .segmentType(CONTROL_VALUE_MAX)
                                .basedType(CREATED_AT)
                                .pageId(response.homePageId())
                                .targetControlId(targetControl.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String submission1Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        String submission2Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());
        String submission3Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(20D).build());

        Submission submission1 = submissionRepository.byId(submission1Id);
        ReflectionTestUtils.setField(submission1, "createdAt", startOfLastMonth());
        submissionRepository.save(submission1);

        QTimeSegmentReport qTimeSegmentReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(5, qTimeSegmentReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(20, qTimeSegmentReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_control_time_segment_report_for_answer_min() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时统计")
                                .segmentType(CONTROL_VALUE_MIN)
                                .basedType(CREATED_AT)
                                .pageId(response.homePageId())
                                .targetControlId(targetControl.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String submission1Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        String submission2Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());
        String submission3Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(20D).build());

        Submission submission1 = submissionRepository.byId(submission1Id);
        ReflectionTestUtils.setField(submission1, "createdAt", startOfLastMonth());
        submissionRepository.save(submission1);

        QTimeSegmentReport qTimeSegmentReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(5, qTimeSegmentReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(10, qTimeSegmentReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_control_time_segment_report_for_given_group() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateQrResponse anotherQr = QrApi.createQr(response.jwt(), groupId);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时统计")
                                .segmentType(CONTROL_VALUE_MIN)
                                .basedType(CREATED_AT)
                                .pageId(response.homePageId())
                                .targetControlId(targetControl.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        String submission1Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        String submission2Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(6D).build());
        String submission3Id = SubmissionApi.newSubmission(response.jwt(), anotherQr.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(4D).build());

        ChartReportQuery noGroupQuery = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();
        assertEquals(4,
                ((QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), noGroupQuery)).getSegmentsData().get(0).get(0).getValue());

        ChartReportQuery groupQuery = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .groupId(response.defaultGroupId())
                .build();
        assertEquals(5,
                ((QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), groupQuery)).getSegmentsData().get(0).get(0).getValue());
    }

    @Test
    public void should_fetch_control_time_segment_report_for_per_season() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时统计")
                                .segmentType(CONTROL_VALUE_MIN)
                                .basedType(CREATED_AT)
                                .pageId(response.homePageId())
                                .targetControlId(targetControl.getId())
                                .build()))
                        .interval(PER_SEASON)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String submission1Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        String submission2Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());
        String submission3Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(20D).build());

        Submission submission1 = submissionRepository.byId(submission1Id);
        ReflectionTestUtils.setField(submission1, "createdAt", startOfLastSeason());
        submissionRepository.save(submission1);

        Submission submission2 = submissionRepository.byId(submission2Id);
        ReflectionTestUtils.setField(submission2, "createdAt", startOfLastSeason().plus(40, DAYS));
        submissionRepository.save(submission2);

        QTimeSegmentReport qTimeSegmentReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(5, qTimeSegmentReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(20, qTimeSegmentReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_control_time_segment_report_for_per_year() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时统计")
                                .segmentType(CONTROL_VALUE_MIN)
                                .basedType(CREATED_AT)
                                .pageId(response.homePageId())
                                .targetControlId(targetControl.getId())
                                .build()))
                        .interval(PER_YEAR)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String submission1Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        String submission2Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());
        String submission3Id = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(20D).build());

        Submission submission1 = submissionRepository.byId(submission1Id);
        ReflectionTestUtils.setField(submission1, "createdAt", startOfLastYear());
        submissionRepository.save(submission1);

        Submission submission2 = submissionRepository.byId(submission2Id);
        ReflectionTestUtils.setField(submission2, "createdAt", startOfLastYear().plus(100, DAYS));
        submissionRepository.save(submission2);

        QTimeSegmentReport qTimeSegmentReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(5, qTimeSegmentReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(20, qTimeSegmentReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_control_time_segment_report_for_submit_count_based_on_date_control() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FDateControl basedControl = defaultDateControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时统计")
                                .segmentType(SUBMIT_COUNT_SUM)
                                .basedType(DATE_CONTROL)
                                .pageId(response.homePageId())
                                .basedControlId(basedControl.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(5).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().toString()).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().minusMonths(1).toString()).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().minusMonths(1).toString()).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().minusMonths(10).toString()).build());

        QTimeSegmentReport qTimeSegmentReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(PER_MONTH, qTimeSegmentReport.getInterval());
        assertEquals(2, qTimeSegmentReport.getSegmentsData().get(0).size());
        assertEquals(2, qTimeSegmentReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(1, qTimeSegmentReport.getSegmentsData().get(0).get(1).getValue());
    }


    @Test
    public void should_fetch_control_time_segment_report_for_submit_count_based_on_date_time_control() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FDateTimeControl basedTimeControl = defaultDateTimeControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), basedTimeControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                .id(UuidGenerator.newShortUuid())
                                .name("分时统计")
                                .segmentType(SUBMIT_COUNT_SUM)
                                .basedType(DATE_CONTROL)
                                .pageId(response.homePageId())
                                .basedControlId(basedTimeControl.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(5).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedTimeControl).date(LocalDate.now().toString()).time(rTime()).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedTimeControl).date(LocalDate.now().minusMonths(1).toString()).time(rTime()).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedTimeControl).date(LocalDate.now().minusMonths(1).toString()).time(rTime()).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedTimeControl).date(LocalDate.now().minusMonths(10).toString()).time(rTime()).build());

        QTimeSegmentReport qTimeSegmentReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(PER_MONTH, qTimeSegmentReport.getInterval());
        assertEquals(2, qTimeSegmentReport.getSegmentsData().get(0).size());
        assertEquals(2, qTimeSegmentReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(1, qTimeSegmentReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_control_time_segment_report_for_multiple_items() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FDateControl basedControl = defaultDateControl();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);

        ControlTimeSegmentReport report = ControlTimeSegmentReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_TIME_SEGMENT_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlTimeSegmentReportSetting.builder()
                        .segmentSettings(List.of(ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                        .id(UuidGenerator.newShortUuid())
                                        .name("提交量")
                                        .segmentType(SUBMIT_COUNT_SUM)
                                        .basedType(DATE_CONTROL)
                                        .pageId(response.homePageId())
                                        .basedControlId(basedControl.getId())
                                        .build(),
                                ControlTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                        .id(UuidGenerator.newShortUuid())
                                        .name("数值和")
                                        .segmentType(CONTROL_VALUE_SUM)
                                        .basedType(DATE_CONTROL)
                                        .pageId(response.homePageId())
                                        .basedControlId(basedControl.getId())
                                        .targetControlId(targetControl.getId())
                                        .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(5).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().toString()).build(), rAnswerBuilder(targetControl).number(1d).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().minusMonths(1).toString()).build(),
                rAnswerBuilder(targetControl).number(2d).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().minusMonths(1).toString()).build(),
                rAnswerBuilder(targetControl).number(3d).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().minusMonths(10).toString()).build(),
                rAnswerBuilder(targetControl).number(4d).build());

        QTimeSegmentReport qTimeSegmentReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(PER_MONTH, qTimeSegmentReport.getInterval());
        assertEquals(2, qTimeSegmentReport.getSegmentsData().get(0).size());
        assertEquals(2, qTimeSegmentReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(1, qTimeSegmentReport.getSegmentsData().get(0).get(1).getValue());

        assertEquals(2, qTimeSegmentReport.getSegmentsData().get(1).size());
        assertEquals(5, qTimeSegmentReport.getSegmentsData().get(1).get(0).getValue());
        assertEquals(1, qTimeSegmentReport.getSegmentsData().get(1).get(1).getValue());
    }

    @Test
    public void should_fetch_attribute_bar_report_for_qr_count() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);
        Attribute numberAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(QR_COUNT_SUM)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of())
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(2, segment1.getValue());
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(1, segment2.getValue());
    }

    @Test
    public void should_fetch_attribute_bar_report_for_attribute_value_sum() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);
        Attribute numberAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_SUM)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of(numberAttribute.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl).number(2D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(6, segment1.getValue());
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(2, segment2.getValue());
    }

    @Test
    public void should_fetch_attribute_bar_report_for_attribute_value_avg() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);
        Attribute numberAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_AVG)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of(numberAttribute.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl).number(2D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(3, segment1.getValue());
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(2, segment2.getValue());
    }

    @Test
    public void should_fetch_attribute_bar_report_for_attribute_value_max() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);
        Attribute numberAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of(numberAttribute.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl).number(2D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(5, segment1.getValue());
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(2, segment2.getValue());
    }

    @Test
    public void should_fetch_attribute_bar_report_for_attribute_value_min() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);
        Attribute numberAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MIN)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of(numberAttribute.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl).number(2D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(1, segment1.getValue());
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(2, segment2.getValue());
    }

    @Test
    public void should_fetch_attribute_bar_report_for_given_range() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);
        Attribute numberAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MIN)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of(numberAttribute.getId()))
                        .range(THIS_MONTH)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl).number(2D).build());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", startOfLastMonth());
        qrRepository.save(qr);

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(5, segment1.getValue());
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(2, segment2.getValue());
    }

    @Test
    public void should_fetch_attribute_bar_report_for_given_group() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);
        Attribute numberAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MIN)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of(numberAttribute.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), groupId);
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl).number(2D).build());

        ChartReportQuery noGroupQuery = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(),
                noGroupQuery);
        List<CategorizedOptionSegment> segments = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(1, segment1.getValue());
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(2, segment2.getValue());

        ChartReportQuery groupQuery = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .groupId(response.defaultGroupId())
                .build();

        QCategorizedOptionSegmentReport groupChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(),
                groupQuery);
        List<CategorizedOptionSegment> groupSegments = groupChartReport.getSegmentsData().get(0);
        assertEquals(2, groupSegments.size());
        CategorizedOptionSegment groupSegment1 = groupSegments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst()
                .get();
        assertEquals(5, groupSegment1.getValue());
        CategorizedOptionSegment groupSegment2 = groupSegments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst()
                .get();
        assertEquals(2, groupSegment2.getValue());
    }

    @Test
    public void should_fetch_attribute_pie_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);
        Attribute numberAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute, checkboxAttribute);

        AttributePieReport report = AttributePieReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_PIE_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(QR_COUNT_SUM)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of())
                        .range(NO_LIMIT)
                        .build())
                .style(PieReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(2, segment1.getValue());
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(1, segment2.getValue());
    }

    @Test
    public void should_fetch_attribute_doughnut_report() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);
        Attribute numberAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute, checkboxAttribute);

        AttributeDoughnutReport report = AttributeDoughnutReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_DOUGHNUT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(QR_COUNT_SUM)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of())
                        .range(NO_LIMIT)
                        .build())
                .style(DoughnutReportStyle.builder().max(10).colors(List.of(rColor())).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(2, segment1.getValue());
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(1, segment2.getValue());
    }

    @Test
    public void should_fetch_attribute_number_range_report_for_qr_count() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute, targetAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(QR_COUNT_SUM)
                        .basedAttributeId(basedAttribute.getId())
                        .numberRangesString("10,20,30,40")
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QNumberRangeSegmentReport chartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(List.of(10D, 20D, 30D, 40D), chartReport.getNumberRanges());
        assertEquals(2, chartReport.getSegments().get(0).getValue());
        assertEquals(10, chartReport.getSegments().get(0).getSegment());
        assertEquals(1, chartReport.getSegments().get(1).getValue());
        assertEquals(20, chartReport.getSegments().get(1).getSegment());
        assertEquals(0, chartReport.getSegments().get(2).getValue());
        assertEquals(30, chartReport.getSegments().get(2).getSegment());
    }

    @Test
    public void should_fetch_attribute_number_range_report_for_attribute_value_sum() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute, targetAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_SUM)
                        .basedAttributeId(basedAttribute.getId())
                        .numberRangesString("10,20,30,40")
                        .targetAttributeId(targetAttribute.getId())
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(9D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QNumberRangeSegmentReport chartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(14, chartReport.getSegments().get(0).getValue());
        assertEquals(6, chartReport.getSegments().get(1).getValue());
        assertEquals(0, chartReport.getSegments().get(2).getValue());
    }

    @Test
    public void should_fetch_attribute_number_range_report_for_attribute_value_avg() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute, targetAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_AVG)
                        .basedAttributeId(basedAttribute.getId())
                        .numberRangesString("10,20,30,40")
                        .targetAttributeId(targetAttribute.getId())
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(9D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QNumberRangeSegmentReport chartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(7, chartReport.getSegments().get(0).getValue());
        assertEquals(6, chartReport.getSegments().get(1).getValue());
        assertEquals(0, chartReport.getSegments().get(2).getValue());
    }

    @Test
    public void should_fetch_attribute_number_range_report_for_attribute_value_max() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute, targetAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MAX)
                        .basedAttributeId(basedAttribute.getId())
                        .numberRangesString("10,20,30,40")
                        .targetAttributeId(targetAttribute.getId())
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(9D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QNumberRangeSegmentReport chartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(9, chartReport.getSegments().get(0).getValue());
        assertEquals(6, chartReport.getSegments().get(1).getValue());
        assertEquals(0, chartReport.getSegments().get(2).getValue());
    }

    @Test
    public void should_fetch_attribute_number_range_report_for_attribute_value_min() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute, targetAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MIN)
                        .basedAttributeId(basedAttribute.getId())
                        .numberRangesString("10,20,30,40")
                        .targetAttributeId(targetAttribute.getId())
                        .range(NO_LIMIT)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(9D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QNumberRangeSegmentReport chartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(5, chartReport.getSegments().get(0).getValue());
        assertEquals(6, chartReport.getSegments().get(1).getValue());
        assertEquals(0, chartReport.getSegments().get(2).getValue());
    }

    @Test
    public void should_fetch_attribute_number_range_report_for_given_range() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute, targetAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MIN)
                        .basedAttributeId(basedAttribute.getId())
                        .numberRangesString("10,20,30,40")
                        .targetAttributeId(targetAttribute.getId())
                        .range(THIS_MONTH)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(9D).build());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", startOfLastMonth());
        qrRepository.save(qr);

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QNumberRangeSegmentReport chartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(9, chartReport.getSegments().get(0).getValue());
        assertEquals(6, chartReport.getSegments().get(1).getValue());
        assertEquals(0, chartReport.getSegments().get(2).getValue());
    }

    @Test
    public void should_fetch_attribute_number_range_report_for_given_group() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute, targetAttribute);

        AttributeNumberRangeSegmentReport report = AttributeNumberRangeSegmentReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeNumberRangeSegmentReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MIN)
                        .basedAttributeId(basedAttribute.getId())
                        .numberRangesString("10,20,30,40")
                        .targetAttributeId(targetAttribute.getId())
                        .range(THIS_MONTH)
                        .build())
                .style(NumberRangeSegmentReportStyle.builder().build())
                .build();

        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), groupId);

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(9D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .groupId(response.defaultGroupId())
                .build();

        QNumberRangeSegmentReport chartReport = (QNumberRangeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(5, chartReport.getSegments().get(0).getValue());
        assertEquals(6, chartReport.getSegments().get(1).getValue());
        assertEquals(0, chartReport.getSegments().get(2).getValue());
    }

    @Test
    public void should_fetch_attribute_time_segment_report_for_qr_count() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute, targetAttribute);

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
                                .segmentType(QR_COUNT_SUM)
                                .basedType(QrTimeBasedType.CREATED_AT)
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", startOfLastMonth());
        qrRepository.save(qr);

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(1, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(2, chartReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_attribute_time_segment_report_for_attribute_value_sum() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);
        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), targetAttribute);

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
                                .segmentType(ATTRIBUTE_VALUE_SUM)
                                .basedType(QrTimeBasedType.CREATED_AT)
                                .targetAttributeId(targetAttribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(9D).build());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", startOfLastMonth());
        qrRepository.save(qr);

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(5, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(15, chartReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_attribute_time_segment_report_for_attribute_value_avg() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), targetAttribute);

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
                                .segmentType(ATTRIBUTE_VALUE_AVG)
                                .basedType(QrTimeBasedType.CREATED_AT)
                                .targetAttributeId(targetAttribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", startOfLastMonth());
        qrRepository.save(qr);

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(5, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(8, chartReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_attribute_time_segment_report_for_attribute_value_max() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), targetAttribute);

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
                                .basedType(QrTimeBasedType.CREATED_AT)
                                .targetAttributeId(targetAttribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", startOfLastMonth());
        qrRepository.save(qr);

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(5, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(10, chartReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_attribute_time_segment_report_for_attribute_value_min() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), targetAttribute);

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
                                .segmentType(ATTRIBUTE_VALUE_MIN)
                                .basedType(QrTimeBasedType.CREATED_AT)
                                .targetAttributeId(targetAttribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", startOfLastMonth());
        qrRepository.save(qr);

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(5, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(6, chartReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_attribute_time_segment_report_for_given_group() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), targetAttribute);

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
                                .segmentType(ATTRIBUTE_VALUE_MIN)
                                .basedType(QrTimeBasedType.CREATED_AT)
                                .targetAttributeId(targetAttribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), groupId);
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", startOfLastMonth());
        qrRepository.save(qr);

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .groupId(response.defaultGroupId())
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(5, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(10, chartReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_chart_report_for_sub_groups() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl, checkboxControl);
        Attribute numberAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_MIN)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of(numberAttribute.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        String subGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), response.defaultGroupId());
        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), subGroupId);
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl).number(2D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .groupId(response.defaultGroupId())
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments.size());
        CategorizedOptionSegment segment1 = segments.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(1, segment1.getValue());
        CategorizedOptionSegment segment2 = segments.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(2, segment2.getValue());
    }

    @Test
    public void should_fetch_attribute_time_segment_report_for_per_season() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), targetAttribute);

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
                                .segmentType(ATTRIBUTE_VALUE_MIN)
                                .basedType(QrTimeBasedType.CREATED_AT)
                                .targetAttributeId(targetAttribute.getId())
                                .build()))
                        .interval(PER_SEASON)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", startOfLastSeason());
        qrRepository.save(qr);

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(PER_SEASON, chartReport.getInterval());
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(5, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(6, chartReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_attribute_time_segment_report_for_per_year() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), targetControl);

        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), targetAttribute);

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
                                .basedType(QrTimeBasedType.CREATED_AT)
                                .targetAttributeId(targetAttribute.getId())
                                .build()))
                        .interval(PER_YEAR)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(10D).build());

        QR qr = qrRepository.byId(response.qrId());
        ReflectionTestUtils.setField(qr, "createdAt", startOfLastYear());
        qrRepository.save(qr);

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(PER_YEAR, chartReport.getInterval());
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(5, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(Year.now().getValue() - 1, chartReport.getSegmentsData().get(0).get(0).getYear());
        assertEquals(10, chartReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_attribute_time_segment_report_for_qr_count_with_based_attribute() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FDateControl basedControl = defaultDateControl();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute);

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
                                .segmentType(QR_COUNT_SUM)
                                .basedType(QrTimeBasedType.DATE_ATTRIBUTE)
                                .basedAttributeId(basedAttribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().minusMonths(1).toString()).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().toString()).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().toString()).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(1, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(2, chartReport.getSegmentsData().get(0).get(1).getValue());
    }


    @Test
    public void should_fetch_attribute_time_segment_report_for_qr_count_with_based_attribute_from_date_time_control() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FDateTimeControl basedTimeControl = defaultDateTimeControl();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedTimeControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedTimeControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute);

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
                                .segmentType(QR_COUNT_SUM)
                                .basedType(QrTimeBasedType.DATE_ATTRIBUTE)
                                .basedAttributeId(basedAttribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedTimeControl).date(LocalDate.now().minusMonths(1).toString()).time(rTime()).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedTimeControl).date(LocalDate.now().toString()).time(rTime()).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedTimeControl).date(LocalDate.now().toString()).time(rTime()).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(1, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(2, chartReport.getSegmentsData().get(0).get(1).getValue());
    }


    @Test
    public void should_fetch_attribute_time_segment_report_for_sum_with_based_attribute() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FDateControl basedControl = defaultDateControl();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute, targetAttribute);

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
                                .segmentType(ATTRIBUTE_VALUE_SUM)
                                .basedType(QrTimeBasedType.DATE_ATTRIBUTE)
                                .basedAttributeId(basedAttribute.getId())
                                .targetAttributeId(targetAttribute.getId())
                                .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl).number(3D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().minusMonths(1).toString()).build(),
                rAnswerBuilder(targetControl).number(3D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().toString()).build(), rAnswerBuilder(targetControl).number(4D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().toString()).build(), rAnswerBuilder(targetControl).number(5D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(3, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(9, chartReport.getSegmentsData().get(0).get(1).getValue());
    }

    @Test
    public void should_fetch_attribute_time_segment_report_mutiple_itmes() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FDateControl basedControl = defaultDateControl();
        FNumberInputControl targetControl1 = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl targetControl2 = defaultNumberInputControlBuilder().precision(0).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), basedControl, targetControl1, targetControl2);
        Attribute basedAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(basedControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute1 = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl1.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute targetAttribute2 = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(targetControl2.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), basedAttribute, targetAttribute1, targetAttribute2);

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
                                        .segmentType(ATTRIBUTE_VALUE_SUM)
                                        .basedType(QrTimeBasedType.DATE_ATTRIBUTE)
                                        .basedAttributeId(basedAttribute.getId())
                                        .targetAttributeId(targetAttribute1.getId())
                                        .build(),
                                AttributeTimeSegmentReportSetting.TimeSegmentSetting.builder()
                                        .id(UuidGenerator.newShortUuid())
                                        .name(rReportName())
                                        .segmentType(ATTRIBUTE_VALUE_SUM)
                                        .basedType(QrTimeBasedType.DATE_ATTRIBUTE)
                                        .basedAttributeId(basedAttribute.getId())
                                        .targetAttributeId(targetAttribute2.getId())
                                        .build()))
                        .interval(PER_MONTH)
                        .build())
                .style(TimeSegmentReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(targetControl1).number(3D).build());
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().minusMonths(1).toString()).build(),
                rAnswerBuilder(targetControl1).number(3D).build(), rAnswerBuilder(targetControl2).number(6D).build());
        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().toString()).build(), rAnswerBuilder(targetControl1).number(4D).build(),
                rAnswerBuilder(targetControl2).number(7D).build());
        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(basedControl).date(LocalDate.now().toString()).build(), rAnswerBuilder(targetControl1).number(5D).build(),
                rAnswerBuilder(targetControl2).number(8D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QTimeSegmentReport chartReport = (QTimeSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        assertEquals(2, chartReport.getSegmentsData().get(0).size());
        assertEquals(3, chartReport.getSegmentsData().get(0).get(0).getValue());
        assertEquals(9, chartReport.getSegmentsData().get(0).get(1).getValue());

        assertEquals(2, chartReport.getSegmentsData().get(1).size());
        assertEquals(6, chartReport.getSegmentsData().get(1).get(0).getValue());
        assertEquals(15, chartReport.getSegmentsData().get(1).get(1).getValue());
    }

    @Test
    public void should_fetch_multiple_report_item_for_control_bar() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl1 = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl numberInputControl2 = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl1, numberInputControl2, checkboxControl);

        ControlBarReport report = ControlBarReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_BAR_REPORT)
                .span(12)
                .aspectRatio(50)
                .setting(ControlCategorizedReportSetting.builder()
                        .segmentType(CONTROL_VALUE_SUM)
                        .pageId(response.homePageId())
                        .basedControlId(checkboxControl.getId())
                        .targetControlIds(List.of(numberInputControl1.getId(), numberInputControl2.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1, optionId2)).build(),
                rAnswerBuilder(numberInputControl1).number(1d).build(), rAnswerBuilder(numberInputControl2).number(2d).build());

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl1).number(3d).build(),
                rAnswerBuilder(numberInputControl2).number(4d).build());

        QCategorizedOptionSegmentReport qChartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments1 = qChartReport.getSegmentsData().get(0);
        assertEquals(2, segments1.size());
        CategorizedOptionSegment segment1 = segments1.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(4, segment1.getValue());
        CategorizedOptionSegment segment2 = segments1.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(1, segment2.getValue());

        List<CategorizedOptionSegment> segments2 = qChartReport.getSegmentsData().get(1);
        assertEquals(2, segments2.size());
        CategorizedOptionSegment segment21 = segments2.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(6, segment21.getValue());
        CategorizedOptionSegment segment22 = segments2.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(2, segment22.getValue());
    }

    @Test
    public void should_fetch_multiple_report_item_for_attribute_bar() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl1 = defaultNumberInputControlBuilder().precision(0).build();
        FNumberInputControl numberInputControl2 = defaultNumberInputControlBuilder().precision(0).build();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl1, numberInputControl2, checkboxControl);
        Attribute numberAttribute1 = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl1.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute numberAttribute2 = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl2.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), numberAttribute1, numberAttribute2, checkboxAttribute);

        AttributeBarReport report = AttributeBarReport.builder()
                .id(newShortUuid())
                .type(ATTRIBUTE_BAR_REPORT)
                .name(rReportName())
                .span(10)
                .aspectRatio(50)
                .setting(AttributeCategorizedReportSetting.builder()
                        .segmentType(ATTRIBUTE_VALUE_SUM)
                        .basedAttributeId(checkboxAttribute.getId())
                        .targetAttributeIds(List.of(numberAttribute1.getId(), numberAttribute2.getId()))
                        .range(NO_LIMIT)
                        .build())
                .style(BarReportStyle.builder().max(10).colors(List.of()).build())
                .build();

        CreateQrResponse qr1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        String optionId1 = checkboxControl.getOptions().get(0).getId();
        String optionId2 = checkboxControl.getOptions().get(1).getId();

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1, optionId2)).build(),
                rAnswerBuilder(numberInputControl1).number(1D).build(), rAnswerBuilder(numberInputControl2).number(2D).build());

        SubmissionApi.newSubmission(response.jwt(), qr1.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId1)).build(), rAnswerBuilder(numberInputControl1).number(3D).build(),
                rAnswerBuilder(numberInputControl2).number(4D).build());

        SubmissionApi.newSubmission(response.jwt(), qr2.getQrId(), response.homePageId(),
                rAnswerBuilder(checkboxControl).optionIds(List.of(optionId2)).build(), rAnswerBuilder(numberInputControl1).number(5D).build(),
                rAnswerBuilder(numberInputControl2).number(6D).build());

        ChartReportQuery query = ChartReportQuery.builder()
                .appId(response.appId())
                .report(report)
                .build();

        QCategorizedOptionSegmentReport chartReport = (QCategorizedOptionSegmentReport) ReportApi.fetchChartReport(response.jwt(), query);
        List<CategorizedOptionSegment> segments1 = chartReport.getSegmentsData().get(0);
        assertEquals(2, segments1.size());
        CategorizedOptionSegment segment1 = segments1.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(4, segment1.getValue());
        CategorizedOptionSegment segment2 = segments1.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(6, segment2.getValue());

        List<CategorizedOptionSegment> segments2 = chartReport.getSegmentsData().get(1);
        assertEquals(2, segments2.size());
        CategorizedOptionSegment segment21 = segments2.stream().filter(segment -> segment.getOption().equals(optionId1)).findFirst().get();
        assertEquals(6, segment21.getValue());
        CategorizedOptionSegment segment22 = segments2.stream().filter(segment -> segment.getOption().equals(optionId2)).findFirst().get();
        assertEquals(8, segment22.getValue());
    }
}
