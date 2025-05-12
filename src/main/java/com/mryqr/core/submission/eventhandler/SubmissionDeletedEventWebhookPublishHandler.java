package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.common.webhook.publish.MryWebhookEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.submission.domain.event.SubmissionDeletedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.setting.SubmissionWebhookType.ON_DELETE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionDeletedEventWebhookPublishHandler extends AbstractDomainEventHandler<SubmissionDeletedEvent> {
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;
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
    public void handle(SubmissionDeletedEvent event) {
        MryTaskRunner.run(() -> publishWebhookEvent(event));
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
}
