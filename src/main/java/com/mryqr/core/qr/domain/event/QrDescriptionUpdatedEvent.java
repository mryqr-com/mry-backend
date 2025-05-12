package com.mryqr.core.qr.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.QR_DESCRIPTION_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_DESCRIPTION_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrDescriptionUpdatedEvent extends QrUpdatedEvent {

    public QrDescriptionUpdatedEvent(String qrId, String appId, User user) {
        super(QR_DESCRIPTION_UPDATED, qrId, appId, user);
    }
}
