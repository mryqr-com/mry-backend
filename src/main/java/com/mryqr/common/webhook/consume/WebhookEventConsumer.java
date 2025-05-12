package com.mryqr.common.webhook.consume;

import com.mryqr.common.webhook.qr.QrCreatedWebhookPayload;
import com.mryqr.common.webhook.qr.QrDeletedWebhookPayload;
import com.mryqr.common.webhook.qr.QrUpdatedWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionApprovedWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionDeletedWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionUpdatedWebhookPayload;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrCreatedEvent;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.event.QrDeletedEvent;
import com.mryqr.core.qr.domain.event.QrUpdatedEvent;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.event.SubmissionApprovedEvent;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.submission.domain.event.SubmissionDeletedEvent;
import com.mryqr.core.submission.domain.event.SubmissionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventConsumer {
    private final AppRepository appRepository;
    private final QrRepository qrRepository;
    private final WebhookCallService webhookCallService;
    private final SubmissionRepository submissionRepository;

    public void consume(DomainEvent domainEvent) {
        if (domainEvent instanceof QrCreatedEvent theEvent) {
            App app = appRepository.cachedById(theEvent.getAppId());
            QR qr = qrRepository.byId(theEvent.getQrId());
            QrCreatedWebhookPayload payload = new QrCreatedWebhookPayload(qr, domainEvent.getId());
            webhookCallService.call(payload, app.getId(), app.getWebhookSetting());
            return;
        }

        if (domainEvent instanceof QrUpdatedEvent theEvent) {
            App app = appRepository.cachedById(theEvent.getAppId());
            QR qr = qrRepository.byId(theEvent.getQrId());
            QrUpdatedWebhookPayload payload = new QrUpdatedWebhookPayload(qr,
                    theEvent.getType(),
                    theEvent.getRaisedAt(),
                    theEvent.getRaisedBy(),
                    domainEvent.getId());
            webhookCallService.call(payload, app.getId(), app.getWebhookSetting());
            return;
        }

        if (domainEvent instanceof QrDeletedEvent theEvent) {
            App app = appRepository.cachedById(theEvent.getAppId());
            QrDeletedWebhookPayload payload = new QrDeletedWebhookPayload(theEvent.getQrId(),
                    theEvent.getPlateId(),
                    theEvent.getCustomId(),
                    theEvent.getGroupId(),
                    theEvent.getAppId(),
                    domainEvent.getId());
            webhookCallService.call(payload, app.getId(), app.getWebhookSetting());
            return;
        }

        if (domainEvent instanceof SubmissionCreatedEvent theEvent) {
            App app = appRepository.cachedById(theEvent.getAppId());
            Submission submission = submissionRepository.byId(theEvent.getSubmissionId());
            QR qr = qrRepository.byId(theEvent.getQrId());
            SubmissionCreatedWebhookPayload payload = new SubmissionCreatedWebhookPayload(submission, qr, domainEvent.getId());
            webhookCallService.call(payload, app.getId(), app.getWebhookSetting());
            return;
        }

        if (domainEvent instanceof SubmissionUpdatedEvent theEvent) {
            App app = appRepository.cachedById(theEvent.getAppId());
            Submission submission = submissionRepository.byId(theEvent.getSubmissionId());
            QR qr = qrRepository.byId(theEvent.getQrId());
            SubmissionUpdatedWebhookPayload payload = new SubmissionUpdatedWebhookPayload(submission,
                    qr,
                    theEvent.getRaisedAt(),
                    theEvent.getRaisedBy(),
                    domainEvent.getId());
            webhookCallService.call(payload, app.getId(), app.getWebhookSetting());
            return;
        }

        if (domainEvent instanceof SubmissionApprovedEvent theEvent) {
            App app = appRepository.cachedById(theEvent.getAppId());
            Submission submission = submissionRepository.byId(theEvent.getSubmissionId());
            QR qr = qrRepository.byId(theEvent.getQrId());
            SubmissionApprovedWebhookPayload payload = new SubmissionApprovedWebhookPayload(submission, qr, domainEvent.getId());
            webhookCallService.call(payload, app.getId(), app.getWebhookSetting());
            return;
        }

        if (domainEvent instanceof SubmissionDeletedEvent theEvent) {
            App app = appRepository.cachedById(theEvent.getAppId());
            QR qr = qrRepository.byId(theEvent.getQrId());
            SubmissionDeletedWebhookPayload payload = new SubmissionDeletedWebhookPayload(
                    theEvent.getSubmissionId(),
                    theEvent.getQrId(),
                    qr.getPlateId(),
                    theEvent.getAppId(),
                    theEvent.getPageId(),
                    domainEvent.getId()
            );
            webhookCallService.call(payload, app.getId(), app.getWebhookSetting());
        }
    }

}
