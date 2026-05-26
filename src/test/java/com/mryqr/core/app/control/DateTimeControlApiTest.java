package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FDateTimeControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.TimestampAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.datetime.DateTimeAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.common.exception.ErrorCode.MANDATORY_ANSWER_REQUIRED;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTimeControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();
        FDateTimeControl control = defaultDateTimeControl();

        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FDateTimeControl control = defaultDateTimeControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        DateTimeAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        App app = appRepository.byId(response.appId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.homePageId(), control.getId()).get();
        Submission submission = submissionRepository.byId(submissionId);
        DateTimeAnswer updatedAnswer = (DateTimeAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertEquals(answer.toInstant().toEpochMilli(), indexedValue.getSv());
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FDateTimeControl control = defaultDateTimeControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(),
                        newSubmissionCommand(response.qrId(), response.homePageId(),
                                rAnswerBuilder(control).date(null).time(null).build())),
                MANDATORY_ANSWER_REQUIRED);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(),
                        newSubmissionCommand(response.qrId(), response.homePageId(),
                                rAnswerBuilder(control).date("2021-03-12").time(null).build())),
                MANDATORY_ANSWER_REQUIRED);


        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(),
                        newSubmissionCommand(response.qrId(), response.homePageId(),
                                rAnswerBuilder(control).date(null).time("12:23").build())),
                MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FDateTimeControl control = defaultDateTimeControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        DateTimeAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));

        App app = appRepository.byId(response.appId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.qrId());
        TimestampAttributeValue attributeValue = (TimestampAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.toInstant(), attributeValue.getTimestamp());
        assertEquals(answer.toInstant().toEpochMilli(), qr.getIndexedValues().valueOf(indexedField).getSv());
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FDateTimeControl control = defaultDateTimeControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        DateTimeAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        App app = appRepository.byId(response.appId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.qrId());
        TimestampAttributeValue attributeValue = (TimestampAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.toInstant(), attributeValue.getTimestamp());
        assertEquals(answer.toInstant().toEpochMilli(), qr.getIndexedValues().valueOf(indexedField).getSv());
    }

}
