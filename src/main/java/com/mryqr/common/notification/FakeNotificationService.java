package com.mryqr.common.notification;

import com.mryqr.common.profile.NonProdProfile;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.setting.notification.NotificationRole;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionApproval;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@NonProdProfile
public class FakeNotificationService implements NotificationService {
    public static String id;

    @Override
    public void notifyOnSubmissionCreated(
            Submission submission,
            Page page,
            App app,
            List<NotificationRole> notifyRoles) {
        id = submission.getId();
        log.info("Notified [{}] on submission[{}] created.", notifyRoles, submission.getId());
    }

    @Override
    public void notifyOnSubmissionUpdated(
            Submission submission,
            Page page,
            App app,
            List<NotificationRole> notifyRoles) {
        id = submission.getId();
        log.info("Notified [{}] on submission[{}] updated.", notifyRoles, submission.getId());
    }

    @Override
    public void notifySubmitterOnSubmissionApproved(
            Submission submission,
            Page page,
            App app,
            SubmissionApproval approval) {
        id = submission.getId();
        log.info("Notified submitter on submission[{}] approved.", submission.getId());
    }

    @Override
    public void notifyOperatorsAssignmentNearExpire(Assignment assignment, App app) {
        id = assignment.getId();
        log.info("Notified assignment[{}] operators[{}] on near expire.", assignment.getId(), assignment.getOperators());
    }

    @Override
    public void notifyOperatorsOnAssignmentCreated(Assignment assignment, App app) {
        id = assignment.getId();
        log.info("Notified operators{} on assignment[{}] created.", assignment.getOperators(), assignment.getId());
    }

    @Override
    public void notifyApproverOnSubmissionCreated(Submission submission, Page page, App app) {
        id = submission.getId();

        log.info("Notified approvers on submission[{}] created.", submission.getId());
    }

    @Override
    public void notifyOnAssignmentFinished(Assignment assignment, App app) {
        id = assignment.getId();
        log.info("Notified on assignment[{}] finished.", assignment.getId());
    }

    @Override
    public void notifyOnAssignmentFailed(Assignment assignment, App app) {
        id = assignment.getId();
        log.info("Notified on assignment[{}] on failed.", assignment.getId());
    }
}
