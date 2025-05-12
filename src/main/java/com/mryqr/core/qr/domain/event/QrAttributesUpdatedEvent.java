package com.mryqr.core.qr.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.QR_ATTRIBUTES_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_ATTRIBUTES_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrAttributesUpdatedEvent extends QrUpdatedEvent {

    public QrAttributesUpdatedEvent(String qrId, String appId, User user) {
        super(QR_ATTRIBUTES_UPDATED, qrId, appId, user);
    }
}
