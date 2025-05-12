package com.mryqr.core.app.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.APP_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("APP_CREATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppCreatedEvent extends AppAwareDomainEvent {

    public AppCreatedEvent(String appId, User user) {
        super(APP_CREATED, appId, user);
    }
}
