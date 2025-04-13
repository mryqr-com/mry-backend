package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.common.event.DomainEventType.APP_PAGE_CHANGED_TO_SUBMIT_PER_INSTANCE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("APP_PAGE_CHANGED_TO_SUBMIT_PER_INSTANCE_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppPageChangedToSubmitPerInstanceEvent extends AppAwareDomainEvent {
    private Set<String> pageIds;

    private String appVersion;

    public AppPageChangedToSubmitPerInstanceEvent(String appId, Set<String> pageIds, String appVersion, User user) {
        super(APP_PAGE_CHANGED_TO_SUBMIT_PER_INSTANCE, appId, user);
        this.pageIds = pageIds;
        this.appVersion = appVersion;
    }
}
