package com.mryqr.core.qr.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.QR_GROUP_CHANGED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_GROUP_CHANGED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrGroupChangedEvent extends QrUpdatedEvent {
    private String oldGroupId;
    private String newGroupId;

    public QrGroupChangedEvent(String qrId, String appId, String oldGroupId, String newGroupId, User user) {
        super(QR_GROUP_CHANGED, qrId, appId, user);
        this.oldGroupId = oldGroupId;
        this.newGroupId = newGroupId;
    }
}
