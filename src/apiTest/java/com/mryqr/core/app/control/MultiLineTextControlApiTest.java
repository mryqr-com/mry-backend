package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FMultiLineTextControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.MultiLineTextAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.multilinetext.MultiLineTextAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.ui.MinMaxSetting.minMaxOf;
import static com.mryqr.core.common.exception.ErrorCode.MANDATORY_ANSWER_REQUIRED;
import static com.mryqr.core.common.exception.ErrorCode.MAX_OVERFLOW;
import static com.mryqr.core.common.exception.ErrorCode.MIN_OVERFLOW;
import static com.mryqr.core.common.exception.ErrorCode.MULTI_LINE_MAX_CONTENT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.MULTI_LINE_MIN_CONTENT_NOT_REACHED;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.defaultFillableSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultMultiLineTextControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultMultipleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static com.mryqr.utils.RandomTestFixture.rSentence;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MultiLineTextControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMultiLineTextControl control = defaultMultipleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_fail_create_control_if_required_length_exceeds_50000() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMultiLineTextControl control = defaultMultiLineTextControlBuilder().minMaxSetting(minMaxOf(1, 50001)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), MAX_OVERFLOW);
    }

    @Test
    public void should_fail_create_control_if_required_length_less_than_0() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMultiLineTextControl control = defaultMultiLineTextControlBuilder().minMaxSetting(minMaxOf(-1, 100)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), MIN_OVERFLOW);
    }


    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLineTextControl control = defaultMultipleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MultiLineTextAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        MultiLineTextAnswer updatedAnswer = (MultiLineTextAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        assertNull(submission.getIndexedValues());
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLineTextControl control = defaultMultiLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MultiLineTextAnswer answer = rAnswerBuilder(control).content(null).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_fail_answer_if_content_exceeds_max_length() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLineTextControl control = defaultMultiLineTextControlBuilder().minMaxSetting(minMaxOf(0, 10)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MultiLineTextAnswer answer = rAnswerBuilder(control).content(rSentence(100)).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MULTI_LINE_MAX_CONTENT_REACHED);
    }


    @Test
    public void should_fail_answer_if_content_less_than_min_length() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLineTextControl control = defaultMultiLineTextControlBuilder().minMaxSetting(minMaxOf(100, 1000)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MultiLineTextAnswer answer = rAnswerBuilder(control).content(rSentence(50)).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MULTI_LINE_MIN_CONTENT_NOT_REACHED);
    }


    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLineTextControl control = defaultMultipleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        MultiLineTextAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QR qr = qrRepository.byId(response.getQrId());
        MultiLineTextAttributeValue attributeValue = (MultiLineTextAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getContent(), attributeValue.getContent());
        assertNull(qr.getIndexedValues());
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLineTextControl control = defaultMultipleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        MultiLineTextAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        QR qr = qrRepository.byId(response.getQrId());
        MultiLineTextAttributeValue attributeValue = (MultiLineTextAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getContent(), attributeValue.getContent());
        assertNull(qr.getIndexedValues());
    }

}
