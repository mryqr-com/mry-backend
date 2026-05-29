package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.display.*;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PAttributeTableControl;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.command.UpdateMemberInfoCommand;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.attributetable.QAttributeTablePresentation;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.command.UpdateQrBaseSettingCommand;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.exception.ErrorCode.VALIDATION_ATTRIBUTE_NOT_EXIST;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.time.LocalDate.ofInstant;
import static java.time.ZoneId.systemDefault;
import static org.junit.jupiter.api.Assertions.*;

public class AttributeTableControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        Attribute fixValueAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(FIXED)
                .fixedValue("whatever").build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), fixValueAttribute);
        PAttributeTableControl control = defaultAttributeTableControlBuilder().attributeIds(newArrayList(fixValueAttribute.getId())).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_if_no_attribute() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PAttributeTableControl control = defaultAttributeTableControlBuilder().attributeIds(newArrayList()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_fail_create_if_attribute_not_exit() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PAttributeTableControl control = defaultAttributeTableControlBuilder().attributeIds(newArrayList(newAttributeId())).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_ATTRIBUTE_NOT_EXIST);
    }

    @Test
    public void should_fetch_attribute_table_presentation_value() {
        PreparedAppResponse response = setupApi.registerWithApp();

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_NAME).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);
        PAttributeTableControl control = defaultAttributeTableControlBuilder().attributeIds(newArrayList(attribute.getId())).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String qrName = rQrName();
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), qrName, response.getDefaultGroupId());

        QAttributeTablePresentation valuesPresentation = (QAttributeTablePresentation) PresentationApi.fetchPresentation(response.getJwt(),
                qrResponse.getQrId(), response.getHomePageId(), control.getId());
        TextDisplayValue displayValue = (TextDisplayValue) valuesPresentation.getValues().get(attribute.getId());
        assertEquals(qrName, displayValue.getText());
    }

    @Test
    public void should_fetch_page_referenced_attribute_presentation_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String memberName = rMemberName();
        String email = rEmail();
        String mobile = rMobile();
        MemberApi.updateMember(response.getJwt(), response.getMemberId(), UpdateMemberInfoCommand.builder()
                .name(memberName)
                .email(email)
                .mobile(mobile)
                .departmentIds(List.of())
                .build());

        Attribute pageSubmitCountAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(PAGE_SUBMIT_COUNT)
                .pageId(response.getHomePageId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute pageSubmitterAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(PAGE_LAST_SUBMITTER)
                .pageId(response.getHomePageId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute pageSubmittedTimeAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(PAGE_LAST_SUBMITTED_TIME)
                .pageId(response.getHomePageId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute pageSubmittedDateAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(PAGE_LAST_SUBMITTED_DATE)
                .pageId(response.getHomePageId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute pageSubmittedMemberAndEmailAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName())
                .type(PAGE_LAST_SUBMITTER_AND_EMAIL).pageId(response.getHomePageId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute pageSubmittedMemberAndMobileAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName())
                .type(PAGE_LAST_SUBMITTER_AND_MOBILE).pageId(response.getHomePageId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(),
                pageSubmitCountAttribute,
                pageSubmitterAttribute,
                pageSubmittedTimeAttribute,
                pageSubmittedDateAttribute,
                pageSubmittedMemberAndEmailAttribute,
                pageSubmittedMemberAndMobileAttribute);

        PAttributeTableControl attributeTableControl = defaultAttributeTableControlBuilder()
                .attributeIds(newArrayList(
                        pageSubmitCountAttribute.getId(),
                        pageSubmitterAttribute.getId(),
                        pageSubmittedTimeAttribute.getId(),
                        pageSubmittedDateAttribute.getId(),
                        pageSubmittedMemberAndEmailAttribute.getId(),
                        pageSubmittedMemberAndMobileAttribute.getId()
                ))
                .build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(attributeTableControl);
        AppApi.updateAppSetting(response.getJwt(), response.getAppId(), setting);

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        QAttributeTablePresentation presentation = (QAttributeTablePresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), attributeTableControl.getId());
        Map<String, DisplayValue> valueMap = presentation.getValues();
        QR qr = qrRepository.byId(response.getQrId());
        Submission submission = submissionRepository.byId(submissionId);

        NumberDisplayValue submitAccountValue = (NumberDisplayValue) valueMap.get(pageSubmitCountAttribute.getId());
        assertEquals(1, submitAccountValue.getNumber());

        TextDisplayValue submitSubmitterValue = (TextDisplayValue) valueMap.get(pageSubmitterAttribute.getId());
        assertEquals(memberName, submitSubmitterValue.getText());

        TimestampDisplayValue submitTimeValue = (TimestampDisplayValue) valueMap.get(pageSubmittedTimeAttribute.getId());
        assertEquals(submission.getCreatedAt(), submitTimeValue.getTimestamp());

        TextDisplayValue submitDateValue = (TextDisplayValue) valueMap.get(pageSubmittedDateAttribute.getId());
        assertEquals(ofInstant(submission.getCreatedAt(), systemDefault()).toString(), submitDateValue.getText());

        EmailedMemberDisplayValue submitterAndEmailValue = (EmailedMemberDisplayValue) valueMap.get(
                pageSubmittedMemberAndEmailAttribute.getId());
        assertEquals(memberName, submitterAndEmailValue.getMember().getName());
        assertEquals(email, submitterAndEmailValue.getMember().getEmail());

        MobiledMemberDisplayValue submitterAndMobileValue = (MobiledMemberDisplayValue) valueMap.get(
                pageSubmittedMemberAndMobileAttribute.getId());
        assertEquals(memberName, submitterAndMobileValue.getMember().getName());
        assertEquals(mobile, submitterAndMobileValue.getMember().getMobile());
    }

    @Test
    public void should_fetch_instance_referenced_attribute_presentation_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        AppApi.enableAppPosition(response.getJwt(), response.getAppId());

        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(), UpdateQrBaseSettingCommand.builder()
                .customId(rCustomId())
                .name(rQrName())
                .geolocation(rGeolocation())
                .build());

        String memberName = rMemberName();
        String email = rEmail();
        String mobile = rMobile();
        MemberApi.updateMember(response.getJwt(), response.getMemberId(), UpdateMemberInfoCommand.builder()
                .name(memberName)
                .email(email)
                .mobile(mobile)
                .departmentIds(List.of())
                .build());

        String anotherMemberName = rMemberName();
        String anotherMemberMobile = rMobile();
        String anotherMemberEmail = rEmail();
        String anotherMemberId = MemberApi.createMember(response.getJwt(), anotherMemberName, anotherMemberMobile, anotherMemberEmail,
                rPassword());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), response.getMemberId(), anotherMemberId);

        Attribute instanceNameAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_NAME)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instancePlateIdAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_PLATE_ID)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceCustomIdAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CUSTOM_ID)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceGeolocationAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GEOLOCATION)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceCreateTimeAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CREATE_TIME)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceCreateDateAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CREATE_DATE)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceCreatorAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CREATOR)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceSubmitCountAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_SUBMIT_COUNT)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceAccessCountAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_ACCESS_COUNT)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceGroupAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceGroupManagersAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP_MANAGERS)
                .range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceGroupManagersMobileAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName())
                .type(INSTANCE_GROUP_MANAGERS_AND_MOBILE).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceGroupManagersEmailAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName())
                .type(INSTANCE_GROUP_MANAGERS_AND_EMAIL).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute instanceActiveStatusAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_ACTIVE_STATUS)
                .range(AttributeStatisticRange.NO_LIMIT).build();

        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(),
                instanceNameAttribute,
                instancePlateIdAttribute,
                instanceCustomIdAttribute,
                instanceGeolocationAttribute,
                instanceCreateTimeAttribute,
                instanceCreateDateAttribute,
                instanceCreatorAttribute,
                instanceSubmitCountAttribute,
                instanceAccessCountAttribute,
                instanceGroupAttribute,
                instanceGroupManagersAttribute,
                instanceGroupManagersMobileAttribute,
                instanceGroupManagersEmailAttribute,
                instanceActiveStatusAttribute
        );

        PAttributeTableControl attributeTableControl = defaultAttributeTableControlBuilder()
                .attributeIds(newArrayList(
                        instanceNameAttribute.getId(),
                        instancePlateIdAttribute.getId(),
                        instanceCustomIdAttribute.getId(),
                        instanceGeolocationAttribute.getId(),
                        instanceCreateTimeAttribute.getId(),
                        instanceCreateDateAttribute.getId(),
                        instanceCreatorAttribute.getId(),
                        instanceSubmitCountAttribute.getId(),
                        instanceAccessCountAttribute.getId(),
                        instanceGroupAttribute.getId(),
                        instanceGroupManagersAttribute.getId(),
                        instanceGroupManagersMobileAttribute.getId(),
                        instanceGroupManagersEmailAttribute.getId(),
                        instanceActiveStatusAttribute.getId()
                ))
                .build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(attributeTableControl);
        AppApi.updateAppSetting(response.getJwt(), response.getAppId(), setting);

        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        QrApi.fetchSubmissionQr(response.getJwt(), response.getPlateId());
        QAttributeTablePresentation presentation = (QAttributeTablePresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), attributeTableControl.getId());
        QR qr = qrRepository.byId(response.getQrId());
        Group group = groupRepository.byId(response.getDefaultGroupId());

        Map<String, DisplayValue> valueMap = presentation.getValues();
        TextDisplayValue instanceNameValue = (TextDisplayValue) valueMap.get(instanceNameAttribute.getId());
        assertEquals(qr.getName(), instanceNameValue.getText());

        TextDisplayValue instancePlateIdValue = (TextDisplayValue) valueMap.get(instancePlateIdAttribute.getId());
        assertEquals(qr.getPlateId(), instancePlateIdValue.getText());

        TextDisplayValue instanceCustomIdValue = (TextDisplayValue) valueMap.get(instanceCustomIdAttribute.getId());
        assertEquals(qr.getCustomId(), instanceCustomIdValue.getText());

        GeolocationDisplayValue instanceGeolocationValue = (GeolocationDisplayValue) valueMap.get(instanceGeolocationAttribute.getId());
        assertEquals(qr.getGeolocation(), instanceGeolocationValue.getGeolocation());

        TimestampDisplayValue instanceCreateTimeValue = (TimestampDisplayValue) valueMap.get(instanceCreateTimeAttribute.getId());
        assertEquals(qr.getCreatedAt(), instanceCreateTimeValue.getTimestamp());

        TextDisplayValue instanceCreateDateValue = (TextDisplayValue) valueMap.get(instanceCreateDateAttribute.getId());
        assertEquals(ofInstant(qr.getCreatedAt(), systemDefault()).toString(), instanceCreateDateValue.getText());

        TextDisplayValue instanceCreatorValue = (TextDisplayValue) valueMap.get(instanceCreatorAttribute.getId());
        assertEquals(memberName, instanceCreatorValue.getText());

        NumberDisplayValue instanceSubmitCountValue = (NumberDisplayValue) valueMap.get(instanceSubmitCountAttribute.getId());
        assertEquals(1, instanceSubmitCountValue.getNumber());

        NumberDisplayValue instanceAccessCountValue = (NumberDisplayValue) valueMap.get(instanceAccessCountAttribute.getId());
        assertTrue(instanceAccessCountValue.getNumber() == 0 || instanceAccessCountValue.getNumber() == 1);//异步处理，有时0有时1

        TextDisplayValue instanceGroupValue = (TextDisplayValue) valueMap.get(instanceGroupAttribute.getId());
        assertEquals(group.getName(), instanceGroupValue.getText());

        TextDisplayValue instanceGroupManagersValue = (TextDisplayValue) valueMap.get(instanceGroupManagersAttribute.getId());
        assertEquals(memberName + ", " + anotherMemberName, instanceGroupManagersValue.getText());

        EmailedMembersDisplayValue instanceGroupManagersEmailValue = (EmailedMembersDisplayValue) valueMap.get(
                instanceGroupManagersEmailAttribute.getId());
        assertTrue(instanceGroupManagersEmailValue.getMembers()
                .contains(EmailedMember.builder().id(response.getMemberId()).name(memberName).email(email).build()));
        assertTrue(instanceGroupManagersEmailValue.getMembers()
                .contains(EmailedMember.builder().id(anotherMemberId).name(anotherMemberName).email(anotherMemberEmail).build()));

        MobiledMembersDisplayValue instanceGroupManagersMobileValue = (MobiledMembersDisplayValue) valueMap.get(
                instanceGroupManagersMobileAttribute.getId());
        assertTrue(instanceGroupManagersMobileValue.getMembers()
                .contains(MobiledMember.builder().id(response.getMemberId()).name(memberName).mobile(mobile).build()));
        assertTrue(instanceGroupManagersMobileValue.getMembers()
                .contains(MobiledMember.builder().id(anotherMemberId).name(anotherMemberName).mobile(anotherMemberMobile).build()));

        BooleanDisplayValue instanceActiveStatusValue = (BooleanDisplayValue) valueMap.get(instanceActiveStatusAttribute.getId());
        assertTrue(instanceActiveStatusValue.isYes());
    }
}
