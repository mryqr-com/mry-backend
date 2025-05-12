package com.mryqr.common.webhook.qr;

import com.mryqr.core.common.domain.event.DomainEventType;
import com.mryqr.core.qr.domain.QR;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.mryqr.common.webhook.WebhookPayloadType.QR_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class QrUpdatedWebhookPayload extends BaseQrWebhookPayload {
    private DomainEventType updateType;
    private Instant updatedAt;
    private String updatedBy;

    public QrUpdatedWebhookPayload(QR qr, DomainEventType updateType, Instant updatedAt, String updatedBy, String eventId) {
        super(QR_UPDATED, qr, eventId);
        this.updateType = updateType;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }
}
