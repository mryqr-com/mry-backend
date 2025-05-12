package com.mryqr.common.security.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.io.IOException;
import java.util.UUID;

import static com.mryqr.core.common.domain.user.User.ANONYMOUS_USER;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

public class JwtAnonymousAuthenticationFilter extends AnonymousAuthenticationFilter {
    private static final String KEY = UUID.randomUUID().toString();

    public JwtAnonymousAuthenticationFilter() {
        super(KEY);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            AnonymousAuthenticationToken token = new AnonymousAuthenticationToken(KEY, ANONYMOUS_USER, createAuthorityList("ROLE_ANONYMOUS"));
            context.setAuthentication(token);
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(req, res);
    }
}
