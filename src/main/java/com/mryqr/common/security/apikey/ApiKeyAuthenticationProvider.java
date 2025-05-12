package com.mryqr.common.security.apikey;

import com.mryqr.common.security.MryAuthenticationToken;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static com.mryqr.common.domain.user.User.robotUser;
import static java.lang.Long.MAX_VALUE;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {
    private final TenantRepository tenantRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        try {
            Tenant tenant = tenantRepository.cachedByApiKey(token.getName());
            if (!tenant.getApiSetting().getApiSecret().equals(token.getCredentials())) {
                throw new BadCredentialsException("Bad credentials for API key.");
            }

            if (!tenant.isDeveloperAllowed()) {
                throw new AuthenticationServiceException("API integration is not enabled.");
            }

            if (!tenant.isActive()) {
                throw new AuthenticationServiceException("Tenant is deactivated.");
            }

            return new MryAuthenticationToken(robotUser(tenant.getId()), MAX_VALUE);
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
