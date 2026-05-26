package com.mryqr.common.security.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

@Getter
public final class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private String jwt;

    public JwtAuthenticationToken(String jwt) {
        super(NO_AUTHORITIES);
        requireNonBlank(jwt, "Jwt must not be null.");
        this.jwt = jwt;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return jwt;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        jwt = null;
    }
}
