package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.AutoCalculateAliasContext;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.common.domain.TextOption;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.CheckboxAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.ui.MinMaxSetting.minMaxOf;
import static com.mryqr.core.common.exception.ErrorCode.CHECKBOX_MAX_SELECTION_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.CHECKBOX_MIN_SELECTION_NOT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.MANDATORY_ANSWER_REQUIRED;
import static com.mryqr.core.common.exception.ErrorCode.MAX_OVERFLOW;
import static com.mryqr.core.common.exception.ErrorCode.MIN_OVERFLOW;
import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_ANSWERS_IN_CHECKBOX_OPTIONS;
import static com.mryqr.core.common.exception.ErrorCode.TEXT_OPTION_ID_DUPLICATED;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.submission.SubmissionApi.newSubmissionRaw;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControl;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultFillableSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultNumberInputControlBuilder;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static com.mryqr.utils.RandomTestFixture.rTextOptions;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckboxControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();
        FCheckboxControl control = defaultCheckboxControl();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_fail_create_control_if_option_ids_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(optionsId).name(randomAlphabetic(10) + "选项").build();
        FCheckboxControl control = defaultCheckboxControlBuilder().options(newArrayList(option1, option2)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), TEXT_OPTION_ID_DUPLICATED);
    }

    @Test
    public void should_fail_create_control_if_max_selection_size_greater_than_20() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl control = defaultCheckboxControlBuilder().minMaxSetting(minMaxOf(1, 21)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), MAX_OVERFLOW);
    }

    @Test
    public void should_fail_create_control_if_min_selection_size_less_than_0() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FCheckboxControl control = defaultCheckboxControlBuilder().minMaxSetting(minMaxOf(-1, 10)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), MIN_OVERFLOW);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl control = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        CheckboxAnswer answer = rAnswer(control);
        List<String> optionIds = answer.getOptionIds();

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
        Submission submission = submissionRepository.byId(submissionId);
        CheckboxAnswer updatedAnswer = (CheckboxAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertTrue(indexedValue.getTv().containsAll(optionIds));
    }


    @Test
    public void should_provide_numeric_value_for_auto_calculated_conttrol() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String optionId1 = newShortUuid();
        String optionId2 = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionId1).name(randomAlphabetic(5) + "选项").numericalValue(3).build();
        TextOption option2 = TextOption.builder().id(optionId2).name(randomAlphabetic(5) + "选项").numericalValue(5).build();

        FCheckboxControl dependantControl = defaultCheckboxControlBuilder().options(newArrayList(option1, option2)).build();
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
        CheckboxAnswer answer = rAnswerBuilder(dependantControl).optionIds(newArrayList(optionId1, optionId2)).build();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        Submission submission = submissionRepository.byId(submissionId);
        NumberInputAnswer updatedAnswer = (NumberInputAnswer) submission.getAnswers().get(calculatedControl.getId());
        assertEquals(16, updatedAnswer.getNumber());
    }


    @Test
    public void should_fail_answer_if_option_not_in_control() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl control = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        CheckboxAnswer answer = rAnswerBuilder(control).optionIds(newArrayList(newShortUuid())).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> newSubmissionRaw(response.getJwt(), command), NOT_ALL_ANSWERS_IN_CHECKBOX_OPTIONS);
    }

    @Test
    public void should_fail_answer_if_filled_size_greater_than_max() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl control = defaultCheckboxControlBuilder().options(rTextOptions(10)).minMaxSetting(minMaxOf(1, 3)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        CheckboxAnswer answer = rAnswerBuilder(control).optionIds(newArrayList(control.allOptionIds())).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> newSubmissionRaw(response.getJwt(), command), CHECKBOX_MAX_SELECTION_REACHED);
    }

    @Test
    public void should_fail_answer_if_filled_size_less_than_min() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl control = defaultCheckboxControlBuilder().minMaxSetting(minMaxOf(3, 5)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        CheckboxAnswer answer = rAnswerBuilder(control).optionIds(newArrayList()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);
        SubmissionApi.newSubmission(response.getJwt(), command);//无填值时，最小限制不起作用
        answer.getOptionIds().add(control.allOptionIds().stream().findFirst().get());

        assertError(() -> newSubmissionRaw(response.getJwt(), command), CHECKBOX_MIN_SELECTION_NOT_REACHED);
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl control = defaultCheckboxControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        CheckboxAnswer answer = rAnswerBuilder(control).optionIds(newArrayList()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl control = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        CheckboxAnswer answer = rAnswer(control);
        List<String> optionIds = answer.getOptionIds();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        CheckboxAttributeValue attributeValue = (CheckboxAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(control.getId(), attributeValue.getControlId());
        assertEquals(optionIds, attributeValue.getOptionIds());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.containsAll(optionIds));
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl control = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        CheckboxAnswer answer = rAnswer(control);
        List<String> optionIds = answer.getOptionIds();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        CheckboxAttributeValue attributeValue = (CheckboxAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(control.getId(), attributeValue.getControlId());
        assertEquals(optionIds, attributeValue.getOptionIds());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.containsAll(optionIds));
    }

}
