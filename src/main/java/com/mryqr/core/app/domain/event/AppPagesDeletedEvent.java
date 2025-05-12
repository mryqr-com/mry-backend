package com.mryqr.core.app.domain.event;

import com.mryqr.core.app.domain.page.PageInfo;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.core.common.domain.event.DomainEventType.PAGES_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("PAGES_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppPagesDeletedEvent extends AppAwareDomainEvent {
    private Set<PageInfo> pages;

    public AppPagesDeletedEvent(String appId, Set<PageInfo> pages, User user) {
        super(PAGES_DELETED, appId, user);
        this.pages = pages;
    }
}
