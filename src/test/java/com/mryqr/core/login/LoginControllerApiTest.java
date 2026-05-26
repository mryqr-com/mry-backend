package com.mryqr.core.login;

import com.mryqr.BaseApiTest;
import com.mryqr.core.login.command.MobileOrEmailLoginCommand;
import com.mryqr.core.login.command.VerificationCodeLoginCommand;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.register.command.RegisterResponse;
import com.mryqr.core.verification.VerificationCodeApi;
import com.mryqr.core.verification.command.CreateLoginVerificationCodeCommand;
import com.mryqr.core.verification.domain.VerificationCode;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoginControllerApiTest extends BaseApiTest {

    @Test
    public void should_login_with_mobile() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);
        String jwt = LoginApi.loginWithMobileOrEmail(mobile, password);
        MemberApi.myMemberInfo(jwt);
    }

    @Test
    public void should_login_with_email() {
        String email = rEmail();
        String password = rPassword();
        setupApi.register(email, password);
        String jwt = LoginApi.loginWithMobileOrEmail(email, password);
        MemberApi.myMemberInfo(jwt);

    }

    @Test
    public void should_login_with_verification_code() {
        String mobile = rMobile();
        setupApi.register(mobile, rPassword());

        String codeId = VerificationCodeApi.createVerificationCodeForLogin(CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);

        String jwt = LoginApi.loginWithVerificationCode(VerificationCodeLoginCommand.builder().mobileOrEmail(mobile).verification(code.getCode()).build());
        MemberApi.myMemberInfo(jwt);

    }

    @Test
    public void should_fail_login_with_non_existing_mobile() {
        MobileOrEmailLoginCommand command = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(rMobile()).password(rPassword()).build();

        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(command), AUTHENTICATION_FAILED);
    }

    @Test
    public void should_fail_login_with_non_existing_email() {
        MobileOrEmailLoginCommand command = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(rEmail()).password(rPassword()).build();
        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(command), AUTHENTICATION_FAILED);
    }

    @Test
    public void should_fail_login_with_wrong_password() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);

        MobileOrEmailLoginCommand command = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(mobile).password(rPassword()).build();
        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(command), AUTHENTICATION_FAILED);
    }

    @Test
    public void should_fail_verification_login_with_wrong_verification_code() {
        String mobile = rMobile();
        setupApi.register(mobile, rPassword());

        VerificationCodeLoginCommand command = VerificationCodeLoginCommand.builder().mobileOrEmail(mobile).verification(rVerificationCode()).build();
        assertError(() -> LoginApi.loginWithVerificationCodeRaw(command), VERIFICATION_CODE_CHECK_FAILED);
    }

    @Test
    public void should_logout() {
        LoginApi.logout();
    }

    @Test
    public void should_refresh_token() throws InterruptedException {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);

        String jwt = LoginApi.loginWithMobileOrEmail(mobile, password);
        Thread.sleep(1000);
        String refreshedJwt = LoginApi.refreshToken(jwt);
        MemberApi.myMemberInfo(refreshedJwt);//validate refreshed token
    }

    @Test
    public void should_failed_login_if_locked() {
        String email = rEmail();
        String password = rPassword();
        MobileOrEmailLoginCommand loginCommand = MobileOrEmailLoginCommand.builder().mobileOrEmail(email).password(password).build();
        RegisterResponse response = setupApi.register(email, password);
        Member member = memberRepository.byId(response.getMemberId());
        assertNotNull(LoginApi.loginWithMobileOrEmail(loginCommand));

        ReflectionTestUtils.setField(member.getFailedLoginCount(), "count", 51);
        memberRepository.save(member);

        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(loginCommand), MEMBER_ALREADY_LOCKED);
    }

    @Test
    public void should_fail_authentication_if_locked() {
        String email = rEmail();
        String password = rPassword();
        MobileOrEmailLoginCommand loginCommand = MobileOrEmailLoginCommand.builder().mobileOrEmail(email).password(password).build();
        LoginResponse response = setupApi.registerWithLogin(email, password);
        Member member = memberRepository.byId(response.memberId());
        assertNotNull(LoginApi.loginWithMobileOrEmail(loginCommand));

        ReflectionTestUtils.setField(member.getFailedLoginCount(), "count", 51);
        memberRepository.save(member);
        assertError(() -> MemberApi.myProfileRaw(response.jwt()), MEMBER_ALREADY_LOCKED);
    }

    @Test
    public void should_failed_login_if_deactivated() {
        LoginResponse response = setupApi.registerWithLogin();

        String password = rPassword();
        String mobile = rMobile();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.jwt(), rMemberName(), mobile, password);
        MemberApi.deactivateMember(response.jwt(), memberResponse.getMemberId());

        MobileOrEmailLoginCommand loginCommand = MobileOrEmailLoginCommand.builder().mobileOrEmail(mobile).password(password).build();
        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(loginCommand), MEMBER_ALREADY_DEACTIVATED);
    }

    @Test
    public void should_fail_authentication_if_deactivated() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.jwt());

        MemberApi.deactivateMember(response.jwt(), memberResponse.getMemberId());
        assertError(() -> MemberApi.myProfileRaw(memberResponse.getJwt()), MEMBER_ALREADY_DEACTIVATED);
    }

    @Test
    public void should_fail_authentication_if_tenant_deactivated() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.jwt());
        Member member = memberRepository.byId(memberResponse.getMemberId());
        ReflectionTestUtils.setField(member, "tenantActive", false);
        memberRepository.save(member);
        assertError(() -> MemberApi.myProfileRaw(memberResponse.getJwt()), TENANT_ALREADY_DEACTIVATED);
    }

    @Test
    public void should_count_failed_password_login() {
        String email = rEmail();
        String password = rPassword();
        RegisterResponse response = setupApi.register(email, password);
        MobileOrEmailLoginCommand loginCommand = MobileOrEmailLoginCommand.builder().mobileOrEmail(email).password(rPassword()).build();
        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(loginCommand), AUTHENTICATION_FAILED);

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(1, member.getFailedLoginCount().getCount());
    }
}