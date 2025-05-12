package com.mryqr.core.app.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.core.common.domain.event.DomainEventType.PAGE_CHANGED_TO_SUBMIT_PER_INSTANCE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("PAGE_CHANGED_TO_SUBMIT_PER_INSTANCE_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class PageChangedToSubmitPerInstanceEvent extends AppAwareDomainEvent {
    private Set<String> pageIds;

    public PageChangedToSubmitPerInstanceEvent(String appId, Set<String> pageIds, User user) {
        super(PAGE_CHANGED_TO_SUBMIT_PER_INSTANCE, appId, user);
        this.pageIds = pageIds;
    }
}
