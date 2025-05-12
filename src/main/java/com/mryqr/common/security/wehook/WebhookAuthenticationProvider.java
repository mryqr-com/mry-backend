package com.mryqr.common.security.wehook;

import com.mryqr.common.properties.CommonProperties;
import com.mryqr.common.security.MryAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.mryqr.common.domain.user.User.robotUser;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static java.lang.Long.MAX_VALUE;

@Component
@RequiredArgsConstructor
public class WebhookAuthenticationProvider implements AuthenticationProvider {
    private final CommonProperties commonProperties;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        try {
            if (!(Objects.equals(commonProperties.getWebhookUserName(), token.getName()) &&
                  Objects.equals(commonProperties.getWebhookPassword(), token.getCredentials()))) {
                throw new BadCredentialsException("Bad credentials.");
            }

            return new MryAuthenticationToken(robotUser(MRY_MANAGE_TENANT_ID), MAX_VALUE);
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
