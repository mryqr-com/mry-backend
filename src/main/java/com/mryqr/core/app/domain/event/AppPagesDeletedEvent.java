package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.page.PageInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.common.event.DomainEventType.APP_PAGES_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("APP_PAGES_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppPagesDeletedEvent extends AppAwareDomainEvent {
    private Set<PageInfo> pages;

    public AppPagesDeletedEvent(String appId, Set<PageInfo> pages, User user) {
        super(APP_PAGES_DELETED, appId, user);
        this.pages = pages;
    }
}
