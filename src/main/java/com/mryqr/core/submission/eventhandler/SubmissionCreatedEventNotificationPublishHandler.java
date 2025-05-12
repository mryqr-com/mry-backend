package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.notification.publish.NotificationEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionCreatedEventNotificationPublishHandler extends AbstractDomainEventHandler<SubmissionCreatedEvent> {
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    protected void doHandle(SubmissionCreatedEvent event) {
        tenantRepository.cachedByIdOptional(event.getArTenantId()).ifPresent(tenant -> {
            if (!tenant.isSubmissionNotifyAllowed()) {
                return;
            }

            appRepository.cachedByIdOptional(event.getAppId())
                    .flatMap(app -> app.pageByIdOptional(event.getPageId())).ifPresent(page -> {
                        try {
                            if ((event.isRaisedByHuman() || tenant.isMryManageTenant()) &&
                                page.shouldNotifyOnCreateSubmission()) {
                                notificationEventPublisher.publish(event);
                            }
                        } catch (Throwable t) {
                            log.error("Fail to publish notification event for create submission[{}]: {}.",
                                    event.getSubmissionId(), t.getMessage());
                        }
                    });
        });
    }

    @Override
    public int priority() {
        return 100;
    }
}
