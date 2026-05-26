package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FImageUploadControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.ImagesAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.imageupload.ImageUploadAnswer;
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

public class ImageUploadControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FImageUploadControl control = defaultImageUploadControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FImageUploadControl control = defaultImageUploadControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        ImageUploadAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        ImageUploadAnswer updatedAnswer = (ImageUploadAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        assertNull(submission.getIndexedValues());
    }

    @Test
    public void should_fail_answer_if_files_size_greater_than_max() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FImageUploadControl control = defaultImageUploadControlBuilder().max(1).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        ImageUploadAnswer answer = rAnswerBuilder(control).images(newArrayList(rImageFile(), rImageFile())).build();
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), MAX_IMAGE_NUMBER_REACHED);
    }

    @Test
    public void should_fail_answer_if_image_id_duplicated() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FImageUploadControl control = defaultImageUploadControlBuilder().max(2).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        UploadedFile imageFile = rImageFile();
        ImageUploadAnswer answer = rAnswerBuilder(control).images(newArrayList(imageFile, imageFile)).build();
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), UPLOAD_IMAGE_ID_DUPLICATED);
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FImageUploadControl control = defaultImageUploadControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        ImageUploadAnswer answer = rAnswerBuilder(control).images(newArrayList()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FImageUploadControl control = defaultImageUploadControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        ImageUploadAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));

        QR qr = qrRepository.byId(response.qrId());
        ImagesAttributeValue attributeValue = (ImagesAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getImages(), attributeValue.getImages());
        assertNull(qr.getIndexedValues());
    }


    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FImageUploadControl control = defaultImageUploadControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        ImageUploadAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        QR qr = qrRepository.byId(response.qrId());
        ImagesAttributeValue attributeValue = (ImagesAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getImages(), attributeValue.getImages());
        assertNull(qr.getIndexedValues());
    }

}
