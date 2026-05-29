package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FEmailControl;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.EmailAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.email.EmailAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.page.control.AnswerUniqueType.UNIQUE_PER_APP;
import static com.mryqr.core.app.domain.page.control.AnswerUniqueType.UNIQUE_PER_INSTANCE;
import static com.mryqr.core.submission.SubmissionApi.newSubmissionRaw;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FEmailControl control = defaultEmailControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FEmailControl control = defaultEmailControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        EmailAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
        Submission submission = submissionRepository.byId(submissionId);
        EmailAnswer updatedAnswer = (EmailAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(answer.getEmail()));
    }

    @Test
    public void should_fail_answer_if_email_already_exists_for_instance() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FEmailControl control = defaultEmailControlBuilder().uniqueType(UNIQUE_PER_INSTANCE).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        EmailAnswer answer = rAnswerBuilder(control).email(rEmail()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);
        assertError(() -> newSubmissionRaw(response.getJwt(), command), ANSWER_NOT_UNIQUE_PER_INSTANCE);

        //其他qr依然可以提交
        CreateQrResponse anotherQr = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        SubmissionApi.newSubmission(response.getJwt(), anotherQr.getQrId(), response.getHomePageId(), answer);
    }

    @Test
    public void should_fail_answer_if_email_already_exists_for_app() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FEmailControl control = defaultEmailControlBuilder().uniqueType(UNIQUE_PER_APP).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        EmailAnswer answer = rAnswerBuilder(control).email(rEmail()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);
        assertError(() -> newSubmissionRaw(response.getJwt(), command), ANSWER_NOT_UNIQUE_PER_APP);

        //其他qr也不能提交
        CreateQrResponse anotherQr = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        NewSubmissionCommand anotherCommand = newSubmissionCommand(anotherQr.getQrId(), response.getHomePageId(), answer);
        assertError(() -> newSubmissionRaw(response.getJwt(), anotherCommand), ANSWER_NOT_UNIQUE_PER_APP);
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FEmailControl control = defaultEmailControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        EmailAnswer answer = rAnswerBuilder(control).email(null).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);
        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FEmailControl control = defaultEmailControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        EmailAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        EmailAttributeValue attributeValue = (EmailAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getEmail(), attributeValue.getEmail());
        assertTrue(qr.getIndexedValues().valueOf(indexedField).getTv().contains(answer.getEmail()));
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FEmailControl control = defaultEmailControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        EmailAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        EmailAttributeValue attributeValue = (EmailAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getEmail(), attributeValue.getEmail());
        assertTrue(qr.getIndexedValues().valueOf(indexedField).getTv().contains(answer.getEmail()));
    }

}
