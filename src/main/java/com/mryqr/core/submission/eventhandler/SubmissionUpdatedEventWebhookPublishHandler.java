package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.webhook.publish.MryWebhookEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.submission.domain.event.SubmissionUpdatedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.setting.SubmissionWebhookType.ON_UPDATE;
import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_UPDATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionUpdatedEventWebhookPublishHandler implements DomainEventHandler {
    private final MryWebhookEventPublisher webhookEventPublisher;
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == SUBMISSION_UPDATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        SubmissionUpdatedEvent theEvent = (SubmissionUpdatedEvent) domainEvent;
        taskRunner.run(() -> publishWebhookEvent(theEvent));
    }

    private void publishWebhookEvent(SubmissionUpdatedEvent theEvent) {
        tenantRepository.cachedByIdOptional(theEvent.getArTenantId()).ifPresent(tenant -> {
            if (!tenant.isDeveloperAllowed()) {
                return;
            }

            appRepository.cachedByIdOptional(theEvent.getAppId()).ifPresent(app -> {
                if (!app.isWebhookEnabled()) {
                    return;
                }

                app.pageByIdOptional(theEvent.getPageId()).ifPresent(page -> {
                    if (page.submissionWebhookTypes().contains(ON_UPDATE)) {
                        webhookEventPublisher.publish(theEvent);
                    }
                });
            });
        });
    }

    @Override
    public int priority() {
        return 100;
    }
}
