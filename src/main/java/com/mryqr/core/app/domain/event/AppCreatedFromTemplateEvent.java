package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.APP_CREATED_FROM_TEMPLATE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("APP_CREATED_FROM_TEMPLATE_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppCreatedFromTemplateEvent extends AppAwareDomainEvent {
    private String appTemplateId;
    private String sourceAppId;

    public AppCreatedFromTemplateEvent(String appTemplateId, String sourceAppId, String appId, User user) {
        super(APP_CREATED_FROM_TEMPLATE, appId, user);
        this.appTemplateId = appTemplateId;
        this.sourceAppId = sourceAppId;
    }
}
