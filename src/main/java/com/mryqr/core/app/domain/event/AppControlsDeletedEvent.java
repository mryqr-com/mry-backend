package com.mryqr.core.app.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.core.common.domain.event.DomainEventType.CONTROLS_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("CONTROLS_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppControlsDeletedEvent extends AppAwareDomainEvent {
    private Set<DeletedControlInfo> controls;

    public AppControlsDeletedEvent(String appId, Set<DeletedControlInfo> controls, User user) {
        super(CONTROLS_DELETED, appId, user);
        this.controls = controls;
    }
}
