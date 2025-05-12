package com.mryqr.common.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class FakeSmsSender implements MrySmsSender {

    @Override
    public boolean sendVerificationCode(String mobile, String code) {
        log.info("Verification code for {} is {}", mobile, code);
        return true;
    }
}
