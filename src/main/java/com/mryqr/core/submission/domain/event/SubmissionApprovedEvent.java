package com.mryqr.core.submission.domain.event;

import com.mryqr.core.app.domain.event.AppAwareDomainEvent;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.submission.domain.SubmissionApproval;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_APPROVED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("SUBMISSION_APPROVED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class SubmissionApprovedEvent extends AppAwareDomainEvent {
    private String submissionId;
    private String qrId;
    private String pageId;
    private SubmissionApproval approval;

    public SubmissionApprovedEvent(String submissionId,
                                   String qrId,
                                   String appId,
                                   String pageId,
                                   SubmissionApproval approval,
                                   User user) {
        super(SUBMISSION_APPROVED, appId, user);
        this.submissionId = submissionId;
        this.qrId = qrId;
        this.pageId = pageId;
        this.approval = approval;
    }
}
