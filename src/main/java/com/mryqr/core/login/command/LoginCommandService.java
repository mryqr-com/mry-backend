package com.mryqr.core.login.command;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.security.jwt.JwtService;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.login.domain.LoginDomainService;
import com.mryqr.core.login.domain.WxJwtService;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.mryqr.core.common.exception.MryException.authenticationException;
import static com.mryqr.core.common.utils.CommonUtils.maskMobileOrEmail;

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
            //401或409时直接抛出异常
            if (t instanceof MryException mryException &&
                    (mryException.getCode().getStatus() == 401 || mryException.getCode().getStatus() == 409)) {
                log.warn("Password login failed for [{}].", maskMobileOrEmail(mobileOrEmail));
                throw mryException;
            }

            //其他情况直接一个笼统的异常
            log.warn("Password login failed for [{}].", maskMobileOrEmail(mobileOrEmail), t);
            throw authenticationException();
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
            //401或409时直接抛出异常
            if (t instanceof MryException mryException &&
                    (mryException.getCode().getStatus() == 401 || mryException.getCode().getStatus() == 409)) {
                log.warn("Verification code login failed for [{}].", maskMobileOrEmail(mobileOrEmail));
                throw mryException;
            }

            //其他情况直接一个笼统的异常
            log.warn("Verification code login failed for [{}].", maskMobileOrEmail(mobileOrEmail), t);
            throw authenticationException();
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
