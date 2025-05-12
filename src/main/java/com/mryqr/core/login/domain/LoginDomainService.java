package com.mryqr.core.login.domain;

import com.mryqr.common.password.MryPasswordEncoder;
import com.mryqr.common.security.jwt.JwtService;
import com.mryqr.common.wx.auth.mobile.MobileWxAuthService;
import com.mryqr.common.wx.auth.mobile.MobileWxAuthUserInfo;
import com.mryqr.common.wx.auth.pc.PcWxAuthService;
import com.mryqr.common.wx.auth.pc.PcWxAuthUserInfo;
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
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginDomainService {
    private final MemberRepository memberRepository;
    private final MryPasswordEncoder mryPasswordEncoder;
    private final JwtService jwtService;
    private final VerificationCodeChecker verificationCodeChecker;
    private final MemberDomainService memberDomainService;
    private final MobileWxAuthService mobileWxAuthService;
    private final PcWxAuthService pcWxAuthService;

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
        try {
            tryBindWx(member, wxIdInfo);
        } catch (Throwable t) {
            log.warn("Failed bind wx[unionId:{},mobileWxOpenId:{},pcWxOpenId:{}] to member[{}].",
                    wxIdInfo.getWxUnionId(), wxIdInfo.getMobileWxOpenId(), wxIdInfo.getPcWxOpenId(), member.getId(), t);
        }

        return jwtService.generateJwt(member.getId());
    }

    private void tryBindWx(Member member, WxIdInfo wxIdInfo) {
        if (wxIdInfo == null) {
            return;
        }

        String wxUnionId = wxIdInfo.getWxUnionId();
        String mobileWxOpenId = wxIdInfo.getMobileWxOpenId();
        String pcWxOpenId = wxIdInfo.getPcWxOpenId();

        if (isNotBlank(mobileWxOpenId)) {
            //只要能够同时完成登录，并提供微信unionId的JWT信息，即可绑定，可能导致将先前的绑定踢掉
            member.bindMobileWx(wxUnionId, mobileWxOpenId, member.toUser());

            //尝试获取微信昵称和头像
            mobileWxAuthService.getAccessToken(wxUnionId).ifPresent(token -> {
                try {
                    MobileWxAuthUserInfo userInfo = mobileWxAuthService.fetchUserInfo(token, mobileWxOpenId);
                    member.updateMobileWxInfo(mobileWxOpenId, userInfo.getNickname(), userInfo.getHeaderImageUrl(), member.toUser());
                } catch (Throwable t) {
                    log.warn("Failed to update mobile wx info for member[{}], will continue bind without these info.", member.getId(), t);
                }
            });

            log.info("Bind mobile wx[unionId={},mobileWxOpenId={}] to member[{}].", wxUnionId, mobileWxOpenId, member.getId());
            memberRepository.save(member);
        } else if (isNotBlank(pcWxOpenId)) {
            //只要能够同时完成登录，并提供微信unionId的JWT信息，即可绑定，可能导致将先前的绑定踢掉
            member.bindPcWx(wxUnionId, pcWxOpenId, member.toUser());

            //尝试获取微信昵称和头像
            pcWxAuthService.getAccessToken(wxUnionId).ifPresent(token -> {
                try {
                    PcWxAuthUserInfo userInfo = pcWxAuthService.fetchUserInfo(token, pcWxOpenId);
                    member.updatePcWxInfo(pcWxOpenId, userInfo.getNickname(), userInfo.getHeaderImageUrl(), member.toUser());
                } catch (Throwable t) {
                    log.warn("Failed to update pc wx info for member[{}], will continue bind without these info.", member.getId(), t);
                }
            });

            log.info("Bind pc wx[unionId={},pcWxOpenId={}] to member[{}].", wxUnionId, pcWxOpenId, member.getId());
            memberRepository.save(member);
        }
    }
}
