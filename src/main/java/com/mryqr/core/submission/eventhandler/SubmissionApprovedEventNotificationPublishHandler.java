package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.notification.publish.NotificationEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.OneTimeDomainEventHandler;
import com.mryqr.core.submission.domain.event.SubmissionApprovedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_APPROVED;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionApprovedEventNotificationPublishHandler extends OneTimeDomainEventHandler {
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == SUBMISSION_APPROVED;
    }

    @Override
    protected void doHandle(DomainEvent domainEvent) {
        SubmissionApprovedEvent theEvent = (SubmissionApprovedEvent) domainEvent;
        tenantRepository.cachedByIdOptional(theEvent.getArTenantId()).ifPresent(tenant -> {
            if (!tenant.isSubmissionNotifyAllowed()) {
                return;
            }

            appRepository.cachedByIdOptional(theEvent.getAppId())
                    .flatMap(app -> app.pageByIdOptional(theEvent.getPageId()))
                    .ifPresent(page -> {
                        try {
                            if ((theEvent.isRaisedByHuman() || tenant.isMryManageTenant()) &&
                                    page.shouldNotifySubmitterOnApproval()) {
                                notificationEventPublisher.publish(theEvent);
                            }
                        } catch (Throwable t) {
                            log.error("Fail to publish notification event for approve submission[{}]: {}.",
                                    theEvent.getSubmissionId(), t.getMessage());
                        }
                    });
        });
    }

}
