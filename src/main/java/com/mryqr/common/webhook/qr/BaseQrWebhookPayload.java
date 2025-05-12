package com.mryqr.common.webhook.qr;

import com.mryqr.common.webhook.WebhookPayload;
import com.mryqr.common.webhook.WebhookPayloadType;
import com.mryqr.core.common.domain.Geolocation;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;

@Getter
@NoArgsConstructor(access = PROTECTED)
public abstract class BaseQrWebhookPayload extends WebhookPayload {
    private String qrId;
    private String name;
    private String plateId;
    private String groupId;
    private boolean template;
    private UploadedFile headerImage;
    private String description;
    private List<AttributeValue> attributeValues;
    private int accessCount;
    private Instant lastAccessedAt;
    private Geolocation geolocation;
    private String customId;
    private String tenantId;
    private Instant createdAt;
    private String createdBy;

    protected BaseQrWebhookPayload(WebhookPayloadType type, QR qr, String eventId) {
        super(type, qr.getAppId(), eventId);
        this.qrId = qr.getId();
        this.name = qr.getName();
        this.plateId = qr.getPlateId();
        this.groupId = qr.getGroupId();
        this.template = qr.isTemplate();
        this.headerImage = qr.getHeaderImage();
        this.description = qr.getDescription();
        this.attributeValues = emptyIfNull(qr.getAttributeValues()).values().stream().collect(toImmutableList());
        this.accessCount = qr.getAccessCount();
        this.lastAccessedAt = qr.getLastAccessedAt();
        this.geolocation = qr.getGeolocation();
        this.customId = qr.getCustomId();
        this.tenantId = qr.getTenantId();
        this.createdAt = qr.getCreatedAt();
        this.createdBy = qr.getCreatedBy();
    }

}
