package com.mryqr.core.qr.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.QR_DEACTIVATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_DEACTIVATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrDeactivatedEvent extends QrUpdatedEvent {

    public QrDeactivatedEvent(String qrId, String appId, User user) {
        super(QR_DEACTIVATED, qrId, appId, user);
    }
}
