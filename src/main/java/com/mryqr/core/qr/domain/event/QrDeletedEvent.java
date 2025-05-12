package com.mryqr.core.qr.domain.event;

import com.mryqr.core.app.domain.event.AppAwareDomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.QR_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrDeletedEvent extends AppAwareDomainEvent {
    private String qrId;
    private String plateId;
    private String customId;
    private String groupId;

    public QrDeletedEvent(String qrId, String plateId, String customId, String groupId, String appId, User user) {
        super(QR_DELETED, appId, user);
        this.qrId = qrId;
        this.plateId = plateId;
        this.customId = customId;
        this.groupId = groupId;
    }
}
