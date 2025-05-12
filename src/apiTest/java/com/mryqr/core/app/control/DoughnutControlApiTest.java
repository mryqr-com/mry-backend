package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.report.ReportRange;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.doughnut.QDoughnutPresentation;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.common.domain.report.SubmissionSegmentType.CONTROL_VALUE_SUM;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.*;

public class DoughnutControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        PDoughnutControl control = defaultDoughnutControlBuilder().pageId(response.getHomePageId()).basedControlId(checkboxControl.getId())
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl, control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_page() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PDoughnutControl control = defaultDoughnutControlBuilder().basedControlId(checkboxControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_control() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PDoughnutControl control = defaultDoughnutControlBuilder().pageId(response.getHomePageId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_value_control() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PDoughnutControl control = defaultDoughnutControlBuilder().segmentType(CONTROL_VALUE_SUM).pageId(response.getHomePageId())
                .basedControlId(checkboxControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_fail_create_control_if_referenced_page_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PDoughnutControl control = defaultDoughnutControlBuilder().pageId(Page.newPageId()).basedControlId(checkboxControl.getId()).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_control_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PDoughnutControl control = defaultDoughnutControlBuilder().pageId(response.getHomePageId()).basedControlId(Control.newControlId())
                .build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_value_control_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PDoughnutControl control = defaultDoughnutControlBuilder().segmentType(CONTROL_VALUE_SUM).pageId(response.getHomePageId())
                .basedControlId(checkboxControl.getId()).targetControlId(Control.newControlId()).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_value_control_is_not_number() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl, singleLineTextControl);
        PDoughnutControl control = defaultDoughnutControlBuilder().segmentType(CONTROL_VALUE_SUM).pageId(response.getHomePageId())
                .basedControlId(checkboxControl.getId()).targetControlId(singleLineTextControl.getId()).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                NOT_SUPPORTED_TARGET_CONTROL_FOR_DOUGHNUT);
    }

    @Test
    public void should_fail_create_control_if_referenced_control_not_support() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), singleLineTextControl);
        PDoughnutControl control = defaultDoughnutControlBuilder().pageId(response.getHomePageId())
                .basedControlId(singleLineTextControl.getId()).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                NOT_SUPPORTED_BASED_CONTROL_FOR_DOUGHNUT);
    }

    @Test
    public void should_fetch_presentation_values_for_submit_count() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FRadioControl radioControl = defaultRadioControlBuilder().options(rTextOptions(10)).build();
        PDoughnutControl control = defaultDoughnutControlBuilder()
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .range(ReportRange.NO_LIMIT)
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, control);

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

        QDoughnutPresentation values = (QDoughnutPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(),
                response.getHomePageId(), control.getId());

        assertEquals(5,
                values.getSegments().stream().filter(count -> count.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertEquals(3,
                values.getSegments().stream().filter(count -> count.getOption().equals(radioAnswer2.getOptionId())).findFirst().get().getValue());
        assertEquals(2,
                values.getSegments().stream().filter(count -> count.getOption().equals(radioAnswer3.getOptionId())).findFirst().get().getValue());
    }

    @Test
    public void should_fetch_presentation_values_for_control_sum() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FRadioControl radioControl = defaultRadioControlBuilder().options(rTextOptions(10)).build();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PDoughnutControl control = defaultDoughnutControlBuilder()
                .segmentType(CONTROL_VALUE_SUM)
                .pageId(response.getHomePageId())
                .basedControlId(radioControl.getId())
                .targetControlId(numberInputControl.getId())
                .range(ReportRange.NO_LIMIT)
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, control, numberInputControl);

        RadioAnswer radioAnswer1 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1,
                rAnswerBuilder(numberInputControl).number(1d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1,
                rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1,
                rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1,
                rAnswerBuilder(numberInputControl).number(4d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer1,
                rAnswerBuilder(numberInputControl).number(5d).build());

        RadioAnswer radioAnswer2 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2,
                rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer2,
                rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(4d).build());

        RadioAnswer radioAnswer3 = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(2).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3,
                rAnswerBuilder(numberInputControl).number(10d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer3);

        //another qr should not be counted
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), radioAnswer1,
                rAnswerBuilder(numberInputControl).number(1d).build());

        QDoughnutPresentation values = (QDoughnutPresentation) PresentationApi.fetchPresentation(response.getJwt(), response.getQrId(),
                response.getHomePageId(), control.getId());

        assertEquals(15,
                values.getSegments().stream().filter(count -> count.getOption().equals(radioAnswer1.getOptionId())).findFirst().get().getValue());
        assertEquals(5,
                values.getSegments().stream().filter(count -> count.getOption().equals(radioAnswer2.getOptionId())).findFirst().get().getValue());
        assertEquals(10,
                values.getSegments().stream().filter(count -> count.getOption().equals(radioAnswer3.getOptionId())).findFirst().get().getValue());
    }
}
