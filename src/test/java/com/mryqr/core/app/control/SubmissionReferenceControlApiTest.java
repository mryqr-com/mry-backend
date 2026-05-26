package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.display.TextDisplayValue;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.control.PSubmissionReferenceControl;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.submissionreference.QSubmissionReferencePresentation;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.*;

public class SubmissionReferenceControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PSubmissionReferenceControl control = defaultSubmissionReferenceControlBuilder().pageId(response.homePageId()).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_if_no_page() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PSubmissionReferenceControl control = defaultSubmissionReferenceControlBuilder().pageId(null).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_fail_create_control_if_referenced_page_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PSubmissionReferenceControl control = defaultSubmissionReferenceControlBuilder().pageId(Page.newPageId()).build();
        App app = appRepository.byId(response.appId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.jwt(), response.appId(), app.getVersion(), setting),
                VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fetch_submission_reference_presentation_value() {
        PreparedQrResponse qrResponse = setupApi.registerWithQr();

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        PSubmissionReferenceControl referenceControl = defaultSubmissionReferenceControlBuilder().pageId(qrResponse.homePageId()).build();
        AppApi.updateAppControls(qrResponse.jwt(), qrResponse.appId(), singleLineTextControl, referenceControl);
        //first submission, will not be targeted
        SubmissionApi.newSubmission(qrResponse.jwt(), qrResponse.qrId(), qrResponse.homePageId(), rAnswer(singleLineTextControl));

        SingleLineTextAnswer singleLineTextAnswer = rAnswer(singleLineTextControl);
        SubmissionApi.newSubmission(qrResponse.jwt(), qrResponse.qrId(), qrResponse.homePageId(), singleLineTextAnswer);
        QSubmissionReferencePresentation presentation = (QSubmissionReferencePresentation) PresentationApi.fetchPresentation(
                qrResponse.jwt(), qrResponse.qrId(), qrResponse.homePageId(), referenceControl.getId());

        TextDisplayValue value = (TextDisplayValue) presentation.getValues().get(singleLineTextControl.getId());
        assertEquals(singleLineTextAnswer.getContent(), value.getText());
    }
}
