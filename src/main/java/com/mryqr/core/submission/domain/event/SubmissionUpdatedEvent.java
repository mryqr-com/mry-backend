package com.mryqr.core.submission.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.event.AppAwareDomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.SUBMISSION_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("SUBMISSION_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class SubmissionUpdatedEvent extends AppAwareDomainEvent {
    private String submissionId;
    private String qrId;
    private String pageId;

    public SubmissionUpdatedEvent(String submissionId, String qrId, String appId, String pageId, User user) {
        super(SUBMISSION_UPDATED, appId, user);
        this.submissionId = submissionId;
        this.qrId = qrId;
        this.pageId = pageId;
    }
}
