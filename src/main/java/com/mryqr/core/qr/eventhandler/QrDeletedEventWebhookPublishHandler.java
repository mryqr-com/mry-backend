package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.common.webhook.publish.MryWebhookEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.qr.domain.event.QrDeletedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.QrWebhookType.ON_DELETE;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrDeletedEventWebhookPublishHandler extends AbstractDomainEventHandler<QrDeletedEvent> {
    private final TenantRepository tenantRepository;
    private final AppRepository appRepository;
    private final MryWebhookEventPublisher webhookEventPublisher;


    @Override
    protected void doHandle(QrDeletedEvent event) {
        MryTaskRunner.run(() -> publishWebhookEvent(event));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }

    @Override
    public int priority() {
        return 100;
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
