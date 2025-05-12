package com.mryqr.core.verification.domain;

import java.util.Optional;

public interface VerificationCodeRepository {
    Optional<VerificationCode> findValidOptional(String mobileOrEmail, String code, VerificationCodeType type);

    boolean existsWithinOneMinutes(String mobileOrEmail, VerificationCodeType type);

    long totalCodeCountOfTodayFor(String mobileOrEmail);

    void save(VerificationCode it);

    VerificationCode byId(String id);

    boolean exists(String arId);
}
