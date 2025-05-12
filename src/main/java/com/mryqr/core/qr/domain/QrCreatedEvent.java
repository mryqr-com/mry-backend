package com.mryqr.core.qr.domain;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.event.AppAwareDomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.QR_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_CREATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrCreatedEvent extends AppAwareDomainEvent {
    private String qrId;
    private String plateId;
    private String groupId;

    public QrCreatedEvent(String qrId, String plateId, String groupId, String appId, User user) {
        super(QR_CREATED, appId, user);
        this.qrId = qrId;
        this.plateId = plateId;
        this.groupId = groupId;
    }
}
