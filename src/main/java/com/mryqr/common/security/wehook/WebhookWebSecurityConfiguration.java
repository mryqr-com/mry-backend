package com.mryqr.common.security.wehook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@RequiredArgsConstructor
public class WebhookWebSecurityConfiguration {
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final WebhookAuthenticationProvider webhookAuthenticationProvider;

    @Bean
    @Order(-2)
    public SecurityFilterChain webhookFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/webhook/**")
                .authorizeHttpRequests(registry -> registry.anyRequest().authenticated())
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .authenticationProvider(this.webhookAuthenticationProvider)
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
