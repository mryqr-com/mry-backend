package com.mryqr.core.login.domain;

import com.mryqr.common.password.MryPasswordEncoder;
import com.mryqr.common.security.jwt.JwtService;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberDomainService;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.verification.domain.VerificationCodeChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.common.exception.MryException.authenticationException;
import static com.mryqr.common.utils.CommonUtils.maskMobileOrEmail;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.core.verification.domain.VerificationCodeType.LOGIN;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginDomainService {
    private final MemberRepository memberRepository;
    private final MryPasswordEncoder mryPasswordEncoder;
    private final JwtService jwtService;
    private final VerificationCodeChecker verificationCodeChecker;
    private final MemberDomainService memberDomainService;

    public String loginWithMobileOrEmail(String mobileOrEmail,
                                         String password,
                                         WxIdInfo wxIdInfo) {
        Member member = memberRepository.byMobileOrEmailOptional(mobileOrEmail)
                .orElseThrow(() -> authenticationException("手机号或邮箱登录失败", mapOf("mobileOrEmail", maskMobileOrEmail(mobileOrEmail))));

        if (!mryPasswordEncoder.matches(password, member.getPassword())) {
            memberDomainService.recordMemberFailedLogin(member);
            throw authenticationException("手机号或邮箱登录失败", mapOf("mobileOrEmail", maskMobileOrEmail(mobileOrEmail)));
        }

        member.checkActive();
        return generateJwtAndTryBindWx(member, wxIdInfo);
    }

    public String loginWithVerificationCode(String mobileOrEmail,
                                            String verificationCode,
                                            WxIdInfo wxIdInfo) {
        verificationCodeChecker.check(mobileOrEmail, verificationCode, LOGIN);
        Member member = memberRepository.byMobileOrEmailOptional(mobileOrEmail)
                .orElseThrow(() -> authenticationException("验证码登录失败", mapOf("mobileOrEmail", maskMobileOrEmail(mobileOrEmail))));

        member.checkActive();
        return generateJwtAndTryBindWx(member, wxIdInfo);
    }

    private String generateJwtAndTryBindWx(Member member, WxIdInfo wxIdInfo) {
        return jwtService.generateJwt(member.getId());
    }

}
