package com.mryqr.core.submission.domain.event;

import com.mryqr.core.app.domain.event.AppAwareDomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("SUBMISSION_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class SubmissionDeletedEvent extends AppAwareDomainEvent {
    private String submissionId;
    private String qrId;
    private String pageId;

    public SubmissionDeletedEvent(String submissionId, String qrId, String appId, String pageId, User user) {
        super(SUBMISSION_DELETED, appId, user);
        this.submissionId = submissionId;
        this.qrId = qrId;
        this.pageId = pageId;
    }
}
