package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.TextOption;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FPointCheckControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.PointCheckAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.pointcheck.PointCheckAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.core.submission.domain.answer.pointcheck.PointCheckValue.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;

public class PointCheckControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FPointCheckControl control = defaultPointCheckControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_fail_create_control_if_option_ids_duplicates() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(optionsId).name(randomAlphabetic(10) + "选项").build();
        FPointCheckControl control = defaultPointCheckControlBuilder().options(newArrayList(option1, option2)).build();
        App app = appRepository.byId(response.appId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.jwt(), response.appId(), app.getVersion(), setting),
                TEXT_OPTION_ID_DUPLICATED);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FPointCheckControl control = defaultPointCheckControlBuilder().build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        PointCheckAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        App app = appRepository.byId(response.appId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.homePageId(), control.getId()).get();
        Submission submission = submissionRepository.byId(submissionId);
        PointCheckAnswer updatedAnswer = (PointCheckAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertEquals(1, indexedValue.getTv().size());
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FPointCheckControl control = defaultPointCheckControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build())
                .build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        PointCheckAnswer answer = rAnswer(control);
        answer.getChecks().entrySet().forEach(entry -> entry.setValue(NONE));
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_fail_if_answer_option_not_match_with_control() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FPointCheckControl control = defaultPointCheckControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        PointCheckAnswer answer = rAnswerBuilder(control).checks(new HashMap<>()).build();
        answer.getChecks().put(newShortUuid(), YES);
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), POINT_CHECK_ANSWER_NOT_MATCH_TO_CONTROL);
    }

    @Test
    public void should_fail_if_has_none_option_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FPointCheckControl control = defaultPointCheckControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build())
                .build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        PointCheckAnswer answer = rAnswer(control);
        answer.getChecks().put(control.getOptions().stream().findAny().get().getId(), NONE);
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), NOT_ALL_POINT_CHECK_ANSWERED);
    }

    @Test
    public void should_fail_if_answer_not_complete_for_non_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FPointCheckControl control = defaultPointCheckControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        PointCheckAnswer answer = rAnswer(control);
        answer.getChecks().put(control.getOptions().stream().findAny().get().getId(), NONE);
        NewSubmissionCommand command = newSubmissionCommand(response.qrId(), response.homePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.jwt(), command), ONLY_PARTIAL_POINT_CHECK_ANSWERED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FPointCheckControl control = defaultPointCheckControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST)
                .pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        PointCheckAnswer answer = rAnswer(control);
        answer.getChecks().entrySet().forEach(entry -> entry.setValue(YES));
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));

        App app = appRepository.byId(response.appId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.qrId());
        PointCheckAttributeValue attributeValue = (PointCheckAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertTrue(attributeValue.isPass());
        assertTrue(qr.getIndexedValues().valueOf(indexedField).getTv().contains("YES"));
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FPointCheckControl control = defaultPointCheckControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        PointCheckAnswer answer = rAnswer(control);
        answer.getChecks().entrySet().forEach(entry -> entry.setValue(NO));
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);

        App app = appRepository.byId(response.appId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.qrId());
        PointCheckAttributeValue attributeValue = (PointCheckAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertFalse(attributeValue.isPass());
        assertTrue(qr.getIndexedValues().valueOf(indexedField).getTv().contains("NO"));
    }
}
