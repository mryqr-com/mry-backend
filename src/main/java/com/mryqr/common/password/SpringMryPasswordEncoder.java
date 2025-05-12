package com.mryqr.common.password;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpringMryPasswordEncoder implements MryPasswordEncoder {

    private final org.springframework.security.crypto.password.PasswordEncoder _passwordEncoder;

    @Override
    public String encode(CharSequence rawPassword) {
        return _passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return _passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
