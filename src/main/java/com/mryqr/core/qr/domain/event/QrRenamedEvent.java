package com.mryqr.core.qr.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.QR_RENAMED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_RENAMED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrRenamedEvent extends QrUpdatedEvent {
    private String name;

    public QrRenamedEvent(String qrId, String appId, String name, User user) {
        super(QR_RENAMED, qrId, appId, user);
        this.name = name;
    }
}
