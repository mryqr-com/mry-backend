package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.AutoCalculateAliasContext;
import com.mryqr.core.app.domain.page.control.FMultiLevelSelectionControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.MultiLevelSelectionAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelection;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelectionAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.common.exception.ErrorCode.MANDATORY_ANSWER_REQUIRED;
import static com.mryqr.core.common.exception.ErrorCode.MULTI_SELECTION_LEVEL1_NOT_PROVIDED;
import static com.mryqr.core.common.exception.ErrorCode.MULTI_SELECTION_LEVEL2_NOT_PROVIDED;
import static com.mryqr.core.common.exception.ErrorCode.MULTI_SELECTION_LEVEL3_NOT_PROVIDED;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelection.joinLevels;
import static com.mryqr.utils.RandomTestFixture.defaultFillableSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultMultiLevelSelectionControl;
import static com.mryqr.utils.RandomTestFixture.defaultMultiLevelSelectionControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultNumberInputControlBuilder;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiLevelSelectionControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市/区县")
                .optionText("四川省/成都市/双流区")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        FMultiLevelSelectionControl updatedControl = (FMultiLevelSelectionControl) app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertEquals(3, updatedControl.getTotalLevel());
        assertEquals(newArrayList("省份", "城市", "区县"), updatedControl.getTitles());
        assertEquals("四川省", updatedControl.getOption().getOptions().get(0).getName());
        assertEquals("成都市", updatedControl.getOption().getOptions().get(0).getOptions().get(0).getName());
        assertEquals("双流区", updatedControl.getOption().getOptions().get(0).getOptions().get(0).getOptions().get(0).getName());
    }

    @Test
    public void should_create_control_with_no_duplication() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市/区县")
                .optionText("四川省/成都市/双流区\n四川省/成都市/双流区\n四川省/成都市\n四川省\n四川省/绵阳市\n四川省/绵阳市")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        FMultiLevelSelectionControl updatedControl = (FMultiLevelSelectionControl) app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertEquals(3, updatedControl.getTotalLevel());
        assertEquals(newArrayList("省份", "城市", "区县"), updatedControl.getTitles());
        assertEquals(1, updatedControl.getOption().getOptions().size());
        assertEquals("四川省", updatedControl.getOption().getOptions().get(0).getName());
        assertEquals(2, updatedControl.getOption().getOptions().get(0).getOptions().size());
        assertEquals("成都市", updatedControl.getOption().getOptions().get(0).getOptions().get(0).getName());
        assertEquals("双流区", updatedControl.getOption().getOptions().get(0).getOptions().get(0).getOptions().get(0).getName());
        assertEquals("绵阳市", updatedControl.getOption().getOptions().get(0).getOptions().get(1).getName());
    }


    @Test
    public void should_create_control_with_wired_option_content() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市/区县")
                .optionText("/四川省/成都市 /双流区\n四川省 /成都市/双流区/\n四川省/成都市/\n四川省/ 绵阳市/\n/陕西省\n陕西省/\n//浙江省\n///\n/ /\n/")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        FMultiLevelSelectionControl updatedControl = (FMultiLevelSelectionControl) app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertEquals(3, updatedControl.getTotalLevel());
        assertEquals(newArrayList("省份", "城市", "区县"), updatedControl.getTitles());
        assertEquals("四川省", updatedControl.getOption().getOptions().get(0).getName());
        assertEquals("陕西省", updatedControl.getOption().getOptions().get(1).getName());
        assertEquals("浙江省", updatedControl.getOption().getOptions().get(2).getName());

        assertEquals(2, updatedControl.getOption().getOptions().get(0).getOptions().size());
        assertEquals("成都市", updatedControl.getOption().getOptions().get(0).getOptions().get(0).getName());
        assertEquals("绵阳市", updatedControl.getOption().getOptions().get(0).getOptions().get(1).getName());

        assertEquals(1, updatedControl.getOption().getOptions().get(0).getOptions().get(0).getOptions().size());
        assertEquals("双流区", updatedControl.getOption().getOptions().get(0).getOptions().get(0).getOptions().get(0).getName());
    }

    @Test
    public void should_create_control_normally_with_numeric_value() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市/区县")
                .optionText("四川省/成都市/双流区:10\n四川省/绵阳市/:20\n陕西省/ :30")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        FMultiLevelSelectionControl updatedControl = (FMultiLevelSelectionControl) app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertEquals(control, updatedControl);
        assertEquals(3, updatedControl.getTotalLevel());
        assertEquals(newArrayList("省份", "城市", "区县"), updatedControl.getTitles());
        assertEquals("四川省", updatedControl.getOption().getOptions().get(0).getName());
        assertEquals(0, updatedControl.getOption().getOptions().get(0).getNumericalValue());
        assertEquals("成都市", updatedControl.getOption().getOptions().get(0).getOptions().get(0).getName());
        assertEquals(0, updatedControl.getOption().getOptions().get(0).getOptions().get(0).getNumericalValue());
        assertEquals("双流区", updatedControl.getOption().getOptions().get(0).getOptions().get(0).getOptions().get(0).getName());
        assertEquals(10, updatedControl.getOption().getOptions().get(0).getOptions().get(0).getOptions().get(0).getNumericalValue());
        assertEquals("绵阳市", updatedControl.getOption().getOptions().get(0).getOptions().get(1).getName());
        assertEquals(20, updatedControl.getOption().getOptions().get(0).getOptions().get(1).getNumericalValue());
        assertEquals("陕西省", updatedControl.getOption().getOptions().get(1).getName());
        assertEquals(30, updatedControl.getOption().getOptions().get(1).getNumericalValue());
    }

    @Test
    public void should_create_control_normally_with_level2() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市")
                .optionText("四川省/成都市\n广东省/广州市")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        FMultiLevelSelectionControl updatedControl = (FMultiLevelSelectionControl) app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertEquals(2, updatedControl.getTotalLevel());
        assertEquals(newArrayList("省份", "城市"), updatedControl.getTitles());
        assertEquals("四川省", updatedControl.getOption().getOptions().get(0).getName());
        assertEquals("成都市", updatedControl.getOption().getOptions().get(0).getOptions().get(0).getName());
        assertTrue(updatedControl.getOption().getOptions().get(0).getOptions().get(0).getOptions().isEmpty());

        assertEquals("广东省", updatedControl.getOption().getOptions().get(1).getName());
        assertEquals("广州市", updatedControl.getOption().getOptions().get(1).getOptions().get(0).getName());
        assertTrue(updatedControl.getOption().getOptions().get(1).getOptions().get(0).getOptions().isEmpty());
    }

    @Test
    public void should_create_control_normally_with_level1() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份")
                .optionText("四川省\n广东省")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        FMultiLevelSelectionControl updatedControl = (FMultiLevelSelectionControl) app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertEquals(1, updatedControl.getTotalLevel());
        assertEquals(newArrayList("省份"), updatedControl.getTitles());
        assertEquals("四川省", updatedControl.getOption().getOptions().get(0).getName());
        assertTrue(updatedControl.getOption().getOptions().get(0).getOptions().isEmpty());

        assertEquals("广东省", updatedControl.getOption().getOptions().get(1).getName());
        assertTrue(updatedControl.getOption().getOptions().get(1).getOptions().isEmpty());
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        App app = appRepository.byId(response.getAppId());

        MultiLevelSelectionAnswer answer = rAnswer(control);
        MultiLevelSelection selection = answer.getSelection();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        IndexedField indexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
        Submission submission = submissionRepository.byId(submissionId);
        MultiLevelSelectionAnswer updatedAnswer = (MultiLevelSelectionAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);

        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(selection.getLevel1()));
        assertTrue(indexedValue.getTv().contains(joinLevels(selection.getLevel1(), selection.getLevel2())));
        assertTrue(indexedValue.getTv().contains(joinLevels(selection.getLevel1(), selection.getLevel2(), selection.getLevel3())));
    }

    @Test
    public void should_answer_with_extra_level_removed_if_required_level_is_lower() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市")
                .optionText("四川省/成都市\n四川省/绵阳市")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MultiLevelSelectionAnswer answer = rAnswerBuilder(control).selection(MultiLevelSelection.builder()
                .level1("四川省")
                .level2("成都市")
                .level3("青羊区")
                .build()).build();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        MultiLevelSelectionAnswer loadedAnswer = (MultiLevelSelectionAnswer) submission.allAnswers().get(control.getId());
        assertEquals("四川省", loadedAnswer.getSelection().getLevel1());
        assertEquals("成都市", loadedAnswer.getSelection().getLevel2());
        assertNull(loadedAnswer.getSelection().getLevel3());
    }

    @Test
    public void should_provide_numeric_value_for_auto_calculated_conttrol() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FMultiLevelSelectionControl dependantControl = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市")
                .optionText("四川省/成都市:23\n四川省/绵阳市:12")
                .build();

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

        App app = appRepository.byId(response.getAppId());
        FMultiLevelSelectionControl selectionControl = (FMultiLevelSelectionControl) app.controlById(dependantControl.getId());


        MultiLevelSelectionAnswer answer = rAnswerBuilder(selectionControl).selection(MultiLevelSelection.builder()
                .level1("四川省")
                .level2("成都市")
                .build()).build();

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        Submission submission = submissionRepository.byId(submissionId);
        NumberInputAnswer updatedAnswer = (NumberInputAnswer) submission.getAnswers().get(calculatedControl.getId());
        assertEquals(46, updatedAnswer.getNumber());
    }


    @Test
    public void should_fail_answer_for_incomplete_level1_even_if_not_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .fillableSetting(defaultFillableSettingBuilder().mandatory(false).build())
                .titleText("省份/城市/区县")
                .optionText("四川省/成都市/双流区\n四川省/成都市/青羊区")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MultiLevelSelectionAnswer answer = rAnswerBuilder(control).selection(MultiLevelSelection.builder()
                .level2("成都市")
                .build()).build();

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MULTI_SELECTION_LEVEL1_NOT_PROVIDED);
    }

    @Test
    public void should_fail_answer_for_incomplete_level2_even_if_not_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .fillableSetting(defaultFillableSettingBuilder().mandatory(false).build())
                .titleText("省份/城市/区县")
                .optionText("四川省/成都市/双流区\n四川省/成都市/青羊区")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MultiLevelSelectionAnswer answer = rAnswerBuilder(control).selection(MultiLevelSelection.builder()
                .level1("四川省")
                .build()).build();

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MULTI_SELECTION_LEVEL2_NOT_PROVIDED);
    }

    @Test
    public void should_fail_answer_for_incomplete_level3_even_if_not_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .fillableSetting(defaultFillableSettingBuilder().mandatory(false).build())
                .titleText("省份/城市/区县")
                .optionText("四川省/成都市/双流区\n四川省/成都市/青羊区")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MultiLevelSelectionAnswer answer = rAnswerBuilder(control).selection(MultiLevelSelection.builder()
                .level1("四川省")
                .level2("成都市")
                .build()).build();

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MULTI_SELECTION_LEVEL3_NOT_PROVIDED);
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .fillableSetting(defaultFillableSettingBuilder().mandatory(true).build())
                .titleText("省份/城市/区县")
                .optionText("四川省/成都市/双流区\n四川省/成都市/青羊区")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MultiLevelSelectionAnswer answer = rAnswerBuilder(control).selection(MultiLevelSelection.builder()
                .build()).build();

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);
        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市/区县")
                .optionText("四川省/成都市/双流区\n四川省/成都市/青羊区")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());

        MultiLevelSelectionAnswer firstAnswer = rAnswerBuilder(control).selection(MultiLevelSelection.builder()
                .level1("四川省")
                .level2("成都市")
                .level3("青羊区")
                .build()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), firstAnswer);

        MultiLevelSelectionAnswer lastAnswer = rAnswerBuilder(control).selection(MultiLevelSelection.builder()
                .level1("四川省")
                .level2("成都市")
                .level3("双流区")
                .build()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), lastAnswer);

        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        MultiLevelSelectionAttributeValue attributeValue = (MultiLevelSelectionAttributeValue) qr.getAttributeValues().get(attribute.getId());
        MultiLevelSelection selection = firstAnswer.getSelection();
        assertEquals(selection, attributeValue.getSelection());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.contains(selection.getLevel1()));
        assertTrue(textValues.contains(joinLevels(selection.getLevel1(), selection.getLevel2())));
        assertTrue(textValues.contains(joinLevels(selection.getLevel1(), selection.getLevel2(), selection.getLevel3())));
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMultiLevelSelectionControl control = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市/区县")
                .optionText("四川省/成都市/双流区\n四川省/成都市/青羊区")
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());

        MultiLevelSelectionAnswer firstAnswer = rAnswerBuilder(control).selection(MultiLevelSelection.builder()
                .level1("四川省")
                .level2("成都市")
                .level3("青羊区")
                .build()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), firstAnswer);

        MultiLevelSelectionAnswer lastAnswer = rAnswerBuilder(control).selection(MultiLevelSelection.builder()
                .level1("四川省")
                .level2("成都市")
                .level3("双流区")
                .build()).build();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), lastAnswer);

        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        MultiLevelSelectionAttributeValue attributeValue = (MultiLevelSelectionAttributeValue) qr.getAttributeValues().get(attribute.getId());
        MultiLevelSelection selection = lastAnswer.getSelection();
        assertEquals(selection, attributeValue.getSelection());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.contains(selection.getLevel1()));
        assertTrue(textValues.contains(joinLevels(selection.getLevel1(), selection.getLevel2())));
        assertTrue(textValues.contains(joinLevels(selection.getLevel1(), selection.getLevel2(), selection.getLevel3())));
    }

}
