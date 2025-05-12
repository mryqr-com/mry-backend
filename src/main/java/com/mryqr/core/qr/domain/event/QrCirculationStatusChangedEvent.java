package com.mryqr.core.qr.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.QR_CIRCULATION_STATUS_CHANGED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_CIRCULATION_STATUS_CHANGED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrCirculationStatusChangedEvent extends QrUpdatedEvent {

    public QrCirculationStatusChangedEvent(String qrId, String appId, User user) {
        super(QR_CIRCULATION_STATUS_CHANGED, qrId, appId, user);
    }
}
