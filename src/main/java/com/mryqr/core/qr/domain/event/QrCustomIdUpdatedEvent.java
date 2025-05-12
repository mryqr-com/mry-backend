package com.mryqr.core.qr.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.QR_CUSTOM_ID_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_CUSTOM_ID_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrCustomIdUpdatedEvent extends QrUpdatedEvent {
    private String customId;

    public QrCustomIdUpdatedEvent(String qrId, String appId, String customId, User user) {
        super(QR_CUSTOM_ID_UPDATED, qrId, appId, user);
        this.customId = customId;
    }
}
