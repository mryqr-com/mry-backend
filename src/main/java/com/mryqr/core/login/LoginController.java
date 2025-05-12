package com.mryqr.core.login;

import com.mryqr.common.security.IpJwtCookieUpdater;
import com.mryqr.common.security.jwt.JwtCookieFactory;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.login.command.JwtTokenResponse;
import com.mryqr.core.login.command.LoginCommandService;
import com.mryqr.core.login.command.MobileOrEmailLoginCommand;
import com.mryqr.core.login.command.VerificationCodeLoginCommand;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class LoginController {
    private final LoginCommandService loginCommandService;
    private final IpJwtCookieUpdater ipJwtCookieUpdater;
    private final JwtCookieFactory jwtCookieFactory;

    @PostMapping(value = "/login")
    public JwtTokenResponse loginWithMobileOrEmail(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   @RequestBody @Valid MobileOrEmailLoginCommand command) {
        String jwt = loginCommandService.loginWithMobileOrEmail(command);
        response.addCookie(ipJwtCookieUpdater.updateCookie(jwtCookieFactory.newJwtCookie(jwt), request));
        return JwtTokenResponse.builder().token(jwt).build();
    }

    @PostMapping(value = "/verification-code-login")
    public JwtTokenResponse loginWithVerificationCode(HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      @RequestBody @Valid VerificationCodeLoginCommand command) {
        String jwt = loginCommandService.loginWithVerificationCode(command);
        response.addCookie(ipJwtCookieUpdater.updateCookie(jwtCookieFactory.newJwtCookie(jwt), request));
        return JwtTokenResponse.builder().token(jwt).build();
    }

    @DeleteMapping(value = "/logout")
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       @AuthenticationPrincipal User user) {
        response.addCookie(ipJwtCookieUpdater.updateCookie(jwtCookieFactory.logoutCookie(), request));
        if (user.isLoggedIn()) {
            log.info("User[{}] tried log out.", user.getMemberId());
        }
    }

    @PutMapping(value = "/refresh-token")
    public JwtTokenResponse refreshToken(HttpServletRequest request,
                                         HttpServletResponse response,
                                         @AuthenticationPrincipal User user) {
        String jwt = loginCommandService.refreshToken(user);
        response.addCookie(ipJwtCookieUpdater.updateCookie(jwtCookieFactory.newJwtCookie(jwt), request));
        return JwtTokenResponse.builder().token(jwt).build();
    }

}
