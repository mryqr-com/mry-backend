package com.mryqr.core.qr.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEventType;
import com.mryqr.core.app.domain.event.AppAwareDomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public abstract class QrUpdatedEvent extends AppAwareDomainEvent {
    private String qrId;

    public QrUpdatedEvent(DomainEventType type, String qrId, String appId, User user) {
        super(type, appId, user);
        this.qrId = qrId;
    }
}
