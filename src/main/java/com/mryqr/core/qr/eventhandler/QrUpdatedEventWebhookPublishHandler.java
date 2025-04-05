package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.common.webhook.publish.MryWebhookEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.qr.domain.event.QrUpdatedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.QrWebhookType.ON_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrUpdatedEventWebhookPublishHandler extends AbstractDomainEventHandler<QrUpdatedEvent> {
    private final TenantRepository tenantRepository;
    private final AppRepository appRepository;
    private final MryWebhookEventPublisher webhookEventPublisher;

    @Override
    public boolean isIdempotent() {
        return true;
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public void handle(QrUpdatedEvent event) {
        MryTaskRunner.run(() -> publishWebhookEvent(event));
    }

    private void publishWebhookEvent(QrUpdatedEvent theEvent) {
        tenantRepository.cachedByIdOptional(theEvent.getArTenantId()).ifPresent(tenant -> {
            if (!tenant.isDeveloperAllowed()) {
                return;
            }

            appRepository.cachedByIdOptional(theEvent.getAppId()).ifPresent(app -> {
                if (app.isWebhookEnabled() && app.qrWebhookTypes().contains(ON_UPDATE)) {
                    webhookEventPublisher.publish(theEvent);
                }
            });
        });
    }
}
