package com.mryqr.integration.qr.query;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegrationQrQueryService {
    private final QrRepository qrRepository;
    private final MryRateLimiter mryRateLimiter;

    public QIntegrationQr fetchQr(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Fetch", 10);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        return transform(qr);
    }

    public QIntegrationQr fetchQrByCustomId(String appId, String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Custom:Fetch", 10);

        QR qr = qrRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        return transform(qr);
    }

    private QIntegrationQr transform(QR qr) {
        return QIntegrationQr.builder()
                .id(qr.getId())
                .name(qr.getName())
                .plateId(qr.getPlateId())
                .appId(qr.getAppId())
                .groupId(qr.getGroupId())
                .template(qr.isTemplate())
                .headerImage(qr.getHeaderImage())
                .description(qr.getDescription())
                .attributeValues(qr.getAttributeValues())
                .accessCount(qr.getAccessCount())
                .lastAccessedAt(qr.getLastAccessedAt())
                .geolocation(qr.getGeolocation())
                .customId(qr.getCustomId())
                .createdAt(qr.getCreatedAt())
                .createdBy(qr.getCreatedBy())
                .active(qr.isActive())
                .build();
    }
}
