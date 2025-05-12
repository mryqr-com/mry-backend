package com.mryqr.common.notification;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.setting.notification.NotificationRole;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionApproval;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CompositeNotificationService implements NotificationService {
    private final List<NotificationService> notificationServices;

    public CompositeNotificationService(List<NotificationService> notificationServices) {
        this.notificationServices = notificationServices;
    }

    @Override
    public void notifyOnSubmissionCreated(Submission submission, Page page, App app, List<NotificationRole> notifyRoles) {
        for (NotificationService notificationService : notificationServices) {
            try {
                notificationService.notifyOnSubmissionCreated(submission, page, app, notifyRoles);
            } catch (Throwable t) {
                log.error("Send submission created notification failed.", t);
            }
        }
    }

    @Override
    public void notifyOnSubmissionUpdated(Submission submission, Page page, App app, List<NotificationRole> notifyRoles) {
        for (NotificationService notificationService : notificationServices) {
            try {
                notificationService.notifyOnSubmissionUpdated(submission, page, app, notifyRoles);
            } catch (Throwable t) {
                log.error("Send submission updated notification failed.", t);
            }
        }
    }

    @Override
    public void notifySubmitterOnSubmissionApproved(Submission submission, Page page, App app, SubmissionApproval approval) {
        for (NotificationService notificationService : notificationServices) {
            try {
                notificationService.notifySubmitterOnSubmissionApproved(submission, page, app, approval);
            } catch (Throwable t) {
                log.error("Send submission approved notification failed.", t);
            }
        }
    }

    @Override
    public void notifyOperatorsAssignmentNearExpire(Assignment assignment, App app) {
        for (NotificationService notificationService : notificationServices) {
            try {
                notificationService.notifyOperatorsAssignmentNearExpire(assignment, app);
            } catch (Throwable t) {
                log.error("Send assignment near expired notification failed.", t);
            }
        }
    }

    @Override
    public void notifyOperatorsOnAssignmentCreated(Assignment assignment, App app) {
        for (NotificationService notificationService : notificationServices) {
            try {
                notificationService.notifyOperatorsOnAssignmentCreated(assignment, app);
            } catch (Throwable t) {
                log.error("Send assignment created notification failed.", t);
            }
        }
    }

    @Override
    public void notifyApproverOnSubmissionCreated(Submission submission, Page page, App app) {
        for (NotificationService notificationService : notificationServices) {
            try {
                notificationService.notifyApproverOnSubmissionCreated(submission, page, app);
            } catch (Throwable t) {
                log.error("Send notification to submission approver failed.", t);
            }
        }
    }

    @Override
    public void notifyOnAssignmentFinished(Assignment assignment, App app) {
        for (NotificationService notificationService : notificationServices) {
            try {
                notificationService.notifyOnAssignmentFinished(assignment, app);
            } catch (Throwable t) {
                log.error("Send notification for assignment finished failed.", t);
            }
        }
    }

    @Override
    public void notifyOnAssignmentFailed(Assignment assignment, App app) {
        for (NotificationService notificationService : notificationServices) {
            try {
                notificationService.notifyOnAssignmentFailed(assignment, app);
            } catch (Throwable t) {
                log.error("Send notification for assignment failure failed.", t);
            }
        }
    }

}
