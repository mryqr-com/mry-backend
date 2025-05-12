package com.mryqr.common.password;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordConfiguration {

    @Bean
    public MryPasswordEncoder passwordEncoder() {
        return new SpringMryPasswordEncoder(new BCryptPasswordEncoder());
    }

}
