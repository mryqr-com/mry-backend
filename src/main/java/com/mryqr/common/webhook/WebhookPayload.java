package com.mryqr.common.webhook;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.common.webhook.qr.QrCreatedWebhookPayload;
import com.mryqr.common.webhook.qr.QrDeletedWebhookPayload;
import com.mryqr.common.webhook.qr.QrUpdatedWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionApprovedWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionDeletedWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionUpdatedWebhookPayload;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static lombok.AccessLevel.PROTECTED;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = SubmissionCreatedWebhookPayload.class, name = "SUBMISSION_CREATED"),
        @JsonSubTypes.Type(value = SubmissionUpdatedWebhookPayload.class, name = "SUBMISSION_UPDATED"),
        @JsonSubTypes.Type(value = SubmissionApprovedWebhookPayload.class, name = "SUBMISSION_APPROVED"),
        @JsonSubTypes.Type(value = SubmissionDeletedWebhookPayload.class, name = "SUBMISSION_DELETED"),
        @JsonSubTypes.Type(value = QrCreatedWebhookPayload.class, name = "QR_CREATED"),
        @JsonSubTypes.Type(value = QrUpdatedWebhookPayload.class, name = "QR_UPDATED"),
        @JsonSubTypes.Type(value = QrDeletedWebhookPayload.class, name = "QR_DELETED"),
})

@Getter
@NoArgsConstructor(access = PROTECTED)
public abstract class WebhookPayload {
    private String id;
    private WebhookPayloadType type;
    private String appId;
    private String eventId;

    private String tenantId;

    public WebhookPayload(WebhookPayloadType type, String appId, String eventId, String tenantId) {
        this.id = newShortUuid();
        this.type = type;
        this.appId = appId;
        this.eventId = eventId;
        this.tenantId = tenantId;
    }
}
