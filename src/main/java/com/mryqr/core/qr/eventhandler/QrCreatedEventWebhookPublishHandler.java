package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.webhook.publish.MryWebhookEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.QrCreatedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.QrWebhookType.ON_CREATE;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCreatedEventWebhookPublishHandler implements DomainEventHandler {
    private final TenantRepository tenantRepository;
    private final AppRepository appRepository;
    private final MryWebhookEventPublisher webhookEventPublisher;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == QR_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        QrCreatedEvent theEvent = (QrCreatedEvent) domainEvent;
        taskRunner.run(() -> publishWebhookEvent(theEvent));
    }

    private void publishWebhookEvent(QrCreatedEvent theEvent) {
        tenantRepository.cachedByIdOptional(theEvent.getArTenantId()).ifPresent(tenant -> {
            if (!tenant.isDeveloperAllowed()) {
                return;
            }

            appRepository.cachedByIdOptional(theEvent.getAppId()).ifPresent(app -> {
                if (app.isWebhookEnabled() && app.qrWebhookTypes().contains(ON_CREATE)) {
                    webhookEventPublisher.publish(theEvent);
                }
            });
        });
    }

    @Override
    public int priority() {
        return 100;
    }
}
