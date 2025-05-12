package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.notification.publish.NotificationEventPublisher;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.management.crm.MryTenantManageApp.MRY_TENANT_MANAGE_APP_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionCreatedEventNotificationPublishHandler extends AbstractDomainEventHandler<SubmissionCreatedEvent> {
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    public void handle(SubmissionCreatedEvent event) {
        tenantRepository.cachedByIdOptional(event.getArTenantId()).ifPresent(tenant -> {
            if (!tenant.isSubmissionNotifyAllowed()) {
                return;
            }

            appRepository.cachedByIdOptional(event.getAppId()).ifPresent(app -> {
                app.pageByIdOptional(event.getPageId()).ifPresent(page -> {
                    try {
                        boolean shouldPublish = page.isApprovalEnabled();

                        if ((event.isFromHumanChannel() || app.getId().equals(MRY_TENANT_MANAGE_APP_ID)) &&
                            page.shouldNotifyOnCreateSubmission()) {
                            shouldPublish = true;
                        }

                        if (shouldPublish) {
                            notificationEventPublisher.publish(event);
                        }
                    } catch (Throwable t) {
                        log.error("Fail to publish notification event for create submission[{}]: {}.",
                                event.getSubmissionId(), t.getMessage());
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
