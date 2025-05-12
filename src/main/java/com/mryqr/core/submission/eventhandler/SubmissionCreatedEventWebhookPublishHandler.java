package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.common.webhook.publish.MryWebhookEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.setting.SubmissionWebhookType.ON_CREATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionCreatedEventWebhookPublishHandler extends AbstractDomainEventHandler<SubmissionCreatedEvent> {
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;
    private final MryWebhookEventPublisher webhookEventPublisher;

    @Override
    public void handle(SubmissionCreatedEvent event) {
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

    private void publishWebhookEvent(SubmissionCreatedEvent theEvent) {
        tenantRepository.cachedByIdOptional(theEvent.getArTenantId()).ifPresent(tenant -> {
            if (!tenant.isDeveloperAllowed()) {
                return;
            }

            appRepository.cachedByIdOptional(theEvent.getAppId()).ifPresent(app -> {
                if (!app.isWebhookEnabled()) {
                    return;
                }

                app.pageByIdOptional(theEvent.getPageId()).ifPresent(page -> {
                    if (page.submissionWebhookTypes().contains(ON_CREATE)) {
                        webhookEventPublisher.publish(theEvent);
                    }
                });
            });
        });
    }
}
