package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.AutoCalculateAliasContext;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FNumberRankingControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.IntegerAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.core.submission.domain.answer.numberranking.NumberRankingAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import com.mryqr.utils.RandomTestFixture;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.exception.ErrorCode.MANDATORY_ANSWER_REQUIRED;
import static com.mryqr.common.exception.ErrorCode.MAX_RANK_REACHED;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.*;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberRankControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberRankingControl control = defaultNumberRankingControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FNumberRankingControl control = defaultNumberRankingControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        NumberRankingAnswer answer = RandomTestFixture.rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
        Submission submission = submissionRepository.byId(submissionId);
        NumberRankingAnswer updatedAnswer = (NumberRankingAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertEquals(answer.getRank(), indexedValue.getSv());
    }

    @Test
    public void should_provide_numeric_value_for_auto_calculated_conttrol() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberRankingControl dependantControl = defaultNumberRankingControl();
        FNumberInputControl calculatedControl = defaultNumberInputControlBuilder()
                .autoCalculateEnabled(true)
                .autoCalculateSetting(FNumberInputControl.AutoCalculateSetting.builder()
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(dependantControl.getId())
                                        .build()))
                                .build())
                        .expression("#number * 2")
                        .build())
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), dependantControl, calculatedControl);
        NumberRankingAnswer answer = rAnswerBuilder(dependantControl).rank(9).build();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        Submission submission = submissionRepository.byId(submissionId);
        NumberInputAnswer updatedAnswer = (NumberInputAnswer) submission.getAnswers().get(calculatedControl.getId());
        assertEquals(18, updatedAnswer.getNumber());
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FNumberRankingControl control = defaultNumberRankingControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        NumberRankingAnswer answer = rAnswerBuilder(control).rank(0).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_fail_answer_if_number_exceeds_max() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FNumberRankingControl control = defaultNumberRankingControlBuilder().max(8).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        NumberRankingAnswer answer = rAnswerBuilder(control).rank(10).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MAX_RANK_REACHED);
    }

    @Test
    public void should_calculate_submission_value_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        FNumberRankingControl control = defaultNumberRankingControlBuilder().max(10).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute firstAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        Attribute lastAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        Attribute maxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_MAX).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        Attribute minAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_MIN).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        Attribute avgAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_AVERAGE).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        Attribute sumAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_SUM).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), firstAttribute, lastAttribute, maxAttribute, minAttribute, avgAttribute, sumAttribute);

        SubmissionApi.newSubmission(response.getJwt(),
                newSubmissionCommand(response.getQrId(), response.getHomePageId(),
                        NumberRankingAnswer.builder().controlId(control.getId()).controlType(control.getType()).rank(2).build()));

        SubmissionApi.newSubmission(response.getJwt(),
                newSubmissionCommand(response.getQrId(), response.getHomePageId(),
                        NumberRankingAnswer.builder().controlId(control.getId()).controlType(control.getType()).rank(1).build()));

        SubmissionApi.newSubmission(response.getJwt(),
                newSubmissionCommand(response.getQrId(), response.getHomePageId(),
                        NumberRankingAnswer.builder().controlId(control.getId()).controlType(control.getType()).rank(3).build()));

        App app = appRepository.byId(response.getAppId());
        IndexedField firstIndexedField = app.indexedFieldForAttributeOptional(firstAttribute.getId()).get();
        IndexedField lastIndexedField = app.indexedFieldForAttributeOptional(lastAttribute.getId()).get();
        IndexedField maxIndexedField = app.indexedFieldForAttributeOptional(maxAttribute.getId()).get();
        IndexedField minIndexedField = app.indexedFieldForAttributeOptional(minAttribute.getId()).get();
        IndexedField avgIndexedField = app.indexedFieldForAttributeOptional(avgAttribute.getId()).get();
        IndexedField sumIndexedField = app.indexedFieldForAttributeOptional(sumAttribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());

        assertEquals(2, ((IntegerAttributeValue) qr.getAttributeValues().get(firstAttribute.getId())).getNumber());
        assertEquals(3, ((IntegerAttributeValue) qr.getAttributeValues().get(lastAttribute.getId())).getNumber());
        assertEquals(3, ((IntegerAttributeValue) qr.getAttributeValues().get(maxAttribute.getId())).getNumber());
        assertEquals(1, ((IntegerAttributeValue) qr.getAttributeValues().get(minAttribute.getId())).getNumber());
        assertEquals(2, ((IntegerAttributeValue) qr.getAttributeValues().get(avgAttribute.getId())).getNumber());
        assertEquals(6, ((IntegerAttributeValue) qr.getAttributeValues().get(sumAttribute.getId())).getNumber());

        assertEquals(2, qr.getIndexedValues().valueOf(firstIndexedField).getSv());
        assertEquals(3, qr.getIndexedValues().valueOf(lastIndexedField).getSv());
        assertEquals(3, qr.getIndexedValues().valueOf(maxIndexedField).getSv());
        assertEquals(1, qr.getIndexedValues().valueOf(minIndexedField).getSv());
        assertEquals(2, qr.getIndexedValues().valueOf(avgIndexedField).getSv());
        assertEquals(6, qr.getIndexedValues().valueOf(sumIndexedField).getSv());
    }

}
