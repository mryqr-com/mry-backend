package com.mryqr.common.security.jwt;

import com.mryqr.core.common.properties.CommonProperties;
import com.mryqr.core.common.properties.JwtProperties;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.utils.MryConstants.AUTH_COOKIE_NAME;
import static java.util.Arrays.asList;

@Component
@RequiredArgsConstructor
public class JwtCookieFactory {
    private final Environment environment;
    private final JwtProperties jwtProperties;
    private final CommonProperties commonProperties;

    public Cookie newJwtCookie(String jwt) {
        String[] activeProfiles = environment.getActiveProfiles();


        if (asList(activeProfiles).contains("prod")) {
            return newProdCookie(jwt);
        }

        return newNonProdCookie(jwt);
    }

    private Cookie newNonProdCookie(String jwt) {
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, jwt);
        cookie.setMaxAge(jwtProperties.getExpire() * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "strict");
        cookie.setDomain(commonProperties.getBaseDomainName());
        return cookie;
    }

    private Cookie newProdCookie(String jwt) {
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, jwt);
        cookie.setMaxAge(jwtProperties.getExpire() * 60);
        cookie.setDomain(commonProperties.getBaseDomainName());
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "strict");
        return cookie;
    }

    public Cookie logoutCookie() {
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setDomain(commonProperties.getBaseDomainName());
        cookie.setPath("/");
        return cookie;
    }
}
