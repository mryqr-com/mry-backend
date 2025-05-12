package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.AutoCalculateAliasContext;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FItemStatusControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.common.domain.TextOption;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.ItemStatusAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.page.control.Control.newControlId;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_NUMERICAL_VALUED;
import static com.mryqr.core.common.exception.ErrorCode.INITIAL_ITEM_STATUS_NOT_VALID;
import static com.mryqr.core.common.exception.ErrorCode.ITEM_STATUS_ANSWER_NOT_IN_CONTROL;
import static com.mryqr.core.common.exception.ErrorCode.MANDATORY_ANSWER_REQUIRED;
import static com.mryqr.core.common.exception.ErrorCode.TEXT_OPTION_ID_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_CONTROL_NOT_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_STATUS_OPTION_NOT_EXISTS;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.defaultFillableSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultItemStatusControl;
import static com.mryqr.utils.RandomTestFixture.defaultItemStatusControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultNumberInputControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemStatusControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FItemStatusControl control = defaultItemStatusControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_create_control_with_auto_calculate_enabled() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(5) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(5) + "选项").build();

        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder()
                .autoCalculateEnabled(true)
                .fillableSetting(defaultFillableSettingBuilder().autoFill(true).build())
                .options(newArrayList(option1, option2))
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .records(newArrayList(FItemStatusControl.AutoCalculateRecord.builder()
                                .id(newShortUuid())
                                .expression("#number > 10")
                                .optionId(optionsId)
                                .build()))
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(numberInputControl.getId())
                                        .build()))
                                .build())
                        .build())
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, itemStatusControl);

        App app = appRepository.byId(response.getAppId());
        FItemStatusControl updatedControl = (FItemStatusControl) app.controlByIdOptional(itemStatusControl.getId()).get();
        assertEquals(itemStatusControl.getAutoCalculateSetting(), updatedControl.getAutoCalculateSetting());
        assertFalse(updatedControl.getFillableSetting().isAutoFill());
        assertTrue(updatedControl.isShouldAutoCalculate());
    }

    @Test
    public void should_fail_create_control_if_option_ids_duplicate() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(5) + "选项").build();
        TextOption option2 = TextOption.builder().id(optionsId).name(randomAlphabetic(5) + "选项").build();
        FItemStatusControl control = defaultItemStatusControlBuilder().options(newArrayList(option1, option2)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), TEXT_OPTION_ID_DUPLICATED);
    }

    @Test
    public void should_fail_create_control_if_initial_option_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FItemStatusControl control = defaultItemStatusControlBuilder().initialOptionId(newShortUuid()).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), INITIAL_ITEM_STATUS_NOT_VALID);
    }

    @Test
    public void should_fail_create_control_if_control_not_exist_for_auto_calculate() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(5) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(5) + "选项").build();
        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder()
                .autoCalculateEnabled(true)
                .options(newArrayList(option1, option2))
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .records(newArrayList(FItemStatusControl.AutoCalculateRecord.builder()
                                .id(newShortUuid())
                                .expression("#number > 10")
                                .optionId(optionsId)
                                .build()))
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(newControlId())
                                        .build()))
                                .build())
                        .build())
                .build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(itemStatusControl);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_option_not_exist_for_auto_calculate() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder()
                .autoCalculateEnabled(true)
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .records(newArrayList(FItemStatusControl.AutoCalculateRecord.builder()
                                .id(newShortUuid())
                                .expression("#number > 10")
                                .optionId(newShortUuid())
                                .build()))
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(numberInputControl.getId())
                                        .build()))
                                .build())
                        .build())
                .build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().addAll(newArrayList(numberInputControl, itemStatusControl));

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), VALIDATION_STATUS_OPTION_NOT_EXISTS);
    }

    @Test
    public void should_fail_create_control_if_control_not_numerical_valued_for_auto_calculate() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl lineTextControl = defaultSingleLineTextControl();
        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(5) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(5) + "选项").build();
        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder()
                .autoCalculateEnabled(true)
                .options(newArrayList(option1, option2))
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .records(newArrayList(FItemStatusControl.AutoCalculateRecord.builder()
                                .id(newShortUuid())
                                .expression("#number > 10")
                                .optionId(optionsId)
                                .build()))
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(lineTextControl.getId())
                                        .build()))
                                .build())
                        .build())
                .build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().addAll(newArrayList(lineTextControl, itemStatusControl));

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), CONTROL_NOT_NUMERICAL_VALUED);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FItemStatusControl control = defaultItemStatusControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        ItemStatusAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
        Submission submission = submissionRepository.byId(submissionId);
        ItemStatusAnswer updatedAnswer = (ItemStatusAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(answer.getOptionId()));
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FItemStatusControl control = defaultItemStatusControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        ItemStatusAnswer answer = rAnswerBuilder(control).optionId(null).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_fail_answer_if_option_not_exists() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FItemStatusControl control = defaultItemStatusControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        ItemStatusAnswer answer = rAnswerBuilder(control).optionId(newShortUuid()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), ITEM_STATUS_ANSWER_NOT_IN_CONTROL);
    }

    @Test
    public void should_answer_with_initial_option() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FItemStatusControl control = defaultItemStatusControl();
        String initialOptionId = control.allOptionIds().stream().findAny().get();
        ReflectionTestUtils.setField(control, "initialOptionId", initialOptionId);
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        ItemStatusAnswer answer = rAnswerBuilder(control).optionId(null).build();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        ItemStatusAnswer updatedAnswer = (ItemStatusAnswer) submission.getAnswers().get(control.getId());
        assertEquals(initialOptionId, updatedAnswer.getOptionId());
    }

    @Test
    public void should_answer_with_auto_calculated_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(5) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(5) + "选项").build();

        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder()
                .autoCalculateEnabled(true)
                .options(newArrayList(option1, option2))
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .records(newArrayList(FItemStatusControl.AutoCalculateRecord.builder()
                                .id(newShortUuid())
                                .expression("#number > 10")
                                .optionId(optionsId)
                                .build()))
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(numberInputControl.getId())
                                        .build()))
                                .build())
                        .build())
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, itemStatusControl);
        NumberInputAnswer answer = rAnswerBuilder(numberInputControl).number(11.0).build();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        Submission submission = submissionRepository.byId(submissionId);
        ItemStatusAnswer updatedAnswer = (ItemStatusAnswer) submission.getAnswers().get(itemStatusControl.getId());
        assertEquals(optionsId, updatedAnswer.getOptionId());

        assertNull(submissionRepository
                .byId(SubmissionApi.newSubmission(response.getJwt(),
                        response.getQrId(),
                        response.getHomePageId(),
                        rAnswerBuilder(numberInputControl).number(9.0).build()))
                .getAnswers().get(itemStatusControl.getId()));
    }


    @Test
    public void should_answer_with_auto_calculated_value_and_ignore_provided_answer() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();

        String optionsId1 = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId1).name(randomAlphabetic(5) + "选项").build();
        String optionId2 = newShortUuid();
        TextOption option2 = TextOption.builder().id(optionId2).name(randomAlphabetic(5) + "选项").build();

        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder()
                .autoCalculateEnabled(true)
                .options(newArrayList(option1, option2))
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .records(newArrayList(FItemStatusControl.AutoCalculateRecord.builder()
                                .id(newShortUuid())
                                .expression("#number > 10")
                                .optionId(optionsId1)
                                .build()))
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(numberInputControl.getId())
                                        .build()))
                                .build())
                        .build())
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, itemStatusControl);
        NumberInputAnswer numberInputAnswer = rAnswerBuilder(numberInputControl).number(11.0).build();
        ItemStatusAnswer statusAnswer = rAnswerBuilder(itemStatusControl).optionId(optionId2).build();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), numberInputAnswer, statusAnswer);
        Submission submission = submissionRepository.byId(submissionId);
        ItemStatusAnswer updatedAnswer = (ItemStatusAnswer) submission.getAnswers().get(itemStatusControl.getId());
        assertEquals(optionsId1, updatedAnswer.getOptionId());
    }


    @Test
    public void should_not_answer_with_auto_calculated_value_with_invalid_expression() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(5) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(5) + "选项").build();

        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder()
                .autoCalculateEnabled(true)
                .options(newArrayList(option1, option2))
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .records(newArrayList(FItemStatusControl.AutoCalculateRecord.builder()
                                .id(newShortUuid())
                                .expression("#number whatever 10")
                                .optionId(optionsId)
                                .build()))
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(numberInputControl.getId())
                                        .build()))
                                .build())
                        .build())
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, itemStatusControl);

        assertNull(submissionRepository
                .byId(SubmissionApi.newSubmission(response.getJwt(),
                        response.getQrId(),
                        response.getHomePageId(),
                        rAnswerBuilder(numberInputControl).number(9.0).build()))
                .getAnswers().get(itemStatusControl.getId()));
    }


    @Test
    public void should_not_answer_with_auto_calculated_value_if_no_value_provided() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(5) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(5) + "选项").build();

        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder()
                .autoCalculateEnabled(true)
                .options(newArrayList(option1, option2))
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .records(newArrayList(FItemStatusControl.AutoCalculateRecord.builder()
                                .id(newShortUuid())
                                .expression("#number < 10")
                                .optionId(optionsId)
                                .build()))
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(numberInputControl.getId())
                                        .build()))
                                .build())
                        .build())
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, itemStatusControl);

        assertNull(submissionRepository
                .byId(SubmissionApi.newSubmission(response.getJwt(),
                        response.getQrId(),
                        response.getHomePageId()))
                .getAnswers().get(itemStatusControl.getId()));
    }


    @Test
    public void should_not_answer_with_auto_calculated_value_with_dependant_answer_not_filled() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(5) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(5) + "选项").build();

        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder()
                .autoCalculateEnabled(true)
                .options(newArrayList(option1, option2))
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .records(newArrayList(FItemStatusControl.AutoCalculateRecord.builder()
                                .id(newShortUuid())
                                .expression("#number < 10")
                                .optionId(optionsId)
                                .build()))
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(numberInputControl.getId())
                                        .build()))
                                .build())
                        .build())
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, itemStatusControl);

        assertNull(submissionRepository
                .byId(SubmissionApi.newSubmission(response.getJwt(),
                        response.getQrId(),
                        response.getHomePageId(),
                        rAnswerBuilder(numberInputControl).number(null).build()))
                .getAnswers().get(itemStatusControl.getId()));
    }


    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FItemStatusControl control = defaultItemStatusControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        ItemStatusAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        ItemStatusAttributeValue attributeValue = (ItemStatusAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(control.getId(), attributeValue.getControlId());
        assertEquals(answer.getOptionId(), attributeValue.getOptionId());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.contains(answer.getOptionId()));
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FItemStatusControl control = defaultItemStatusControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        ItemStatusAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        ItemStatusAttributeValue attributeValue = (ItemStatusAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(control.getId(), attributeValue.getControlId());
        assertEquals(answer.getOptionId(), attributeValue.getOptionId());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.contains(answer.getOptionId()));
    }

}
