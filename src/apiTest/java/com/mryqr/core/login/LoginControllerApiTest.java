package com.mryqr.core.login;

import com.mryqr.BaseApiTest;
import com.mryqr.core.login.command.MobileOrEmailLoginCommand;
import com.mryqr.core.login.command.VerificationCodeLoginCommand;
import com.mryqr.core.login.domain.WxJwtService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import static com.mryqr.core.common.exception.ErrorCode.AUTHENTICATION_FAILED;
import static com.mryqr.core.common.exception.ErrorCode.MEMBER_ALREADY_DEACTIVATED;
import static com.mryqr.core.common.exception.ErrorCode.MEMBER_ALREADY_LOCKED;
import static com.mryqr.core.common.exception.ErrorCode.TENANT_ALREADY_DEACTIVATED;
import static com.mryqr.core.common.exception.ErrorCode.VERIFICATION_CODE_CHECK_FAILED;
import static com.mryqr.utils.RandomTestFixture.rEmail;
import static com.mryqr.utils.RandomTestFixture.rMemberName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rMobileWxOpenId;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static com.mryqr.utils.RandomTestFixture.rPcWxOpenId;
import static com.mryqr.utils.RandomTestFixture.rVerificationCode;
import static com.mryqr.utils.RandomTestFixture.rWxUnionId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoginControllerApiTest extends BaseApiTest {

    @Autowired
    private WxJwtService wxJwtService;

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
    public void should_login_and_bind_mobile_wx() {
        String email = rEmail();
        String password = rPassword();

        RegisterResponse response = setupApi.register(email, password);

        String mobileWxOpenId = rMobileWxOpenId();
        String wxUnionId = rWxUnionId();

        MobileOrEmailLoginCommand loginCommand = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(email)
                .password(password)
                .wxIdInfo(wxJwtService.generateMobileWxIdInfoJwt(wxUnionId, mobileWxOpenId))
                .build();

        LoginApi.loginWithMobileOrEmail(loginCommand);

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(mobileWxOpenId, member.getMobileWxOpenId());
        assertEquals(wxUnionId, member.getWxUnionId());
    }

    @Test
    public void should_login_and_bind_pc_wx() {
        String mobile = rMobile();
        String password = rPassword();

        RegisterResponse response = setupApi.register(mobile, password);

        String pcWxOpenId = rPcWxOpenId();
        String wxUnionId = rWxUnionId();
        MobileOrEmailLoginCommand loginCommand = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(mobile)
                .password(password)
                .wxIdInfo(wxJwtService.generatePcWxIdInfoJwt(wxUnionId, pcWxOpenId))
                .build();

        LoginApi.loginWithMobileOrEmail(loginCommand);

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(pcWxOpenId, member.getPcWxOpenId());
        assertEquals(wxUnionId, member.getWxUnionId());
    }

    @Test
    public void should_login_with_verification_code_and_bind_mobile_wx() {
        String mobile = rMobile();
        RegisterResponse response = setupApi.register(mobile, rPassword());

        String mobileWxOpenId = rMobileWxOpenId();
        String wxUnionId = rWxUnionId();

        String codeId = VerificationCodeApi.createVerificationCodeForLogin(CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        LoginApi.loginWithVerificationCode(VerificationCodeLoginCommand.builder()
                .mobileOrEmail(mobile)
                .verification(code.getCode())
                .wxIdInfo(wxJwtService.generateMobileWxIdInfoJwt(wxUnionId, mobileWxOpenId))
                .build());

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(mobileWxOpenId, member.getMobileWxOpenId());
        assertEquals(wxUnionId, member.getWxUnionId());
    }

    @Test
    public void should_login_with_verification_code_and_bind_pc_wx() {
        String mobile = rMobile();
        RegisterResponse response = setupApi.register(mobile, rPassword());

        String pcWxOpenId = rPcWxOpenId();
        String wxUnionId = rWxUnionId();

        String codeId = VerificationCodeApi.createVerificationCodeForLogin(CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        LoginApi.loginWithVerificationCode(VerificationCodeLoginCommand.builder()
                .mobileOrEmail(mobile)
                .verification(code.getCode())
                .wxIdInfo(wxJwtService.generatePcWxIdInfoJwt(wxUnionId, pcWxOpenId))
                .build());

        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(pcWxOpenId, member.getPcWxOpenId());
        assertEquals(wxUnionId, member.getWxUnionId());
    }

    @Test
    public void should_override_previous_wx_bind_info() {
        String email = rEmail();
        String password = rPassword();

        LoginResponse response = setupApi.registerWithLogin(email, password);
        Member member = memberRepository.byId(response.getMemberId());
        member.bindMobileWx(rWxUnionId(), rMobileWxOpenId(), member.toUser());
        memberRepository.save(member);

        String mobileWxOpenId = rMobileWxOpenId();
        String wxUnionId = rWxUnionId();
        MobileOrEmailLoginCommand loginCommand = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(email)
                .password(password)
                .wxIdInfo(wxJwtService.generateMobileWxIdInfoJwt(wxUnionId, mobileWxOpenId))
                .build();

        LoginApi.loginWithMobileOrEmail(loginCommand);
        Member updatedMember = memberRepository.byId(response.getMemberId());
        assertEquals(mobileWxOpenId, updatedMember.getMobileWxOpenId());
        assertEquals(wxUnionId, updatedMember.getWxUnionId());
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
    public void should_login_but_not_bind_wx_with_invalid_format_openid() {
        String mobile = rMobile();
        String password = rPassword();

        RegisterResponse response = setupApi.register(mobile, password);

        MobileOrEmailLoginCommand loginCommand = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(mobile)
                .password(password)
                .wxIdInfo("some_invalid_jwt_openid")
                .build();

        LoginApi.loginWithMobileOrEmail(loginCommand);
        Member member = memberRepository.byId(response.getMemberId());
        assertNull(member.getMobileWxOpenId());
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
        Member member = memberRepository.byId(response.getMemberId());
        assertNotNull(LoginApi.loginWithMobileOrEmail(loginCommand));

        ReflectionTestUtils.setField(member.getFailedLoginCount(), "count", 51);
        memberRepository.save(member);
        assertError(() -> MemberApi.myProfileRaw(response.getJwt()), MEMBER_ALREADY_LOCKED);
    }

    @Test
    public void should_failed_login_if_deactivated() {
        LoginResponse response = setupApi.registerWithLogin();

        String password = rPassword();
        String mobile = rMobile();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), mobile, password);
        MemberApi.deactivateMember(response.getJwt(), memberResponse.getMemberId());

        MobileOrEmailLoginCommand loginCommand = MobileOrEmailLoginCommand.builder().mobileOrEmail(mobile).password(password).build();
        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(loginCommand), MEMBER_ALREADY_DEACTIVATED);
    }

    @Test
    public void should_fail_authentication_if_deactivated() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        MemberApi.deactivateMember(response.getJwt(), memberResponse.getMemberId());
        assertError(() -> MemberApi.myProfileRaw(memberResponse.getJwt()), MEMBER_ALREADY_DEACTIVATED);
    }

    @Test
    public void should_fail_authentication_if_tenant_deactivated() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
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