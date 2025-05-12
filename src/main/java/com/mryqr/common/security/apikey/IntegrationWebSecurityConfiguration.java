package com.mryqr.common.security.apikey;

import com.mryqr.common.security.MdcFilter;
import com.mryqr.core.common.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.ExceptionTranslationFilter;

@Configuration
@RequiredArgsConstructor
public class IntegrationWebSecurityConfiguration {
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final ApiKeyAuthenticationProvider apiKeyAuthenticationProvider;

    @Bean
    @Order(-1)
    public SecurityFilterChain integrationFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/integration/**")
                .authorizeHttpRequests(registry -> registry.anyRequest().hasRole(Role.ROBOT.name()))
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .authenticationProvider(this.apiKeyAuthenticationProvider)
                .addFilterBefore(new MdcFilter(), ExceptionTranslationFilter.class)
                .httpBasic().authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .headers().and()
                .cors().disable()
                .anonymous().disable()
                .csrf().disable()
                .servletApi().disable()
                .logout().disable()
                .sessionManagement().disable()
                .securityContext().disable()
                .requestCache().disable()
                .formLogin().disable();
        return http.build();
    }

}
