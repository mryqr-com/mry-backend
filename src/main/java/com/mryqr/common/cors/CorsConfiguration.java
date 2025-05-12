package com.mryqr.common.cors;

import com.mryqr.core.common.properties.CommonProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
@RequiredArgsConstructor
public class CorsConfiguration {
    private final CommonProperties commonProperties;

    @Bean
    @Profile("prod")
    public FilterRegistrationBean<CorsFilter> prodCorsFilter() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedOriginPatterns(List.of("*." + commonProperties.getBaseDomainName()));
        configuration.setAllowedMethods(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    @Profile("!prod")
    public FilterRegistrationBean<CorsFilter> noneProdCorsFilter() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(HIGHEST_PRECEDENCE);
        return bean;
    }

}
