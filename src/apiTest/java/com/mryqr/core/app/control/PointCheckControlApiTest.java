package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FPointCheckControl;
import com.mryqr.core.common.domain.TextOption;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.common.domain.indexedfield.IndexedValue;
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
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.common.exception.ErrorCode.MANDATORY_ANSWER_REQUIRED;
import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_POINT_CHECK_ANSWERED;
import static com.mryqr.core.common.exception.ErrorCode.ONLY_PARTIAL_POINT_CHECK_ANSWERED;
import static com.mryqr.core.common.exception.ErrorCode.POINT_CHECK_ANSWER_NOT_MATCH_TO_CONTROL;
import static com.mryqr.core.common.exception.ErrorCode.TEXT_OPTION_ID_DUPLICATED;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.core.submission.domain.answer.pointcheck.PointCheckValue.NO;
import static com.mryqr.core.submission.domain.answer.pointcheck.PointCheckValue.NONE;
import static com.mryqr.core.submission.domain.answer.pointcheck.PointCheckValue.YES;
import static com.mryqr.utils.RandomTestFixture.defaultFillableSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultPointCheckControl;
import static com.mryqr.utils.RandomTestFixture.defaultPointCheckControlBuilder;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PointCheckControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FPointCheckControl control = defaultPointCheckControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }


    @Test
    public void should_fail_create_control_if_option_ids_duplicates() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(optionsId).name(randomAlphabetic(10) + "选项").build();
        FPointCheckControl control = defaultPointCheckControlBuilder().options(newArrayList(option1, option2)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), TEXT_OPTION_ID_DUPLICATED);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FPointCheckControl control = defaultPointCheckControlBuilder().build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        PointCheckAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
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
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FPointCheckControl control = defaultPointCheckControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        PointCheckAnswer answer = rAnswer(control);
        answer.getChecks().entrySet().forEach(entry -> entry.setValue(NONE));
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }


    @Test
    public void should_fail_if_answer_option_not_match_with_control() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FPointCheckControl control = defaultPointCheckControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        PointCheckAnswer answer = rAnswerBuilder(control).checks(new HashMap<>()).build();
        answer.getChecks().put(newShortUuid(), YES);
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), POINT_CHECK_ANSWER_NOT_MATCH_TO_CONTROL);
    }


    @Test
    public void should_fail_if_has_none_option_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FPointCheckControl control = defaultPointCheckControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        PointCheckAnswer answer = rAnswer(control);
        answer.getChecks().put(control.getOptions().stream().findAny().get().getId(), NONE);
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), NOT_ALL_POINT_CHECK_ANSWERED);
    }


    @Test
    public void should_fail_if_answer_not_complete_for_non_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FPointCheckControl control = defaultPointCheckControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        PointCheckAnswer answer = rAnswer(control);
        answer.getChecks().put(control.getOptions().stream().findAny().get().getId(), NONE);
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), ONLY_PARTIAL_POINT_CHECK_ANSWERED);
    }


    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FPointCheckControl control = defaultPointCheckControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        PointCheckAnswer answer = rAnswer(control);
        answer.getChecks().entrySet().forEach(entry -> entry.setValue(YES));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        PointCheckAttributeValue attributeValue = (PointCheckAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertTrue(attributeValue.isPass());
        assertTrue(qr.getIndexedValues().valueOf(indexedField).getTv().contains("YES"));
    }


    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FPointCheckControl control = defaultPointCheckControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        PointCheckAnswer answer = rAnswer(control);
        answer.getChecks().entrySet().forEach(entry -> entry.setValue(NO));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        PointCheckAttributeValue attributeValue = (PointCheckAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertFalse(attributeValue.isPass());
        assertTrue(qr.getIndexedValues().valueOf(indexedField).getTv().contains("NO"));
    }

}
