package com.mryqr.core.login;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.security.jwt.JwtCookieFactory;
import com.mryqr.common.wx.auth.mobile.MobileWxAuthAccessTokenInfo;
import com.mryqr.common.wx.auth.mobile.MobileWxAuthService;
import com.mryqr.common.wx.auth.mobile.MobileWxAuthUserInfo;
import com.mryqr.core.common.properties.PropertyService;
import com.mryqr.core.login.command.LoginCommandService;
import com.mryqr.core.login.domain.WxJwtService;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Validated
@Controller
@RequiredArgsConstructor
public class MobileWxOAuth2LoginController {
    private final MobileWxAuthService mobileWxAuthService;
    private final MemberRepository memberRepository;
    private final LoginCommandService loginCommandService;
    private final WxJwtService wxJwtService;
    private final JwtCookieFactory jwtCookieFactory;
    private final MryRateLimiter mryRateLimiter;
    private final PropertyService propertyService;

    @GetMapping("/mobile-wx/auth2-callback")
    public String callback(@RequestParam("code") @Size(max = 100) String code,
                           @CookieValue(value = "fromUrl", defaultValue = "") String fromUrl,
                           HttpServletResponse response) {
        mryRateLimiter.applyFor("Wx:MobileCallback", 500);

        MobileWxAuthAccessTokenInfo tokenInfo = mobileWxAuthService.fetchAccessToken(code);
        String mobileWxOpenId = tokenInfo.getOpenId();
        String wxUnionId = tokenInfo.getUnionId();
        Optional<Member> optionalMember = memberRepository.byWxUnionIdOptional(wxUnionId);

        //未绑定时，返回登录界面，并同时带上openId和unionId(包含在jwt中)以便后续成功登陆后自动绑定
        if (optionalMember.isEmpty()) {
            log.info("Mobile wx openId[{}] with unionId[{}] not bound to a member, redirect to login page.", mobileWxOpenId, wxUnionId);
            return "redirect:" + propertyService.clientLoginUrl() + "?wx="
                    + wxJwtService.generateMobileWxIdInfoJwt(wxUnionId, mobileWxOpenId) + "&from=" + fromUrl;
        }

        Member member = optionalMember.get();

        //每次微信登录时均尝试获取最新的昵称和头像并保存
        MobileWxAuthUserInfo userInfo = mobileWxAuthService.fetchUserInfo(tokenInfo.getAccessToken(), mobileWxOpenId);
        if (member.updateMobileWxInfo(mobileWxOpenId, userInfo.getNickname(), userInfo.getHeaderImageUrl(), member.toUser())) {
            log.info("Updated user mobile wx info:[{}]-[{}]", mobileWxOpenId, userInfo.getNickname());
            memberRepository.save(member);
        }

        //登录成功，植入cookie
        String jwt = loginCommandService.wxLoginMember(member.getId());
        response.addCookie(jwtCookieFactory.newJwtCookie(jwt));

        //如果没有先前页面，则重定向到默认主页
        String redirectUrl = isNotBlank(fromUrl) ? fromUrl : propertyService.clientDefaultHomeUrl();

        log.info("Member[{}] logged in via mobile wx[unionId={}].", member.getId(), wxUnionId);
        return "redirect:" + redirectUrl;
    }
}
