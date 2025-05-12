package com.mryqr.core.qr.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.QR_HEADER_IMAGE_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_HEADER_IMAGE_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrHeaderImageUpdatedEvent extends QrUpdatedEvent {

    public QrHeaderImageUpdatedEvent(String qrId, String appId, User user) {
        super(QR_HEADER_IMAGE_UPDATED, qrId, appId, user);
    }
}
