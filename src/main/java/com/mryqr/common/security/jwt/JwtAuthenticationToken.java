package com.mryqr.common.security.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;

@Getter
public final class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private String jwt;

    public JwtAuthenticationToken(String jwt) {
        super(null);
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
