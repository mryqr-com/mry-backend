package com.mryqr.core.member;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.UuidGenerator;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.CreateAppResponse;
import com.mryqr.core.app.command.SetAppManagersCommand;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.TenantCachedApp;
import com.mryqr.core.department.DepartmentApi;
import com.mryqr.core.department.command.CreateDepartmentCommand;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.member.command.*;
import com.mryqr.core.member.command.importmember.MemberImportRecord;
import com.mryqr.core.member.command.importmember.MemberImportResponse;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.event.MemberCreatedEvent;
import com.mryqr.core.member.domain.event.MemberDeletedEvent;
import com.mryqr.core.member.domain.event.MemberDepartmentsChangedEvent;
import com.mryqr.core.member.domain.event.MemberNameChangedEvent;
import com.mryqr.core.member.query.QListMember;
import com.mryqr.core.member.query.QMemberBaseSetting;
import com.mryqr.core.member.query.QMemberInfo;
import com.mryqr.core.member.query.QMemberReference;
import com.mryqr.core.member.query.profile.QClientMemberProfile;
import com.mryqr.core.member.query.profile.QConsoleMemberProfile;
import com.mryqr.core.order.OrderApi;
import com.mryqr.core.order.StubOrderPaidNotifyApi;
import com.mryqr.core.order.command.CreateOrderCommand;
import com.mryqr.core.order.command.CreateOrderResponse;
import com.mryqr.core.order.domain.detail.ExtraMemberOrderDetail;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.tenant.domain.ResourceUsage;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.query.QConsoleTenantProfile;
import com.mryqr.core.tenant.query.QPackagesStatus;
import com.mryqr.core.verification.VerificationCodeApi;
import com.mryqr.core.verification.command.CreateChangeMobileVerificationCodeCommand;
import com.mryqr.core.verification.command.CreateFindbackPasswordVerificationCodeCommand;
import com.mryqr.core.verification.command.IdentifyMobileVerificationCodeCommand;
import com.mryqr.core.verification.domain.VerificationCode;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.domain.user.Role.TENANT_ADMIN;
import static com.mryqr.common.domain.user.Role.TENANT_MEMBER;
import static com.mryqr.common.event.DomainEventType.*;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.core.department.domain.Department.newDepartmentId;
import static com.mryqr.core.order.domain.PaymentType.WX_NATIVE;
import static com.mryqr.core.order.domain.detail.OrderDetailType.EXTRA_MEMBER;
import static com.mryqr.core.verification.VerificationCodeApi.createVerificationCodeForChangeMobile;
import static com.mryqr.core.verification.VerificationCodeApi.createVerificationCodeForIdentifyMobile;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

class MemberControllerApiTest extends BaseApiTest {

    @Test
    public void tenant_admin_should_be_able_to_create_member() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        String jwt = loginResponse.getJwt();

        String newMemberName = rMemberName();
        String newMemberMobile = rMobile();
        String newMemberPassword = rPassword();
        String memberId = MemberApi.createMember(jwt, newMemberName, newMemberMobile, newMemberPassword);

