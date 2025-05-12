package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.trend.QTrendDataSet;
import com.mryqr.core.presentation.query.trend.QTrendPresentation;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.domain.report.SubmissionReportTimeBasedType.CREATED_AT;
import static com.mryqr.common.domain.report.SubmissionReportTimeBasedType.DATE_CONTROL;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.*;

public class TrendControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTrendControl control = defaultTrendControlBuilder().trendItems(newArrayList(
                TrendItem.builder().id(newShortUuid()).name(rTrendItemName()).basedType(CREATED_AT).pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId()).build())).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_page_for_trend_item() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTrendControl control = defaultTrendControlBuilder().trendItems(newArrayList(
                TrendItem.builder().id(newShortUuid()).name(rTrendItemName()).basedType(CREATED_AT).targetControlId(numberInputControl.getId())
                        .build())).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_control_for_trend_item() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTrendControl control = defaultTrendControlBuilder().trendItems(newArrayList(
                        TrendItem.builder().id(newShortUuid()).name(rTrendItemName()).basedType(CREATED_AT).pageId(response.getHomePageId()).build()))
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_trend_items() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTrendControl control = defaultTrendControlBuilder().trendItems(newArrayList()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_if_no_based_control() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTrendControl control = defaultTrendControlBuilder().trendItems(newArrayList(
                TrendItem.builder().id(newShortUuid()).name(rTrendItemName()).basedType(DATE_CONTROL).pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId()).build())).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_fail_create_control_if_referenced_page_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTrendControl control = defaultTrendControlBuilder().trendItems(newArrayList(
                TrendItem.builder().id(newShortUuid()).name(rTrendItemName()).basedType(CREATED_AT).pageId(Page.newPageId())
                        .targetControlId(numberInputControl.getId()).build())).build();

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
        PTrendControl control = defaultTrendControlBuilder().trendItems(newArrayList(
                TrendItem.builder().id(newShortUuid()).name(rTrendItemName()).basedType(CREATED_AT).pageId(response.getHomePageId())
                        .targetControlId(Control.newControlId()).build())).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_control_not_support_trend() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), singleLineTextControl);
        PTrendControl control = defaultTrendControlBuilder().trendItems(newArrayList(
                TrendItem.builder().id(newShortUuid()).name(rTrendItemName()).basedType(CREATED_AT).pageId(response.getHomePageId())
                        .targetControlId(singleLineTextControl.getId()).build())).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                NOT_SUPPORTED_TARGET_CONTROL_FOR_TREND);
    }

    @Test
    public void should_fail_create_control_if_trend_item_id_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), singleLineTextControl);
        String itemId = newShortUuid();
        PTrendControl control = defaultTrendControlBuilder()
                .trendItems(newArrayList(
                        TrendItem.builder().id(itemId).name(rTrendItemName()).basedType(CREATED_AT).pageId(response.getHomePageId())
                                .targetControlId(singleLineTextControl.getId()).build(),
                        TrendItem.builder().id(itemId).name(rTrendItemName()).basedType(CREATED_AT).pageId(response.getHomePageId())
                                .targetControlId(singleLineTextControl.getId()).build()))
                .build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                TREND_ITEM_ID_DUPLICATED);
    }

    @Test
    public void should_fail_create_control_if_based_control_not_exists() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PTrendControl control = defaultTrendControlBuilder().trendItems(newArrayList(
                TrendItem.builder().id(newShortUuid()).name(rTrendItemName()).basedType(DATE_CONTROL).pageId(response.getHomePageId())
                        .basedControlId(Control.newControlId()).targetControlId(numberInputControl.getId()).build())).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_based_control_not_date_control() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(3).build();
        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), targetControl, basedControl);
        PTrendControl control = defaultTrendControlBuilder().trendItems(newArrayList(
                TrendItem.builder().id(newShortUuid()).name(rTrendItemName()).basedType(DATE_CONTROL).pageId(response.getHomePageId())
                        .basedControlId(basedControl.getId()).targetControlId(targetControl.getId()).build())).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), CONTROL_NOT_DATE);
    }

    @Test
    public void should_fetch_trend_values_for_created_at() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PTrendControl control = defaultTrendControlBuilder()
                .trendItems(newArrayList(TrendItem.builder().id(newShortUuid()).basedType(CREATED_AT).pageId(response.getHomePageId())
                        .targetControlId(numberInputControl.getId()).name("trendName").build()))
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(3d).build());
        Submission submission = submissionRepository.byId(submissionId);
        ReflectionTestUtils.setField(submission, "createdAt", submission.getCreatedAt().plus(1, DAYS));
        submissionRepository.save(submission);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(4d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(5d).build());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(6d).build());

        QTrendPresentation presentation = (QTrendPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(),
                response.getHomePageId(), control.getId());
        QTrendDataSet dataSet = presentation.getDataSets().get(0);
        assertEquals(2, dataSet.getRecords().size());
        assertEquals("trendName", dataSet.getLabel());
        assertEquals(5, dataSet.getRecords().get(0).getNumber());
        assertEquals(LocalDate.now().toString(), dataSet.getRecords().get(0).getDate());
        assertEquals(3, dataSet.getRecords().get(1).getNumber());
    }

    @Test
    public void should_fetch_trend_values_for_date_control() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(3).build();
        FDateControl basedControl = defaultDateControl();

        PTrendControl control = defaultTrendControlBuilder()
                .trendItems(newArrayList(TrendItem.builder().id(newShortUuid()).basedType(DATE_CONTROL).pageId(response.getHomePageId())
                        .basedControlId(basedControl.getId()).targetControlId(targetControl.getId()).name("trendName").build()))
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), targetControl, basedControl, control);

        LocalDate now = LocalDate.now();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).date(now.minusDays(2).toString()).build(), rAnswerBuilder(targetControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).date(now.minusDays(1).toString()).build(), rAnswerBuilder(targetControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).date(now.toString()).build(), rAnswerBuilder(targetControl).number(1d).build());

        QTrendPresentation presentation = (QTrendPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(),
                response.getHomePageId(), control.getId());
        QTrendDataSet dataSet = presentation.getDataSets().get(0);
        assertEquals(3, dataSet.getRecords().size());
        assertEquals(3, dataSet.getRecords().get(0).getNumber());
        assertEquals(now.minusDays(2).toString(), dataSet.getRecords().get(0).getDate());

        assertEquals(2, dataSet.getRecords().get(1).getNumber());
        assertEquals(now.minusDays(1).toString(), dataSet.getRecords().get(1).getDate());

        assertEquals(1, dataSet.getRecords().get(2).getNumber());
        assertEquals(now.toString(), dataSet.getRecords().get(2).getDate());
    }
}
