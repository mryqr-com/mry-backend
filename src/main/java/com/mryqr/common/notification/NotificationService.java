package com.mryqr.common.notification;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.setting.notification.NotificationRole;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionApproval;

import java.util.List;

public interface NotificationService {

    void notifyOnSubmissionCreated(Submission submission, Page page, App app, List<NotificationRole> notifyRoles);

    void notifyOnSubmissionUpdated(Submission submission, Page page, App app, List<NotificationRole> notifyRoles);

    void notifySubmitterOnSubmissionApproved(Submission submission, Page page, App app, SubmissionApproval approval);

    void notifyApproverOnSubmissionCreated(Submission submission, Page page, App app);

    void notifyOperatorsOnAssignmentCreated(Assignment assignment, App app);

    void notifyOperatorsAssignmentNearExpire(Assignment assignment, App app);

    void notifyOnAssignmentFinished(Assignment assignment, App app);

    void notifyOnAssignmentFailed(Assignment assignment, App app);
}