        Member member = memberRepository.byId(memberId);
        assertEquals(memberId, member.getId());
        assertEquals(newMemberName, member.getName());
        assertEquals(newMemberMobile, member.getMobile());
        assertEquals(TENANT_MEMBER, member.getRole());
        assertNotNull(member.getPassword());
    }

    @Test
    public void should_raise_created_event_when_create_member() {
        LoginResponse response = setupApi.registerWithLogin();

        String memberId = MemberApi.createMember(response.getJwt());

        MemberCreatedEvent memberCreatedEvent = latestEventFor(memberId, MEMBER_CREATED, MemberCreatedEvent.class);
        assertEquals(memberId, memberCreatedEvent.getMemberId());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(2, tenant.getResourceUsage().getMemberCount());
    }

    @Test
    public void should_raise_departments_changed_event_when_create_member() {
        LoginResponse response = setupApi.registerWithLogin();
        String departmentId = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());

        String memberId = MemberApi.createMember(response.getJwt(), CreateMemberCommand.builder()
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .mobile(rMobile())
                .password(rPassword())
                .build());

        MemberDepartmentsChangedEvent event = latestEventFor(memberId, MEMBER_DEPARTMENTS_CHANGED, MemberDepartmentsChangedEvent.class);
        assertTrue(event.getAddedDepartmentIds().contains(departmentId));
        assertTrue(event.getRemovedDepartmentIds().isEmpty());
        assertEquals(memberId, event.getMemberId());
    }

    @Test
    public void member_add_to_department_should_sync_to_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.enableGroupSync(response.getJwt(), response.getAppId());
        String name = rDepartmentName();
        String departmentId = DepartmentApi.createDepartment(response.getJwt(), name);
        assertFalse(
                groupRepository.byDepartmentIdOptional(departmentId, response.getAppId()).get().getMembers().contains(response.getMemberId()));

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.getJwt(), response.getMemberId(), command);
        assertTrue(
                groupRepository.byDepartmentIdOptional(departmentId, response.getAppId()).get().getMembers().contains(response.getMemberId()));
    }

    @Test
    public void non_admin_should_fail_create_member() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).getJwt();

        CreateMemberResponse newMember = MemberApi.createMemberAndLogin(jwt, rMemberName(), rMobile(), rPassword());
        CreateMemberCommand command = CreateMemberCommand.builder()
                .mobile(rMobile())
                .password(rPassword())
                .name(rMemberName())
                .departmentIds(List.of())
                .build();

        assertError(() -> MemberApi.createMemberRaw(newMember.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void should_fail_create_member_if_mobile_already_exist() {
        String mobile = rMobile();
        String jwt = setupApi.registerWithLogin(mobile, rPassword()).getJwt();

        CreateMemberCommand command = CreateMemberCommand.builder()
                .mobile(mobile)
                .password(rPassword())
                .name(rMemberName())
                .departmentIds(List.of())
                .build();

        assertError(() -> MemberApi.createMemberRaw(jwt, command), MEMBER_WITH_MOBILE_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_create_member_if_email_already_exist() {
        String email = rEmail();
        String jwt = setupApi.registerWithLogin(email, rPassword()).getJwt();

        CreateMemberCommand command = CreateMemberCommand.builder()
                .email(email)
                .password(rPassword())
                .name(rMemberName())
                .departmentIds(List.of())
                .build();

        assertError(() -> MemberApi.createMemberRaw(jwt, command), MEMBER_WITH_EMAIL_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_create_member_if_both_mobile_and_email_is_empty() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).getJwt();

        CreateMemberCommand command = CreateMemberCommand.builder()
                .mobile(null)
                .email(null)
                .password(rPassword())
                .name(rMemberName())
                .departmentIds(List.of())
                .build();

        assertError(() -> MemberApi.createMemberRaw(jwt, command), MOBILE_EMAIL_CANNOT_BOTH_EMPTY);
    }

    @Test
    public void should_fail_create_member_if_exceed_packages_limit() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        int maxMemberCount = tenant.getPackages().effectiveMaxMemberCount();
        tenant.setMemberCount(maxMemberCount, User.NOUSER);
        tenantRepository.save(tenant);

        String jwt = loginResponse.getJwt();
        CreateMemberCommand anotherCommand = CreateMemberCommand.builder()
                .mobile(rMobile())
                .password(rPassword())
                .name(rMemberName())
                .departmentIds(List.of())
                .build();

        assertError(() -> MemberApi.createMemberRaw(jwt, anotherCommand), MEMBER_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_fail_create_member_if_department_not_exists() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).getJwt();

        CreateMemberCommand command = CreateMemberCommand.builder()
                .mobile(null)
                .email(null)
                .password(rPassword())
                .name(rMemberName())
                .departmentIds(List.of(newDepartmentId()))
                .build();
        assertError(() -> MemberApi.createMemberRaw(jwt, command), NOT_ALL_DEPARTMENTS_EXITS);
    }

    @Test
    public void should_create_member_if_has_extra_member() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());

        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        int maxMemberCount = tenant.getPackages().effectiveMaxMemberCount();
        tenant.setMemberCount(maxMemberCount, User.NOUSER);
        tenantRepository.save(tenant);

        String jwt = loginResponse.getJwt();
        CreateMemberCommand anotherCommand = CreateMemberCommand.builder()
                .mobile(rMobile())
                .password(rPassword())
                .name(rMemberName())
                .departmentIds(List.of())
                .build();

        assertError(() -> MemberApi.createMemberRaw(jwt, anotherCommand), MEMBER_COUNT_LIMIT_REACHED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraMemberOrderDetail.builder()
                        .type(EXTRA_MEMBER)
                        .amount(10)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(loginResponse.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");
        String memberId = MemberApi.createMember(loginResponse.getJwt());
        assertTrue(memberRepository.exists(memberId));
    }

    @Test
    public void should_import_members_via_excel() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();

        ClassPathResource resource = new ClassPathResource("testdata/member/normal-import-members.xlsx");
        MemberImportResponse importResponse = MemberApi.importMembers(response.getJwt(), resource.getFile());

        //有可能该测试多次运行，导致重复手机号等情况而无法成功
        assertTrue(importResponse.getImportedCount() == 1 || importResponse.getImportedCount() == 0);

        //无论上面是否成功，数据库中应该都有数据
        assertTrue(memberRepository.byMobileOrEmailOptional("19444444444").isPresent());
        assertTrue(memberRepository.byMobileOrEmailOptional("abcdedf@abcdefg.com").isPresent());

        MemberImportResponse againResponse = MemberApi.importMembers(response.getJwt(), resource.getFile());
        List<MemberImportRecord> errorRecords = againResponse.getErrorRecords();
        assertEquals(7, errorRecords.size());
        assertEquals(7, againResponse.getReadCount());
        assertEquals(0, againResponse.getImportedCount());

        assertEquals(2, errorRecords.get(0).getRowIndex());
        assertTrue(errorRecords.get(0).getErrors().contains("添加成员失败，手机号已被占用。"));
        assertTrue(errorRecords.get(1).getErrors().contains("添加成员失败，邮箱已被占用。"));
        assertTrue(errorRecords.get(2).getErrors().contains("添加成员失败，手机号和邮箱不能同时为空。"));
        assertTrue(errorRecords.get(3).getErrors().contains("姓名不能为空"));
        assertTrue(errorRecords.get(4).getErrors().contains("初始密码不能为空"));
        assertTrue(errorRecords.get(5).getErrors().contains("手机号格式错误"));
        assertTrue(errorRecords.get(6).getErrors().contains("邮箱格式错误"));
    }

    @Test
    public void should_fail_import_members_excel_if_packages_too_low() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant theTenant = tenantRepository.byId(response.getTenantId());
        setupApi.updateTenantPlan(theTenant, theTenant.currentPlan().withBatchImportMemberAllowed(false));

        ClassPathResource resource = new ClassPathResource("testdata/member/normal-import-members.xlsx");
        File file = resource.getFile();
        assertError(() -> MemberApi.importMembersRaw(response.getJwt(), file), BATCH_MEMBER_IMPORT_NOT_ALLOWED);
    }

    @Test
    public void should_fail_import_members_excel_if_wrong_format() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();

        ClassPathResource resource = new ClassPathResource("testdata/member/a-text-file.txt");
        File file = resource.getFile();
        assertError(() -> MemberApi.importMembersRaw(response.getJwt(), file), INVALID_MEMBER_EXCEL);
    }

    @Test
    public void should_fail_import_members_if_max_members_reached() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        ResourceUsage resourceUsage = tenant.getResourceUsage();
        ReflectionTestUtils.setField(resourceUsage, "memberCount", 10000);
        tenantRepository.save(tenant);

        ClassPathResource resource = new ClassPathResource("testdata/member/normal-import-members.xlsx");
        File file = resource.getFile();
        assertError(() -> MemberApi.importMembersRaw(response.getJwt(), file), MEMBER_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_fail_import_members_if_no_name_field() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();

        ClassPathResource resource = new ClassPathResource("testdata/member/no-name-import-members.xlsx");
        File file = resource.getFile();
        assertError(() -> MemberApi.importMembersRaw(response.getJwt(), file), INVALID_MEMBER_EXCEL);
    }

    @Test
    public void should_fail_import_members_if_no_records() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();

        ClassPathResource resource = new ClassPathResource("testdata/member/empty-import-members.xlsx");
        File file = resource.getFile();
        assertError(() -> MemberApi.importMembersRaw(response.getJwt(), file), NO_RECORDS_FOR_MEMBER_IMPORT);
    }

    @Test
    public void tenant_admin_can_update_member() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).getJwt();
        String memberId = MemberApi.createMember(jwt, rMemberName(), rMobile(), rPassword());

        String mobile = rMobile();
        String email = rEmail();
        String name = rMemberName();
        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(mobile)
                .email(email)
                .name(name)
                .departmentIds(List.of())
                .build();

        MemberApi.updateMember(jwt, memberId, command);

        MemberNameChangedEvent event = latestEventFor(memberId, MEMBER_NAME_CHANGED, MemberNameChangedEvent.class);
        assertEquals(memberId, event.getMemberId());

        Member member = memberRepository.byId(memberId);
        assertEquals(mobile, member.getMobile());
        assertEquals(email, member.getEmail());
        assertEquals(name, member.getName());
    }

    @Test
    public void update_department_should_raise_domain_event() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        String departmentId1 = DepartmentApi.createDepartment(loginResponse.getJwt(), rDepartmentName());
        String departmentId2 = DepartmentApi.createDepartment(loginResponse.getJwt(), rDepartmentName());
        String departmentId3 = DepartmentApi.createDepartment(loginResponse.getJwt(), rDepartmentName());

        String name = rMemberName();
        String memberId = MemberApi.createMember(loginResponse.getJwt(), CreateMemberCommand.builder()
                .name(name)
                .departmentIds(List.of(departmentId1, departmentId2))
                .mobile(rMobile())
                .password(rPassword())
                .build());

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile())
                .name(name)
                .departmentIds(List.of(departmentId2, departmentId3))
                .build();

        MemberApi.updateMember(loginResponse.getJwt(), memberId, command);
        MemberDepartmentsChangedEvent event = latestEventFor(memberId, MEMBER_DEPARTMENTS_CHANGED, MemberDepartmentsChangedEvent.class);
        assertEquals(memberId, event.getMemberId());
        assertEquals(1, event.getRemovedDepartmentIds().size());
        assertTrue(event.getRemovedDepartmentIds().contains(departmentId1));
        assertEquals(1, event.getAddedDepartmentIds().size());
        assertTrue(event.getAddedDepartmentIds().contains(departmentId3));
    }

    @Test
    public void non_tenant_admin_should_fail_update_member() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).getJwt();
        CreateMemberResponse nonTenantAdminMember = MemberApi.createMemberAndLogin(jwt, rMemberName(), rMobile(), rPassword());

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile())
                .email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of())
                .build();

        assertError(() -> MemberApi.updateMemberRaw(nonTenantAdminMember.getJwt(), nonTenantAdminMember.getMemberId(), command), ACCESS_DENIED);
    }

    @Test
    public void should_fail_update_member_if_both_mobile_and_email_are_empty() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(null)
                .email(null)
                .name(rMemberName())
                .departmentIds(List.of())
                .build();

        assertError(() -> MemberApi.updateMemberRaw(response.getJwt(), response.getMemberId(), command), MOBILE_EMAIL_CANNOT_BOTH_EMPTY);
    }

    @Test
    public void should_fail_update_member_if_department_not_exists() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile())
                .email(null)
                .name(rMemberName())
                .departmentIds(List.of(newDepartmentId()))
                .build();

        assertError(() -> MemberApi.updateMemberRaw(response.getJwt(), response.getMemberId(), command), NOT_ALL_DEPARTMENTS_EXITS);
    }

    @Test
    public void should_update_member_role() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        String memberId = MemberApi.createMember(loginResponse.getJwt());
        assertEquals(TENANT_MEMBER, memberRepository.byId(memberId).getRole());

        MemberApi.updateMemberRole(loginResponse.getJwt(), memberId, UpdateMemberRoleCommand.builder().role(TENANT_ADMIN).build());
        assertEquals(TENANT_ADMIN, memberRepository.byId(memberId).getRole());
    }

    @Test
    public void should_fail_update_member_role_to_normal_member_if_no_admin_left() {
        LoginResponse response = setupApi.registerWithLogin();

        assertError(() -> MemberApi.updateMemberRoleRaw(response.getJwt(), response.getMemberId(),
                UpdateMemberRoleCommand.builder().role(TENANT_MEMBER).build()), NO_ACTIVE_TENANT_ADMIN_LEFT);
    }

    @Test
    public void should_fail_update_member_to_admin_if_max_reached() {
        LoginResponse response = setupApi.registerWithLogin();

        IntStream.range(1, 10).forEach(value -> {
            String memberId = MemberApi.createMember(response.getJwt());
            MemberApi.updateMemberRole(response.getJwt(), memberId, UpdateMemberRoleCommand.builder().role(TENANT_ADMIN).build());
        });

        String memberId = MemberApi.createMember(response.getJwt());
        assertError(
                () -> MemberApi.updateMemberRoleRaw(response.getJwt(), memberId, UpdateMemberRoleCommand.builder().role(TENANT_ADMIN).build()),
                MAX_TENANT_ADMIN_REACHED);
    }

    @Test
    public void should_top_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateAppResponse appResponse = AppApi.createApp(response.getJwt());
        MemberApi.topApp(response.getJwt(), response.getAppId());
        MemberApi.topApp(response.getJwt(), appResponse.getAppId());
        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(appResponse.getAppId(), member.getTopAppIds().get(0));
        assertEquals(response.getAppId(), member.getTopAppIds().get(1));
    }

    @Test
    public void should_cancel_top_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateAppResponse appResponse = AppApi.createApp(response.getJwt());
        MemberApi.topApp(response.getJwt(), response.getAppId());
        MemberApi.topApp(response.getJwt(), appResponse.getAppId());
        MemberApi.cancelTopApp(response.getJwt(), appResponse.getAppId());
        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(1, member.getTopAppIds().size());
        assertEquals(response.getAppId(), member.getTopAppIds().get(0));
    }

    @Test
    public void tenant_admin_can_delete_member() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        String memberId = MemberApi.createMember(response.getJwt(), rMemberName(), rMobile(), rPassword());

        MemberApi.deleteMember(response.getJwt(), memberId);

        Optional<Member> member = memberRepository.byIdOptional(memberId);
        assertFalse(member.isPresent());
    }

    @Test
    public void should_raise_event_when_delete_member() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.getJwt());
        AppApi.setAppManager(response.getJwt(), response.getAppId(), memberId);
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberId);
        assertTrue(appRepository.byId(response.getAppId()).getManagers().contains(memberId));
        assertTrue(groupRepository.byId(response.getDefaultGroupId()).getManagers().contains(memberId));
        assertEquals(2, tenantRepository.byId(response.getTenantId()).getResourceUsage().getMemberCount());

        MemberApi.deleteMember(response.getJwt(), memberId);

        MemberDeletedEvent event = latestEventFor(memberId, MEMBER_DELETED, MemberDeletedEvent.class);
        assertEquals(memberId, event.getMemberId());
        assertFalse(appRepository.byId(response.getAppId()).getManagers().contains(memberId));
        assertFalse(groupRepository.byId(response.getDefaultGroupId()).getManagers().contains(memberId));
        assertEquals(1, tenantRepository.byId(response.getTenantId()).getResourceUsage().getMemberCount());
    }

    @Test
    public void should_fail_delete_member_if_only_one_admin_left() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        assertError(() -> MemberApi.deleteMemberRaw(response.getJwt(), response.getMemberId()), NO_ACTIVE_TENANT_ADMIN_LEFT);
    }

    @Test
    public void should_fail_delete_member_if_its_the_only_one_active_tenant_admin() {
        PreparedAppResponse response = setupApi.registerWithApp();
        assertError(() -> MemberApi.deleteMemberRaw(response.getJwt(), response.getMemberId()), NO_ACTIVE_TENANT_ADMIN_LEFT);
    }

    @Test
    public void delete_member_should_also_delete_them_from_app_groups() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        String memberId = MemberApi.createMember(response.getJwt(), rMemberName(), rMobile(), rPassword());
        CreateAppResponse app = AppApi.createApp(response.getJwt());
        GroupApi.addGroupMembers(response.getJwt(), app.getDefaultGroupId(), memberId);
        Group group = groupRepository.byId(app.getDefaultGroupId());
        assertTrue(group.getMembers().contains(memberId));

        List<AppCachedGroup> appCachedGroups = groupRepository.cachedAppAllGroups(app.getAppId());
        Group cachedGroup = groupRepository.cachedById(app.getDefaultGroupId());
        String groupsKey = "Cache:APP_GROUPS::" + app.getAppId();
        String defaultGroupKey = "Cache:GROUP::" + app.getDefaultGroupId();

        assertEquals(TRUE, stringRedisTemplate.hasKey(groupsKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(defaultGroupKey));

        MemberApi.deleteMember(response.getJwt(), memberId);

        Group updatedGroup = groupRepository.byId(app.getDefaultGroupId());
        assertFalse(updatedGroup.getMembers().contains(memberId));
        assertEquals(0, updatedGroup.getManagers().size());
        assertEquals(0, updatedGroup.getMembers().size());
        assertEquals(FALSE, stringRedisTemplate.hasKey(groupsKey));
        assertEquals(FALSE, stringRedisTemplate.hasKey(defaultGroupKey));
    }

    @Test
    public void delete_member_should_also_delete_from_app_managers() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.getJwt());
        AppApi.setAppManagers(response.getJwt(), response.getAppId(), memberId);
        assertTrue(appRepository.byId(response.getAppId()).getManagers().contains(memberId));

        App cachedApp = appRepository.cachedById(response.getAppId());
        List<TenantCachedApp> cachedApps = appRepository.cachedTenantAllApps(response.getTenantId());
        String appsKey = "Cache:TENANT_APPS::" + response.getTenantId();
        String appKey = "Cache:APP::" + response.getAppId();
        assertEquals(TRUE, stringRedisTemplate.hasKey(appKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(appsKey));

        MemberApi.deleteMember(response.getJwt(), memberId);
        assertFalse(appRepository.byId(response.getAppId()).getManagers().contains(memberId));
        assertEquals(FALSE, stringRedisTemplate.hasKey(appKey));
        assertEquals(FALSE, stringRedisTemplate.hasKey(appsKey));
    }

    @Test
    public void delete_member_should_also_delete_from_department_managers() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.getJwt());
        String departmentId = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.getJwt(), memberId, command);
        DepartmentApi.addDepartmentManager(response.getJwt(), departmentId, memberId);
        assertTrue(departmentRepository.byId(departmentId).getManagers().contains(memberId));

        departmentRepository.cachedTenantAllDepartments(response.getTenantId());
        String key = "Cache:TENANT_DEPARTMENTS::" + response.getTenantId();
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));

        MemberApi.deleteMember(response.getJwt(), memberId);
        assertFalse(departmentRepository.byId(departmentId).getManagers().contains(memberId));
        assertEquals(FALSE, stringRedisTemplate.hasKey(key));
    }

    @Test
    public void should_reset_password_for_member() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        String memberId = MemberApi.createMember(response.getJwt(), rMemberName(), rMobile(), rPassword());
        String newPassword = rPassword();

        MemberApi.resetPassword(response.getJwt(), memberId, ResetMemberPasswordCommand.builder().password(newPassword).build());

        Member member = memberRepository.byId(memberId);
        assertTrue(mryPasswordEncoder.matches(newPassword, member.getPassword()));
    }

    @Test
    public void should_unbind_wx_for_member() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        String memberId = MemberApi.createMember(response.getJwt(), rMemberName(), rMobile(), rPassword());
        Member member = memberRepository.byId(memberId);
        ReflectionTestUtils.setField(member, "pcWxOpenId", rPcWxOpenId());
        ReflectionTestUtils.setField(member, "mobileWxOpenId", rMobileWxOpenId());
        memberRepository.save(member);
        Member changedMember = memberRepository.byId(memberId);
        assertNotNull(changedMember.getMobileWxOpenId());
        assertNotNull(changedMember.getPcWxOpenId());

        MemberApi.unbindWx(response.getJwt(), memberId);

        Member updatedMember = memberRepository.byId(memberId);
        assertNull(updatedMember.getMobileWxOpenId());
        assertNull(updatedMember.getPcWxOpenId());
    }

    @Test
    public void should_update_my_base_setting() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        String newName = rMemberName();

        UpdateMyBaseSettingCommand command = UpdateMyBaseSettingCommand.builder().name(newName).build();
        MemberApi.updateMyBaseSetting(response.getJwt(), command);

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(newName, member.getName());
    }

    @Test
    public void should_sync_new_name_if_name_changed() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse memberAndLogin = MemberApi.createMemberAndLogin(response.getJwt());
        String submissionId = SubmissionApi.newSubmission(memberAndLogin.getJwt(), response.getQrId(), response.getHomePageId());

        String newName = rMemberName();

        UpdateMyBaseSettingCommand command = UpdateMyBaseSettingCommand.builder().name(newName).build();
        MemberApi.updateMyBaseSetting(response.getJwt(), command);

        MemberNameChangedEvent event = latestEventFor(response.getMemberId(), MEMBER_NAME_CHANGED, MemberNameChangedEvent.class);
        assertEquals(response.getMemberId(), event.getMemberId());

        App app = appRepository.byId(response.getAppId());
        assertEquals(newName, app.getCreator());

        QR qr = qrRepository.byId(response.getQrId());
        assertEquals(newName, qr.getCreator());

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(memberAndLogin.getName(), submission.getCreator());
    }

    @Test
    public void should_update_and_delete_my_avatar() {
        PreparedAppResponse response = setupApi.registerWithApp();

        UploadedFile avatar = rImageFile();
        MemberApi.updateMyAvatar(response.getJwt(), UpdateMyAvatarCommand.builder().avatar(avatar).build());

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(avatar, member.getAvatar());

        MemberApi.deleteMyAvatar(response.getJwt());
        Member updatedMember = memberRepository.byId(response.getMemberId());
        assertNull(updatedMember.getAvatar());
    }

    @Test
    public void should_change_my_mobile() {
        String oldMobile = rMobile();
        String password = rPassword();
        LoginResponse response = setupApi.registerWithLogin(oldMobile, password);

        String newMobile = rMobile();
        String codeId = createVerificationCodeForChangeMobile(response.getJwt(),
                CreateChangeMobileVerificationCodeCommand.builder().mobile(newMobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        MemberApi.changeMyMobile(response.getJwt(),
                ChangeMyMobileCommand.builder().mobile(newMobile).verification(code.getCode()).password(password).build());

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(newMobile, member.getMobile());
    }

    @Test
    public void should_fail_change_my_mobile_if_password_not_match() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        String newMobile = rMobile();
        String codeId = createVerificationCodeForChangeMobile(response.getJwt(),
                CreateChangeMobileVerificationCodeCommand.builder().mobile(newMobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        ChangeMyMobileCommand command = ChangeMyMobileCommand.builder().mobile(newMobile).verification(code.getCode()).password(rPassword())
                .build();

        assertError(() -> MemberApi.changeMyMobileRaw(response.getJwt(), command), PASSWORD_NOT_MATCH);
    }

    @Test
    public void should_fail_change_my_mobile_if_mobile_already_exists() {
        String password = rPassword();
        LoginResponse response = setupApi.registerWithLogin(rMobile(), password);
        String alreadyExistsMobile = rMobile();
        String codeId = createVerificationCodeForChangeMobile(response.getJwt(),
                CreateChangeMobileVerificationCodeCommand.builder().mobile(alreadyExistsMobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        setupApi.registerWithLogin(alreadyExistsMobile, rPassword());

        ChangeMyMobileCommand command = ChangeMyMobileCommand.builder().mobile(alreadyExistsMobile).verification(code.getCode())
                .password(password).build();

        assertError(() -> MemberApi.changeMyMobileRaw(response.getJwt(), command), MEMBER_WITH_MOBILE_ALREADY_EXISTS);
    }

    @Test
    public void should_identify_my_mobile() {
        String mobile = rMobile();
        LoginResponse response = setupApi.registerWithLogin(mobile, rPassword());

        String codeId = createVerificationCodeForIdentifyMobile(response.getJwt(),
                IdentifyMobileVerificationCodeCommand.builder().mobile(mobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        MemberApi.identifyMyMobile(response.getJwt(), IdentifyMyMobileCommand.builder().mobile(mobile).verification(code.getCode()).build());

        Member member = memberRepository.byId(response.getMemberId());
        assertTrue(member.isMobileIdentified());
    }

    @Test
    public void should_fail_identify_my_mobile_if_mobile_already_exists() {
        String password = rPassword();
        LoginResponse response = setupApi.registerWithLogin(rMobile(), password);
        String alreadyExistsMobile = rMobile();
        String codeId = createVerificationCodeForIdentifyMobile(response.getJwt(),
                IdentifyMobileVerificationCodeCommand.builder().mobile(alreadyExistsMobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        setupApi.registerWithLogin(alreadyExistsMobile, rPassword());

        IdentifyMyMobileCommand command = IdentifyMyMobileCommand.builder().mobile(alreadyExistsMobile).verification(code.getCode()).build();

        assertError(() -> MemberApi.identifyMyMobileRaw(response.getJwt(), command), MEMBER_WITH_MOBILE_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_identify_my_mobile_is_mobile_number_not_the_same() {
        String wrongMobile = rMobile();
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        String codeId = createVerificationCodeForIdentifyMobile(response.getJwt(),
                IdentifyMobileVerificationCodeCommand.builder().mobile(wrongMobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);

        IdentifyMyMobileCommand command = IdentifyMyMobileCommand.builder().mobile(wrongMobile).verification(code.getCode()).build();
        assertError(() -> MemberApi.identifyMyMobileRaw(response.getJwt(), command), IDENTIFY_MOBILE_NOT_THE_SAME);
    }

    @Test
    public void should_change_my_password() {
        String mobile = rMobile();
        String password = rPassword();

        LoginResponse response = setupApi.registerWithLogin(mobile, password);
        String newPassword = rPassword();
        MemberApi.changeMyPassword(response.getJwt(),
                ChangeMyPasswordCommand.builder()
                        .oldPassword(password)
                        .newPassword(newPassword)
                        .confirmNewPassword(newPassword)
                        .build());

        LoginApi.loginWithMobileOrEmail(mobile, newPassword);
    }

    @Test
    public void should_fail_change_my_password_if_old_password_not_correct() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        String newPassword = rPassword();
        String wrongOldPassword = rPassword();
        ChangeMyPasswordCommand command = ChangeMyPasswordCommand.builder()
                .oldPassword(wrongOldPassword)
                .newPassword(newPassword)
                .confirmNewPassword(newPassword)
                .build();

        assertError(() -> MemberApi.changeMyPasswordRaw(response.getJwt(), command), PASSWORD_NOT_MATCH);
    }

    @Test
    public void should_fail_change_my_password_if_confirm_password_not_correct() {
        String oldPassword = rPassword();
        LoginResponse response = setupApi.registerWithLogin(rMobile(), oldPassword);

        String newPassword = rPassword();
        String wrongNewPassword = rPassword();
        ChangeMyPasswordCommand command = ChangeMyPasswordCommand.builder()
                .oldPassword(oldPassword)
                .newPassword(newPassword)
                .confirmNewPassword(wrongNewPassword)
                .build();

        assertError(() -> MemberApi.changeMyPasswordRaw(response.getJwt(), command), PASSWORD_CONFIRM_NOT_MATCH);
    }

    @Test
    public void should_fail_change_my_password_if_provided_the_same_password() {
        String oldPassword = rPassword();
        LoginResponse response = setupApi.registerWithLogin(rMobile(), oldPassword);

        ChangeMyPasswordCommand command = ChangeMyPasswordCommand.builder()
                .oldPassword(oldPassword)
                .newPassword(oldPassword)
                .confirmNewPassword(oldPassword)
                .build();

        assertError(() -> MemberApi.changeMyPasswordRaw(response.getJwt(), command), NEW_PASSWORD_SAME_WITH_OLD);
    }

    @Test
    public void should_unbind_my_wx() {
        String mobile = rMobile();
        String password = rPassword();
        LoginResponse response = setupApi.registerWithLogin(mobile, password);
        Member member = memberRepository.byId(response.getMemberId());
        ReflectionTestUtils.setField(member, "pcWxOpenId", rPcWxOpenId());
        ReflectionTestUtils.setField(member, "mobileWxOpenId", rMobileWxOpenId());
        memberRepository.save(member);
        Member changedMember = memberRepository.byId(response.getMemberId());
        assertNotNull(changedMember.getMobileWxOpenId());
        assertNotNull(changedMember.getPcWxOpenId());

        MemberApi.unbindMyWx(response.getJwt());

        Member updatedMember = memberRepository.byId(response.getMemberId());
        assertNull(updatedMember.getMobileWxOpenId());
        assertNull(updatedMember.getPcWxOpenId());
    }

    @Test
    public void should_findback_password() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);

        String codeId = VerificationCodeApi.createVerificationCodeForFindbackPassword(
                CreateFindbackPasswordVerificationCodeCommand.builder()
                        .mobileOrEmail(mobile)
                        .build()
        );

        VerificationCode code = verificationCodeRepository.byId(codeId);
        String newPassword = rPassword();
        MemberApi.findbackPassword(
                FindbackPasswordCommand.builder()
                        .mobileOrEmail(mobile)
                        .password(newPassword)
                        .verification(code.getCode())
                        .build());

        LoginApi.loginWithMobileOrEmail(mobile, newPassword);
    }

    @Test
    public void should_fail_findback_password_if_for_wrong_member() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.registerWithLogin(mobile, password);

        String codeId = VerificationCodeApi.createVerificationCodeForFindbackPassword(
                CreateFindbackPasswordVerificationCodeCommand.builder()
                        .mobileOrEmail(mobile)
                        .build()
        );
        VerificationCode code = verificationCodeRepository.byId(codeId);
        FindbackPasswordCommand command = FindbackPasswordCommand.builder()
                .mobileOrEmail(rMobile())
                .password(rPassword())
                .verification(code.getCode())
                .build();

        assertError(() -> MemberApi.findbackPasswordRaw(command), VERIFICATION_CODE_CHECK_FAILED);
    }

    @Test
    public void should_fetch_own_profile() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        QConsoleMemberProfile profile = MemberApi.myProfile(response.getJwt());

        assertEquals(response.getMemberId(), profile.getMemberId());
        assertEquals(response.getTenantId(), profile.getTenantId());
        assertNotNull(profile.getName());
        assertEquals(TENANT_ADMIN, profile.getRole());
        assertTrue(profile.isHasManagedApps());
        QConsoleTenantProfile tenantProfile = profile.getTenantProfile();
        assertEquals(response.getTenantId(), tenantProfile.getTenantId());
        assertNotNull(tenantProfile.getName());
        assertTrue(profile.isMobileIdentified());
    }

    @Test
    public void should_fetch_tenant_package_status() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        QConsoleMemberProfile profile = MemberApi.myProfile(response.getJwt());
        QPackagesStatus packagesStatus = profile.getTenantProfile().getPackagesStatus();
        assertNotNull(packagesStatus);
    }

    @Test
    public void normal_member_should_fetch_own_profile() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        CreateMemberResponse createMemberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());

        QConsoleMemberProfile profile = MemberApi.myProfile(createMemberResponse.getJwt());

        assertEquals(TENANT_MEMBER, profile.getRole());
        assertFalse(profile.isHasManagedApps());
    }

    @Test
    public void app_manager_should_fetch_own_profile() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        CreateMemberResponse newMember = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        SetAppManagersCommand setAppManagersCommand = SetAppManagersCommand.builder().managers(newArrayList(newMember.getMemberId())).build();
        AppApi.setAppManagers(response.getJwt(), response.getAppId(), setAppManagersCommand);

        QConsoleMemberProfile profile = MemberApi.myProfile(newMember.getJwt());

        assertTrue(profile.isHasManagedApps());
    }

    @Test
    public void should_fetch_my_client_profile() {
        String mobile = rMobile();
        String password = rPassword();
        LoginResponse response = setupApi.registerWithLogin(mobile, password);

        QClientMemberProfile profile = MemberApi.myClientProfile(response.getJwt());

        assertNotNull(profile.getMemberName());
        assertNotNull(profile.getTenantName());
        assertEquals(response.getMemberId(), profile.getMemberId());
        assertEquals(response.getTenantId(), profile.getTenantId());
    }

    @Test
    public void should_fetch_my_member_info() {
        String mobile = rMobile();
        LoginResponse response = setupApi.registerWithLogin(mobile, rPassword());

        QMemberInfo memberInfo = MemberApi.myMemberInfo(response.getJwt());

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(response.getMemberId(), memberInfo.getMemberId());
        assertEquals(response.getTenantId(), memberInfo.getTenantId());
        assertEquals(member.getName(), memberInfo.getName());
        assertEquals(member.getMobile(), memberInfo.getMobile());
        assertNotNull(memberInfo.getRole());
    }

    @Test
    public void should_fetch_my_base_setting() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        QMemberBaseSetting baseSetting = MemberApi.myBaseSetting(response.getJwt());

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(response.getMemberId(), baseSetting.getId());
        assertEquals(member.getName(), baseSetting.getName());
    }

    @Test
    public void tenant_admin_should_fetch_all_members() {
        LoginResponse admin = setupApi.registerWithLogin(rMobile(), rPassword());
        //可以创建很多成员

        IntStream.range(1, 30).forEach(value -> MemberApi.createMember(admin.getJwt(), rMemberName(), rMobile(), rPassword()));

        PagedList<QListMember> firstPage = MemberApi.listMembers(admin.getJwt(), null, null, null, false, 1, 20);
        assertEquals(20, firstPage.getData().size());
        assertEquals(30, firstPage.getTotalNumber());
        assertEquals(1, firstPage.getPageIndex());
        assertEquals(20, firstPage.getPageSize());

        QListMember aMember = firstPage.getData().get(0);
        assertNotNull(aMember.getId());
        assertNotNull(aMember.getName());
        assertNotNull(aMember.getMobile());
        assertNotNull(aMember.getRole());
        assertNotNull(aMember.getCreatedAt());
        assertTrue(aMember.isActive());

        PagedList<QListMember> secondPage = MemberApi.listMembers(admin.getJwt(), null, null, null, false, 2, 20);
        assertEquals(10, secondPage.getData().size());
        assertEquals(30, secondPage.getTotalNumber());
        assertEquals(2, secondPage.getPageIndex());
        assertEquals(20, secondPage.getPageSize());
    }

    @Test
    public void should_fetch_member_list_and_search() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        String firstMemberName = rMemberName();
        String memberId1 = MemberApi.createMember(response.getJwt(), firstMemberName, rMobile(), rPassword());
        Member member1 = memberRepository.byId(memberId1);
        String customId = UuidGenerator.newShortUuid();
        member1.updateCustomId(customId, member1.toUser());
        memberRepository.save(member1);

        String secondMemberMobile = rMobile();
        String memberId2 = MemberApi.createMember(response.getJwt(), rMemberName(), secondMemberMobile, rPassword());

        String thirdMemberEmail = rEmail();
        String memberId3 = MemberApi.createMember(response.getJwt(), rMemberName(), rMobile(), thirdMemberEmail, rPassword());

        PagedList<QListMember> allMembers = MemberApi.listMembers(response.getJwt(), null, null, null, false, 1, 20);
        assertEquals(4, allMembers.getData().size());
        assertEquals(memberId3, allMembers.getData().get(0).getId());
        assertEquals(memberId2, allMembers.getData().get(1).getId());
        assertEquals(memberId1, allMembers.getData().get(2).getId());
        assertEquals(response.getMemberId(), allMembers.getData().get(3).getId());

        //模糊搜索姓名
        PagedList<QListMember> fuzzyNameResult = MemberApi.listMembers(response.getJwt(), null, firstMemberName.substring(1), null, false, 1,
                20);
        assertEquals(1, fuzzyNameResult.getData().size());
        assertEquals(memberId1, fuzzyNameResult.getData().get(0).getId());

        //精确搜索手机号
        PagedList<QListMember> mobileResult = MemberApi.listMembers(response.getJwt(), null, secondMemberMobile, null, false, 1, 20);
        assertEquals(1, mobileResult.getData().size());
        assertEquals(memberId2, mobileResult.getData().get(0).getId());

        //精确搜索邮箱
        PagedList<QListMember> emailResult = MemberApi.listMembers(response.getJwt(), null, thirdMemberEmail, null, false, 1, 20);
        assertEquals(1, emailResult.getData().size());
        assertEquals(memberId3, emailResult.getData().get(0).getId());

        //直接搜索memberId
        PagedList<QListMember> byIdResult = MemberApi.listMembers(response.getJwt(), null, memberId1, null, false, 1, 20);
        assertEquals(1, byIdResult.getData().size());
        assertEquals(memberId1, byIdResult.getData().get(0).getId());

        //搜索customId
        PagedList<QListMember> byCustomIdResult = MemberApi.listMembers(response.getJwt(), null, customId, null, false, 1, 20);
        assertEquals(1, byCustomIdResult.getData().size());
        assertEquals(memberId1, byCustomIdResult.getData().get(0).getId());

        //模糊搜索手机号
        PagedList<QListMember> fuzzyMobileResult = MemberApi.listMembers(response.getJwt(), null, secondMemberMobile.substring(0, 5), null,
                false, 1, 20);
        assertEquals(1, fuzzyMobileResult.getData().size());
        assertEquals(memberId2, fuzzyMobileResult.getData().get(0).getId());

        //基于姓名排序
        PagedList<QListMember> allAscOrdersMembers = MemberApi.listMembers(response.getJwt(), null, null, "name", true, 1, 20);
        PagedList<QListMember> allDescOrdersMembers = MemberApi.listMembers(response.getJwt(), null, null, "name", false, 1, 20);
        List<String> allAscNames = allAscOrdersMembers.getData().stream().map(QListMember::getName).collect(toList());
        List<String> allDescNames = allDescOrdersMembers.getData().stream().map(QListMember::getName).collect(toList());
        assertNotEquals(allAscNames, allDescNames);
    }

    @Test
    public void should_list_members_by_department() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId1 = DepartmentApi.createDepartment(response.getJwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        String departmentId2 = DepartmentApi.createDepartment(response.getJwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());

        String memberId1 = MemberApi.createMember(response.getJwt(), CreateMemberCommand.builder()
                .name(rMemberName())
                .departmentIds(List.of(departmentId1))
                .mobile(rMobile())
                .password(rPassword())
                .build());

        String memberId2 = MemberApi.createMember(response.getJwt(), CreateMemberCommand.builder()
                .name(rMemberName())
                .departmentIds(List.of(departmentId2))
                .mobile(rMobile())
                .password(rPassword())
                .build());

        String memberId3 = MemberApi.createMember(response.getJwt(), CreateMemberCommand.builder()
                .name(rMemberName())
                .departmentIds(List.of(departmentId1, departmentId2))
                .mobile(rMobile())
                .password(rPassword())
                .build());

        PagedList<QListMember> members0 = MemberApi.listMembers(response.getJwt(), null, null, null, false, 1, 10);
        assertEquals(4, members0.getData().size());

        PagedList<QListMember> members1 = MemberApi.listMembers(response.getJwt(), departmentId1, null, null, false, 1, 10);
        assertEquals(2, members1.getData().size());
        List<String> memberIds1 = members1.getData().stream().map(QListMember::getId).toList();
        assertTrue(memberIds1.contains(memberId1));
        assertTrue(memberIds1.contains(memberId3));

        PagedList<QListMember> members2 = MemberApi.listMembers(response.getJwt(), departmentId2, null, null, false, 1, 10);
        assertEquals(2, members2.getData().size());
        List<String> memberIds2 = members2.getData().stream().map(QListMember::getId).toList();
        assertTrue(memberIds2.contains(memberId2));
        assertTrue(memberIds2.contains(memberId3));
    }

    @Test
    public void all_members_should_be_able_to_fetch_all_member_reference() {
        LoginResponse response = setupApi.registerWithLogin();

        String name = rMemberName();
        String mobile = rMobile();
        String email = rEmail();
        String memberId = MemberApi.createMember(response.getJwt(), name, mobile, email, rPassword());
        MemberApi.createMember(response.getJwt(), rMemberName(), rMobile(), rPassword());
        CreateMemberResponse createMemberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());

        List<QMemberReference> memberReferences = MemberApi.allMemberReferences(createMemberResponse.getJwt());
        assertEquals(4, memberReferences.size());
        QMemberReference memberReference = memberReferences.stream()
                .filter(memberDetailedReference1 -> memberId.equals(memberDetailedReference1.getId())).findFirst().get();
        assertTrue(memberReference.getShowName().contains("****"));
    }

    @Test
    public void should_fetch_all_member_reference_for_given_tenant() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        MemberApi.createMember(response.getJwt(), rMemberName(), rMobile(), rPassword());
        MemberApi.createMember(response.getJwt(), rMemberName(), rMobile(), rPassword());

        List<QMemberReference> memberReferences = MemberApi.allMemberReferences(response.getJwt(), response.getTenantId());

        assertEquals(3, memberReferences.size());
        assertNotNull(memberReferences.get(0).getShowName());
        assertNotNull(memberReferences.get(0).getId());
    }

    @Test
    public void member_from_other_tenant_should_fail_fetch_all_member_reference_for_given_tenant() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        LoginResponse other = setupApi.registerWithLogin(rMobile(), rPassword());

        assertError(() -> MemberApi.allMemberReferencesRaw(other.getJwt(), response.getTenantId()), WRONG_TENANT);
    }

    @Test
    public void should_deactivate_member() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.getJwt());

        MemberApi.deactivateMember(response.getJwt(), memberId);
        assertFalse(memberRepository.byId(memberId).isActive());
    }

    @Test
    public void should_fail_deactivate_if_its_the_only_one_tenant_admin() {
        PreparedAppResponse response = setupApi.registerWithApp();
        assertError(() -> MemberApi.deactivateMemberRaw(response.getJwt(), response.getMemberId()), NO_ACTIVE_TENANT_ADMIN_LEFT);
    }

    @Test
    public void should_fail_deactivate_if_its_the_only_one_active_tenant_admin() {
        PreparedAppResponse response = setupApi.registerWithApp();
        assertError(() -> MemberApi.deactivateMemberRaw(response.getJwt(), response.getMemberId()), NO_ACTIVE_TENANT_ADMIN_LEFT);
    }

    @Test
    public void should_activate_member() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.getJwt());

        MemberApi.deactivateMember(response.getJwt(), memberId);
        assertFalse(memberRepository.byId(memberId).isActive());

        MemberApi.activateMember(response.getJwt(), memberId);
        assertTrue(memberRepository.byId(memberId).isActive());
    }

    @Test
    public void should_cache_member() {
        LoginResponse response = setupApi.registerWithLogin();
        String key = "Cache:MEMBER::" + response.getMemberId();
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));
        Member member = memberRepository.cachedById(response.getMemberId());
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));

        memberRepository.save(member);
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));
    }

    @Test
    public void should_cache_member_references() {
        LoginResponse response = setupApi.registerWithLogin();
        memberRepository.save(memberRepository.byId(response.getMemberId()));
        String key = "Cache:TENANT_MEMBERS::" + response.getTenantId();
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));

        memberRepository.cachedAllMemberReferences(response.getTenantId());
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));

        memberRepository.save(memberRepository.byId(response.getMemberId()));
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));
    }

    @Test
    public void save_member_should_evict_cache() {
        LoginResponse response = setupApi.registerWithLogin();
        String newMemberId = MemberApi.createMember(response.getJwt());
        String membersKey = "Cache:TENANT_MEMBERS::" + response.getTenantId();
        String memberKey = "Cache:MEMBER::" + response.getMemberId();
        String newMemberKey = "Cache:MEMBER::" + newMemberId;

        memberRepository.cachedById(response.getMemberId());
        memberRepository.cachedById(newMemberId);
        memberRepository.cachedTenantAllMembers(response.getTenantId());
        assertEquals(TRUE, stringRedisTemplate.hasKey(membersKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(memberKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(newMemberKey));

        Member member = memberRepository.byId(newMemberId);
        memberRepository.save(member);

        assertNotEquals(TRUE, stringRedisTemplate.hasKey(membersKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(memberKey));
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(newMemberKey));
    }

    @Test
    public void delete_member_should_evict_cache() {
        LoginResponse response = setupApi.registerWithLogin();
        String newMemberId = MemberApi.createMember(response.getJwt());
        String membersKey = "Cache:TENANT_MEMBERS::" + response.getTenantId();
        String memberKey = "Cache:MEMBER::" + response.getMemberId();
        String newMemberKey = "Cache:MEMBER::" + newMemberId;

        memberRepository.cachedById(response.getMemberId());
        memberRepository.cachedById(newMemberId);
        memberRepository.cachedTenantAllMembers(response.getTenantId());
        assertEquals(TRUE, stringRedisTemplate.hasKey(membersKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(memberKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(newMemberKey));

        Member member = memberRepository.byId(newMemberId);
        member.onDelete(User.NOUSER);
        memberRepository.delete(member);

        assertNotEquals(TRUE, stringRedisTemplate.hasKey(membersKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(memberKey));
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(newMemberKey));
    }
}