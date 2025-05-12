package com.mryqr.common.notification.consume;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.notification.CompositeNotificationService;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.assignment.event.AssignmentCreatedEvent;
import com.mryqr.core.assignment.event.AssignmentFailedEvent;
import com.mryqr.core.assignment.event.AssignmentFinishedEvent;
import com.mryqr.core.assignment.event.AssignmentNearExpiredEvent;
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
    private final AppRepository appRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final CompositeNotificationService notificationService;


    public void consume(DomainEvent domainEvent) {
        if (domainEvent.getRaisedAt().isBefore(now().minus(4, HOURS))) {
            log.warn("Notification event[{}:{}] is more than 4 hours old, skip.", domainEvent.getType(), domainEvent.getId());
            return;
        }

        if (domainEvent instanceof SubmissionCreatedEvent theEvent) {
            Submission submission = submissionRepository.byId(theEvent.getSubmissionId());
            App app = appRepository.cachedById(submission.getAppId());
            Page page = app.pageById(theEvent.getPageId());
            if (page.shouldNotifyOnCreateSubmission()) {
                notificationService.notifyOnSubmissionCreated(submission, page, app, page.notifyOnCreateRoles());
            }

            if (page.isApprovalEnabled()) {
                notificationService.notifyApproverOnSubmissionCreated(submission, page, app);
            }
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
            return;
        }

        if (domainEvent instanceof AssignmentNearExpiredEvent theEvent) {
            Assignment assignment = assignmentRepository.byId(theEvent.getAssignmentId());
            App app = appRepository.cachedById(assignment.getAppId());
            notificationService.notifyOperatorsAssignmentNearExpire(assignment, app);
            return;
        }

        if (domainEvent instanceof AssignmentFinishedEvent theEvent) {
            Assignment assignment = assignmentRepository.byId(theEvent.getAssignmentId());
            App app = appRepository.cachedById(assignment.getAppId());
            notificationService.notifyOnAssignmentFinished(assignment, app);
            return;
        }

        if (domainEvent instanceof AssignmentFailedEvent theEvent) {
            Assignment assignment = assignmentRepository.byId(theEvent.getAssignmentId());
            App app = appRepository.cachedById(assignment.getAppId());
            notificationService.notifyOnAssignmentFailed(assignment, app);
        }

    }

}
