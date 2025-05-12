package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.common.event.DomainEventType.APP_ATTRIBUTES_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("APP_ATTRIBUTES_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppAttributesDeletedEvent extends AppAwareDomainEvent {
    private Set<DeletedAttributeInfo> attributes;

    public AppAttributesDeletedEvent(String appId, Set<DeletedAttributeInfo> attributes, User user) {
        super(APP_ATTRIBUTES_DELETED, appId, user);
        this.attributes = attributes;
    }
}
