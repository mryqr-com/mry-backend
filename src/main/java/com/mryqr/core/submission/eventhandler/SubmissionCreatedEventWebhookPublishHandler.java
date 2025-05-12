package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.webhook.publish.MryWebhookEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.setting.SubmissionWebhookType.ON_CREATE;
import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionCreatedEventWebhookPublishHandler implements DomainEventHandler {
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;
    private final MryWebhookEventPublisher webhookEventPublisher;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == SUBMISSION_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        SubmissionCreatedEvent theEvent = (SubmissionCreatedEvent) domainEvent;
        taskRunner.run(() -> publishWebhookEvent(theEvent));
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
