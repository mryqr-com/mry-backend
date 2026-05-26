package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.display.*;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.attributedashboard.QAttributeDashboardPresentation;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.address.AddressAnswer;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.datetime.DateTimeAnswer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.email.EmailAnswer;
import com.mryqr.core.submission.domain.answer.geolocation.GeolocationAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.itemcount.ItemCountAnswer;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import com.mryqr.core.submission.domain.answer.memberselect.MemberSelectAnswer;
import com.mryqr.core.submission.domain.answer.mobilenumber.MobileNumberAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.core.submission.domain.answer.numberranking.NumberRankingAnswer;
import com.mryqr.core.submission.domain.answer.pointcheck.PointCheckAnswer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.exception.ErrorCode.VALIDATION_ATTRIBUTE_NOT_EXIST;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.*;

public class AttributeDashboardControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        Attribute fixValueAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(FIXED)
                .fixedValue("whatever").build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), fixValueAttribute);
        PAttributeDashboardControl control = defaultAttributeDashboardControlBuilder().attributeIds(newArrayList(fixValueAttribute.getId()))
                .build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_if_no_attribute() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PAttributeDashboardControl control = defaultAttributeDashboardControlBuilder().attributeIds(newArrayList()).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_fail_create_if_attribute_not_exit() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PAttributeDashboardControl control = defaultAttributeDashboardControlBuilder().attributeIds(newArrayList(Attribute.newAttributeId()))
                .build();
        App app = appRepository.byId(response.appId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.jwt(), response.appId(), app.getVersion(), setting),
                VALIDATION_ATTRIBUTE_NOT_EXIST);
    }

    @Test
    public void should_fetch_all_control_reference_attribute_presentation_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FRadioControl radioControl = defaultRadioControlBuilder().build();
        Attribute radioControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(radioControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        RadioAnswer radioAnswer = rAnswer(radioControl);

        FCheckboxControl checkboxControl = defaultCheckboxControlBuilder().build();
        Attribute checkboxControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST)
                .pageId(response.homePageId()).controlId(checkboxControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        CheckboxAnswer checkboxAnswer = rAnswer(checkboxControl);

        FDropdownControl dropdownControl = defaultDropdownControlBuilder().build();
        Attribute dropdownControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(dropdownControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        DropdownAnswer dropdownAnswer = rAnswer(dropdownControl);

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControlBuilder().build();
        Attribute singleLineTextControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(singleLineTextControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        SingleLineTextAnswer singleLineTextAnswer = rAnswer(singleLineTextControl);

        FMemberSelectControl memberSelectControl = defaultMemberSelectControlBuilder().build();
        Attribute memberSelectControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(memberSelectControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        MemberSelectAnswer memberSelectAnswer = rAnswer(memberSelectControl, response.memberId());

        FAddressControl addressControl = defaultAddressControlBuilder().build();
        Attribute addressControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(addressControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AddressAnswer addressAnswer = rAnswer(addressControl);

        FGeolocationControl geolocationControl = defaultGeolocationControlBuilder().build();
        Attribute geolocationControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(geolocationControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        GeolocationAnswer geolocationAnswer = rAnswer(geolocationControl);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().build();
        Attribute numberInputControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        NumberInputAnswer numberInputAnswer = rAnswer(numberInputControl);

        FNumberRankingControl numberRankingControl = defaultNumberRankingControlBuilder().build();
        Attribute numberRankingControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(numberRankingControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        NumberRankingAnswer numberRankingAnswer = rAnswer(numberRankingControl);

        FMobileNumberControl mobileNumberControl = defaultMobileNumberControlBuilder().build();
        Attribute mobileNumberControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(mobileNumberControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        MobileNumberAnswer mobileNumberAnswer = rAnswer(mobileNumberControl);

        FIdentifierControl identifierControl = defaultIdentifierControlBuilder().build();
        Attribute identifierControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(identifierControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        IdentifierAnswer identifierAnswer = rAnswer(identifierControl);

        FEmailControl emailControl = defaultEmailControlBuilder().build();
        Attribute emailControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(emailControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        EmailAnswer emailAnswer = rAnswer(emailControl);

        FDateControl dateControl = defaultDateControlBuilder().build();
        Attribute dateControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(dateControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        DateAnswer dateAnswer = rAnswer(dateControl);


        FDateTimeControl dateTimeControl = defaultDateTimeControlBuilder().build();
        Attribute dateControlTimeRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(dateTimeControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        DateTimeAnswer dateTimeAnswer = rAnswer(dateTimeControl);

        FTimeControl timeControl = defaultTimeControlBuilder().build();
        Attribute timeControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(timeControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        TimeAnswer timeAnswer = rAnswer(timeControl);

        FItemCountControl itemCountControl = defaultItemCountControlBuilder().build();
        Attribute itemCountControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(itemCountControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        ItemCountAnswer itemCountAnswer = rAnswer(itemCountControl);

        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder().build();
        Attribute itemStatusControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(itemStatusControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        ItemStatusAnswer itemStatusAnswer = rAnswer(itemStatusControl);

        FPointCheckControl pointCheckControl = defaultPointCheckControlBuilder().build();
        Attribute pointCheckControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(pointCheckControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        PointCheckAnswer pointCheckAnswer = rAnswer(pointCheckControl);

        AppApi.updateAppControls(response.jwt(), response.appId(),
                radioControl, checkboxControl, dropdownControl, singleLineTextControl, memberSelectControl, addressControl,
                geolocationControl, numberInputControl, numberRankingControl, mobileNumberControl, identifierControl, emailControl,
                dateControl, dateTimeControl, timeControl, itemCountControl, itemStatusControl, pointCheckControl);

        PAttributeDashboardControl attributeDashboardControl = defaultAttributeDashboardControlBuilder()
                .attributeIds(newArrayList(
                        radioControlRefAttribute.getId(),
                        checkboxControlRefAttribute.getId(),
                        dropdownControlRefAttribute.getId(),
                        singleLineTextControlRefAttribute.getId(),
                        memberSelectControlRefAttribute.getId(),
                        addressControlRefAttribute.getId(),
                        geolocationControlRefAttribute.getId(),
                        numberInputControlRefAttribute.getId(),
                        numberRankingControlRefAttribute.getId(),
                        mobileNumberControlRefAttribute.getId(),
                        identifierControlRefAttribute.getId(),
                        emailControlRefAttribute.getId(),
                        dateControlRefAttribute.getId(),
                        timeControlRefAttribute.getId(),
                        itemCountControlRefAttribute.getId(),
                        itemStatusControlRefAttribute.getId(),
                        pointCheckControlRefAttribute.getId(),
                        dateControlTimeRefAttribute.getId()
                ))
                .build();

        AppApi.updateAppAttributes(response.jwt(), response.appId(),
                newArrayList(
                        radioControlRefAttribute,
                        checkboxControlRefAttribute,
                        dropdownControlRefAttribute,
                        singleLineTextControlRefAttribute,
                        memberSelectControlRefAttribute,
                        addressControlRefAttribute,
                        geolocationControlRefAttribute,
                        numberInputControlRefAttribute,
                        numberRankingControlRefAttribute,
                        mobileNumberControlRefAttribute,
                        identifierControlRefAttribute,
                        emailControlRefAttribute,
                        dateControlRefAttribute,
                        timeControlRefAttribute,
                        itemCountControlRefAttribute,
                        itemStatusControlRefAttribute,
                        pointCheckControlRefAttribute,
                        dateControlTimeRefAttribute
                )
        );

        App app = appRepository.byId(response.appId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(attributeDashboardControl);
        AppApi.updateAppSetting(response.jwt(), response.appId(), setting);

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(),
                radioAnswer, checkboxAnswer, dropdownAnswer, singleLineTextAnswer, memberSelectAnswer, addressAnswer,
                geolocationAnswer, numberInputAnswer, numberRankingAnswer, mobileNumberAnswer, identifierAnswer, emailAnswer,
                dateAnswer, dateTimeAnswer, timeAnswer, itemCountAnswer, itemStatusAnswer, pointCheckAnswer);

        Member member = memberRepository.byId(response.memberId());
        CreateMemberResponse fetcherMember = MemberApi.createMemberAndLogin(response.jwt());//只要有足够权限者即可查看
        QAttributeDashboardPresentation presentation = (QAttributeDashboardPresentation) PresentationApi.fetchPresentation(
                fetcherMember.getJwt(), response.qrId(), response.homePageId(), attributeDashboardControl.getId());

        Map<String, DisplayValue> valueMap = presentation.getValues();

        TextOptionDisplayValue radioValue = (TextOptionDisplayValue) valueMap.get(radioControlRefAttribute.getId());
        assertEquals(radioAnswer.getOptionId(), radioValue.getOptionId());

        TextOptionsDisplayValue checkboxValue = (TextOptionsDisplayValue) valueMap.get(checkboxControlRefAttribute.getId());
        assertEquals(checkboxAnswer.getOptionIds(), checkboxValue.getOptionIds());

        TextOptionsDisplayValue dropdownValue = (TextOptionsDisplayValue) valueMap.get(dropdownControlRefAttribute.getId());
        assertEquals(dropdownAnswer.getOptionIds(), dropdownValue.getOptionIds());

        TextDisplayValue singleTextValue = (TextDisplayValue) valueMap.get(singleLineTextControlRefAttribute.getId());
        assertEquals(singleLineTextAnswer.getContent(), singleTextValue.getText());

        TextDisplayValue memberSelectValue = (TextDisplayValue) valueMap.get(memberSelectControlRefAttribute.getId());
        assertEquals(member.getName(), memberSelectValue.getText());

        AddressDisplayValue addressValue = (AddressDisplayValue) valueMap.get(addressControlRefAttribute.getId());
        assertEquals(addressAnswer.getAddress().getProvince(), addressValue.getAddress().getProvince());

        GeolocationDisplayValue geolocationValue = (GeolocationDisplayValue) valueMap.get(geolocationControlRefAttribute.getId());
        assertEquals(geolocationAnswer.getGeolocation(), geolocationValue.getGeolocation());

        NumberDisplayValue numberInputValue = (NumberDisplayValue) valueMap.get(numberInputControlRefAttribute.getId());
        assertEquals(numberInputAnswer.getNumber(), numberInputValue.getNumber());

        NumberDisplayValue numberRankValue = (NumberDisplayValue) valueMap.get(numberRankingControlRefAttribute.getId());
        assertEquals(numberRankingAnswer.getRank(), numberRankValue.getNumber());

        TextDisplayValue mobileValue = (TextDisplayValue) valueMap.get(mobileNumberControlRefAttribute.getId());
        assertEquals(mobileNumberAnswer.getMobileNumber(), mobileValue.getText());

        TextDisplayValue identifierValue = (TextDisplayValue) valueMap.get(identifierControlRefAttribute.getId());
        assertEquals(identifierAnswer.getContent(), identifierValue.getText());

        TextDisplayValue emailValue = (TextDisplayValue) valueMap.get(emailControlRefAttribute.getId());
        assertEquals(emailAnswer.getEmail(), emailValue.getText());

        TextDisplayValue dateValue = (TextDisplayValue) valueMap.get(dateControlRefAttribute.getId());
        assertEquals(dateAnswer.getDate(), dateValue.getText());

        TimestampDisplayValue dateTimeValue = (TimestampDisplayValue) valueMap.get(dateControlTimeRefAttribute.getId());
        assertEquals(dateTimeAnswer.toInstant(), dateTimeValue.getTimestamp());

        TextDisplayValue timeValue = (TextDisplayValue) valueMap.get(timeControlRefAttribute.getId());
        assertEquals(timeAnswer.getTime(), timeValue.getText());

        ItemCountDisplayValue itemCountValue = (ItemCountDisplayValue) valueMap.get(itemCountControlRefAttribute.getId());
        assertEquals(itemCountAnswer.getItems(), itemCountValue.getItems());

        TextOptionDisplayValue itemStatusValue = (TextOptionDisplayValue) valueMap.get(itemStatusControlRefAttribute.getId());
        assertEquals(itemStatusAnswer.getOptionId(), itemStatusValue.getOptionId());

        PointCheckDisplayValue pointCheckValue = (PointCheckDisplayValue) valueMap.get(pointCheckControlRefAttribute.getId());
        assertEquals(pointCheckAnswer.isPassed(), pointCheckValue.isPass());
    }

    @Test
    public void should_fetch_statistic_attribute_presentation_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().build();
        Attribute averageAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_AVERAGE)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute maxAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_MAX)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute minAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_MIN)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute sumAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_SUM)
                .pageId(response.homePageId()).controlId(numberInputControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        NumberInputAnswer answer1 = rAnswerBuilder(numberInputControl).number(1d).build();
        NumberInputAnswer answer2 = rAnswerBuilder(numberInputControl).number(2d).build();
        NumberInputAnswer answer3 = rAnswerBuilder(numberInputControl).number(3d).build();

        AppApi.updateAppControls(response.jwt(), response.appId(), numberInputControl);

        PAttributeDashboardControl attributeDashboardControl = defaultAttributeDashboardControlBuilder()
                .attributeIds(newArrayList(
                        averageAttribute.getId(),
                        maxAttribute.getId(),
                        minAttribute.getId(),
                        sumAttribute.getId()
                ))
                .build();

        AppApi.updateAppAttributes(response.jwt(), response.appId(),
                newArrayList(
                        averageAttribute,
                        maxAttribute,
                        minAttribute,
                        sumAttribute
                )
        );

        App app = appRepository.byId(response.appId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(attributeDashboardControl);
        AppApi.updateAppSetting(response.jwt(), response.appId(), setting);

        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer1);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer2);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer3);

        CreateMemberResponse fetcherMember = MemberApi.createMemberAndLogin(response.jwt());//只要有足够权限者即可查看
        QAttributeDashboardPresentation presentation = (QAttributeDashboardPresentation) PresentationApi.fetchPresentation(
                fetcherMember.getJwt(), response.qrId(), response.homePageId(), attributeDashboardControl.getId());

        Map<String, DisplayValue> valueMap = presentation.getValues();

        NumberDisplayValue averageValue = (NumberDisplayValue) valueMap.get(averageAttribute.getId());
        assertEquals(2, averageValue.getNumber());

        NumberDisplayValue sumValue = (NumberDisplayValue) valueMap.get(sumAttribute.getId());
        assertEquals(6, sumValue.getNumber());

        NumberDisplayValue maxValue = (NumberDisplayValue) valueMap.get(maxAttribute.getId());
        assertEquals(3, maxValue.getNumber());

        NumberDisplayValue minValue = (NumberDisplayValue) valueMap.get(minAttribute.getId());
        assertEquals(1, minValue.getNumber());
    }

    @Test
    public void should_fetch_attribute_dashboard_presentation_value() {
        PreparedAppResponse response = setupApi.registerWithApp();

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_NAME).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);
        PAttributeDashboardControl control = defaultAttributeDashboardControlBuilder().attributeIds(newArrayList(attribute.getId())).build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        String qrName = rQrName();
        CreateQrResponse qrResponse = QrApi.createQr(response.jwt(), qrName, response.defaultGroupId());

        QAttributeDashboardPresentation valuesPresentation = (QAttributeDashboardPresentation) PresentationApi.fetchPresentation(
                response.jwt(), qrResponse.getQrId(), response.homePageId(), control.getId());
        TextDisplayValue displayValue = (TextDisplayValue) valuesPresentation.getValues().get(attribute.getId());
        assertEquals(qrName, displayValue.getText());
    }
}
