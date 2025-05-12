package com.mryqr.core.qr.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.QR_PLATE_RESET;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_PLATE_RESET_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrPlateResetEvent extends QrUpdatedEvent {
    private String oldPlateId;
    private String newPlateId;

    public QrPlateResetEvent(String qrId, String appId, String oldPlateId, String newPlateId, User user) {
        super(QR_PLATE_RESET, qrId, appId, user);
        this.oldPlateId = oldPlateId;
        this.newPlateId = newPlateId;
    }
}
