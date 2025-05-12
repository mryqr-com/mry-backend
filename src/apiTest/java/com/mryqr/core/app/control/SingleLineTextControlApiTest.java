package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.TextAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
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
import static com.mryqr.core.common.exception.ErrorCode.SINGLE_LINE_MAX_CONTENT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.SINGLE_LINE_MIN_CONTENT_NOT_REACHED;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.defaultFillableSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControlBuilder;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static com.mryqr.utils.RandomTestFixture.rSentence;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SingleLineTextControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_fail_create_control_if_required_length_exceeds_100() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().minMaxSetting(minMaxOf(1, 101)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), MAX_OVERFLOW);
    }

    @Test
    public void should_fail_create_control_if_required_length_less_than_0() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().minMaxSetting(minMaxOf(-1, 10)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), MIN_OVERFLOW);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        SingleLineTextAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        SingleLineTextAnswer updatedAnswer = (SingleLineTextAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        assertNull(submission.getIndexedValues());
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        SingleLineTextAnswer answer = rAnswerBuilder(control).content(null).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_fail_answer_if_content_exceeds_max_length() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().minMaxSetting(minMaxOf(0, 10)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        SingleLineTextAnswer answer = rAnswerBuilder(control).content(rSentence(100)).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), SINGLE_LINE_MAX_CONTENT_REACHED);
    }

    @Test
    public void should_fail_answer_if_content_less_than_min_length() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().minMaxSetting(minMaxOf(20, 50)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        SingleLineTextAnswer answer = rAnswerBuilder(control).content(rSentence(10)).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), SINGLE_LINE_MIN_CONTENT_NOT_REACHED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        SingleLineTextAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QR qr = qrRepository.byId(response.getQrId());
        TextAttributeValue attributeValue = (TextAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getContent(), attributeValue.getText());
        assertNull(qr.getIndexedValues());
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        SingleLineTextAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        QR qr = qrRepository.byId(response.getQrId());
        TextAttributeValue attributeValue = (TextAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getContent(), attributeValue.getText());
        assertNull(qr.getIndexedValues());
    }

}
