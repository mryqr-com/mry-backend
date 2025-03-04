package com.mryqr.common.security.jwt;

import com.mryqr.common.properties.JwtProperties;
import com.mryqr.common.security.IpJwtCookieUpdater;
import com.mryqr.common.security.MdcFilter;
import com.mryqr.common.tracing.MryTracingService;
import com.mryqr.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.springframework.http.HttpMethod.*;

@Configuration
@RequiredArgsConstructor
public class JwtWebSecurityConfiguration {
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final JwtService jwtService;
    private final JwtCookieFactory jwtCookieFactory;
    private final IpJwtCookieUpdater ipJwtCookieUpdater;
    private final JwtProperties jwtProperties;
    private final MryObjectMapper objectMapper;
    private final MryTracingService mryTracingService;

    @Bean
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        ProviderManager authenticationManager = new ProviderManager(this.jwtAuthenticationProvider);
        http.authorizeHttpRequests(registry -> registry
                        .requestMatchers(DELETE, "/logout").permitAll()
                        .requestMatchers(POST, "/aliyun/oss-token-requisitions").permitAll()
                        .requestMatchers(GET, "/qrs/submission-qrs/*").permitAll()
                        .requestMatchers(GET, "/presentations/**").permitAll()
                        .requestMatchers(POST, "/submissions").permitAll()
                        .requestMatchers(POST, "/submissions/auto-calculate/number-input").permitAll()
                        .requestMatchers(POST, "/submissions/auto-calculate/item-status").permitAll()
                        .requestMatchers(GET, "/plans").permitAll()
                        .requestMatchers(GET, "/printing-products").permitAll()
                        .requestMatchers(GET, "/mobile-wx/auth2-callback").permitAll()
                        .requestMatchers(GET, "/pc-wx/auth2-callback").permitAll()
                        .requestMatchers(GET, "/wx/mobile-info").permitAll()
                        .requestMatchers(GET, "/wx/pc-info").permitAll()
                        .requestMatchers(POST, "/wx/jssdk-config").permitAll()
                        .requestMatchers(POST, "/preorders/pay-callback/wxpay").permitAll()
                        .requestMatchers(POST, "/verification-codes/for-register").permitAll()
                        .requestMatchers(POST, "/verification-codes/for-login").permitAll()
                        .requestMatchers(POST, "/verification-codes/for-findback-password").permitAll()
                        .requestMatchers(POST, "/login").permitAll()
                        .requestMatchers(POST, "/verification-code-login").permitAll()
                        .requestMatchers(POST, "/members/findback-password").permitAll()
                        .requestMatchers(POST, "/registration").permitAll()
                        .requestMatchers(POST, "/platform/qr-generation-record").permitAll()
                        .requestMatchers(GET, "/tenants/public-profile/*").permitAll()
                        .requestMatchers("/about",
                                "/favicon.ico",
                                "/error",
                                "/api-testing/webhook",
                                "/api-testing/orders/**",
                                "/apptemplates/**").permitAll()
                        .anyRequest().authenticated())
                .authenticationManager(authenticationManager)
                .exceptionHandling(it -> it.accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(authenticationEntryPoint))
                .addFilterAfter(new JwtAuthenticationFilter(authenticationManager, objectMapper, mryTracingService), BasicAuthenticationFilter.class)
                .addFilterAfter(new AutoRefreshJwtFilter(jwtService,
                                jwtCookieFactory,
                                ipJwtCookieUpdater,
                                jwtProperties.getAheadAutoRefresh()),
                        AuthorizationFilter.class)
                .addFilterBefore(new MdcFilter(), ExceptionTranslationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers(Customizer.withDefaults())
                .cors(AbstractHttpConfigurer::disable)
                .anonymous(configurer -> configurer.authenticationFilter(new JwtAnonymousAuthenticationFilter()))
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
