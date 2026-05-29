package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.stat.TimeSegment;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.timesegment.QTimeSegmentPresentation;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static com.mryqr.common.domain.stat.SubmissionSegmentType.*;
import static com.mryqr.common.domain.stat.SubmissionTimeBasedType.CREATED_AT;
import static com.mryqr.common.domain.stat.SubmissionTimeBasedType.DATE_CONTROL;
import static com.mryqr.common.domain.stat.TimeSegmentInterval.*;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.CommonUtils.currentSeason;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.time.LocalDate.now;
import static java.time.ZoneId.systemDefault;
import static org.junit.jupiter.api.Assertions.*;

public class TimeSegmentControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally_for_control_value() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_create_control_normally_for_submit_count() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(SUBMIT_COUNT_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(null)
                        .build()))
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_create_control_for_date_control_as_based_type() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        FDateControl dateControl = defaultDateControl();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(DATE_CONTROL)
                        .pageId(response.getHomePageId())
                        .basedControlId(dateControl.getId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, dateControl, control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }


    @Test
    public void should_create_control_for_date_time_control_as_based_type() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        FDateTimeControl dateTimeControl = defaultDateTimeControl();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(DATE_CONTROL)
                        .pageId(response.getHomePageId())
                        .basedControlId(dateTimeControl.getId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, dateTimeControl, control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_page_for_control_value_interval() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(CREATED_AT)
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_page_for_submit_count_interval() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(SUBMIT_COUNT_SUM)
                        .basedType(CREATED_AT)
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_control_for_control_value_interval() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .build()))
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_if_no_based_control_id() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        FDateControl dateControl = defaultDateControl();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(DATE_CONTROL)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, dateControl, control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_fail_create_control_if_referenced_page_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(SUBMIT_COUNT_SUM)
                        .basedType(CREATED_AT)
                        .pageId(Page.newPageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_control_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(Control.newControlId())
                        .build()))
                .build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_control_not_support_time_segment() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), singleLineTextControl);
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(singleLineTextControl.getId())
                        .build()))
                .build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                NOT_SUPPORTED_TARGET_CONTROL_FOR_TIME_SEGMENT);
    }

    @Test
    public void should_fail_create_control_if_referenced_based_control_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(DATE_CONTROL)
                        .pageId(response.getHomePageId())
                        .basedControlId(Control.newControlId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_based_control_not_support_time_segment() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), singleLineTextControl, numberInputControl);
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(DATE_CONTROL)
                        .pageId(response.getHomePageId())
                        .basedControlId(singleLineTextControl.getId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                NOT_SUPPORTED_BASED_CONTROL_FOR_TIME_SEGMENT);
    }

    @Test
    public void should_fetch_time_segment_control_values_based_on_month() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_MONTH)
                .max(5)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        LocalDate localDate1 = now().minusMonths(5);
        createSubmission(response, numberInputControl, localDate1, 10);

        LocalDate localDate2 = now().minusMonths(4);
        createSubmission(response, numberInputControl, localDate2, 30);

        LocalDate localDate3 = now().minusMonths(3);
        createSubmission(response, numberInputControl, localDate3, 40);
        createSubmission(response, numberInputControl, localDate3, 50);

        LocalDate localDate4 = now();
        createSubmission(response, numberInputControl, localDate4, 60);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(10d).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_MONTH, presentation.getInterval());

        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(3, segments.size());

        assertEquals(localDate2.getYear(), segments.get(0).getYear());
        assertEquals(localDate2.getMonthValue(), segments.get(0).getPeriod());
        assertEquals(30, segments.get(0).getValue());

        assertEquals(localDate3.getYear(), segments.get(1).getYear());
        assertEquals(localDate3.getMonthValue(), segments.get(1).getPeriod());
        assertEquals(90, segments.get(1).getValue());

        assertEquals(localDate4.getYear(), segments.get(2).getYear());
        assertEquals(localDate4.getMonthValue(), segments.get(2).getPeriod());
        assertEquals(60, segments.get(2).getValue());
    }

    @Test
    public void should_fetch_time_segment_control_average_values() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_AVG)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_MONTH)
                .max(5)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        LocalDate localDate = now();
        createSubmission(response, numberInputControl, localDate, 10);
        createSubmission(response, numberInputControl, localDate, 20);
        createSubmission(response, numberInputControl, localDate, 30);
        createSubmission(response, numberInputControl, localDate, 40);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(10d).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_MONTH, presentation.getInterval());

        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(1, segments.size());
        assertEquals(localDate.getYear(), segments.get(0).getYear());
        assertEquals(localDate.getMonthValue(), segments.get(0).getPeriod());
        assertEquals(25, segments.get(0).getValue());
    }

    @Test
    public void should_fetch_time_segment_control_max_values() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_MAX)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_MONTH)
                .max(5)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        LocalDate localDate = now();
        createSubmission(response, numberInputControl, localDate, 10);
        createSubmission(response, numberInputControl, localDate, 20);
        createSubmission(response, numberInputControl, localDate, 30);
        createSubmission(response, numberInputControl, localDate, 40);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(10d).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_MONTH, presentation.getInterval());

        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(1, segments.size());
        assertEquals(localDate.getYear(), segments.get(0).getYear());
        assertEquals(localDate.getMonthValue(), segments.get(0).getPeriod());
        assertEquals(40, segments.get(0).getValue());
    }

    @Test
    public void should_fetch_time_segment_control_min_values() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_MIN)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_MONTH)
                .max(5)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        LocalDate localDate = now();
        createSubmission(response, numberInputControl, localDate, 10);
        createSubmission(response, numberInputControl, localDate, 20);
        createSubmission(response, numberInputControl, localDate, 30);
        createSubmission(response, numberInputControl, localDate, 40);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(10d).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_MONTH, presentation.getInterval());

        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(1, segments.size());
        assertEquals(localDate.getYear(), segments.get(0).getYear());
        assertEquals(localDate.getMonthValue(), segments.get(0).getPeriod());
        assertEquals(10, segments.get(0).getValue());
    }

    @Test
    public void should_fetch_time_segment_control_values_based_on_season() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_SEASON)
                .max(2)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        createSubmission(response, numberInputControl, now().minusMonths(11).withDayOfMonth(5), 10);
        createSubmission(response, numberInputControl, now().minusMonths(10).withDayOfMonth(5), 20);
        createSubmission(response, numberInputControl, now().minusMonths(9).withDayOfMonth(5), 30);
        createSubmission(response, numberInputControl, now().minusMonths(8).withDayOfMonth(5), 40);
        createSubmission(response, numberInputControl, now().minusMonths(7).withDayOfMonth(5), 50);
        createSubmission(response, numberInputControl, now().minusMonths(6).withDayOfMonth(5), 60);
        createSubmission(response, numberInputControl, now().minusMonths(5).withDayOfMonth(5), 70);
        createSubmission(response, numberInputControl, now().minusMonths(4).withDayOfMonth(5), 80);
        createSubmission(response, numberInputControl, now().minusMonths(3).withDayOfMonth(5), 90);
        createSubmission(response, numberInputControl, now(), 120);
        createSubmission(response, numberInputControl, now(), 130);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(10d).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_SEASON, presentation.getInterval());
        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertTrue(segments.size() >= 2);

        assertEquals(now().getYear(), segments.get(segments.size() - 1).getYear());
        assertEquals(currentSeason(), segments.get(segments.size() - 1).getPeriod());
        assertEquals(250, segments.get(segments.size() - 1).getValue());
    }

    @Test
    public void should_fetch_time_segment_control_values_based_on_year() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_YEAR)
                .max(2)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        createSubmission(response, numberInputControl, now().minusYears(1), 10);
        createSubmission(response, numberInputControl, now().minusYears(1), 20);
        createSubmission(response, numberInputControl, now().minusYears(2), 40);
        createSubmission(response, numberInputControl, now(), 120);
        createSubmission(response, numberInputControl, now(), 130);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(10d).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_YEAR, presentation.getInterval());
        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(2, segments.size());

        assertEquals(now().getYear() - 1, segments.get(0).getYear());
        assertEquals(now().getYear() - 1, segments.get(0).getPeriod());
        assertEquals(30, segments.get(0).getValue());

        assertEquals(now().getYear(), segments.get(1).getYear());
        assertEquals(now().getYear(), segments.get(1).getPeriod());
        assertEquals(250, segments.get(1).getValue());
    }

    @Test
    public void should_fetch_time_segment_submit_count_based_on_month() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(SUBMIT_COUNT_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_MONTH)
                .max(5)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        LocalDate localDate1 = now().minusMonths(5).withDayOfMonth(5);
        createSubmission(response, numberInputControl, localDate1, 10);

        LocalDate localDate2 = now().minusMonths(4).withDayOfMonth(5);
        createSubmission(response, numberInputControl, localDate2, 30);

        LocalDate localDate3 = now().minusMonths(3).withDayOfMonth(5);
        createSubmission(response, numberInputControl, localDate3, 40);
        createSubmission(response, numberInputControl, localDate3, 50);

        LocalDate localDate4 = now();
        createSubmission(response, numberInputControl, localDate4, 60);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(10d).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_MONTH, presentation.getInterval());
        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(3, segments.size());

        assertEquals(localDate2.getYear(), segments.get(0).getYear());
        assertEquals(localDate2.getMonthValue(), segments.get(0).getPeriod());
        assertEquals(1, segments.get(0).getValue());

        assertEquals(localDate3.getYear(), segments.get(1).getYear());
        assertEquals(localDate3.getMonthValue(), segments.get(1).getPeriod());
        assertEquals(2, segments.get(1).getValue());

        assertEquals(localDate4.getYear(), segments.get(2).getYear());
        assertEquals(localDate4.getMonthValue(), segments.get(2).getPeriod());
        assertEquals(1, segments.get(2).getValue());
    }

    @Test
    public void should_fetch_time_segment_submit_count_based_on_season() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(SUBMIT_COUNT_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_SEASON)
                .max(2)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        createSubmission(response, numberInputControl, now().minusMonths(11).withDayOfMonth(5), 10);
        createSubmission(response, numberInputControl, now().minusMonths(10).withDayOfMonth(5), 20);
        createSubmission(response, numberInputControl, now().minusMonths(9).withDayOfMonth(5), 30);
        createSubmission(response, numberInputControl, now().minusMonths(8).withDayOfMonth(5), 40);
        createSubmission(response, numberInputControl, now().minusMonths(7).withDayOfMonth(5), 50);
        createSubmission(response, numberInputControl, now().minusMonths(6).withDayOfMonth(5), 60);
        createSubmission(response, numberInputControl, now().minusMonths(5).withDayOfMonth(5), 70);
        createSubmission(response, numberInputControl, now().minusMonths(4).withDayOfMonth(5), 80);
        createSubmission(response, numberInputControl, now().minusMonths(3).withDayOfMonth(5), 90);
        createSubmission(response, numberInputControl, now(), 120);
        createSubmission(response, numberInputControl, now(), 130);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(10d).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_SEASON, presentation.getInterval());
        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertTrue(segments.size() >= 2);

        assertEquals(now().getYear(), segments.get(segments.size() - 1).getYear());
        assertEquals(currentSeason(), segments.get(segments.size() - 1).getPeriod());
        assertEquals(2, segments.get(segments.size() - 1).getValue());
    }

    @Test
    public void should_fetch_time_segment_submit_count_based_on_year() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(SUBMIT_COUNT_SUM)
                        .basedType(CREATED_AT)
                        .pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_YEAR)
                .max(2)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        createSubmission(response, numberInputControl, now().minusYears(1), 10);
        createSubmission(response, numberInputControl, now().minusYears(1), 20);
        createSubmission(response, numberInputControl, now().minusYears(2), 40);
        createSubmission(response, numberInputControl, now(), 120);
        createSubmission(response, numberInputControl, now(), 130);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(10d).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_YEAR, presentation.getInterval());
        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(2, segments.size());

        assertEquals(now().getYear() - 1, segments.get(0).getYear());
        assertEquals(now().getYear() - 1, segments.get(0).getPeriod());
        assertEquals(2, segments.get(0).getValue());

        assertEquals(now().getYear(), segments.get(1).getYear());
        assertEquals(now().getYear(), segments.get(1).getPeriod());
        assertEquals(2, segments.get(1).getValue());
    }

    private void createSubmission(PreparedQrResponse response, FNumberInputControl numberInputControl, LocalDate localDate, double value) {
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(value).build());
        Submission submission = submissionRepository.byId(submissionId);
        ReflectionTestUtils.setField(submission, "createdAt", localDate.atTime(5, 0).atZone(systemDefault()).toInstant());
        submissionRepository.save(submission);
    }

    @Test
    public void should_fetch_time_segment_submit_count_based_on_date_control() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        FDateControl dateControl = defaultDateControl();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(SUBMIT_COUNT_SUM)
                        .basedType(DATE_CONTROL)
                        .pageId(response.getHomePageId())
                        .basedControlId(dateControl.getId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_MONTH)
                .max(20)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, dateControl, control);

        int thisYear = now().getYear();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 1, 2).toString()).build(),
                rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 2, 2).toString()).build(),
                rAnswerBuilder(numberInputControl).number(2D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 3, 2).toString()).build(),
                rAnswerBuilder(numberInputControl).number(3D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 3, 3).toString()).build(),
                rAnswerBuilder(numberInputControl).number(4D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(1D).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_MONTH, presentation.getInterval());
        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(3, segments.size());

        assertEquals(thisYear, segments.get(0).getYear());
        assertEquals(1, segments.get(0).getPeriod());
        assertEquals(1, segments.get(0).getValue());

        assertEquals(2, segments.get(1).getPeriod());
        assertEquals(1, segments.get(1).getValue());

        assertEquals(3, segments.get(2).getPeriod());
        assertEquals(2, segments.get(2).getValue());
    }

    @Test
    public void should_fetch_time_segment_control_value_based_on_date_control() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        FDateControl dateControl = defaultDateControl();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(DATE_CONTROL)
                        .pageId(response.getHomePageId())
                        .basedControlId(dateControl.getId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_MONTH)
                .max(20)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, dateControl, control);

        int thisYear = now().getYear();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 1, 2).toString()).build(),
                rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 2, 2).toString()).build(),
                rAnswerBuilder(numberInputControl).number(2D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 3, 2).toString()).build(),
                rAnswerBuilder(numberInputControl).number(3D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 3, 3).toString()).build(),
                rAnswerBuilder(numberInputControl).number(4D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(2D).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_MONTH, presentation.getInterval());
        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(3, segments.size());

        assertEquals(thisYear, segments.get(0).getYear());
        assertEquals(1, segments.get(0).getPeriod());
        assertEquals(1, segments.get(0).getValue());

        assertEquals(2, segments.get(1).getPeriod());
        assertEquals(2, segments.get(1).getValue());

        assertEquals(3, segments.get(2).getPeriod());
        assertEquals(7, segments.get(2).getValue());
    }

    @Test
    public void should_fetch_time_segment_submit_count_based_on_date_time_control() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        FDateTimeControl dateTimeControl = defaultDateTimeControl();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(SUBMIT_COUNT_SUM)
                        .basedType(DATE_CONTROL)
                        .pageId(response.getHomePageId())
                        .basedControlId(dateTimeControl.getId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_MONTH)
                .max(20)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, dateTimeControl, control);

        int thisYear = now().getYear();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateTimeControl).date(LocalDate.of(thisYear, 1, 2).toString()).time(rTime()).build(),
                rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateTimeControl).date(LocalDate.of(thisYear, 2, 2).toString()).time(rTime()).build(),
                rAnswerBuilder(numberInputControl).number(2D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateTimeControl).date(LocalDate.of(thisYear, 3, 2).toString()).time(rTime()).build(),
                rAnswerBuilder(numberInputControl).number(3D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateTimeControl).date(LocalDate.of(thisYear, 3, 3).toString()).time(rTime()).build(),
                rAnswerBuilder(numberInputControl).number(4D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(1D).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_MONTH, presentation.getInterval());
        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(3, segments.size());

        assertEquals(thisYear, segments.get(0).getYear());
        assertEquals(1, segments.get(0).getPeriod());
        assertEquals(1, segments.get(0).getValue());

        assertEquals(2, segments.get(1).getPeriod());
        assertEquals(1, segments.get(1).getValue());

        assertEquals(3, segments.get(2).getPeriod());
        assertEquals(2, segments.get(2).getValue());
    }

    @Test
    public void should_fetch_time_segment_control_value_based_on_date_time_control() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        FDateTimeControl dateTimeControl = defaultDateTimeControl();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(CONTROL_VALUE_SUM)
                        .basedType(DATE_CONTROL)
                        .pageId(response.getHomePageId())
                        .basedControlId(dateTimeControl.getId())
                        .targetControlId(numberInputControl.getId())
                        .build()))
                .interval(PER_MONTH)
                .max(20)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, dateTimeControl, control);

        int thisYear = now().getYear();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateTimeControl).date(LocalDate.of(thisYear, 1, 2).toString()).time(rTime()).build(),
                rAnswerBuilder(numberInputControl).number(1D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateTimeControl).date(LocalDate.of(thisYear, 2, 2).toString()).time(rTime()).build(),
                rAnswerBuilder(numberInputControl).number(2D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateTimeControl).date(LocalDate.of(thisYear, 3, 2).toString()).time(rTime()).build(),
                rAnswerBuilder(numberInputControl).number(3D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateTimeControl).date(LocalDate.of(thisYear, 3, 3).toString()).time(rTime()).build(),
                rAnswerBuilder(numberInputControl).number(4D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(2D).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_MONTH, presentation.getInterval());
        List<TimeSegment> segments = presentation.getSegmentsData().get(0);
        assertEquals(3, segments.size());

        assertEquals(thisYear, segments.get(0).getYear());
        assertEquals(1, segments.get(0).getPeriod());
        assertEquals(1, segments.get(0).getValue());

        assertEquals(2, segments.get(1).getPeriod());
        assertEquals(2, segments.get(1).getValue());

        assertEquals(3, segments.get(2).getPeriod());
        assertEquals(7, segments.get(2).getValue());
    }

    @Test
    public void should_fetch_multiple_time_segment_control_values() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl1 = defaultNumberInputControlBuilder().precision(3).build();
        FNumberInputControl numberInputControl2 = defaultNumberInputControlBuilder().precision(3).build();
        FDateControl dateControl = defaultDateControl();
        PTimeSegmentControl control = defaultTimeSegmentControlBuilder()
                .segmentSettings(List.of(
                        PTimeSegmentControl.TimeSegmentSetting.builder()
                                .id(newShortUuid())
                                .name("未命名统计项")
                                .segmentType(CONTROL_VALUE_SUM)
                                .basedType(DATE_CONTROL)
                                .pageId(response.getHomePageId())
                                .basedControlId(dateControl.getId())
                                .targetControlId(numberInputControl1.getId())
                                .build(),
                        PTimeSegmentControl.TimeSegmentSetting.builder()
                                .id(newShortUuid())
                                .name("未命名统计项")
                                .segmentType(CONTROL_VALUE_MAX)
                                .basedType(DATE_CONTROL)
                                .pageId(response.getHomePageId())
                                .basedControlId(dateControl.getId())
                                .targetControlId(numberInputControl2.getId())
                                .build()))
                .interval(PER_MONTH)
                .max(20)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl1, numberInputControl2, dateControl, control);

        int thisYear = now().getYear();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 1, 2).toString()).build(),
                rAnswerBuilder(numberInputControl1).number(1D).build(), rAnswerBuilder(numberInputControl2).number(1D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 2, 2).toString()).build(),
                rAnswerBuilder(numberInputControl1).number(2D).build(), rAnswerBuilder(numberInputControl2).number(2D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 3, 2).toString()).build(),
                rAnswerBuilder(numberInputControl1).number(3D).build(), rAnswerBuilder(numberInputControl2).number(3D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(dateControl).date(LocalDate.of(thisYear, 3, 3).toString()).build(),
                rAnswerBuilder(numberInputControl1).number(4D).build(), rAnswerBuilder(numberInputControl2).number(4D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl1).number(2D).build());

        QTimeSegmentPresentation presentation = (QTimeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(PER_MONTH, presentation.getInterval());
        List<TimeSegment> segments1 = presentation.getSegmentsData().get(0);
        assertEquals(3, segments1.size());

        assertEquals(thisYear, segments1.get(0).getYear());
        assertEquals(1, segments1.get(0).getPeriod());
        assertEquals(1, segments1.get(0).getValue());

        assertEquals(2, segments1.get(1).getPeriod());
        assertEquals(2, segments1.get(1).getValue());

        assertEquals(3, segments1.get(2).getPeriod());
        assertEquals(7, segments1.get(2).getValue());

        List<TimeSegment> segments2 = presentation.getSegmentsData().get(1);
        assertEquals(3, segments2.size());

        assertEquals(thisYear, segments2.get(0).getYear());
        assertEquals(1, segments2.get(0).getPeriod());
        assertEquals(1, segments2.get(0).getValue());

        assertEquals(2, segments2.get(1).getPeriod());
        assertEquals(2, segments2.get(1).getValue());

        assertEquals(3, segments2.get(2).getPeriod());
        assertEquals(4, segments2.get(2).getValue());
    }
}
