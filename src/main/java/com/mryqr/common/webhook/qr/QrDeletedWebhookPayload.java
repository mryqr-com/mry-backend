package com.mryqr.common.webhook.qr;

import com.mryqr.common.webhook.WebhookPayload;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.common.webhook.WebhookPayloadType.QR_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class QrDeletedWebhookPayload extends WebhookPayload {
    private String qrId;
    private String plateId;
    private String customId;
    private String groupId;

    public QrDeletedWebhookPayload(String qrId,
                                   String plateId,
                                   String customId,
                                   String groupId,
                                   String appId,
                                   String eventId,
                                   String tenantId) {
        super(QR_DELETED, appId, eventId, tenantId);
        this.qrId = qrId;
        this.plateId = plateId;
        this.customId = customId;
        this.groupId = groupId;
    }
}
