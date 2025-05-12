package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.control.PBarControl;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.bar.QBarPresentation;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static com.mryqr.core.app.domain.page.control.Control.newControlId;
import static com.mryqr.core.common.domain.report.ReportRange.LAST_30_DAYS;
import static com.mryqr.core.common.domain.report.ReportRange.LAST_7_DAYS;
import static com.mryqr.core.common.domain.report.ReportRange.LAST_90_DAYS;
import static com.mryqr.core.common.domain.report.ReportRange.LAST_HALF_YEAR;
import static com.mryqr.core.common.domain.report.ReportRange.LAST_ONE_YEAR;
import static com.mryqr.core.common.domain.report.ReportRange.NO_LIMIT;
import static com.mryqr.core.common.domain.report.ReportRange.THIS_MONTH;
import static com.mryqr.core.common.domain.report.ReportRange.THIS_SEASON;
import static com.mryqr.core.common.domain.report.ReportRange.THIS_WEEK;
import static com.mryqr.core.common.domain.report.ReportRange.THIS_YEAR;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.CONTROL_VALUE_AVG;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.CONTROL_VALUE_MAX;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.CONTROL_VALUE_MIN;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.CONTROL_VALUE_SUM;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.core.common.exception.ErrorCode.NOT_SUPPORTED_BASED_CONTROL_BAR;
import static com.mryqr.core.common.exception.ErrorCode.NOT_SUPPORTED_TARGET_CONTROL_FOR_BAR;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_CONTROL_NOT_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.utils.RandomTestFixture.defaultBarControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControl;
import static com.mryqr.utils.RandomTestFixture.defaultNumberInputControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControl;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rTextOptions;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BarControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        PBarControl control = defaultBarControlBuilder().pageId(response.getHomePageId()).basedControlId(checkboxControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl, control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_page() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PBarControl control = defaultBarControlBuilder().basedControlId(checkboxControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_control() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PBarControl control = defaultBarControlBuilder().pageId(response.getHomePageId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_value_control() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PBarControl control = defaultBarControlBuilder().segmentType(CONTROL_VALUE_SUM).pageId(response.getHomePageId()).basedControlId(checkboxControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }


    @Test
    public void should_fail_create_control_if_referenced_page_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PBarControl control = defaultBarControlBuilder().pageId(Page.newPageId()).basedControlId(checkboxControl.getId()).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_control_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PBarControl control = defaultBarControlBuilder().pageId(response.getHomePageId()).basedControlId(newControlId()).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), VALIDATION_CONTROL_NOT_EXIST);
    }


    @Test
    public void should_fail_create_control_if_referenced_value_control_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PBarControl control = defaultBarControlBuilder().segmentType(CONTROL_VALUE_SUM).pageId(response.getHomePageId()).basedControlId(checkboxControl.getId()).targetControlIds(List.of(newControlId())).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), VALIDATION_CONTROL_NOT_EXIST);
    }


    @Test
    public void should_fail_create_control_if_referenced_value_control_is_not_number() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl, singleLineTextControl);
        PBarControl control = defaultBarControlBuilder().segmentType(CONTROL_VALUE_SUM).pageId(response.getHomePageId()).basedControlId(checkboxControl.getId()).targetControlIds(List.of(singleLineTextControl.getId())).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), NOT_SUPPORTED_TARGET_CONTROL_FOR_BAR);
    }


    @Test
    public void should_fail_create_control_if_referenced_control_not_support_bar() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), singleLineTextControl);
        PBarControl control = defaultBarControlBuilder().pageId(response.getHomePageId()).basedControlId(singleLineTextControl.getId()).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), NOT_SUPPORTED_BASED_CONTROL_BAR);
    }


    @Test
    public void should_fetch_bar_values_for_submit_count() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControlBuilder().options(rTextOptions(10)).build();
        PBarControl barControl = defaultBarControlBuilder()
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of())
                .range(NO_LIMIT)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);

        RadioAnswer radioAnswer3 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(2).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3);

        //another qr should not be counted
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), radioAnswer1);

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(5, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertEquals(3, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())).findFirst().get().getValue());
        assertEquals(2, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer3.getOptionId())).findFirst().get().getValue());
    }


    @Test
    public void should_fetch_bar_values_for_control_sum() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControlBuilder().options(rTextOptions(10)).build();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(CONTROL_VALUE_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of(numberInputControl.getId()))
                .range(NO_LIMIT)
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl, numberInputControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(1d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(4d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(5d).build());

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2, rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2, rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(numberInputControl).number(4d).build());

        RadioAnswer radioAnswer3 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(2).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3, rAnswerBuilder(numberInputControl).number(10d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3);

        //another qr should not be counted
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(1d).build());

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(15, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertEquals(5, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())).findFirst().get().getValue());
        assertEquals(10, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer3.getOptionId())).findFirst().get().getValue());
    }


    @Test
    public void should_fetch_bar_values_for_control_average() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControlBuilder().options(rTextOptions(10)).build();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(CONTROL_VALUE_AVG)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of(numberInputControl.getId()))
                .range(NO_LIMIT)
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl, numberInputControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(1d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(4d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(5d).build());

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2, rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2, rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(numberInputControl).number(4d).build());

        RadioAnswer radioAnswer3 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(2).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3, rAnswerBuilder(numberInputControl).number(10d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3);

        //another qr should not be counted
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(1d).build());

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(3, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertEquals(2.5, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())).findFirst().get().getValue());
        assertEquals(10, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer3.getOptionId())).findFirst().get().getValue());
    }


    @Test
    public void should_fetch_bar_values_for_control_max() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControlBuilder().options(rTextOptions(10)).build();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(CONTROL_VALUE_MAX)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of(numberInputControl.getId()))
                .range(NO_LIMIT)
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl, numberInputControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(1d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(4d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(5d).build());

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2, rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2, rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(numberInputControl).number(4d).build());

        RadioAnswer radioAnswer3 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(2).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3, rAnswerBuilder(numberInputControl).number(10d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3);

        //another qr should not be counted
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(1d).build());

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(5, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertEquals(3, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())).findFirst().get().getValue());
        assertEquals(10, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer3.getOptionId())).findFirst().get().getValue());
    }


    @Test
    public void should_fetch_bar_values_for_control_min() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControlBuilder().options(rTextOptions(10)).build();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(CONTROL_VALUE_MIN)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of(numberInputControl.getId()))
                .range(NO_LIMIT)
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl, numberInputControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(1d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(4d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(5d).build());

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2, rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2, rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(numberInputControl).number(4d).build());

        RadioAnswer radioAnswer3 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(2).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3, rAnswerBuilder(numberInputControl).number(10d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3);

        //another qr should not be counted
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl).number(1d).build());

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(1, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertEquals(2, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())).findFirst().get().getValue());
        assertEquals(10, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer3.getOptionId())).findFirst().get().getValue());
    }

    @Test
    public void should_fetch_bar_values_for_this_week() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of())
                .range(THIS_WEEK)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);

        Submission submission = submissionRepository.byId(submissionId2);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(8, DAYS));
        submissionRepository.save(submission);

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(1, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertFalse(barValues.getSegmentsData().get(0).stream().anyMatch(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())));
    }

    @Test
    public void should_fetch_bar_values_for_this_month() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of())
                .range(THIS_MONTH)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);

        Submission submission = submissionRepository.byId(submissionId2);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(32, DAYS));
        submissionRepository.save(submission);

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(1, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertFalse(barValues.getSegmentsData().get(0).stream().anyMatch(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())));
    }

    @Test
    public void should_fetch_bar_values_for_this_season() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of())
                .range(THIS_SEASON)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);

        Submission submission = submissionRepository.byId(submissionId2);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(100, DAYS));
        submissionRepository.save(submission);

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(1, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertFalse(barValues.getSegmentsData().get(0).stream().anyMatch(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())));
    }

    @Test
    public void should_fetch_bar_values_for_this_year() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of())
                .range(THIS_YEAR)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);

        Submission submission = submissionRepository.byId(submissionId2);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(370, DAYS));
        submissionRepository.save(submission);

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(1, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertFalse(barValues.getSegmentsData().get(0).stream().anyMatch(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())));
    }

    @Test
    public void should_fetch_bar_values_for_last_7_days() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of())
                .range(LAST_7_DAYS)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);

        Submission submission = submissionRepository.byId(submissionId2);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(8, DAYS));
        submissionRepository.save(submission);

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(1, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertFalse(barValues.getSegmentsData().get(0).stream().anyMatch(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())));
    }

    @Test
    public void should_fetch_bar_values_for_last_30_days() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of())
                .range(LAST_30_DAYS)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);

        Submission submission = submissionRepository.byId(submissionId2);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(31, DAYS));
        submissionRepository.save(submission);

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(1, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertFalse(barValues.getSegmentsData().get(0).stream().anyMatch(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())));
    }

    @Test
    public void should_fetch_bar_values_for_last_90_days() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of())
                .range(LAST_90_DAYS)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);

        Submission submission = submissionRepository.byId(submissionId2);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(91, DAYS));
        submissionRepository.save(submission);

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(1, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertFalse(barValues.getSegmentsData().get(0).stream().anyMatch(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())));
    }


    @Test
    public void should_fetch_bar_values_for_last_half_year() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of())
                .range(LAST_HALF_YEAR)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);

        Submission submission = submissionRepository.byId(submissionId2);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(200, DAYS));
        submissionRepository.save(submission);

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(1, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertFalse(barValues.getSegmentsData().get(0).stream().anyMatch(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())));
    }


    @Test
    public void should_fetch_bar_values_for_last_one_year() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of())
                .range(LAST_ONE_YEAR)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1);

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2);

        Submission submission = submissionRepository.byId(submissionId2);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minus(370, DAYS));
        submissionRepository.save(submission);

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(1, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertFalse(barValues.getSegmentsData().get(0).stream().anyMatch(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())));
    }

    @Test
    public void should_fetch_multiple_bar_values_() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControlBuilder().options(rTextOptions(10)).build();
        FNumberInputControl numberInputControl1 = defaultNumberInputControlBuilder().precision(3).build();
        FNumberInputControl numberInputControl2 = defaultNumberInputControlBuilder().precision(3).build();
        PBarControl barControl = defaultBarControlBuilder()
                .segmentType(CONTROL_VALUE_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlIds(List.of(numberInputControl1.getId(), numberInputControl2.getId()))
                .range(NO_LIMIT)
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, barControl, numberInputControl1, numberInputControl2);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl1).number(1d).build(), rAnswerBuilder(numberInputControl2).number(10d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl1).number(2d).build(), rAnswerBuilder(numberInputControl2).number(20d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl1).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl1).number(4d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl1).number(5d).build());

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2, rAnswerBuilder(numberInputControl1).number(2d).build(), rAnswerBuilder(numberInputControl2).number(30d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2, rAnswerBuilder(numberInputControl1).number(3d).build(), rAnswerBuilder(numberInputControl2).number(40d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(numberInputControl1).number(4d).build());

        RadioAnswer radioAnswer3 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(2).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3, rAnswerBuilder(numberInputControl1).number(10d).build(), rAnswerBuilder(numberInputControl2).number(50d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3);

        //another qr should not be counted
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), radioAnswer1, rAnswerBuilder(numberInputControl1).number(1d).build());

        QBarPresentation barValues = (QBarPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(), response.getHomePageId(), barControl.getId());

        assertEquals(15, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertEquals(5, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())).findFirst().get().getValue());
        assertEquals(10, barValues.getSegmentsData().get(0).stream().filter(barCount -> barCount.getOption().equals(radioAnswer3.getOptionId())).findFirst().get().getValue());

        assertEquals(30, barValues.getSegmentsData().get(1).stream().filter(barCount -> barCount.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertEquals(70, barValues.getSegmentsData().get(1).stream().filter(barCount -> barCount.getOption().equals(radioAnswer2.getOptionId())).findFirst().get().getValue());
        assertEquals(50, barValues.getSegmentsData().get(1).stream().filter(barCount -> barCount.getOption().equals(radioAnswer3.getOptionId())).findFirst().get().getValue());
    }

}
