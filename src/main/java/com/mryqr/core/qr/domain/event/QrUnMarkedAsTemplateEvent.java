package com.mryqr.core.qr.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.QR_UNMARKED_AS_TEMPLATE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("QR_UNMARKED_AS_TEMPLATE_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class QrUnMarkedAsTemplateEvent extends QrUpdatedEvent {

    public QrUnMarkedAsTemplateEvent(String qrId, String appId, User user) {
        super(QR_UNMARKED_AS_TEMPLATE, qrId, appId, user);
    }
}
