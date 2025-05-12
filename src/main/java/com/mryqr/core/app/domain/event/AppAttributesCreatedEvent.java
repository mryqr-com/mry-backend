package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.attribute.AttributeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.common.event.DomainEventType.APP_ATTRIBUTES_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("APP_ATTRIBUTES_CREATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppAttributesCreatedEvent extends AppAwareDomainEvent {
    private Set<AttributeInfo> attributes;

    public AppAttributesCreatedEvent(String appId, Set<AttributeInfo> attributes, User user) {
        super(APP_ATTRIBUTES_CREATED, appId, user);
        this.attributes = attributes;
    }
}
