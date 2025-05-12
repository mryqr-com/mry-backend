package com.mryqr.common.email;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.profile.ProdProfile;
import com.mryqr.common.properties.PropertyService;
import com.mryqr.common.utils.CommonUtils;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.utils.CommonUtils.maskMobileOrEmail;
import static java.net.http.HttpClient.newHttpClient;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
@ProdProfile
@RequiredArgsConstructor
public class DefaultEmailSender implements MryEmailSender {
    private final JavaMailSender mailSender;
    private final PropertyService propertyService;

    @Override
    public void sendVerificationCode(String email, String code) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setFrom("码如云 <noreply@directmail.mryqr.com>");
            mailMessage.setSubject("验证码");
            mailMessage.setText("您的验证码为：" + code + "，该验证码10分钟内有效，请勿泄露于他人。");
            mailSender.send(mailMessage);
            log.info("Sent email verification code to [{}].", maskMobileOrEmail(email));
        } catch (Throwable t) {
            log.error("Error while send notification code to email[{}].", email, t);
        }
    }

    @Override
    public void notifyWebhookNotAccessible(List<String> emails, String appName, String webhookUrl) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(emails.toArray(String[]::new));
            mailMessage.setFrom("码如云 <noreply@directmail.mryqr.com>");
            mailMessage.setSubject("Webhook无法访问（" + appName + "）");
            mailMessage.setText("您好，您的码如云应用（" + appName +
                                "）的Webhook当前已经无法访问，系统已经自动停止了向该Webhook发送消息，请您及时检查。\n\nWebhook URL: " + webhookUrl);
            mailSender.send(mailMessage);
            log.info("Sent webhook not accessible notify email to {}.",
                    emails.stream().map(CommonUtils::maskMobileOrEmail).collect(toImmutableList()));
        } catch (Throwable t) {
            log.error("Error while notify webhook inaccessible: {}.", emails, t);
        }
    }

    @Override
    public void sendInvoice(String email, List<UploadedFile> invoiceFiles) {
        if (isBlank(email) || isEmpty(invoiceFiles)) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(email);
            helper.setSubject("发票");
            helper.setFrom("码如云 <noreply@directmail.mryqr.com>");
            helper.setText("您的码如云发票已经开具完毕，请查收附件。");

            HttpClient httpClient = newHttpClient();
            invoiceFiles.forEach(file -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .header("Referer", propertyService.consoleBaseUrl())
                            .uri(new URI(file.getFileUrl()))
                            .build();

                    HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    helper.addAttachment(file.getName(), new ByteArrayDataSource(response.body(), "application/octet-stream"));
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            });
            mailSender.send(mimeMessage);
            log.info("Sent invoice files to [{}].", maskMobileOrEmail(email));
        } catch (Throwable t) {
            log.error("Failed to send invoice files to [{}].", email, t);
        }
    }
}
