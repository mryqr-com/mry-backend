package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FMemberSelectControl;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.MembersAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.memberselect.MemberSelectAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import com.mryqr.utils.RandomTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.ui.MinMaxSetting.minMaxOf;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemberSelectControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMemberSelectControl control = defaultMemberSelectControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control.getId(), updatedControl.getId());
    }

    @Test
    public void should_fail_create_control_for_multiple_if_option_size_is_greater_than_100() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMemberSelectControl control = defaultMemberSelectControlBuilder().multiple(true).minMaxSetting(minMaxOf(1, 101)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), MAX_OVERFLOW);
    }

    @Test
    public void should_fail_create_control_for_multiple_if_option_size_is_less_than_0() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMemberSelectControl control = defaultMemberSelectControlBuilder().multiple(true).minMaxSetting(minMaxOf(-1, 10)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), MIN_OVERFLOW);
    }

    @Test
    public void create_control_should_reset_min_max_setting_for_non_multiple() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FMemberSelectControl control = defaultMemberSelectControlBuilder().multiple(false).minMaxSetting(minMaxOf(5, 100)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        FMemberSelectControl updatedControl = (FMemberSelectControl) app.controlByIdOptional(control.getId()).get();
        assertEquals(0, updatedControl.getMinMaxSetting().getMin());
        assertEquals(10, updatedControl.getMinMaxSetting().getMax());
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FMemberSelectControl control = defaultMemberSelectControlBuilder().multiple(rBool()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MemberSelectAnswer answer = RandomTestFixture.rAnswer(control, response.getMemberId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
        Submission submission = submissionRepository.byId(submissionId);
        MemberSelectAnswer updatedAnswer = (MemberSelectAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertTrue(indexedValue.getTv().containsAll(answer.getMemberIds()));
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FMemberSelectControl control = defaultMemberSelectControlBuilder().fillableSetting(
                defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MemberSelectAnswer answer = rAnswerBuilder(control).memberIds(newArrayList()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_fail_answer_for_multiple_if_member_size_greater_than_max() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String memberId1 = MemberApi.createMember(response.getJwt());
        String memberId2 = MemberApi.createMember(response.getJwt());
        String memberId3 = MemberApi.createMember(response.getJwt());
        FMemberSelectControl control = defaultMemberSelectControlBuilder().multiple(true).minMaxSetting(minMaxOf(1, 2)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MemberSelectAnswer answer = rAnswerBuilder(control).memberIds(newArrayList(memberId1, memberId2, memberId3)).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MEMBER_MAX_SELECTION_REACHED);
    }

    @Test
    public void should_fail_answer_for_multiple_if_member_size_is_less_than_min() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String memberId1 = MemberApi.createMember(response.getJwt());
        String memberId2 = MemberApi.createMember(response.getJwt());
        FMemberSelectControl control = defaultMemberSelectControlBuilder().multiple(true).minMaxSetting(minMaxOf(3, 4)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MemberSelectAnswer answer = rAnswerBuilder(control).memberIds(newArrayList(memberId1, memberId2)).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MEMBER_MIN_SELECTION_NOT_REACHED);
    }

    @Test
    public void should_fail_answer_for_single_if_more_than_1_member_provided() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String memberId1 = MemberApi.createMember(response.getJwt());
        String memberId2 = MemberApi.createMember(response.getJwt());
        FMemberSelectControl control = defaultMemberSelectControlBuilder().multiple(false).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MemberSelectAnswer answer = rAnswerBuilder(control).memberIds(newArrayList(memberId1, memberId2)).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), SINGLE_MEMBER_ONLY_ALLOW_SINGLE_ANSWER);
    }

    @Test
    public void should_fail_answer_if_member_not_exists() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FMemberSelectControl control = defaultMemberSelectControlBuilder().build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        MemberSelectAnswer answer = rAnswerBuilder(control).memberIds(newArrayList(Member.newMemberId())).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), NOT_ALL_MEMBERS_EXIST);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String memberId1 = MemberApi.createMember(response.getJwt());
        String memberId2 = MemberApi.createMember(response.getJwt());
        FMemberSelectControl control = defaultMemberSelectControlBuilder().multiple(true).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST)
                .pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        MemberSelectAnswer answer = rAnswer(control, memberId1, memberId2);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        MembersAttributeValue attributeValue = (MembersAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getMemberIds(), attributeValue.getMemberIds());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.containsAll(answer.getMemberIds()));
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String memberId1 = MemberApi.createMember(response.getJwt());
        String memberId2 = MemberApi.createMember(response.getJwt());
        FMemberSelectControl control = defaultMemberSelectControlBuilder().multiple(true).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        MemberSelectAnswer answer = rAnswer(control, memberId1, memberId2);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        MembersAttributeValue attributeValue = (MembersAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getMemberIds(), attributeValue.getMemberIds());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.containsAll(answer.getMemberIds()));
    }
}
