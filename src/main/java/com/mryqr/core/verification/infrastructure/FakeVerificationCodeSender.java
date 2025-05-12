package com.mryqr.core.verification.infrastructure;

import com.mryqr.core.tenant.domain.task.TenantSmsUsageCountTask;
import com.mryqr.core.verification.domain.VerificationCode;
import com.mryqr.core.verification.domain.VerificationCodeSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.utils.CommonUtils.isMobileNumber;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class FakeVerificationCodeSender implements VerificationCodeSender {
    private final TenantSmsUsageCountTask tenantSmsUsageCountTask;

    @Override
    public void send(VerificationCode code) {
        String mobileOrEmail = code.getMobileOrEmail();
        String tenantId = code.getTenantId();

        if (isMobileNumber(mobileOrEmail) && isNotBlank(tenantId)) {
            tenantSmsUsageCountTask.run(tenantId);
        }

        log.info("Verification code for {} is {}", mobileOrEmail, code.getCode());
    }
}
