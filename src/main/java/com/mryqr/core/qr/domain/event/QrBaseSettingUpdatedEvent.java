package com.mryqr.core.qr.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.QR_BASE_SETTING_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_BASE_SETTING_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrBaseSettingUpdatedEvent extends QrUpdatedEvent {

    public QrBaseSettingUpdatedEvent(String qrId, String appId, User user) {
        super(QR_BASE_SETTING_UPDATED, qrId, appId, user);
    }
}
