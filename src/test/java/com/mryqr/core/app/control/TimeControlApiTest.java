package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FTimeControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.LocalTimeAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import com.mryqr.utils.RandomTestFixture;
import org.junit.jupiter.api.Test;

import static com.mryqr.common.exception.ErrorCode.MANDATORY_ANSWER_REQUIRED;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TimeControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FTimeControl control = defaultTimeControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FTimeControl control = defaultTimeControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        TimeAnswer answer = RandomTestFixture.rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        TimeAnswer updatedAnswer = (TimeAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        assertNull(submission.getIndexedValues());
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FTimeControl control = defaultTimeControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        TimeAnswer answer = rAnswerBuilder(control).time(null).build();
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FTimeControl control = defaultTimeControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        TimeAnswer answer = RandomTestFixture.rAnswer(control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), RandomTestFixture.rAnswer(control));

        QR qr = qrRepository.byId(response.qrId());
        LocalTimeAttributeValue attributeValue = (LocalTimeAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getTime(), attributeValue.getTime());
        assertNull(qr.getIndexedValues());
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FTimeControl control = defaultTimeControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        TimeAnswer answer = RandomTestFixture.rAnswer(control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), RandomTestFixture.rAnswer(control));
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        QR qr = qrRepository.byId(response.qrId());
        LocalTimeAttributeValue attributeValue = (LocalTimeAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getTime(), attributeValue.getTime());
        assertNull(qr.getIndexedValues());
    }

}
