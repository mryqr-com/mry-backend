package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FMobileNumberControl;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.MobileAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.mobilenumber.MobileNumberAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import com.mryqr.utils.RandomTestFixture;
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

public class MobileControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMobileNumberControl control = defaultMobileControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMobileNumberControl control = defaultMobileControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        MobileNumberAnswer answer = RandomTestFixture.rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        App app = appRepository.byId(response.appId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.homePageId(), control.getId()).get();
        Submission submission = submissionRepository.byId(submissionId);
        MobileNumberAnswer updatedAnswer = (MobileNumberAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(answer.getMobileNumber()));
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMobileNumberControl control = defaultMobileNumberControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        MobileNumberAnswer answer = rAnswerBuilder(control).mobileNumber(null).build();
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_fail_answer_if_mobile_already_exists_for_instance() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMobileNumberControl control = defaultMobileNumberControlBuilder().uniqueType(UNIQUE_PER_INSTANCE).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        MobileNumberAnswer answer = rAnswerBuilder(control).mobileNumber(rMobile()).build();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);
        assertError(() -> newSubmissionRaw(response.jwt(), command), ANSWER_NOT_UNIQUE_PER_INSTANCE);

        //其他qr依然可以提交
        CreateQrResponse anotherQr = QrApi.createQr(response.jwt(), response.defaultGroupId());
        SubmissionApi.newSubmission(response.jwt(), anotherQr.getQrId(), response.homePageId(), answer);
    }

    @Test
    public void should_fail_answer_if_email_already_exists_for_app() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMobileNumberControl control = defaultMobileNumberControlBuilder().uniqueType(UNIQUE_PER_APP).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        MobileNumberAnswer answer = rAnswerBuilder(control).mobileNumber(rMobile()).build();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);
        assertError(() -> newSubmissionRaw(response.jwt(), command), ANSWER_NOT_UNIQUE_PER_APP);

        //其他qr也不能提交
        CreateQrResponse anotherQr = QrApi.createQr(response.jwt(), response.defaultGroupId());
        NewSubmissionCommand anotherCommand = newSubmissionCommand(anotherQr.getQrId(), response.homePageId(), answer);
        assertError(() -> newSubmissionRaw(response.jwt(), anotherCommand), ANSWER_NOT_UNIQUE_PER_APP);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMobileNumberControl control = defaultMobileControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        MobileNumberAnswer answer = RandomTestFixture.rAnswer(control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), RandomTestFixture.rAnswer(control));

        App app = appRepository.byId(response.appId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.qrId());
        MobileAttributeValue attributeValue = (MobileAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getMobileNumber(), attributeValue.getMobile());
        assertTrue(qr.getIndexedValues().valueOf(indexedField).getTv().contains(answer.getMobileNumber()));
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMobileNumberControl control = defaultMobileControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        MobileNumberAnswer answer = RandomTestFixture.rAnswer(control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), RandomTestFixture.rAnswer(control));
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        App app = appRepository.byId(response.appId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.qrId());
        MobileAttributeValue attributeValue = (MobileAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getMobileNumber(), attributeValue.getMobile());
        assertTrue(qr.getIndexedValues().valueOf(indexedField).getTv().contains(answer.getMobileNumber()));
    }

}
