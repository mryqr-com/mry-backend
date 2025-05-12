package com.mryqr.core.app.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.core.common.domain.event.DomainEventType.ATTRIBUTES_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ATTRIBUTES_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppAttributesDeletedEvent extends AppAwareDomainEvent {
    private Set<DeletedAttributeInfo> attributes;

    public AppAttributesDeletedEvent(String appId, Set<DeletedAttributeInfo> attributes, User user) {
        super(ATTRIBUTES_DELETED, appId, user);
        this.attributes = attributes;
    }
}
