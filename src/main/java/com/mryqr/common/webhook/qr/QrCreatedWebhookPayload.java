package com.mryqr.common.webhook.qr;

import com.mryqr.core.qr.domain.QR;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.common.webhook.WebhookPayloadType.QR_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class QrCreatedWebhookPayload extends BaseQrWebhookPayload {
    public QrCreatedWebhookPayload(QR qr, String eventId) {
        super(QR_CREATED, qr, eventId);
    }
}
