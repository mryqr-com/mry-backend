package com.mryqr.common.security.apikey;

import com.mryqr.common.domain.user.Role;
import com.mryqr.common.security.MdcFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
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
                .exceptionHandling(it -> it.accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(authenticationEntryPoint))
                .authenticationProvider(this.apiKeyAuthenticationProvider)
                .addFilterBefore(new MdcFilter(), ExceptionTranslationFilter.class)
                .httpBasic(configurer -> configurer.authenticationEntryPoint(authenticationEntryPoint))
                .headers(Customizer.withDefaults())
                .cors(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .servletApi(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .securityContext(AbstractHttpConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
