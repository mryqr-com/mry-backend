package com.mryqr.common.email;

import com.mryqr.core.common.domain.UploadedFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("!prod")
public class FakeMryEmailSender implements MryEmailSender {
    @Override
    public void sendVerificationCode(String email, String code) {
        log.info("Verification code for {} is {}", email, code);
    }

    @Override
    public void notifyWebhookNotAccessible(List<String> emails, String appName, String webhookUrl) {
        log.info("Notify App[{}] webhook[{}] not accessible to {}.", appName, webhookUrl, emails);
    }

    @Override
    public void sendInvoice(String email, List<UploadedFile> invoiceFiles) {
        log.info("Send invoice files to {}.", email);
    }
}
