package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.common.event.DomainEventType.APP_CONTROL_OPTIONS_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("APP_CONTROL_OPTIONS_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppControlOptionsDeletedEvent extends AppAwareDomainEvent {
    private Set<DeletedTextOptionInfo> controlOptions;

    public AppControlOptionsDeletedEvent(String appId, Set<DeletedTextOptionInfo> controlOptions, User user) {
        super(APP_CONTROL_OPTIONS_DELETED, appId, user);
        this.controlOptions = controlOptions;
    }
}
