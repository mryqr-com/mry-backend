package com.mryqr.common.security.jwt;

import com.mryqr.common.security.IpJwtCookieUpdater;
import com.mryqr.common.security.MryAuthenticationToken;
import com.mryqr.core.common.domain.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

public class AutoRefreshJwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final JwtCookieFactory jwtCookieFactory;
    private final IpJwtCookieUpdater ipJwtCookieUpdater;
    private final int aheadAutoRefreshMilli;

    public AutoRefreshJwtFilter(JwtService jwtService,
                                JwtCookieFactory jwtCookieFactory,
                                IpJwtCookieUpdater ipJwtCookieUpdater,
                                int aheadAutoRefresh) {
        this.jwtService = jwtService;
        this.jwtCookieFactory = jwtCookieFactory;
        this.ipJwtCookieUpdater = ipJwtCookieUpdater;
        this.aheadAutoRefreshMilli = aheadAutoRefresh * 60 * 1000;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof MryAuthenticationToken token && authentication.isAuthenticated()) {
            User user = token.getUser();
            if (user.isHumanUser()) {
                long timeLeft = token.getExpiration() - Instant.now().toEpochMilli();
                if (timeLeft > 0 && timeLeft < aheadAutoRefreshMilli) {
                    Cookie cookie = jwtCookieFactory.newJwtCookie(jwtService.generateJwt(user.getMemberId()));
                    response.addCookie(ipJwtCookieUpdater.updateCookie(cookie, request));
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
