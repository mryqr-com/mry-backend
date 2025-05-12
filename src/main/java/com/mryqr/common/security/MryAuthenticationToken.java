package com.mryqr.common.security;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Getter
public final class MryAuthenticationToken extends AbstractAuthenticationToken {
    private final User user;
    private final long expiration;

    public MryAuthenticationToken(User user, long expiration) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        requireNonNull(user, "User must not be null.");
        this.user = user;
        this.expiration = expiration;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }
}
