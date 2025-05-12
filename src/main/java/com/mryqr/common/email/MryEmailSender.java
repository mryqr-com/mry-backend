package com.mryqr.common.email;

import com.mryqr.core.common.domain.UploadedFile;

import java.util.List;

public interface MryEmailSender {
    void sendVerificationCode(String email, String code);

    void notifyWebhookNotAccessible(List<String> emails, String appName, String webhookUrl);

    void sendInvoice(String email, List<UploadedFile> invoiceFiles);
}
