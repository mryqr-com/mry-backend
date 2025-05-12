package com.mryqr.common.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;

import static com.google.common.net.InetAddresses.isInetAddress;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class IpJwtCookieUpdater {
    public Cookie updateCookie(Cookie cookie, HttpServletRequest request) {
        String referer = request.getHeader("referer");
        if (isBlank(referer)) {
            return cookie;
        }

        try {
            URL url = new URL(referer);
            String host = url.getHost();
            if (isInetAddress(host) || "localhost".equals(host)) {
                cookie.setDomain(host);
                return cookie;
            }
        } catch (Exception e) {
            log.error("Cannot update cookie to referer[{}].", referer);
        }
        return cookie;
    }
}
