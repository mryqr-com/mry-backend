package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.APP_DELETED;
import static lombok.AccessLevel.PRIVATE;


@Getter
@TypeAlias("APP_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppDeletedEvent extends AppAwareDomainEvent {

    public AppDeletedEvent(String appId, User user) {
        super(APP_DELETED, appId, user);
    }
}
