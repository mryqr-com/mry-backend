package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.common.event.DomainEventType.APP_PAGE_CHANGED_TO_SUBMIT_PER_MEMBER;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("APP_PAGE_CHANGED_TO_SUBMIT_PER_MEMBER_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppPageChangedToSubmitPerMemberEvent extends AppAwareDomainEvent {
    private Set<String> pageIds;

    private String appVersion;

    public AppPageChangedToSubmitPerMemberEvent(String appId, Set<String> pageIds, String appVersion, User user) {
        super(APP_PAGE_CHANGED_TO_SUBMIT_PER_MEMBER, appId, user);
        this.pageIds = pageIds;
        this.appVersion = appVersion;
    }
}
