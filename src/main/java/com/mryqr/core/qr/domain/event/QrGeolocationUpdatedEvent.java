package com.mryqr.core.qr.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.QR_GEOLOCATION_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_GEOLOCATION_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrGeolocationUpdatedEvent extends QrUpdatedEvent {

    public QrGeolocationUpdatedEvent(String qrId, String appId, User user) {
        super(QR_GEOLOCATION_UPDATED, qrId, appId, user);
    }
}
