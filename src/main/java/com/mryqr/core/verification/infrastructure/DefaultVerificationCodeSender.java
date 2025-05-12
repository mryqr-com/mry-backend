package com.mryqr.core.verification.infrastructure;

import com.mryqr.common.email.MryEmailSender;
import com.mryqr.common.sms.MrySmsSender;
import com.mryqr.core.tenant.domain.task.TenantSmsUsageCountTask;
import com.mryqr.core.verification.domain.VerificationCode;
import com.mryqr.core.verification.domain.VerificationCodeSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.utils.CommonUtils.isMobileNumber;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class DefaultVerificationCodeSender implements VerificationCodeSender {
    private final TaskExecutor taskExecutor;
    private final MryEmailSender mryEmailSender;
    private final MrySmsSender mrySmsSender;
    private final TenantSmsUsageCountTask tenantSmsUsageCountTask;

    public void send(VerificationCode code) {
        String mobileOrEmail = code.getMobileOrEmail();
        String theCode = code.getCode();

        if (isMobileNumber(mobileOrEmail)) {
            taskExecutor.execute(() -> {
                boolean result = mrySmsSender.sendVerificationCode(mobileOrEmail, theCode);
                if (result && isNotBlank(code.getTenantId())) {
                    tenantSmsUsageCountTask.run(code.getTenantId());
                }
            });
        } else {
            taskExecutor.execute(() -> mryEmailSender.sendVerificationCode(mobileOrEmail, theCode));
        }
    }

}
