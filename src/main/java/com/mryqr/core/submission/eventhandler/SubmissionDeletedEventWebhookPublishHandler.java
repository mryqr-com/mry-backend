package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.webhook.publish.MryWebhookEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.submission.domain.event.SubmissionDeletedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.setting.SubmissionWebhookType.ON_DELETE;
import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_DELETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionDeletedEventWebhookPublishHandler implements DomainEventHandler {
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;
    private final MryWebhookEventPublisher webhookEventPublisher;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == SUBMISSION_DELETED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        SubmissionDeletedEvent theEvent = (SubmissionDeletedEvent) domainEvent;
        taskRunner.run(() -> publishWebhookEvent(theEvent));
    }

    private void publishWebhookEvent(SubmissionDeletedEvent theEvent) {
        tenantRepository.cachedByIdOptional(theEvent.getArTenantId()).ifPresent(tenant -> {
            if (!tenant.isDeveloperAllowed()) {
                return;
            }

            appRepository.cachedByIdOptional(theEvent.getAppId()).ifPresent(app -> {
                if (!app.isWebhookEnabled()) {
                    return;
                }

                app.pageByIdOptional(theEvent.getPageId()).ifPresent(page -> {
                    if (page.submissionWebhookTypes().contains(ON_DELETE)) {
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
