package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.webhook.publish.MryWebhookEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrDeletedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.QrWebhookType.ON_DELETE;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_DELETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrDeletedEventWebhookPublishHandler implements DomainEventHandler {
    private final TenantRepository tenantRepository;
    private final AppRepository appRepository;
    private final MryWebhookEventPublisher webhookEventPublisher;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == QR_DELETED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        QrDeletedEvent event = (QrDeletedEvent) domainEvent;
        taskRunner.run(() -> publishWebhookEvent(event));
    }

    protected void publishWebhookEvent(QrDeletedEvent theEvent) {
        tenantRepository.cachedByIdOptional(theEvent.getArTenantId()).ifPresent(tenant -> {
            if (!tenant.isDeveloperAllowed()) {
                return;
            }

            appRepository.cachedByIdOptional(theEvent.getAppId()).ifPresent(app -> {
                if (app.isWebhookEnabled() && app.qrWebhookTypes().contains(ON_DELETE)) {
                    webhookEventPublisher.publish(theEvent);
                }
            });
        });
    }

}
