package com.mryqr.core.verification;

import com.mryqr.BaseApiTest;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.login.command.VerificationCodeLoginCommand;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.register.command.RegisterResponse;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.verification.command.*;
import com.mryqr.core.verification.domain.VerificationCode;
import com.mryqr.utils.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.stream.IntStream;

import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.common.exception.ErrorCode.VERIFICATION_CODE_CHECK_FAILED;
import static com.mryqr.common.utils.MryConstants.NO_TENANT_ID;
import static com.mryqr.core.verification.VerificationCodeApi.*;
import static com.mryqr.core.verification.domain.VerificationCodeType.LOGIN;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.*;

class VerificationControllerApiTest extends BaseApiTest {

    @Test
    public void should_create_verification_code_for_register() {
        String mobile = rMobile();

        CreateRegisterVerificationCodeCommand command = CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        String returnId = createVerificationCodeForRegister(command);
        VerificationCode verificationCode = verificationCodeRepository.byId(returnId);

        assertNotNull(verificationCode);
        assertEquals(mobile, verificationCode.getMobileOrEmail());
    }

    @Test
    public void should_fail_create_verification_code_for_register_if_mobile_is_occupied() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);

        CreateRegisterVerificationCodeCommand command = CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForRegister(command)));
    }

    @Test
    public void should_fail_create_verification_code_for_register_if_email_is_occupied() {
        String email = rEmail();
        String password = rPassword();
        setupApi.register(email, password);

        CreateRegisterVerificationCodeCommand command = CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(email).build();
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForRegister(command)));
    }

    @Test
    public void should_create_verification_code_for_login() {
        String mobile = rMobile();
        String password = rPassword();
        RegisterResponse response = setupApi.register(mobile, password);

        CreateLoginVerificationCodeCommand command = CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        String returnId = createVerificationCodeForLogin(command);
        VerificationCode verificationCode = verificationCodeRepository.byId(returnId);

        assertNotNull(verificationCode);
        assertEquals(mobile, verificationCode.getMobileOrEmail());

        int usedSmsCount = tenantRepository.byId(response.getTenantId()).getResourceUsage().getSmsSentCountForCurrentMonth();
        assertEquals(1, usedSmsCount);
    }

    @Test
    public void should_fail_create_verification_code_for_login_if_max_sms_sent_count_reached() {
        String mobile = rMobile();
        String password = rPassword();
        LoginResponse response = setupApi.registerWithLogin(mobile, password);
        CreateLoginVerificationCodeCommand command = CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        assertTrue(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForLogin(command)));

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        IntStream.range(0, tenant.currentPlan().getMaxSmsCountPerMonth() + 1)
                .forEach(value -> tenant.getResourceUsage().increaseSmsSentCountForCurrentMonth());
        tenantRepository.save(tenant);

        String newMemberMobile = rMobile();
        MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), newMemberMobile, rPassword());
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForLogin(
                CreateLoginVerificationCodeCommand.builder().mobileOrEmail(newMemberMobile).build())));
    }

    @Test
    public void should_use_extra_remain_sms_count() {
        String mobile = rMobile();
        String password = rPassword();
        LoginResponse response = setupApi.registerWithLogin(mobile, password);
        CreateLoginVerificationCodeCommand command = CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        assertTrue(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForLogin(command)));

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        IntStream.range(0, tenant.currentPlan().getMaxSmsCountPerMonth() + 1)
                .forEach(value -> tenant.getResourceUsage().increaseSmsSentCountForCurrentMonth());
        tenantRepository.save(tenant);
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForLogin(command)));
        int smsCount = tenant.getResourceUsage().getSmsSentCountForCurrentMonth();

        tenant.getPackages().increaseExtraRemainSmsCount(1000);
        tenantRepository.save(tenant);
        String newMemberMobile = rMobile();
        MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), newMemberMobile, rPassword());
        assertTrue(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForLogin(
                CreateLoginVerificationCodeCommand.builder().mobileOrEmail(newMemberMobile).build())));

        Tenant updatedTenant = tenantRepository.byId(response.getTenantId());
        assertEquals(smsCount + 1, updatedTenant.getResourceUsage().getSmsSentCountForCurrentMonth());
        assertEquals(999, updatedTenant.getPackages().getExtraRemainSmsCount());
    }

    @Test
    public void should_fail_create_verification_code_for_login_if_user_not_exists_for_mobile() {
        CreateLoginVerificationCodeCommand command = CreateLoginVerificationCodeCommand.builder().mobileOrEmail(rMobile()).build();
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForLogin(command)));
    }

    @Test
    public void should_fail_create_verification_code_for_login_if_user_not_exists_for_email() {
        CreateLoginVerificationCodeCommand command = CreateLoginVerificationCodeCommand.builder().mobileOrEmail(rEmail()).build();
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForLogin(command)));
    }

    @Test
    public void should_create_verification_code_for_findback_password() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);

        CreateFindbackPasswordVerificationCodeCommand command = CreateFindbackPasswordVerificationCodeCommand.builder().mobileOrEmail(mobile)
                .build();
        String codeId = createVerificationCodeForFindbackPassword(command);
        VerificationCode verificationCode = verificationCodeRepository.byId(codeId);

        assertNotNull(verificationCode);
        assertEquals(mobile, verificationCode.getMobileOrEmail());
    }

    @Test
    public void should_create_verification_code_for_findback_password_if_mobile_not_exists_for_user() {
        CreateFindbackPasswordVerificationCodeCommand command = CreateFindbackPasswordVerificationCodeCommand.builder().mobileOrEmail(rMobile())
                .build();
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForFindbackPassword(command)));
    }

    @Test
    public void should_create_verification_code_for_findback_password_if_email_not_exists_for_user() {
        CreateFindbackPasswordVerificationCodeCommand command = CreateFindbackPasswordVerificationCodeCommand.builder().mobileOrEmail(rEmail())
                .build();
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForFindbackPassword(command)));
    }

    @Test
    public void should_create_verification_code_for_change_mobile() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        String mobile = rMobile();
        CreateChangeMobileVerificationCodeCommand command = CreateChangeMobileVerificationCodeCommand.builder().mobile(mobile).build();
        String codeId = createVerificationCodeForChangeMobile(response.getJwt(), command);
        VerificationCode verificationCode = verificationCodeRepository.byId(codeId);

        assertNotNull(verificationCode);
        assertEquals(mobile, verificationCode.getMobileOrEmail());
    }

    @Test
    public void should_fail_create_verification_code_for_change_mobile_if_mobile_already_exists_for_user() {
        String mobile = rMobile();
        setupApi.registerWithLogin(mobile, rPassword());
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        CreateChangeMobileVerificationCodeCommand command = CreateChangeMobileVerificationCodeCommand.builder().mobile(mobile).build();
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForChangeMobile(response.getJwt(), command)));
    }

    @Test
    public void should_create_verification_code_for_identify_mobile() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        String mobile = rMobile();
        IdentifyMobileVerificationCodeCommand command = IdentifyMobileVerificationCodeCommand.builder().mobile(mobile).build();
        String codeId = createVerificationCodeForIdentifyMobile(response.getJwt(), command);
        VerificationCode verificationCode = verificationCodeRepository.byId(codeId);

        assertNotNull(verificationCode);
        assertEquals(mobile, verificationCode.getMobileOrEmail());
    }

    @Test
    public void should_fail_create_verification_code_resend_within_1_minute() {
        String mobile = rMobile();
        CreateRegisterVerificationCodeCommand command = CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        assertTrue(verificationCodeRepository.exists(createVerificationCodeForRegister(command)));
        assertFalse(verificationCodeRepository.exists(createVerificationCodeForRegister(command)));
    }

    @Test
    public void should_fail_create_verification_code_if_too_many_for_today() {
        String mobile = rMobile();
        IntStream.range(1, 22).forEach(value -> {
            VerificationCode verificationCode = new VerificationCode(mobile, LOGIN, NO_TENANT_ID, NO_USER);
            verificationCodeRepository.save(verificationCode);
        });

        CreateRegisterVerificationCodeCommand command = CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        assertFalse(verificationCodeRepository.exists(createVerificationCodeForRegister(command)));
    }

    @Test
    public void should_fail_check_verification_code_if_already_used_3_times() {
        String mobile = rMobile();
        setupApi.register(mobile, rPassword());
        VerificationCode code = makeOverUsedVerificationCode(mobile);

        VerificationCodeLoginCommand command = VerificationCodeLoginCommand.builder().mobileOrEmail(mobile).verification(code.getCode())
                .build();
        assertError(() -> LoginApi.loginWithVerificationCodeRaw(command), VERIFICATION_CODE_CHECK_FAILED);
    }

    private VerificationCode makeOverUsedVerificationCode(String mobile) {
        String codeId = createVerificationCodeForLogin(CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        code.use();
        code.use();
        code.use();
        verificationCodeRepository.save(code);
        return code;
    }

    @Test
    public void should_fail_check_verification_code_if_older_than_10_minutes() {
        String mobile = rMobile();
        setupApi.register(mobile, rPassword());

        VerificationCode code = makeExpiredVerificationCode(mobile);

        VerificationCodeLoginCommand command = VerificationCodeLoginCommand.builder().mobileOrEmail(mobile).verification(code.getCode())
                .build();
        assertError(() -> LoginApi.loginWithVerificationCodeRaw(command), VERIFICATION_CODE_CHECK_FAILED);
    }

    private VerificationCode makeExpiredVerificationCode(String mobile) {
        String codeId = createVerificationCodeForLogin(CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        ReflectionTestUtils.setField(code, "createdAt", Instant.now().minus(11, MINUTES));
        verificationCodeRepository.save(code);
        return code;
    }

    @Test
    public void should_fail_check_verification_code_if_wrong_type_provided() {
        String mobile = rMobile();
        CreateRegisterVerificationCodeCommand registerVerificationCodeCommand = CreateRegisterVerificationCodeCommand.builder()
                .mobileOrEmail(mobile).build();
        String codeId = createVerificationCodeForRegister(registerVerificationCodeCommand);
        VerificationCode code = verificationCodeRepository.byId(codeId);

        VerificationCodeLoginCommand command = VerificationCodeLoginCommand.builder().mobileOrEmail(mobile).verification(code.getCode())
                .build();
        assertError(() -> LoginApi.loginWithVerificationCodeRaw(command), VERIFICATION_CODE_CHECK_FAILED);
    }
}