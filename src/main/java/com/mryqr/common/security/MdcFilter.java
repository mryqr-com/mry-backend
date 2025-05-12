package com.mryqr.common.security;

import com.mryqr.core.common.domain.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof MryAuthenticationToken token) {
            User user = token.getUser();
            MDC.put("memberId", user.getMemberId());
            MDC.put("tenantId", user.getTenantId());
        }
        filterChain.doFilter(request, response);
        MDC.clear();
    }

}
