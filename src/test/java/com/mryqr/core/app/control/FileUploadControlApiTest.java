package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FFileUploadControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.FilesAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.fileupload.FileUploadAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FileUploadControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FFileUploadControl control = defaultFileUploadControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FFileUploadControl control = defaultFileUploadControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        FileUploadAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        FileUploadAnswer updatedAnswer = (FileUploadAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        assertNull(submission.getIndexedValues());
    }

    @Test
    public void should_fail_answer_if_file_size_greater_than_max() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FFileUploadControl control = defaultFileUploadControlBuilder().max(1).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        FileUploadAnswer answer = rAnswerBuilder(control).files(newArrayList(rUploadedFile(), rUploadedFile())).build();
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), MAX_FILE_NUMBER_REACHED);
    }

    @Test
    public void should_fail_answer_if_file_id_duplicated() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FFileUploadControl control = defaultFileUploadControlBuilder().max(2).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        UploadedFile uploadedFile = rUploadedFile();
        FileUploadAnswer answer = rAnswerBuilder(control).files(newArrayList(uploadedFile, uploadedFile)).build();
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), UPLOAD_FILE_ID_DUPLICATED);
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FFileUploadControl control = defaultFileUploadControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build())
                .build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        FileUploadAnswer answer = rAnswerBuilder(control).files(newArrayList()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FFileUploadControl control = defaultFileUploadControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST)
                .pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        FileUploadAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));

        QR qr = qrRepository.byId(response.qrId());
        FilesAttributeValue attributeValue = (FilesAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getFiles(), attributeValue.getFiles());
        assertNull(qr.getIndexedValues());
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FFileUploadControl control = defaultFileUploadControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        FileUploadAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        QR qr = qrRepository.byId(response.qrId());
        FilesAttributeValue attributeValue = (FilesAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getFiles(), attributeValue.getFiles());
        assertNull(qr.getIndexedValues());
    }
}
