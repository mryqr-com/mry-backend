package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.CountedItem;
import com.mryqr.common.domain.TextOption;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.AutoCalculateAliasContext;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FItemCountControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.ItemCountAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.itemcount.ItemCountAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import com.mryqr.utils.RandomTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;

public class ItemCountControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FItemCountControl control = defaultItemCountControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_fail_create_control_if_option_ids_duplicate() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String optionsId = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionsId).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(optionsId).name(randomAlphabetic(10) + "选项").build();
        FItemCountControl control = defaultItemCountControlBuilder().options(newArrayList(option1, option2)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                TEXT_OPTION_ID_DUPLICATED);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FItemCountControl control = defaultItemCountControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        ItemCountAnswer answer = RandomTestFixture.rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        ItemCountAnswer updatedAnswer = (ItemCountAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
    }

    @Test
    public void should_provide_numeric_value_for_auto_calculated_conttrol() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String optionId1 = newShortUuid();
        String optionId2 = newShortUuid();
        TextOption option1 = TextOption.builder().id(optionId1).name(randomAlphabetic(5) + "选项").numericalValue(3).build();
        TextOption option2 = TextOption.builder().id(optionId2).name(randomAlphabetic(5) + "选项").numericalValue(5).build();

        FItemCountControl dependantControl = defaultItemCountControlBuilder().options(newArrayList(option1, option2)).build();
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
        ItemCountAnswer answer = rAnswerBuilder(dependantControl).items(newArrayList(
                CountedItem.builder()
                        .id(newShortUuid())
                        .optionId(optionId1)
                        .number(2)
                        .build(),
                CountedItem.builder()
                        .id(newShortUuid())
                        .optionId(optionId2)
                        .number(1)
                        .build())).build();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        Submission submission = submissionRepository.byId(submissionId);
        NumberInputAnswer updatedAnswer = (NumberInputAnswer) submission.getAnswers().get(calculatedControl.getId());
        assertEquals(22, updatedAnswer.getNumber());
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FItemCountControl control = defaultItemCountControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build())
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        ItemCountAnswer answer = rAnswerBuilder(control).items(newArrayList()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_fail_answer_if_item_size_greater_than_max() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FItemCountControl control = defaultItemCountControlBuilder().maxItem(1).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        List<CountedItem> items = control.allOptionIds().stream()
                .map(optionId -> CountedItem.builder().id(newShortUuid()).optionId(optionId).number(1).build()).collect(toList());
        ItemCountAnswer answer = rAnswerBuilder(control).items(items).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MAX_ITEM_NUMBER_REACHED);
    }

    @Test
    public void should_fail_answer_if_item_ids_duplicate() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FItemCountControl control = defaultItemCountControlBuilder().maxItem(10).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        String itemId = newShortUuid();
        List<CountedItem> items = control.allOptionIds().stream().limit(2)
                .map(optionId -> CountedItem.builder().id(itemId).optionId(optionId).number(1).build()).collect(toList());
        ItemCountAnswer answer = rAnswerBuilder(control).items(items).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), COUNTED_ITEM_ID_DUPLICATED);
    }

    @Test
    public void should_fail_answer_if_item_option_id_duplicate() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FItemCountControl control = defaultItemCountControlBuilder().maxItem(10).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        String duplicateOptionId = control.allOptionIds().stream().findFirst().get();
        List<CountedItem> items = control.allOptionIds().stream().limit(2)
                .map(optionId -> CountedItem.builder().id(newShortUuid()).optionId(duplicateOptionId).number(1).build()).collect(toList());
        ItemCountAnswer answer = rAnswerBuilder(control).items(items).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), ITEM_ANSWER_OPTION_DUPLICATED);
    }

    @Test
    public void should_fail_answer_if_item_option_id_not_exists() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FItemCountControl control = defaultItemCountControlBuilder().maxItem(10).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        List<CountedItem> items = control.allOptionIds().stream().limit(2)
                .map(optionId -> CountedItem.builder().id(newShortUuid()).optionId(newShortUuid()).number(1).build()).collect(toList());
        ItemCountAnswer answer = rAnswerBuilder(control).items(items).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), NOT_ALL_ANSWERS_IN_OPTIONS);
    }

    @Test
    public void should_fail_answer_if_item_number_exceeds_max() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FItemCountControl control = defaultItemCountControlBuilder().maxNumberPerItem(1).maxItem(2).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        List<CountedItem> items = control.allOptionIds().stream().limit(2)
                .map(optionId -> CountedItem.builder().id(newShortUuid()).optionId(optionId).number(2).build()).collect(toList());
        ItemCountAnswer answer = rAnswerBuilder(control).items(items).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MAX_ITEM_COUNT_REACHED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FItemCountControl control = defaultItemCountControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST)
                .pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        ItemCountAnswer answer = RandomTestFixture.rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), RandomTestFixture.rAnswer(control));

        QR qr = qrRepository.byId(response.getQrId());
        ItemCountAttributeValue attributeValue = (ItemCountAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getItems(), attributeValue.getItems());
        assertNotNull(qr.getIndexedValues());

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        Set<String> textIndexValues = answer.getItems().stream().map(CountedItem::getOptionId).collect(Collectors.toSet());
        assertTrue(qr.getIndexedValues().valueOf(indexedField).getTv().containsAll(textIndexValues));
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FItemCountControl control = defaultItemCountControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        ItemCountAnswer answer = RandomTestFixture.rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), RandomTestFixture.rAnswer(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        QR qr = qrRepository.byId(response.getQrId());
        ItemCountAttributeValue attributeValue = (ItemCountAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getItems(), attributeValue.getItems());
        assertNotNull(qr.getIndexedValues());
        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        Set<String> textIndexValues = answer.getItems().stream().map(CountedItem::getOptionId).collect(Collectors.toSet());
        assertTrue(qr.getIndexedValues().valueOf(indexedField).getTv().containsAll(textIndexValues));
    }
}
