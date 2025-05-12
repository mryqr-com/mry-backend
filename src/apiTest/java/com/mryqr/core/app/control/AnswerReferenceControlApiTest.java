package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.display.TextDisplayValue;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.answerreference.QAnswerReferencePresentation;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.*;

public class AnswerReferenceControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        PAnswerReferenceControl control = defaultAnswerReferenceControlBuilder().pageId(response.getHomePageId())
                .controlId(checkboxControl.getId()).build();
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
        PAnswerReferenceControl control = defaultAnswerReferenceControlBuilder().controlId(checkboxControl.getId()).build();
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
        PAnswerReferenceControl control = defaultAnswerReferenceControlBuilder().pageId(response.getHomePageId()).build();
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
        PAnswerReferenceControl control = defaultAnswerReferenceControlBuilder().pageId(Page.newPageId()).controlId(checkboxControl.getId())
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

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl);
        PAnswerReferenceControl control = defaultAnswerReferenceControlBuilder().pageId(response.getHomePageId())
                .controlId(Control.newControlId()).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_control_not_referencable() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PSectionTitleViewControl sectionTitleControl = defaultSectionTitleControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), sectionTitleControl);
        PAnswerReferenceControl control = defaultAnswerReferenceControlBuilder().pageId(response.getHomePageId())
                .controlId(sectionTitleControl.getId()).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                CONTROL_NOT_SUPPORT_REFERENCE);
    }

    @Test
    public void should_fetch_answer_reference_presentation_value() {
        PreparedQrResponse qrResponse = setupApi.registerWithQr();

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        PAnswerReferenceControl referenceControl = defaultAnswerReferenceControlBuilder().pageId(qrResponse.getHomePageId())
                .controlId(singleLineTextControl.getId()).build();
        AppApi.updateAppControls(qrResponse.getJwt(), qrResponse.getAppId(), singleLineTextControl, referenceControl);
        //first submission, will not be targeted
        SubmissionApi.newSubmission(qrResponse.getJwt(), qrResponse.getQrId(), qrResponse.getHomePageId(), rAnswer(singleLineTextControl));

        SingleLineTextAnswer singleLineTextAnswer = rAnswer(singleLineTextControl);
        SubmissionApi.newSubmission(qrResponse.getJwt(), qrResponse.getQrId(), qrResponse.getHomePageId(), singleLineTextAnswer);
        QAnswerReferencePresentation presentation = (QAnswerReferencePresentation) PresentationApi.fetchPresentation(qrResponse.getJwt(),
                qrResponse.getQrId(), qrResponse.getHomePageId(), referenceControl.getId());

        TextDisplayValue value = (TextDisplayValue) presentation.getValue();
        assertEquals(singleLineTextAnswer.getContent(), value.getText());
    }
}
