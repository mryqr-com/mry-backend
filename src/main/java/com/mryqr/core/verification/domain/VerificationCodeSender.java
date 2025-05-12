package com.mryqr.core.verification.domain;

public interface VerificationCodeSender {
    void send(VerificationCode code);
}
