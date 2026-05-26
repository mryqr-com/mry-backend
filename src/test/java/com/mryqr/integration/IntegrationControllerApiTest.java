package com.mryqr.integration;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.Geolocation;
import com.mryqr.common.domain.idnode.IdTreeHierarchy;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.setting.PageSetting;
import com.mryqr.core.department.DepartmentApi;
import com.mryqr.core.department.command.CreateDepartmentCommand;
import com.mryqr.core.department.domain.Department;
import com.mryqr.core.department.domain.event.DepartmentManagersChangedEvent;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.group.command.CreateGroupCommand;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.event.GroupCreatedEvent;
import com.mryqr.core.group.domain.event.GroupManagersChangedEvent;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.command.UpdateMemberInfoCommand;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.event.MemberAddedToDepartmentEvent;
import com.mryqr.core.member.domain.event.MemberRemovedFromDepartmentEvent;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.IdentifierAttributeValue;
import com.mryqr.core.qr.domain.attribute.TextAttributeValue;
import com.mryqr.core.qr.domain.event.*;
import com.mryqr.core.register.command.RegisterResponse;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.integration.app.query.QIntegrationApp;
import com.mryqr.integration.app.query.QIntegrationListApp;
import com.mryqr.integration.department.command.IntegrationCreateDepartmentCommand;
import com.mryqr.integration.department.command.IntegrationUpdateDepartmentCustomIdCommand;
import com.mryqr.integration.department.query.QIntegrationDepartment;
import com.mryqr.integration.department.query.QIntegrationListDepartment;
import com.mryqr.integration.group.command.*;
import com.mryqr.integration.group.query.QIntegrationGroup;
import com.mryqr.integration.group.query.QIntegrationListGroup;
import com.mryqr.integration.member.command.IntegrationCreateMemberCommand;
import com.mryqr.integration.member.command.IntegrationUpdateMemberCustomIdCommand;
import com.mryqr.integration.member.command.IntegrationUpdateMemberInfoCommand;
import com.mryqr.integration.member.query.QIntegrationListMember;
import com.mryqr.integration.member.query.QIntegrationMember;
import com.mryqr.integration.qr.command.*;
import com.mryqr.integration.qr.query.QIntegrationQr;
import com.mryqr.integration.submission.command.IntegrationNewSubmissionCommand;
import com.mryqr.integration.submission.command.IntegrationUpdateSubmissionCommand;
import com.mryqr.integration.submission.query.QIntegrationSubmission;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.mryqr.common.event.DomainEventType.*;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeType.DIRECT_INPUT;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CUSTOM_ID;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_MEMBER;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.util.List.of;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrationControllerApiTest extends BaseApiTest {

    @Test
    public void domain_events_mechanism_should_work_for_integration() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .build());

        GroupCreatedEvent groupCreatedEvent = latestEventFor(groupId, GROUP_CREATED, GroupCreatedEvent.class);
        assertEquals(groupId, groupCreatedEvent.getGroupId());
        assertEquals(response.appId(), groupCreatedEvent.getAppId());
        Tenant updatedTenant = tenantRepository.byId(response.tenantId());
        assertEquals(2, updatedTenant.getResourceUsage().getGroupCountForApp(response.appId()));
    }

    @Test
    public void should_list_apps() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        App app = appRepository.byId(response.appId());

        List<QIntegrationListApp> listApps = IntegrationApi.listApps(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret());
        assertEquals(1, listApps.size());
        QIntegrationListApp listApp = listApps.get(0);

        assertEquals(app.getId(), listApp.getId());
        assertEquals(app.getName(), listApp.getName());
        assertEquals(app.isActive(), listApp.isActive());
        assertEquals(app.isLocked(), listApp.isLocked());
        assertEquals(app.getVersion(), listApp.getVersion());
        assertEquals(app.getPermission(), listApp.getPermission());
        assertEquals(app.getOperationPermission(), listApp.getOperationPermission());
    }

    @Test
    public void should_fetch_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        App app = appRepository.byId(response.appId());

        QIntegrationApp qIntegrationApp = IntegrationApi.fetchApp(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(),
                app.getId());

        assertEquals(app.getId(), qIntegrationApp.getId());
        assertEquals(app.getName(), qIntegrationApp.getName());
        assertEquals(app.getIcon(), qIntegrationApp.getIcon());
        assertEquals(app.isActive(), qIntegrationApp.isActive());
        assertEquals(app.isLocked(), qIntegrationApp.isLocked());
        assertEquals(app.getVersion(), qIntegrationApp.getVersion());
        assertEquals(app.getSetting(), qIntegrationApp.getSetting());
        assertEquals(app.getPermission(), qIntegrationApp.getPermission());
        assertEquals(app.getOperationPermission(), qIntegrationApp.getOperationPermission());
        assertEquals(app.getCreatedAt(), qIntegrationApp.getCreatedAt());
        assertEquals(app.getCreatedBy(), qIntegrationApp.getCreatedBy());
    }

    @Test
    public void should_deactivate_and_activate_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationApi.deactivateApp(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), response.appId());
        assertFalse(appRepository.byId(response.appId()).isActive());

        IntegrationApi.activateApp(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), response.appId());
        assertTrue(appRepository.byId(response.appId()).isActive());
    }

    @Test
    public void should_new_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        SingleLineTextAnswer answer = rAnswer(control);
        String submissionId = IntegrationApi.createSubmission(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationNewSubmissionCommand.builder()
                        .pageId(response.homePageId())
                        .answers(newHashSet(answer))
                        .build());

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(1, submission.allAnswers().size());
        assertEquals(response.appId(), submission.getAppId());
        assertEquals(response.qrId(), submission.getQrId());
        assertEquals(response.homePageId(), submission.getPageId());
        assertNull(submission.getCreatedBy());
        assertEquals(response.defaultGroupId(), submission.getGroupId());
        assertEquals(response.tenantId(), submission.getTenantId());
        assertEquals(control.getId(), submission.allAnswers().values().stream().findAny().get().getControlId());
        assertNull(submission.getApproval());
    }

    @Test
    public void should_new_submission_for_member() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        SingleLineTextAnswer answer = rAnswer(control);
        String submissionId = IntegrationApi.createSubmission(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationNewSubmissionCommand.builder()
                        .pageId(response.homePageId())
                        .answers(newHashSet(answer))
                        .memberId(response.memberId())
                        .build());

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(response.memberId(), submission.getCreatedBy());
    }

    @Test
    public void should_new_submission_for_custom_member_id() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customMemberId = rCustomId();
        IntegrationCreateMemberCommand command = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(customMemberId)
                .build();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                command);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        SingleLineTextAnswer answer = rAnswer(control);
        String submissionId = IntegrationApi.createSubmission(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationNewSubmissionCommand.builder()
                        .pageId(response.homePageId())
                        .answers(newHashSet(answer))
                        .memberCustomId(customMemberId)
                        .build());

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(memberId, submission.getCreatedBy());
    }

    @Test
    public void should_new_submission_by_qr_custom_id() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String qrCustomId = rCustomId();
        IntegrationCreateQrSimpleCommand createQrCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(qrCustomId)
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createQrCommand);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        SingleLineTextAnswer answer = rAnswer(control);
        String submissionId = IntegrationApi.createSubmissionByQrCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(),
                qrCustomId,
                IntegrationNewSubmissionCommand.builder()
                        .pageId(response.homePageId())
                        .answers(newHashSet(answer))
                        .build());

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(1, submission.allAnswers().size());
        assertEquals(response.appId(), submission.getAppId());
        assertEquals(qrResponse.getQrId(), submission.getQrId());
        assertEquals(response.homePageId(), submission.getPageId());
        assertNull(submission.getCreatedBy());
        assertEquals(response.defaultGroupId(), submission.getGroupId());
        assertEquals(response.tenantId(), submission.getTenantId());
        assertEquals(control.getId(), submission.allAnswers().values().stream().findAny().get().getControlId());
        assertNull(submission.getApproval());
    }

    @Test
    public void should_update_submission_when_create_if_submission_already_exists_for_once_per_instance() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder().submitType(ONCE_PER_INSTANCE).build();
        AppApi.updateAppHomePageSettingAndControls(response.jwt(), response.appId(), pageSetting, control);

        String submissionId = IntegrationApi.createSubmission(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationNewSubmissionCommand.builder()
                        .pageId(response.homePageId())
                        .answers(newHashSet(rAnswer(control)))
                        .build());

        SingleLineTextAnswer newAnswer = rAnswer(control);
        String updatedSubmissionId = IntegrationApi.createSubmission(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationNewSubmissionCommand.builder()
                        .pageId(response.homePageId())
                        .answers(newHashSet(newAnswer))
                        .build());

        assertEquals(submissionId, updatedSubmissionId);
        Submission submission = submissionRepository.byId(submissionId);
        SingleLineTextAnswer updatedAnswer = (SingleLineTextAnswer) submission.getAnswers().get(control.getId());
        assertEquals(newAnswer.getContent(), updatedAnswer.getContent());
    }

    @Test
    public void should_not_create_submission_for_once_per_member_submit_type() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder().submitType(ONCE_PER_MEMBER).build();
        AppApi.updateAppHomePageSettingAndControls(response.jwt(), response.appId(), pageSetting, control);

        assertError(() -> IntegrationApi.createSubmissionRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationNewSubmissionCommand.builder()
                        .pageId(response.homePageId())
                        .answers(newHashSet(rAnswer(control)))
                        .build()), SUBMISSION_REQUIRE_MEMBER);
    }

    @Test
    public void should_fail_create_submission_if_qr_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        QrApi.deactivate(response.jwt(), response.qrId());

        assertError(() -> IntegrationApi.createSubmissionRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationNewSubmissionCommand.builder()
                        .pageId(response.homePageId())
                        .answers(newHashSet(rAnswer(control)))
                        .build()), QR_NOT_ACTIVE);
    }

    @Test
    public void should_fail_create_submission_if_group_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr();
        GroupApi.createGroup(response.jwt(), response.appId());

        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        GroupApi.deactivateGroup(response.jwt(), response.defaultGroupId());

        assertError(() -> IntegrationApi.createSubmissionRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationNewSubmissionCommand.builder()
                        .pageId(response.homePageId())
                        .answers(newHashSet(rAnswer(control)))
                        .build()), GROUP_NOT_ACTIVE);
    }

    @Test
    public void should_create_both_submission_and_qr_for_template_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        QrApi.markTemplate(response.jwt(), response.qrId());
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        SingleLineTextAnswer answer = rAnswer(control);
        String submissionId = IntegrationApi.createSubmission(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationNewSubmissionCommand.builder()
                        .pageId(response.homePageId())
                        .answers(newHashSet(answer))
                        .build());

        Submission submission = submissionRepository.byId(submissionId);
        assertNotEquals(response.qrId(), submission.getQrId());
        QR qr = qrRepository.byId(submission.getQrId());
        assertEquals(response.appId(), qr.getAppId());
        assertEquals(response.defaultGroupId(), qr.getGroupId());
        assertNotEquals(response.plateId(), qr.getPlateId());
        Plate plate = plateRepository.byId(qr.getPlateId());
        assertEquals(response.defaultGroupId(), plate.getGroupId());
        assertEquals(qr.getId(), plate.getQrId());
        assertEquals(qr.getAppId(), plate.getAppId());
    }

    @Test
    public void should_update_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));
        SingleLineTextAnswer updateAnswer = rAnswer(control);
        IntegrationApi.updateSubmission(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), submissionId,
                IntegrationUpdateSubmissionCommand.builder().answers(newHashSet(updateAnswer)).build());

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(updateAnswer, submission.answerForControlOptional(control.getId()).get());
    }

    @Test
    public void should_update_submission_for_member() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));
        SingleLineTextAnswer updateAnswer = rAnswer(control);
        IntegrationApi.updateSubmission(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), submissionId,
                IntegrationUpdateSubmissionCommand.builder().answers(newHashSet(updateAnswer)).memberId(response.memberId()).build());

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(updateAnswer, submission.answerForControlOptional(control.getId()).get());
        assertEquals(response.memberId(), submission.getUpdatedBy());
    }

    @Test
    public void should_update_submission_for_custom_member_id() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customMemberId = rCustomId();
        IntegrationCreateMemberCommand command = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(customMemberId)
                .build();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                command);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));
        SingleLineTextAnswer updateAnswer = rAnswer(control);
        IntegrationApi.updateSubmission(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), submissionId,
                IntegrationUpdateSubmissionCommand.builder().answers(newHashSet(updateAnswer)).memberCustomId(customMemberId).build());

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(updateAnswer, submission.answerForControlOptional(control.getId()).get());
        assertEquals(memberId, submission.getUpdatedBy());
    }

    @Test
    public void should_fail_update_submission_if_qr_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));
        QrApi.deactivate(response.jwt(), response.qrId());

        IntegrationUpdateSubmissionCommand updateSubmissionCommand = IntegrationUpdateSubmissionCommand.builder()
                .answers(newHashSet(rAnswer(control))).build();
        assertError(
                () -> IntegrationApi.updateSubmissionRaw(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), submissionId,
                        updateSubmissionCommand), QR_NOT_ACTIVE);
    }

    @Test
    public void should_delete_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));
        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(1, submission.allAnswers().size());
        assertEquals(response.appId(), submission.getAppId());

        IntegrationApi.deleteSubmission(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), submissionId);
        Optional<Submission> deleted = submissionRepository.byIdOptional(submissionId);
        assertTrue(deleted.isEmpty());
    }

    @Test
    public void should_fetch_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        String submissionId = SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), rAnswer(control));
        SubmissionApi.updateSubmission(response.jwt(), submissionId, rAnswer(control));

        QIntegrationSubmission qIntegrationSubmission = IntegrationApi.fetchSubmission(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), submissionId);
        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(submission.getId(), qIntegrationSubmission.getId());
        assertEquals(submission.getQrId(), qIntegrationSubmission.getQrId());
        assertEquals(submission.getPlateId(), qIntegrationSubmission.getPlateId());
        assertEquals(submission.getGroupId(), qIntegrationSubmission.getGroupId());
        assertEquals(submission.getAppId(), qIntegrationSubmission.getAppId());
        assertEquals(submission.getPageId(), qIntegrationSubmission.getPageId());
        assertEquals(submission.getAnswers(), qIntegrationSubmission.getAnswers());
        assertEquals(submission.getApproval(), qIntegrationSubmission.getApproval());
        assertEquals(submission.getUpdatedAt(), qIntegrationSubmission.getUpdatedAt());
        assertEquals(submission.getUpdatedBy(), qIntegrationSubmission.getUpdatedBy());
        assertEquals(submission.getCreatedAt(), qIntegrationSubmission.getCreatedAt());
        assertEquals(submission.getCreatedBy(), qIntegrationSubmission.getCreatedBy());
    }

    @Test
    public void should_create_qr_simple() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateQrSimpleCommand command = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(rCustomId())
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), command);
        assertNotNull(qrResponse.getQrId());
        assertNotNull(qrResponse.getPlateId());
        assertEquals(response.appId(), qrResponse.getAppId());
        assertEquals(response.defaultGroupId(), qrResponse.getGroupId());

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertNotNull(qr);
        assertEquals(qrResponse.getPlateId(), qr.getPlateId());
        assertEquals(command.getCustomId(), qr.getCustomId());
    }

    @Test
    public void should_fail_create_qr_if_custom_id_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customId = rCustomId();
        IntegrationCreateQrSimpleCommand command = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(customId)
                .build();
        IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), command);

        IntegrationCreateQrSimpleCommand anotherCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is another QR")
                .groupId(response.defaultGroupId())
                .customId(customId)
                .build();

        assertError(
                () -> IntegrationApi.createQrSimpleRaw(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), anotherCommand),
                QR_WITH_CUSTOM_ID_ALREADY_EXISTS);
    }

    @Test
    public void should_create_qr_advanced() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        IntegrationCreateQrAdvancedCommand command = IntegrationCreateQrAdvancedCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(rCustomId())
                .description(rSentence(100))
                .headerImageUrl(rImageFile().getFileUrl())
                .directAttributeValues(Maps.of(attribute.getId(), "hello"))
                .geolocation(rGeolocation())
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrAdvanced(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                command);

        assertNotNull(qrResponse.getQrId());
        assertNotNull(qrResponse.getPlateId());
        assertEquals(response.appId(), qrResponse.getAppId());
        assertEquals(response.defaultGroupId(), qrResponse.getGroupId());

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertNotNull(qr);
        assertEquals(qrResponse.getPlateId(), qr.getPlateId());
        assertEquals(command.getName(), qr.getName());
        assertEquals(command.getDescription(), qr.getDescription());
        assertEquals(command.getCustomId(), qr.getCustomId());
        assertEquals(command.getHeaderImageUrl(), qr.getHeaderImage().getFileUrl());
        assertEquals(command.getGeolocation(), qr.getGeolocation());
        TextAttributeValue attributeValue = (TextAttributeValue) qr.attributeValueOf(attribute.getId());
        assertEquals("hello", attributeValue.getText());
    }

    @Test
    public void should_delete_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        assertTrue(qrRepository.byIdOptional(response.qrId()).isPresent());

        IntegrationApi.deleteQr(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), response.qrId());

        assertFalse(qrRepository.byIdOptional(response.qrId()).isPresent());
    }

    @Test
    public void should_deactivate_and_activate_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationApi.deactivateQr(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), response.qrId());
        assertFalse(qrRepository.byId(response.qrId()).isActive());

        IntegrationApi.activateQr(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), response.qrId());
        assertTrue(qrRepository.byId(response.qrId()).isActive());
    }

    @Test
    public void should_deactivate_and_activate_qr_by_custom_id() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customId = rCustomId();
        IntegrationCreateQrSimpleCommand createQrCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(customId)
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createQrCommand);

        IntegrationApi.deactivateQrByCustomId(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), response.appId(),
                customId);
        assertFalse(qrRepository.byId(qrResponse.getQrId()).isActive());

        IntegrationApi.activateQrByCustomId(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), response.appId(),
                customId);
        assertTrue(qrRepository.byId(response.qrId()).isActive());
    }

    @Test
    public void should_delete_qr_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateQrSimpleCommand createQrCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(rCustomId())
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createQrCommand);
        Optional<QR> qrOptional = qrRepository.byIdOptional(qrResponse.getQrId());
        assertTrue(qrOptional.isPresent());

        IntegrationApi.deleteQrByCustomId(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), response.appId(),
                qrOptional.get().getCustomId());

        assertFalse(qrRepository.byIdOptional(qrResponse.getQrId()).isPresent());
    }

    @Test
    public void should_rename_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationApi.renameQr(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationRenameQrCommand.builder().name("aQrName").build());

        QR qr = qrRepository.byId(response.qrId());
        assertEquals("aQrName", qr.getName());
    }

    @Test
    public void should_rename_qr_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateQrSimpleCommand createQrCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(rCustomId())
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createQrCommand);

        IntegrationApi.renameQrByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(),
                createQrCommand.getCustomId(),
                IntegrationRenameQrCommand.builder().name("aQrName").build());

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertEquals("aQrName", qr.getName());
    }

    @Test
    public void should_update_qr_base_setting() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);
        IntegrationUpdateQrBaseSettingCommand command = IntegrationUpdateQrBaseSettingCommand.builder()
                .name(rQrName())
                .description(rSentence(100))
                .headerImageUrl(rImageFile().getFileUrl())
                .directAttributeValues(Maps.of(attribute.getId(), "hello"))
                .geolocation(rGeolocation())
                .customId(rCustomId())
                .build();

        IntegrationApi.updateQrBaseSetting(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                command
        );

        QR qr = qrRepository.byId(response.qrId());
        assertEquals(command.getName(), qr.getName());
        assertEquals(command.getDescription(), qr.getDescription());
        assertEquals(command.getHeaderImageUrl(), qr.getHeaderImage().getFileUrl());
        assertEquals("image/png", qr.getHeaderImage().getType());
        assertEquals(command.getGeolocation(), qr.getGeolocation());
        assertEquals(command.getCustomId(), qr.getCustomId());
        TextAttributeValue attributeValue = (TextAttributeValue) qr.attributeValueOf(attribute.getId());
        assertEquals("hello", attributeValue.getText());
    }

    @Test
    public void should_update_qr_base_setting_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateQrSimpleCommand createQrCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(rCustomId())
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createQrCommand);

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);
        IntegrationUpdateQrBaseSettingCommand command = IntegrationUpdateQrBaseSettingCommand.builder()
                .name(rQrName())
                .description(rSentence(100))
                .headerImageUrl(rImageFile().getFileUrl())
                .directAttributeValues(Maps.of(attribute.getId(), "hello"))
                .geolocation(rGeolocation())
                .customId(rCustomId())
                .build();

        IntegrationApi.updateQrBaseSettingByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(),
                createQrCommand.getCustomId(),
                command
        );

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertEquals(command.getName(), qr.getName());
        assertEquals(command.getDescription(), qr.getDescription());
        assertEquals(command.getHeaderImageUrl(), qr.getHeaderImage().getFileUrl());
        assertEquals("image/png", qr.getHeaderImage().getType());
        assertEquals(command.getGeolocation(), qr.getGeolocation());
        assertEquals(command.getCustomId(), qr.getCustomId());
        TextAttributeValue attributeValue = (TextAttributeValue) qr.attributeValueOf(attribute.getId());
        assertEquals("hello", attributeValue.getText());
    }

    @Test
    public void should_update_qr_name() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationApi.updateQrDescription(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationUpdateQrDescriptionCommand.builder().description("some description").build());
        QrDescriptionUpdatedEvent event = latestEventFor(response.qrId(), QR_DESCRIPTION_UPDATED, QrDescriptionUpdatedEvent.class);
        assertEquals(response.qrId(), event.getQrId());

        QR qr = qrRepository.byId(response.qrId());
        assertEquals("some description", qr.getDescription());
    }

    @Test
    public void should_update_qr_name_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateQrSimpleCommand createQrCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(rCustomId())
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createQrCommand);

        IntegrationApi.updateQrDescriptionByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(),
                createQrCommand.getCustomId(),
                IntegrationUpdateQrDescriptionCommand.builder().description("some description").build());

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertEquals("some description", qr.getDescription());
    }

    @Test
    public void should_update_qr_header_image() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String headerImageUrl = rUrl();
        IntegrationApi.updateQrHeaderImage(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationUpdateQrHeaderImageCommand.builder().headerImageUrl(headerImageUrl).build());

        QrHeaderImageUpdatedEvent event = latestEventFor(response.qrId(), QR_HEADER_IMAGE_UPDATED, QrHeaderImageUpdatedEvent.class);
        assertEquals(response.qrId(), event.getQrId());

        QR qr = qrRepository.byId(response.qrId());
        assertEquals(headerImageUrl, qr.getHeaderImage().getFileUrl());
    }

    @Test
    public void should_update_qr_header_image_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateQrSimpleCommand createQrCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(rCustomId())
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createQrCommand);

        String headerImageUrl = rUrl();
        IntegrationApi.updateQrHeaderImageByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(),
                createQrCommand.getCustomId(),
                IntegrationUpdateQrHeaderImageCommand.builder().headerImageUrl(headerImageUrl).build());

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertEquals(headerImageUrl, qr.getHeaderImage().getFileUrl());
    }

    @Test
    public void should_update_qr_direct_attributes() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        Attribute attribute1 = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        Attribute attribute2 = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute1, attribute2);

        IntegrationUpdateQrDirectAttributesCommand command = IntegrationUpdateQrDirectAttributesCommand.builder()
                .directAttributeValues(Maps.of(attribute1.getId(), "hello1", attribute2.getId(), "hello2"))
                .build();

        IntegrationApi.updateQrDirectAttributes(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                command
        );

        QR qr = qrRepository.byId(response.qrId());
        assertEquals("hello1", ((TextAttributeValue) qr.attributeValueOf(attribute1.getId())).getText());
        assertEquals("hello2", ((TextAttributeValue) qr.attributeValueOf(attribute2.getId())).getText());

        IntegrationApi.updateQrDirectAttributes(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationUpdateQrDirectAttributesCommand.builder()
                        .directAttributeValues(Maps.of(attribute1.getId(), "hello11"))
                        .build()
        );

        QR updatedQr = qrRepository.byId(response.qrId());
        assertEquals("hello11", ((TextAttributeValue) updatedQr.attributeValueOf(attribute1.getId())).getText());
        assertEquals("hello2", ((TextAttributeValue) updatedQr.attributeValueOf(attribute2.getId())).getText());
        QrAttributesUpdatedEvent event = latestEventFor(response.qrId(), QR_ATTRIBUTES_UPDATED, QrAttributesUpdatedEvent.class);
        assertEquals(response.qrId(), event.getQrId());

        Map<String, String> map = newHashMap();
        map.put(attribute1.getId(), null);
        IntegrationApi.updateQrDirectAttributes(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationUpdateQrDirectAttributesCommand.builder()
                        .directAttributeValues(map)
                        .build()
        );

        QR finalQr = qrRepository.byId(response.qrId());
        assertNull(finalQr.attributeValueOf(attribute1.getId()));
        assertEquals("hello2", ((TextAttributeValue) finalQr.attributeValueOf(attribute2.getId())).getText());
    }

    @Test
    public void should_update_qr_direct_attributes_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        Attribute attribute1 = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        Attribute attribute2 = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute1, attribute2);

        IntegrationCreateQrSimpleCommand createQrCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(rCustomId())
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createQrCommand);

        IntegrationUpdateQrDirectAttributesCommand command = IntegrationUpdateQrDirectAttributesCommand.builder()
                .directAttributeValues(Maps.of(attribute1.getId(), "hello1", attribute2.getId(), "hello2"))
                .build();

        IntegrationApi.updateQrDirectAttributesByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(),
                createQrCommand.getCustomId(),
                command
        );

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertEquals("hello1", ((TextAttributeValue) qr.attributeValueOf(attribute1.getId())).getText());
        assertEquals("hello2", ((TextAttributeValue) qr.attributeValueOf(attribute2.getId())).getText());
    }

    @Test
    public void should_update_qr_geolocation() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        Geolocation geolocation = rGeolocation();
        IntegrationApi.updateQrGeolocation(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                IntegrationUpdateQrGeolocationCommand.builder().geolocation(geolocation).build()
        );

        QrGeolocationUpdatedEvent event = latestEventFor(response.qrId(), QR_GEOLOCATION_UPDATED, QrGeolocationUpdatedEvent.class);
        assertEquals(response.qrId(), event.getQrId());

        QR qr = qrRepository.byId(response.qrId());
        assertEquals(geolocation, qr.getGeolocation());
    }

    @Test
    public void should_update_qr_geolocation_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateQrSimpleCommand createQrCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(rCustomId())
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createQrCommand);

        Geolocation geolocation = rGeolocation();

        IntegrationApi.updateQrGeolocationByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(),
                createQrCommand.getCustomId(),
                IntegrationUpdateQrGeolocationCommand.builder().geolocation(geolocation).build()
        );

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertEquals(geolocation, qr.getGeolocation());
    }

    @Test
    public void should_update_qr_custom_id() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationUpdateQrCustomIdCommand command = IntegrationUpdateQrCustomIdCommand.builder().customId(rCustomId()).build();
        IntegrationApi.updateQrCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                command
        );

        QR qr = qrRepository.byId(response.qrId());
        assertEquals(command.getCustomId(), qr.getCustomId());
    }

    @Test
    public void should_fail_update_qr_custom_id_if_duplicated() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationUpdateQrCustomIdCommand command = IntegrationUpdateQrCustomIdCommand.builder().customId(rCustomId()).build();
        IntegrationApi.updateQrCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                command
        );

        CreateQrResponse qrResponse = QrApi.createQr(response.jwt(), response.defaultGroupId());

        assertError(() -> IntegrationApi.updateQrCustomIdRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                qrResponse.getQrId(),
                command
        ), QR_WITH_CUSTOM_ID_ALREADY_EXISTS);
    }

    @Test
    public void should_raise_event_when_update_custom_id() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String qrId = response.qrId();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        Attribute instanceCustomIdAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(INSTANCE_CUSTOM_ID).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), instanceCustomIdAttribute);

        IntegrationUpdateQrCustomIdCommand command = IntegrationUpdateQrCustomIdCommand.builder().customId(rCustomId()).build();
        IntegrationApi.updateQrCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId(),
                command
        );

        QrCustomIdUpdatedEvent event = latestEventFor(qrId, QR_CUSTOM_ID_UPDATED, QrCustomIdUpdatedEvent.class);
        assertEquals(qrId, event.getQrId());

        QR qr = qrRepository.byId(qrId);
        IdentifierAttributeValue customIdAttributeValue = (IdentifierAttributeValue) qr.attributeValueOf(instanceCustomIdAttribute.getId());
        assertEquals(command.getCustomId(), customIdAttributeValue.getContent());
    }

    @Test
    public void should_fetch_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        QIntegrationQr qIntegrationQr = IntegrationApi.fetchQr(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.qrId());

        QR qr = qrRepository.byId(response.qrId());
        assertEquals(qr.getId(), qIntegrationQr.getId());
        assertEquals(qr.getName(), qIntegrationQr.getName());
        assertEquals(qr.getPlateId(), qIntegrationQr.getPlateId());
        assertEquals(qr.getAppId(), qIntegrationQr.getAppId());
        assertEquals(qr.getGroupId(), qIntegrationQr.getGroupId());
        assertEquals(qr.isTemplate(), qIntegrationQr.isTemplate());
        assertEquals(qr.getHeaderImage(), qIntegrationQr.getHeaderImage());
        assertEquals(qr.getDescription(), qIntegrationQr.getDescription());
        assertEquals(qr.getAttributeValues(), qIntegrationQr.getAttributeValues());
        assertEquals(qr.getAccessCount(), qIntegrationQr.getAccessCount());
        assertEquals(qr.getLastAccessedAt(), qIntegrationQr.getLastAccessedAt());
        assertEquals(qr.getCustomId(), qIntegrationQr.getCustomId());
        assertEquals(qr.getCreatedAt(), qIntegrationQr.getCreatedAt());
        assertEquals(qr.getCreatedBy(), qIntegrationQr.getCreatedBy());
        assertEquals(qr.getGeolocation(), qIntegrationQr.getGeolocation());
    }

    @Test
    public void should_fetch_qr_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateQrSimpleCommand createQrCommand = IntegrationCreateQrSimpleCommand.builder()
                .name("This is a QR")
                .groupId(response.defaultGroupId())
                .customId(rCustomId())
                .build();
        IntegrationCreateQrResponse qrResponse = IntegrationApi.createQrSimple(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createQrCommand);

        QIntegrationQr qIntegrationQr = IntegrationApi.fetchQrByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                qrResponse.getAppId(),
                createQrCommand.getCustomId());

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertEquals(qr.getId(), qIntegrationQr.getId());
        assertEquals(qr.getName(), qIntegrationQr.getName());
        assertEquals(qr.getPlateId(), qIntegrationQr.getPlateId());
        assertEquals(qr.getAppId(), qIntegrationQr.getAppId());
        assertEquals(qr.getGroupId(), qIntegrationQr.getGroupId());
        assertEquals(qr.isTemplate(), qIntegrationQr.isTemplate());
        assertEquals(qr.getHeaderImage(), qIntegrationQr.getHeaderImage());
        assertEquals(qr.getDescription(), qIntegrationQr.getDescription());
        assertEquals(qr.getAttributeValues(), qIntegrationQr.getAttributeValues());
        assertEquals(qr.getAccessCount(), qIntegrationQr.getAccessCount());
        assertEquals(qr.getLastAccessedAt(), qIntegrationQr.getLastAccessedAt());
        assertEquals(qr.getCustomId(), qIntegrationQr.getCustomId());
        assertEquals(qr.getCreatedAt(), qIntegrationQr.getCreatedAt());
        assertEquals(qr.getCreatedBy(), qIntegrationQr.getCreatedBy());
        assertEquals(qr.getGeolocation(), qIntegrationQr.getGeolocation());
    }

    @Test
    public void should_create_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateGroupCommand command = IntegrationCreateGroupCommand.builder()
                .appId(response.appId())
                .name("aGroupName")
                .customId(rCustomId())
                .build();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                command);

        Group group = groupRepository.byId(groupId);
        assertEquals(command.getName(), group.getName());
        assertEquals(command.getCustomId(), group.getCustomId());
    }

    @Test
    public void should_delete_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String groupId = GroupApi.createGroup(response.jwt(), response.appId());

        assertTrue(groupRepository.byIdOptional(groupId).isPresent());
        assertTrue(groupHierarchyRepository.byAppId(response.appId()).containsGroupId(groupId));

        IntegrationApi.deleteGroup(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), groupId);
        assertFalse(groupRepository.byIdOptional(groupId).isPresent());
        assertFalse(groupHierarchyRepository.byAppId(response.appId()).containsGroupId(groupId));
    }

    @Test
    public void delete_group_should_also_delete_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String groupId = GroupApi.createGroup(response.jwt(), CreateGroupCommand.builder()
                .parentGroupId(response.defaultGroupId())
                .appId(response.appId())
                .name(rGroupName()).build());

        String groupId2 = GroupApi.createGroup(response.jwt(), CreateGroupCommand.builder()
                .parentGroupId(groupId)
                .appId(response.appId())
                .name(rGroupName()).build());

        IntegrationApi.deleteGroup(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret(), groupId);
        assertFalse(groupRepository.exists(groupId2));
        IdTreeHierarchy hierarchy = groupHierarchyRepository.byAppId(response.appId()).getHierarchy();
        assertFalse(hierarchy.allIds().contains(groupId));
        assertFalse(hierarchy.allIds().contains(groupId2));
    }

    @Test
    public void should_delete_group_by_custom_id() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customId = rCustomId();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .customId(customId)
                        .build());

        IntegrationApi.deleteGroupByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(),
                customId);

        assertFalse(groupRepository.byIdOptional(groupId).isPresent());
    }

    @Test
    public void should_fail_delete_group_if_only_one_visible_group_left() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        assertError(() -> IntegrationApi.deleteGroupRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), response.defaultGroupId()), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_rename_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationApi.renameGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationRenameGroupCommand.builder().name("aGroupName").build());

        Group group = groupRepository.byId(response.defaultGroupId());
        assertEquals("aGroupName", group.getName());
    }

    @Test
    public void should_rename_group_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customId = rCustomId();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .customId(customId)
                        .build());

        IntegrationRenameGroupCommand command = IntegrationRenameGroupCommand.builder().name(rGroupName()).build();
        IntegrationApi.renameGroupByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(),
                customId,
                command);

        Group group = groupRepository.byId(groupId);
        assertEquals(command.getName(), group.getName());
    }

    @Test
    public void should_update_group_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationUpdateGroupCustomIdCommand command = IntegrationUpdateGroupCustomIdCommand.builder().customId(rCustomId()).build();
        IntegrationApi.updateGroupCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                command);
        Group group = groupRepository.byId(response.defaultGroupId());
        assertEquals(command.getCustomId(), group.getCustomId());
    }

    @Test
    public void should_fail_update_group_custom_id_if_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customId = rCustomId();
        IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .customId(customId)
                        .build());
        IntegrationUpdateGroupCustomIdCommand command = IntegrationUpdateGroupCustomIdCommand.builder().customId(customId).build();
        assertError(() -> IntegrationApi.updateGroupCustomIdRaw(tenant.getApiSetting().getApiKey(),
                        tenant.getApiSetting().getApiSecret(),
                        response.defaultGroupId(),
                        command)
                , GROUP_WITH_CUSTOM_ID_ALREADY_EXISTS);
    }

    @Test
    public void should_archive_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .customId(rCustomId())
                        .build());

        IntegrationApi.archiveGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                groupId);

        Group group = groupRepository.byId(groupId);
        assertTrue(group.isArchived());
    }

    @Test
    public void archive_and_unarchive_group_should_also_do_it_for_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String groupId1 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(response.defaultGroupId()).build());
        String groupId2 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId1).build());
        String groupId3 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId2).build());

        IntegrationApi.archiveGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                groupId1);
        assertTrue(groupRepository.byId(groupId1).isArchived());
        assertTrue(groupRepository.byId(groupId2).isArchived());
        assertTrue(groupRepository.byId(groupId3).isArchived());

        IntegrationApi.unArchiveGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                groupId1);
        assertFalse(groupRepository.byId(groupId1).isArchived());
        assertFalse(groupRepository.byId(groupId2).isArchived());
        assertFalse(groupRepository.byId(groupId3).isArchived());
    }

    @Test
    public void should_archive_group_by_custom_id() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customId = rCustomId();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .customId(customId)
                        .build());

        IntegrationApi.archiveGroupByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(), customId);

        Group group = groupRepository.byId(groupId);
        assertTrue(group.isArchived());
    }

    @Test
    public void should_unarchive_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .customId(rCustomId())
                        .build());

        IntegrationApi.archiveGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                groupId);
        assertTrue(groupRepository.byId(groupId).isArchived());

        IntegrationApi.unArchiveGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                groupId);
        assertFalse(groupRepository.byId(groupId).isArchived());
    }

    @Test
    public void should_unarchive_group_by_custom_id() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customId = rCustomId();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .customId(customId)
                        .build());

        IntegrationApi.archiveGroupByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(), customId);
        assertTrue(groupRepository.byId(groupId).isArchived());

        IntegrationApi.unArchiveGroupByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(), customId);
        assertFalse(groupRepository.byId(groupId).isArchived());
    }

    @Test
    public void should_deactivate_and_activate_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .customId(rCustomId())
                        .build());

        IntegrationApi.deactivateGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                groupId);
        assertFalse(groupRepository.byId(groupId).isActive());

        IntegrationApi.activateGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                groupId);
        assertTrue(groupRepository.byId(groupId).isActive());
    }

    @Test
    public void deactivate_and_activate_group_should_also_do_it_for_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String groupId1 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(response.defaultGroupId()).build());
        String groupId2 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId1).build());
        String groupId3 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId2).build());

        IntegrationApi.deactivateGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                groupId1);
        assertFalse(groupRepository.byId(groupId1).isActive());
        assertFalse(groupRepository.byId(groupId2).isActive());
        assertFalse(groupRepository.byId(groupId3).isActive());

        IntegrationApi.activateGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                groupId1);
        assertTrue(groupRepository.byId(groupId1).isActive());
        assertTrue(groupRepository.byId(groupId2).isActive());
        assertTrue(groupRepository.byId(groupId3).isActive());
    }

    @Test
    public void should_fail_deactivate_if_only_one_active_group_left() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        IntegrationApi.deactivateGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId());

        assertError(() -> IntegrationApi.deactivateGroupRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), anotherGroupId), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_deactivate_and_activate_group_by_custom_id() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customId = rCustomId();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .customId(customId)
                        .build());

        IntegrationApi.deactivationGroupByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(), customId);
        assertFalse(groupRepository.byId(groupId).isActive());

        IntegrationApi.activationGroupByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(), customId);
        assertTrue(groupRepository.byId(groupId).isActive());
    }

    @Test
    public void should_add_managers_to_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());

        IntegrationApi.addGroupManagers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationAddGroupManagersCommand.builder().memberIds(of(memberId)).build());

        assertNotNull(latestEventFor(response.defaultGroupId(), GROUP_MANAGERS_CHANGED, GroupManagersChangedEvent.class));
        Group group = groupRepository.byId(response.defaultGroupId());
        assertTrue(group.getManagers().contains(memberId));
    }

    @Test
    public void add_group_manager_should_also_add_as_group_member() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());

        IntegrationApi.addGroupManagers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationAddGroupManagersCommand.builder().memberIds(of(memberId)).build());

        Group group = groupRepository.byId(response.defaultGroupId());
        assertTrue(group.getManagers().contains(memberId));
        assertTrue(group.getMembers().contains(memberId));
    }

    @Test
    public void should_fail_add_managers_if_size_exceed_limit() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        List<String> memberIds = IntStream.range(1, 7)
                .mapToObj(value -> MemberApi.createMember(response.jwt())).collect(toList());
        IntegrationApi.addGroupManagers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationAddGroupManagersCommand.builder().memberIds(memberIds).build());

        List<String> moreMemberIds = IntStream.range(1, 7)
                .mapToObj(value -> MemberApi.createMember(response.jwt())).collect(toList());

        assertError(() -> IntegrationApi.addGroupManagersRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationAddGroupManagersCommand.builder().memberIds(moreMemberIds).build()), MAX_GROUP_MANAGER_REACHED);
    }

    @Test
    public void should_add_managers_to_group_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateGroupCommand createGroupCommand = IntegrationCreateGroupCommand.builder()
                .appId(response.appId())
                .name("aGroupName")
                .customId(rCustomId())
                .build();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createGroupCommand);

        String memberCustomId = rCustomId();
        IntegrationCreateMemberCommand createMemberCommand = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(memberCustomId)
                .build();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createMemberCommand);

        IntegrationApi.addGroupManagersByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(), createGroupCommand.getCustomId(),
                IntegrationCustomAddGroupManagersCommand.builder()
                        .memberCustomIds(newArrayList(memberCustomId))
                        .build());

        Group group = groupRepository.byId(groupId);
        assertTrue(group.getManagers().contains(memberId));
    }

    @Test
    public void should_remove_group_managers() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());

        IntegrationApi.addGroupManagers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationAddGroupManagersCommand.builder().memberIds(of(memberId)).build());
        assertTrue(groupRepository.byId(response.defaultGroupId()).getManagers().contains(memberId));

        IntegrationApi.removeGroupManagers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationRemoveGroupManagersCommand.builder().memberIds(List.of(memberId)).build());
        assertFalse(groupRepository.byId(response.defaultGroupId()).getManagers().contains(memberId));
        assertNotNull(latestEventFor(response.defaultGroupId(), GROUP_MANAGERS_CHANGED, GroupManagersChangedEvent.class));
    }

    @Test
    public void should_remove_group_managers_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateGroupCommand createGroupCommand = IntegrationCreateGroupCommand.builder()
                .appId(response.appId())
                .name("aGroupName")
                .customId(rCustomId())
                .build();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createGroupCommand);

        String memberCustomId = rCustomId();
        IntegrationCreateMemberCommand createMemberCommand = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(memberCustomId)
                .build();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createMemberCommand);

        IntegrationApi.addGroupManagersByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(), createGroupCommand.getCustomId(),
                IntegrationCustomAddGroupManagersCommand.builder()
                        .memberCustomIds(newArrayList(memberCustomId))
                        .build());

        assertTrue(groupRepository.byId(groupId).getManagers().contains(memberId));

        IntegrationApi.removeGroupManagersByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), response.appId(),
                createGroupCommand.getCustomId(),
                IntegrationCustomRemoveGroupManagersCommand.builder().memberCustomIds(List.of(memberCustomId)).build());

        assertFalse(groupRepository.byId(groupId).getManagers().contains(memberId));
        assertNotNull(latestEventFor(groupId, GROUP_MANAGERS_CHANGED, GroupManagersChangedEvent.class));
    }

    @Test
    public void should_add_common_members_to_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());

        IntegrationApi.addGroupMembers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationAddGroupMembersCommand.builder().memberIds(of(memberId)).build());

        Group group = groupRepository.byId(response.defaultGroupId());
        assertTrue(group.getMembers().contains(memberId));
    }

    @Test
    public void should_add_members_to_group_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateGroupCommand createGroupCommand = IntegrationCreateGroupCommand.builder()
                .appId(response.appId())
                .name("aGroupName")
                .customId(rCustomId())
                .build();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createGroupCommand);

        String memberCustomId = rCustomId();
        IntegrationCreateMemberCommand createMemberCommand = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(memberCustomId)
                .build();

        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createMemberCommand);

        IntegrationApi.addGroupMembersByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(), createGroupCommand.getCustomId(),
                IntegrationCustomAddGroupMembersCommand.builder()
                        .memberCustomIds(newArrayList(memberCustomId))
                        .build());

        Group group = groupRepository.byId(groupId);
        assertTrue(group.getMembers().contains(memberId));
    }

    @Test
    public void should_remove_group_members() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());

        IntegrationApi.addGroupMembers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationAddGroupMembersCommand.builder().memberIds(of(memberId)).build());
        assertTrue(groupRepository.byId(response.defaultGroupId()).getMembers().contains(memberId));

        IntegrationApi.removeGroupMembers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationRemoveGroupMembersCommand.builder().memberIds(List.of(memberId)).build());
        assertFalse(groupRepository.byId(response.defaultGroupId()).getMembers().contains(memberId));
    }

    @Test
    public void remove_group_members_should_also_remove_managers() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());
        IntegrationApi.addGroupManagers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationAddGroupManagersCommand.builder().memberIds(of(memberId)).build());

        Group group = groupRepository.byId(response.defaultGroupId());
        assertTrue(group.getManagers().contains(memberId));
        assertTrue(group.getMembers().contains(memberId));

        IntegrationApi.removeGroupMembers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId(),
                IntegrationRemoveGroupMembersCommand.builder().memberIds(List.of(memberId)).build());

        Group updatedGroup = groupRepository.byId(response.defaultGroupId());
        assertFalse(updatedGroup.getManagers().contains(memberId));
        assertFalse(updatedGroup.getMembers().contains(memberId));
        GroupManagersChangedEvent event = latestEventFor(response.defaultGroupId(), GROUP_MANAGERS_CHANGED, GroupManagersChangedEvent.class);
        assertEquals(response.defaultGroupId(), event.getGroupId());
    }

    @Test
    public void should_remove_group_members_by_group_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateGroupCommand createGroupCommand = IntegrationCreateGroupCommand.builder()
                .appId(response.appId())
                .name("aGroupName")
                .customId(rCustomId())
                .build();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createGroupCommand);

        String memberCustomId = rCustomId();
        IntegrationCreateMemberCommand createMemberCommand = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(memberCustomId)
                .build();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createMemberCommand);

        IntegrationApi.addGroupMembersByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(), createGroupCommand.getCustomId(),
                IntegrationCustomAddGroupMembersCommand.builder()
                        .memberCustomIds(newArrayList(memberCustomId))
                        .build());

        assertTrue(groupRepository.byId(groupId).getMembers().contains(memberId));

        IntegrationApi.removeGroupMembersByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), response.appId(),
                createGroupCommand.getCustomId(),
                IntegrationCustomRemoveGroupMembersCommand.builder().memberCustomIds(List.of(memberCustomId)).build());

        assertFalse(groupRepository.byId(groupId).getMembers().contains(memberId));
    }

    @Test
    public void should_fetch_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        QIntegrationGroup qIntegrationGroup = IntegrationApi.fetchGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.defaultGroupId());

        Group group = groupRepository.byId(response.defaultGroupId());

        assertEquals(group.getId(), qIntegrationGroup.getId());
        assertEquals(group.getName(), qIntegrationGroup.getName());
        assertEquals(group.getAppId(), qIntegrationGroup.getAppId());
        assertEquals(group.getManagers(), qIntegrationGroup.getManagers());
        assertEquals(group.getMembers(), qIntegrationGroup.getMembers());
        assertEquals(group.isArchived(), qIntegrationGroup.isArchived());
        assertEquals(group.getCreatedBy(), qIntegrationGroup.getCreatedBy());
        assertEquals(group.getCreatedAt(), qIntegrationGroup.getCreatedAt());
    }

    @Test
    public void should_fetch_group_by_custom_id() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customId = rCustomId();
        String groupId = IntegrationApi.createGroup(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateGroupCommand.builder()
                        .appId(response.appId())
                        .name("aGroupName")
                        .customId(customId)
                        .build());

        QIntegrationGroup qIntegrationGroup = IntegrationApi.fetchGroupByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId(), customId);

        Group group = groupRepository.byId(groupId);

        assertEquals(group.getId(), qIntegrationGroup.getId());
        assertEquals(group.getName(), qIntegrationGroup.getName());
        assertEquals(group.getCustomId(), qIntegrationGroup.getCustomId());
        assertEquals(group.getAppId(), qIntegrationGroup.getAppId());
        assertEquals(group.getManagers(), qIntegrationGroup.getManagers());
        assertEquals(group.getMembers(), qIntegrationGroup.getMembers());
        assertEquals(group.isArchived(), qIntegrationGroup.isArchived());
        assertEquals(group.getCreatedBy(), qIntegrationGroup.getCreatedBy());
        assertEquals(group.getCreatedAt(), qIntegrationGroup.getCreatedAt());
    }

    @Test
    public void should_list_app_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        List<QIntegrationListGroup> list = IntegrationApi.listGroups(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.appId()
        );

        assertEquals(1, list.size());
        QIntegrationListGroup qIntegrationGroup = list.get(0);
        Group group = groupRepository.byId(qIntegrationGroup.getId());

        assertEquals(group.getId(), qIntegrationGroup.getId());
        assertEquals(group.getName(), qIntegrationGroup.getName());
        assertEquals(group.getCustomId(), qIntegrationGroup.getCustomId());
        assertEquals(group.getAppId(), qIntegrationGroup.getAppId());
        assertEquals(group.isArchived(), qIntegrationGroup.isArchived());
        assertEquals(group.isActive(), qIntegrationGroup.isActive());
    }

    @Test
    public void should_create_member() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateMemberCommand command = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(rCustomId())
                .build();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                command);

        Member member = memberRepository.byId(memberId);
        assertEquals(command.getName(), member.getName());
        assertEquals(command.getEmail(), member.getEmail());
        assertEquals(command.getMobile(), member.getMobile());
        assertEquals(command.getCustomId(), member.getCustomId());
    }

    @Test
    public void should_fail_create_member_if_custom_id_already_exists() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customId = rCustomId();
        IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateMemberCommand.builder()
                        .name(rMemberName())
                        .email(rEmail())
                        .mobile(rMobile())
                        .password(rPassword())
                        .customId(customId)
                        .build());

        assertError(() -> IntegrationApi.createMemberRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateMemberCommand.builder()
                        .name(rMemberName())
                        .email(rEmail())
                        .mobile(rMobile())
                        .password(rPassword())
                        .customId(customId)
                        .build()), MEMBER_WITH_CUSTOM_ID_ALREADY_EXISTS);
    }

    @Test
    public void should_update_member_custom_id() {
        RegisterResponse response = setupApi.register(rMobile(), rPassword());
        Tenant tenant = tenantRepository.byId(response.getTenantId());

        IntegrationUpdateMemberCustomIdCommand command = IntegrationUpdateMemberCustomIdCommand.builder().customId(rCustomId()).build();
        IntegrationApi.updateMemberCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.getMemberId(),
                command
        );

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(command.getCustomId(), member.getCustomId());
    }

    @Test
    public void should_fail_update_member_custom_id_if_duplicated() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationUpdateMemberCustomIdCommand command = IntegrationUpdateMemberCustomIdCommand.builder().customId(rCustomId()).build();
        IntegrationApi.updateMemberCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                response.memberId(),
                command
        );

        String memberId = MemberApi.createMember(response.jwt());
        assertError(() -> IntegrationApi.updateMemberCustomIdRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                memberId,
                command
        ), MEMBER_WITH_CUSTOM_ID_ALREADY_EXISTS);
    }

    @Test
    public void should_delete_member() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());

        assertTrue(memberRepository.byIdOptional(memberId).isPresent());

        IntegrationApi.deleteMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), memberId);

        assertFalse(memberRepository.byIdOptional(memberId).isPresent());
    }

    @Test
    public void should_fail_delete_member_if_no_tenant_admin_left() throws InterruptedException {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        assertError(() -> IntegrationApi.deleteMemberRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), response.memberId()), NO_ACTIVE_TENANT_ADMIN_LEFT);
    }

    @Test
    public void should_delete_member_by_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateMemberCommand createMemberCommand = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(rCustomId())
                .build();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createMemberCommand);

        assertTrue(memberRepository.byIdOptional(memberId).isPresent());

        IntegrationApi.deleteMemberByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createMemberCommand.getCustomId());

        assertFalse(memberRepository.byIdOptional(memberId).isPresent());
    }

    @Test
    public void should_update_member_info() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationUpdateMemberInfoCommand command = IntegrationUpdateMemberInfoCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .build();

        IntegrationApi.updateMemberInfo(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), response.memberId(), command);

        Member member = memberRepository.byId(response.memberId());
        assertEquals(command.getName(), member.getName());
        assertEquals(command.getEmail(), member.getEmail());
        assertEquals(command.getMobile(), member.getMobile());
    }

    @Test
    public void should_update_member_info_by_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateMemberCommand createMemberCommand = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(rCustomId())
                .build();

        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createMemberCommand);

        IntegrationUpdateMemberInfoCommand command = IntegrationUpdateMemberInfoCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .build();

        IntegrationApi.updateMemberInfoByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createMemberCommand.getCustomId(), command);

        Member member = memberRepository.byId(memberId);
        assertEquals(command.getName(), member.getName());
        assertEquals(command.getEmail(), member.getEmail());
        assertEquals(command.getMobile(), member.getMobile());
    }

    @Test
    public void should_deactivate_and_activate_member() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());

        IntegrationApi.deactivateMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), memberId);
        assertFalse(memberRepository.byId(memberId).isActive());

        IntegrationApi.activateMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), memberId);
        assertTrue(memberRepository.byId(memberId).isActive());
    }

    @Test
    public void should_deactivate_and_activate_member_by_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());
        String customId = rCustomId();
        IntegrationApi.updateMemberCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), memberId,
                IntegrationUpdateMemberCustomIdCommand.builder().customId(customId).build());

        IntegrationApi.deactivateMemberByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), customId);
        assertFalse(memberRepository.byId(memberId).isActive());

        IntegrationApi.activateMemberByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), customId);
        assertTrue(memberRepository.byId(memberId).isActive());
    }

    @Test
    public void should_fetch_member() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateMemberCommand createMemberCommand = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(rCustomId())
                .build();

        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createMemberCommand);

        QIntegrationMember fetchedMember = IntegrationApi.fetchMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), memberId);

        Member dbMember = memberRepository.byId(memberId);

        assertEquals(dbMember.getName(), fetchedMember.getName());
        assertEquals(dbMember.getRole(), fetchedMember.getRole());
        assertEquals(dbMember.getMobile(), fetchedMember.getMobile());
        assertEquals(dbMember.getEmail(), fetchedMember.getEmail());
        assertEquals(dbMember.getCustomId(), fetchedMember.getCustomId());
        assertEquals(dbMember.isActive(), fetchedMember.isActive());
        assertEquals(dbMember.getDepartmentIds(), fetchedMember.getDepartmentIds());
        assertEquals(dbMember.getCreatedAt(), fetchedMember.getCreatedAt());
        assertEquals(dbMember.getCreatedAt(), fetchedMember.getCreatedAt());
    }

    @Test
    public void should_fetch_member_by_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateMemberCommand createMemberCommand = IntegrationCreateMemberCommand.builder()
                .name(rMemberName())
                .email(rEmail())
                .mobile(rMobile())
                .password(rPassword())
                .customId(rCustomId())
                .build();

        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                createMemberCommand);

        QIntegrationMember fetchedMember = IntegrationApi.fetchMemberByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), createMemberCommand.getCustomId());

        Member dbMember = memberRepository.byId(memberId);

        assertEquals(dbMember.getName(), fetchedMember.getName());
        assertEquals(dbMember.getRole(), fetchedMember.getRole());
        assertEquals(dbMember.getMobile(), fetchedMember.getMobile());
        assertEquals(dbMember.getEmail(), fetchedMember.getEmail());
        assertEquals(dbMember.getCustomId(), fetchedMember.getCustomId());
        assertEquals(dbMember.getCreatedAt(), fetchedMember.getCreatedAt());
        assertEquals(dbMember.getCreatedAt(), fetchedMember.getCreatedAt());
    }

    @Test
    public void should_list_members() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        List<QIntegrationListMember> members = IntegrationApi.listMembers(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret());
        assertEquals(1, members.size());
        QIntegrationListMember listMember = members.get(0);
        Member member = memberRepository.byId(response.memberId());
        assertEquals(member.getId(), listMember.getId());
        assertEquals(member.getName(), listMember.getName());
        assertEquals(member.getRole(), listMember.getRole());
        assertEquals(member.getEmail(), listMember.getEmail());
        assertEquals(member.getMobile(), listMember.getMobile());
        assertEquals(member.getCustomId(), listMember.getCustomId());
        assertEquals(member.isActive(), listMember.isActive());
    }

    @Test
    public void should_create_department() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateDepartmentCommand command = IntegrationCreateDepartmentCommand.builder().name(rDepartmentName()).customId(rCustomId())
                .build();
        String departmentId = IntegrationApi.createDepartment(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), command);

        Department department = departmentRepository.byId(departmentId);
        assertEquals(department.getId(), departmentId);
        DepartmentHierarchy hierarchy = departmentHierarchyRepository.byTenantId(response.tenantId());
        assertTrue(hierarchy.containsDepartmentId(department.getId()));
        assertEquals(command.getCustomId(), department.getCustomId());
    }

    @Test
    public void should_update_department_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        IntegrationCreateDepartmentCommand command = IntegrationCreateDepartmentCommand.builder().name(rDepartmentName()).customId(rCustomId())
                .build();
        String departmentId = IntegrationApi.createDepartment(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), command);

        IntegrationUpdateDepartmentCustomIdCommand updateCommand = IntegrationUpdateDepartmentCustomIdCommand.builder().customId(rCustomId())
                .build();
        IntegrationApi.updateDepartmentCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, updateCommand);

        Department department = departmentRepository.byId(departmentId);
        assertEquals(updateCommand.getCustomId(), department.getCustomId());
    }

    @Test
    public void should_add_department_member() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());
        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());

        IntegrationApi.addDepartmentMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, memberId);

        Member member = memberRepository.byId(memberId);
        assertTrue(member.getDepartmentIds().contains(departmentId));
        assertEquals(1, member.getDepartmentIds().size());

        MemberAddedToDepartmentEvent event = latestEventFor(memberId, MEMBER_ADDED_TO_DEPARTMENT, MemberAddedToDepartmentEvent.class);
        assertEquals(departmentId, event.getDepartmentId());
    }

    @Test
    public void should_add_department_member_by_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customMemberId = rCustomId();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateMemberCommand.builder()
                        .name(rMemberName())
                        .email(rEmail())
                        .mobile(rMobile())
                        .password(rPassword())
                        .customId(customMemberId)
                        .build());

        String departmentCustomId = rCustomId();
        String departmentId = IntegrationApi.createDepartment(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateDepartmentCommand.builder().
                        name(rDepartmentName()).
                        customId(departmentCustomId)
                        .build());

        IntegrationApi.addDepartmentMemberByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentCustomId, customMemberId);

        Member member = memberRepository.byId(memberId);
        assertTrue(member.getDepartmentIds().contains(departmentId));
        assertEquals(1, member.getDepartmentIds().size());

        MemberAddedToDepartmentEvent event = latestEventFor(memberId, MEMBER_ADDED_TO_DEPARTMENT, MemberAddedToDepartmentEvent.class);
        assertEquals(departmentId, event.getDepartmentId());
    }

    @Test
    public void should_remove_member_from_department() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());
        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());

        IntegrationApi.addDepartmentMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, memberId);

        assertTrue(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));

        IntegrationApi.removeDepartmentMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, memberId);

        assertFalse(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));

        MemberRemovedFromDepartmentEvent event = latestEventFor(memberId, MEMBER_REMOVED_FROM_DEPARTMENT,
                MemberRemovedFromDepartmentEvent.class);
        assertEquals(departmentId, event.getDepartmentId());
    }

    @Test
    public void should_remove_department_member_by_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customMemberId = rCustomId();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateMemberCommand.builder()
                        .name(rMemberName())
                        .email(rEmail())
                        .mobile(rMobile())
                        .password(rPassword())
                        .customId(customMemberId)
                        .build());

        String departmentCustomId = rCustomId();
        String departmentId = IntegrationApi.createDepartment(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateDepartmentCommand.builder().
                        name(rDepartmentName()).
                        customId(departmentCustomId)
                        .build());

        IntegrationApi.addDepartmentMemberByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentCustomId, customMemberId);

        assertTrue(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));

        IntegrationApi.removeDepartmentMemberByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentCustomId, customMemberId);

        assertFalse(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));
        MemberRemovedFromDepartmentEvent event = latestEventFor(memberId, MEMBER_REMOVED_FROM_DEPARTMENT,
                MemberRemovedFromDepartmentEvent.class);
        assertEquals(departmentId, event.getDepartmentId());
    }

    @Test
    public void should_add_manager_to_department() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());
        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());

        IntegrationApi.addDepartmentMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, memberId);

        assertTrue(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));

        IntegrationApi.addDepartmentManager(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, memberId);

        Department department = departmentRepository.byId(departmentId);
        assertTrue(department.getManagers().contains(memberId));

        DepartmentManagersChangedEvent event = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED, DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());
    }

    @Test
    public void should_fail_add_department_manager_if_not_member() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());
        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());

        assertError(() -> IntegrationApi.addDepartmentManagerRaw(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, memberId), NOT_DEPARTMENT_MEMBER);
    }

    @Test
    public void should_add_department_manager_by_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customMemberId = rCustomId();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateMemberCommand.builder()
                        .name(rMemberName())
                        .email(rEmail())
                        .mobile(rMobile())
                        .password(rPassword())
                        .customId(customMemberId)
                        .build());

        String departmentCustomId = rCustomId();
        String departmentId = IntegrationApi.createDepartment(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateDepartmentCommand.builder().
                        name(rDepartmentName()).
                        customId(departmentCustomId)
                        .build());

        IntegrationApi.addDepartmentMemberByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentCustomId, customMemberId);

        assertTrue(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));

        IntegrationApi.addDepartmentManagerByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentCustomId, customMemberId);

        Department department = departmentRepository.byId(departmentId);
        assertTrue(department.getManagers().contains(memberId));

        DepartmentManagersChangedEvent event = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED, DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());
    }

    @Test
    public void should_remove_manager_from_department() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt());
        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());

        IntegrationApi.addDepartmentMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, memberId);

        assertTrue(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));

        IntegrationApi.addDepartmentManager(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, memberId);
        assertTrue(departmentRepository.byId(departmentId).getManagers().contains(memberId));
        DepartmentManagersChangedEvent event = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED, DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());

        IntegrationApi.removeDepartmentManager(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, memberId);
        assertFalse(departmentRepository.byId(departmentId).getManagers().contains(memberId));

        DepartmentManagersChangedEvent event1 = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED, DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, event1.getDepartmentId());
        assertNotEquals(event.getId(), event1.getId());
    }

    @Test
    public void should_remove_department_manager_by_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String customMemberId = rCustomId();
        String memberId = IntegrationApi.createMember(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateMemberCommand.builder()
                        .name(rMemberName())
                        .email(rEmail())
                        .mobile(rMobile())
                        .password(rPassword())
                        .customId(customMemberId)
                        .build());

        String departmentCustomId = rCustomId();
        String departmentId = IntegrationApi.createDepartment(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateDepartmentCommand.builder().
                        name(rDepartmentName()).
                        customId(departmentCustomId)
                        .build());

        IntegrationApi.addDepartmentMemberByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentCustomId, customMemberId);
        assertTrue(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));

        IntegrationApi.addDepartmentManagerByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentCustomId, customMemberId);
        assertTrue(departmentRepository.byId(departmentId).getManagers().contains(memberId));
        DepartmentManagersChangedEvent event = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED, DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());

        IntegrationApi.removeDepartmentManagerByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentCustomId, customMemberId);
        assertFalse(departmentRepository.byId(departmentId).getManagers().contains(memberId));
        DepartmentManagersChangedEvent event1 = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED, DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, event1.getDepartmentId());
        assertNotEquals(event.getId(), event1.getId());
    }

    @Test
    public void should_delete_department() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());
        assertTrue(departmentHierarchyRepository.byTenantId(response.tenantId()).containsDepartmentId(departmentId));
        assertTrue(departmentRepository.exists(departmentId));

        IntegrationApi.deleteDepartment(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId);

        assertFalse(departmentRepository.exists(departmentId));
        assertFalse(departmentHierarchyRepository.byTenantId(response.tenantId()).containsDepartmentId(departmentId));
    }

    @Test
    public void delete_department_should_also_delete_sub_departments() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(response.jwt(), departmentId, rDepartmentName());

        IntegrationApi.deleteDepartment(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId);
        assertFalse(departmentRepository.exists(departmentId));
        assertFalse(departmentRepository.exists(subDepartmentId));
    }

    @Test
    public void should_delete_department_by_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String departmentCustomId = rCustomId();
        String departmentId = IntegrationApi.createDepartment(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(),
                IntegrationCreateDepartmentCommand.builder().
                        name(rDepartmentName()).
                        customId(departmentCustomId)
                        .build());
        assertTrue(departmentRepository.exists(departmentId));

        IntegrationApi.deleteDepartmentByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentCustomId);
        assertFalse(departmentRepository.exists(departmentId));
    }

    @Test
    public void should_list_departments() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String name = rDepartmentName();
        String departmentId = DepartmentApi.createDepartment(response.jwt(), name);
        IntegrationUpdateDepartmentCustomIdCommand updateCommand = IntegrationUpdateDepartmentCustomIdCommand.builder().customId(rCustomId())
                .build();
        IntegrationApi.updateDepartmentCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, updateCommand);

        List<QIntegrationListDepartment> departments = IntegrationApi.listDepartments(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret());
        QIntegrationListDepartment department = departments.get(0);
        assertEquals(updateCommand.getCustomId(), department.getCustomId());
        assertEquals(name, department.getName());
        assertEquals(departmentId, department.getId());
    }

    @Test
    public void should_fetch_department() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt(), rMemberName(), rMobile(), rPassword());
        String departmentId = DepartmentApi.createDepartment(response.jwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        IntegrationUpdateDepartmentCustomIdCommand updateCommand = IntegrationUpdateDepartmentCustomIdCommand.builder().customId(rCustomId())
                .build();
        IntegrationApi.updateDepartmentCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, updateCommand);

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.jwt(), memberId, command);
        DepartmentApi.addDepartmentManager(response.jwt(), departmentId, memberId);

        QIntegrationDepartment qDepartment = IntegrationApi.fetchDepartment(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId);

        Department department = departmentRepository.byId(departmentId);
        assertEquals(department.getId(), qDepartment.getId());
        assertEquals(updateCommand.getCustomId(), qDepartment.getCustomId());
        assertEquals(department.getName(), qDepartment.getName());
        assertEquals(department.getCreatedAt(), qDepartment.getCreatedAt());
        assertTrue(qDepartment.getMembers().contains(memberId));
        assertTrue(qDepartment.getManagers().contains(memberId));
    }

    @Test
    public void should_fetch_department_by_custom_id() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        String memberId = MemberApi.createMember(response.jwt(), rMemberName(), rMobile(), rPassword());
        String departmentId = DepartmentApi.createDepartment(response.jwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        String departmentCustomId = rCustomId();
        IntegrationUpdateDepartmentCustomIdCommand updateCommand = IntegrationUpdateDepartmentCustomIdCommand.builder()
                .customId(departmentCustomId).build();
        IntegrationApi.updateDepartmentCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentId, updateCommand);

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.jwt(), memberId, command);
        DepartmentApi.addDepartmentManager(response.jwt(), departmentId, memberId);

        QIntegrationDepartment qDepartment = IntegrationApi.fetchDepartmentByCustomId(tenant.getApiSetting().getApiKey(),
                tenant.getApiSetting().getApiSecret(), departmentCustomId);

        Department department = departmentRepository.byId(departmentId);
        assertEquals(department.getId(), qDepartment.getId());
        assertEquals(updateCommand.getCustomId(), qDepartment.getCustomId());
        assertEquals(department.getName(), qDepartment.getName());
        assertEquals(department.getCreatedAt(), qDepartment.getCreatedAt());
        assertTrue(qDepartment.getMembers().contains(memberId));
        assertTrue(qDepartment.getManagers().contains(memberId));
    }

    @Test
    public void should_fail_authentication_for_wrong_credentials() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.tenantId());

        assertError(() -> IntegrationApi.listAppsRaw(tenant.getApiSetting().getApiKey(), "wrongApiSecret"), AUTHENTICATION_FAILED);
        assertError(() -> IntegrationApi.listAppsRaw("wrongApiKey", tenant.getApiSetting().getApiSecret()), AUTHENTICATION_FAILED);
    }
}
