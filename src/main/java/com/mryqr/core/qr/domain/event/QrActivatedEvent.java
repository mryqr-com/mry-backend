package com.mryqr.core.qr.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.QR_ACTIVATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_ACTIVATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrActivatedEvent extends QrUpdatedEvent {

    public QrActivatedEvent(String qrId, String appId, User user) {
        super(QR_ACTIVATED, qrId, appId, user);
    }
}
