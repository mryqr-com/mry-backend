package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.common.webhook.publish.MryWebhookEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.submission.domain.event.SubmissionUpdatedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.setting.SubmissionWebhookType.ON_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionUpdatedEventWebhookPublishHandler extends AbstractDomainEventHandler<SubmissionUpdatedEvent> {
    private final MryWebhookEventPublisher webhookEventPublisher;
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;

    @Override
    public boolean isIdempotent() {
        return true;
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    protected void doHandle(SubmissionUpdatedEvent event) {
        MryTaskRunner.run(() -> publishWebhookEvent(event));
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
}
