package com.mryqr.core.login.command;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.security.jwt.JwtService;
import com.mryqr.core.login.domain.LoginDomainService;
import com.mryqr.core.login.domain.WxJwtService;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.mryqr.common.exception.MryException.authenticationException;
import static com.mryqr.common.utils.CommonUtils.maskMobileOrEmail;
import static com.mryqr.common.utils.MapUtils.mapOf;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCommandService {
    private final LoginDomainService loginDomainService;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final WxJwtService wxJwtService;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public String loginWithMobileOrEmail(MobileOrEmailLoginCommand command) {
        String mobileOrEmail = command.getMobileOrEmail();
        mryRateLimiter.applyFor("Login:MobileOrEmail:" + mobileOrEmail, 1);

        try {
            String token = loginDomainService.loginWithMobileOrEmail(
                    mobileOrEmail,
                    command.getPassword(),
                    wxJwtService.wxIdInfoFromJwt(command.getWxIdInfo()));
            log.info("User[{}] logged in using password.", maskMobileOrEmail(command.getMobileOrEmail()));
            return token;
        } catch (Throwable t) {
            if (t instanceof MryException mryException &&
                (mryException.getCode().getStatus() == 401 || mryException.getCode().getStatus() == 409)) {
                throw mryException;
            }

            throw authenticationException("手机号或密码登录失败", mapOf("mobileOrEmail", maskMobileOrEmail(mobileOrEmail)));
        }
    }

    @Transactional
    public String loginWithVerificationCode(VerificationCodeLoginCommand command) {
        String mobileOrEmail = command.getMobileOrEmail();

        mryRateLimiter.applyFor("Login:MobileOrEmail:" + mobileOrEmail, 1);

        try {
            String token = loginDomainService.loginWithVerificationCode(
                    mobileOrEmail,
                    command.getVerification(),
                    wxJwtService.wxIdInfoFromJwt(command.getWxIdInfo()));
            log.info("User[{}] logged in using verification code.", maskMobileOrEmail(command.getMobileOrEmail()));
            return token;
        } catch (Throwable t) {
            if (t instanceof MryException mryException &&
                (mryException.getCode().getStatus() == 401 || mryException.getCode().getStatus() == 409)) {
                throw mryException;
            }

            throw authenticationException("验证码登录失败", mapOf("mobileOrEmail", maskMobileOrEmail(mobileOrEmail)));
        }
    }

    @Transactional
    public String refreshToken(User user) {
        mryRateLimiter.applyFor("Login:RefreshToken:All", 1000);

        Member member = memberRepository.cachedById(user.getMemberId());
        log.info("User[{}] refreshed token.", user.getMemberId());
        return jwtService.generateJwt(member.getId());
    }

    public String wxLoginMember(String memberId) {
        mryRateLimiter.applyFor("Login:WxLogin:All", 1000);
        return jwtService.generateJwt(memberId);
    }
}
