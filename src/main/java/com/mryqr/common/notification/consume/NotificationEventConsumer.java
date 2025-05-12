package com.mryqr.common.notification.consume;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.notification.NotificationService;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.assignment.event.AssignmentCreatedEvent;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.event.SubmissionApprovedEvent;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.submission.domain.event.SubmissionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {
    private final NotificationService notificationService;
    private final AppRepository appRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;

    public void consume(DomainEvent domainEvent) {
        if (domainEvent.getRaisedAt().isBefore(now().minus(4, HOURS))) {
            log.warn("Notification event[{}:{}] is more than 4 hours old, skip.", domainEvent.getType(), domainEvent.getId());
            return;
        }

        if (domainEvent instanceof SubmissionCreatedEvent theEvent) {
            Submission submission = submissionRepository.byId(theEvent.getSubmissionId());
            App app = appRepository.cachedById(submission.getAppId());
            Page page = app.pageById(theEvent.getPageId());
            notificationService.notifyOnSubmissionCreated(submission, page, app, page.notifyOnCreateRoles());
            return;
        }

        if (domainEvent instanceof SubmissionUpdatedEvent theEvent) {
            Submission submission = submissionRepository.byId(theEvent.getSubmissionId());
            App app = appRepository.cachedById(submission.getAppId());
            Page page = app.pageById(theEvent.getPageId());
            notificationService.notifyOnSubmissionUpdated(submission, page, app, page.notifyOnUpdateRoles());
            return;
        }

        if (domainEvent instanceof SubmissionApprovedEvent theEvent) {
            Submission submission = submissionRepository.byId(theEvent.getSubmissionId());
            App app = appRepository.cachedById(submission.getAppId());
            Page page = app.pageById(theEvent.getPageId());
            notificationService.notifySubmitterOnSubmissionApproved(submission, page, app, submission.getApproval());
            return;
        }

        if (domainEvent instanceof AssignmentCreatedEvent theEvent) {
            Assignment assignment = assignmentRepository.byId(theEvent.getAssignmentId());
            App app = appRepository.cachedById(assignment.getAppId());
            notificationService.notifyOperatorsOnAssignmentCreated(assignment, app);
        }

    }

}
