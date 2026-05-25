package com.mryqr.core.register;

import com.mryqr.BaseApiTest;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.event.MemberCreatedEvent;
import com.mryqr.core.plan.domain.PlanType;
import com.mryqr.core.register.command.RegisterCommand;
import com.mryqr.core.register.command.RegisterResponse;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.event.TenantCreatedEvent;
import com.mryqr.core.verification.VerificationCodeApi;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static com.mryqr.common.domain.user.Role.TENANT_ADMIN;
import static com.mryqr.common.event.DomainEventType.MEMBER_CREATED;
import static com.mryqr.common.event.DomainEventType.TENANT_CREATED;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegisterControllerApiTest extends BaseApiTest {

    @Test
    public void should_register_with_mobile() {
        String tenantName = rTenantName();
        String mobile = rMobile();
        String username = rMemberName();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobile);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterResponse response = RegisterApi.register(RegisterCommand.builder()
                .mobileOrEmail(mobile)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .tenantName(tenantName)
                .agreement(true)
                .build());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertNotNull(tenant);
        assertEquals(tenantName, tenant.getName());
        assertEquals(PlanType.FREE, tenant.getPackages().currentPlanType());
        assertEquals(2099, LocalDate.ofInstant(tenant.getPackages().expireAt(), ZoneId.systemDefault()).getYear());
        assertEquals(tenantName, tenant.getName());
        assertNotNull(tenant.getApiSetting());
        assertNotNull(tenant.getApiSetting().getApiKey());
        assertNotNull(tenant.getApiSetting().getApiSecret());

        Member member = memberRepository.byId(response.getMemberId());
        assertNotNull(member);
        assertEquals(response.getTenantId(), member.getTenantId());
        assertEquals(username, member.getName());
        assertEquals(mobile, member.getMobile());
        assertEquals(TENANT_ADMIN, member.getRole());

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(tenant.getId());
        assertEquals(tenant.getId(), departmentHierarchy.getTenantId());
    }

    @Test
    public void should_register_with_email() {
        String tenantName = rTenantName();
        String email = rEmail();
        String username = rMemberName();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(email);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterResponse response = RegisterApi.register(RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .tenantName(tenantName)
                .agreement(true)
                .build());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertNotNull(tenant);
        assertEquals(tenantName, tenant.getName());
        assertEquals(PlanType.FREE, tenant.getPackages().currentPlanType());
        assertEquals(2099, LocalDate.ofInstant(tenant.getPackages().expireAt(), ZoneId.systemDefault()).getYear());
        Member member = memberRepository.byId(response.getMemberId());
        assertNotNull(member);
        assertEquals(username, member.getName());
        assertEquals(email, member.getEmail());
        assertEquals(TENANT_ADMIN, member.getRole());
    }

    @Test
    public void should_fail_to_register_if_mobile_already_exists() {
        String tenantName = rTenantName();
        String mobile = rMobile();
        String username = rMemberName();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobile);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(mobile)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .tenantName(tenantName)
                .agreement(true)
                .build();

        RegisterApi.register(command);//先注册以占用手机号
        assertError(() -> RegisterApi.registerRaw(command), MEMBER_WITH_MOBILE_OR_EMAIL_ALREADY_EXISTS);
    }


    @Test
    public void should_fail_to_register_if_email_already_exists() {
        String tenantName = rTenantName();
        String email = rEmail();
        String username = rMemberName();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(email);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .tenantName(tenantName)
                .agreement(true)
                .build();

        RegisterApi.register(command);//先注册以占用邮箱
        assertError(() -> RegisterApi.registerRaw(command), MEMBER_WITH_MOBILE_OR_EMAIL_ALREADY_EXISTS);
    }

    @Test
    @Disabled
    public void should_fail_to_register_if_verification_not_valid() {
        String tenantName = rTenantName();
        String email = rEmail();
        String username = rMemberName();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(rVerificationCode())
                .password(rPassword())
                .username(username)
                .tenantName(tenantName)
                .agreement(true)
                .build();

        assertError(() -> RegisterApi.registerRaw(command), VERIFICATION_CODE_CHECK_FAILED);
    }

    @Test
    public void should_fail_to_register_if_not_agree_agreement() {
        String tenantName = rTenantName();
        String email = rEmail();
        String username = rMemberName();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(rVerificationCode())
                .password(rPassword())
                .username(username)
                .tenantName(tenantName)
                .agreement(false)
                .build();

        assertError(() -> RegisterApi.registerRaw(command), MUST_SIGN_AGREEMENT);
    }

    @Test
    public void should_raise_tenant_created_event_after_register() {
        String tenantName = rTenantName();
        String mobile = rMobile();
        String username = rMemberName();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobile);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterResponse response = RegisterApi.register(RegisterCommand.builder()
                .mobileOrEmail(mobile)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .tenantName(tenantName)
                .agreement(true)
                .build());

        TenantCreatedEvent tenantCreatedEvent = latestEventFor(response.getTenantId(), TENANT_CREATED, TenantCreatedEvent.class);
        assertEquals(response.getTenantId(), tenantCreatedEvent.getTenantId());
    }

    @Test
    public void should_raise_event_after_register() {
        String tenantName = rTenantName();
        String mobile = rMobile();
        String username = rMemberName();
        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobile);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterResponse response = RegisterApi.register(RegisterCommand.builder()
                .mobileOrEmail(mobile)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .tenantName(tenantName)
                .agreement(true)
                .build());

        MemberCreatedEvent memberCreatedEvent = latestEventFor(response.getMemberId(), MEMBER_CREATED, MemberCreatedEvent.class);
        assertEquals(response.getMemberId(), memberCreatedEvent.getMemberId());
        TenantCreatedEvent tenantCreatedEvent = latestEventFor(response.getTenantId(), TENANT_CREATED, TenantCreatedEvent.class);
        assertEquals(response.getTenantId(), tenantCreatedEvent.getTenantId());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(1, tenant.getResourceUsage().getMemberCount());
    }

}