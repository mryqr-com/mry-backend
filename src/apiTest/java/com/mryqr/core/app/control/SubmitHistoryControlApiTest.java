package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.display.*;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.submithistory.QSubmitHistoryPresentation;
import com.mryqr.core.presentation.query.submithistory.QSubmitHistorySubmission;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.address.AddressAnswer;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
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
import static com.mryqr.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.*;

public class SubmitHistoryControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PSubmitHistoryControl control = defaultSubmitHistoryControlBuilder().pageIds(newArrayList(response.getHomePageId())).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_if_no_page_selected() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PSubmitHistoryControl control = defaultSubmitHistoryControlBuilder().build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_fail_create_control_if_referenced_page_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PSubmitHistoryControl control = defaultSubmitHistoryControlBuilder().pageIds(newArrayList(Page.newPageId())).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fetch_submit_history_presentation_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        ControlFillableSetting fillableSetting = ControlFillableSetting.builder().submissionSummaryEligible(true).build();

        FRadioControl radioControl = defaultRadioControlBuilder().fillableSetting(fillableSetting).build();
        RadioAnswer radioAnswer = rAnswer(radioControl);

        FCheckboxControl checkboxControl = defaultCheckboxControlBuilder().fillableSetting(fillableSetting).build();
        CheckboxAnswer checkboxAnswer = rAnswer(checkboxControl);

        FDropdownControl dropdownControl = defaultDropdownControlBuilder().fillableSetting(fillableSetting).build();
        DropdownAnswer dropdownAnswer = rAnswer(dropdownControl);

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControlBuilder().fillableSetting(fillableSetting).build();
        SingleLineTextAnswer singleLineTextAnswer = rAnswer(singleLineTextControl);

        FMemberSelectControl memberSelectControl = defaultMemberSelectControlBuilder().fillableSetting(fillableSetting).build();
        MemberSelectAnswer memberSelectAnswer = rAnswer(memberSelectControl, response.getMemberId());

        FAddressControl addressControl = defaultAddressControlBuilder().fillableSetting(fillableSetting).build();
        AddressAnswer addressAnswer = rAnswer(addressControl);

        FGeolocationControl geolocationControl = defaultGeolocationControlBuilder().fillableSetting(fillableSetting).build();
        GeolocationAnswer geolocationAnswer = rAnswer(geolocationControl);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().fillableSetting(fillableSetting).build();
        NumberInputAnswer numberInputAnswer = rAnswer(numberInputControl);

        FNumberRankingControl numberRankingControl = defaultNumberRankingControlBuilder().fillableSetting(fillableSetting).build();
        NumberRankingAnswer numberRankingAnswer = rAnswer(numberRankingControl);

        FMobileNumberControl mobileNumberControl = defaultMobileNumberControlBuilder().fillableSetting(fillableSetting).build();
        MobileNumberAnswer mobileNumberAnswer = rAnswer(mobileNumberControl);

        FIdentifierControl identifierControl = defaultIdentifierControlBuilder().fillableSetting(fillableSetting).build();
        IdentifierAnswer identifierAnswer = rAnswer(identifierControl);

        FEmailControl emailControl = defaultEmailControlBuilder().fillableSetting(fillableSetting).build();
        EmailAnswer emailAnswer = rAnswer(emailControl);

        FDateControl dateControl = defaultDateControlBuilder().fillableSetting(fillableSetting).build();
        DateAnswer dateAnswer = rAnswer(dateControl);

        FTimeControl timeControl = defaultTimeControlBuilder().fillableSetting(fillableSetting).build();
        TimeAnswer timeAnswer = rAnswer(timeControl);

        FItemCountControl itemCountControl = defaultItemCountControlBuilder().fillableSetting(fillableSetting).build();
        ItemCountAnswer itemCountAnswer = rAnswer(itemCountControl);

        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder().fillableSetting(fillableSetting).build();
        ItemStatusAnswer itemStatusAnswer = rAnswer(itemStatusControl);

        FPointCheckControl pointCheckControl = defaultPointCheckControlBuilder().fillableSetting(fillableSetting).build();
        PointCheckAnswer pointCheckAnswer = rAnswer(pointCheckControl);

        PSubmitHistoryControl submitHistoryControl = defaultSubmitHistoryControlBuilder().pageIds(newArrayList(response.getHomePageId()))
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(),
                radioControl, checkboxControl, dropdownControl, singleLineTextControl, memberSelectControl, addressControl,
                geolocationControl, numberInputControl, numberRankingControl, mobileNumberControl, identifierControl, emailControl,
                dateControl, timeControl, itemCountControl, itemStatusControl, pointCheckControl, submitHistoryControl);

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                radioAnswer, checkboxAnswer, dropdownAnswer, singleLineTextAnswer, memberSelectAnswer, addressAnswer,
                geolocationAnswer, numberInputAnswer, numberRankingAnswer, mobileNumberAnswer, identifierAnswer, emailAnswer,
                dateAnswer, timeAnswer, itemCountAnswer, itemStatusAnswer, pointCheckAnswer);

        Submission dbSubmission = submissionRepository.byId(submissionId);
        Member member = memberRepository.byId(response.getMemberId());

        CreateMemberResponse fetcherMember = MemberApi.createMemberAndLogin(response.getJwt());//只要有足够权限者即可查看
        QSubmitHistoryPresentation presentation = (QSubmitHistoryPresentation) PresentationApi.fetchPresentation(fetcherMember.getJwt(),
                response.getQrId(), response.getHomePageId(), submitHistoryControl.getId());
        QSubmitHistorySubmission submission = presentation.getSubmissions().get(0);
        assertEquals(dbSubmission.getId(), submission.getId());
        assertEquals(dbSubmission.getPageId(), submission.getPageId());
        assertEquals(dbSubmission.getCreatedAt(), submission.getCreatedAt());
        assertEquals(member.getId(), submission.getCreatedBy());
        assertEquals(member.getName(), submission.getCreator());
        Map<String, DisplayValue> valueMap = submission.getValues();

        TextOptionDisplayValue radioValue = (TextOptionDisplayValue) valueMap.get(radioControl.getId());
        assertEquals(radioAnswer.getOptionId(), radioValue.getOptionId());

        TextOptionsDisplayValue checkboxValue = (TextOptionsDisplayValue) valueMap.get(checkboxControl.getId());
        assertEquals(checkboxAnswer.getOptionIds(), checkboxValue.getOptionIds());

        TextOptionsDisplayValue dropdownValue = (TextOptionsDisplayValue) valueMap.get(dropdownControl.getId());
        assertEquals(dropdownAnswer.getOptionIds(), dropdownValue.getOptionIds());

        TextDisplayValue singleTextValue = (TextDisplayValue) valueMap.get(singleLineTextControl.getId());
        assertEquals(singleLineTextAnswer.getContent(), singleTextValue.getText());

        TextDisplayValue memberSelectValue = (TextDisplayValue) valueMap.get(memberSelectControl.getId());
        assertEquals(member.getName(), memberSelectValue.getText());

        AddressDisplayValue addressValue = (AddressDisplayValue) valueMap.get(addressControl.getId());
        assertEquals(addressAnswer.getAddress().getProvince(), addressValue.getAddress().getProvince());

        GeolocationDisplayValue geolocationValue = (GeolocationDisplayValue) valueMap.get(geolocationControl.getId());
        assertEquals(geolocationAnswer.getGeolocation(), geolocationValue.getGeolocation());

        NumberDisplayValue numberInputValue = (NumberDisplayValue) valueMap.get(numberInputControl.getId());
        assertEquals(numberInputAnswer.getNumber(), numberInputValue.getNumber());

        NumberDisplayValue numberRankValue = (NumberDisplayValue) valueMap.get(numberRankingControl.getId());
        assertEquals(numberRankingAnswer.getRank(), numberRankValue.getNumber());

        TextDisplayValue mobileValue = (TextDisplayValue) valueMap.get(mobileNumberControl.getId());
        assertEquals(mobileNumberAnswer.getMobileNumber(), mobileValue.getText());

        TextDisplayValue identifierValue = (TextDisplayValue) valueMap.get(identifierControl.getId());
        assertEquals(identifierAnswer.getContent(), identifierValue.getText());

        TextDisplayValue emailValue = (TextDisplayValue) valueMap.get(emailControl.getId());
        assertEquals(emailAnswer.getEmail(), emailValue.getText());

        TextDisplayValue dateValue = (TextDisplayValue) valueMap.get(dateControl.getId());
        assertEquals(dateAnswer.getDate(), dateValue.getText());

        TextDisplayValue timeValue = (TextDisplayValue) valueMap.get(timeControl.getId());
        assertEquals(timeAnswer.getTime(), timeValue.getText());

        ItemCountDisplayValue itemCountValue = (ItemCountDisplayValue) valueMap.get(itemCountControl.getId());
        assertEquals(itemCountAnswer.getItems(), itemCountValue.getItems());

        TextOptionDisplayValue itemStatusValue = (TextOptionDisplayValue) valueMap.get(itemStatusControl.getId());
        assertEquals(itemStatusAnswer.getOptionId(), itemStatusValue.getOptionId());

        PointCheckDisplayValue pointCheckValue = (PointCheckDisplayValue) valueMap.get(pointCheckControl.getId());
        assertEquals(pointCheckAnswer.isPassed(), pointCheckValue.isPass());
    }
}
