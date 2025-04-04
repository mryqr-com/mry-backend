package com.mryqr.common.event.consume;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MryConstants.CONSUMING_DOMAIN_EVENT_COLLECTION;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

// Wrapper for DomainEvent when consuming

// Can add more context information if required (such as if the event is redelivered etc.),
// but should not be coupled to a specific messaging middleware

// Normally should be created as early as when the domain event is delivered from messaging middleware,
// as in such case many context information is available
@Getter
@FieldNameConstants
@NoArgsConstructor(access = PRIVATE)
@TypeAlias("CONSUMING_DOMAIN_EVENT")
@Document(CONSUMING_DOMAIN_EVENT_COLLECTION)
public class ConsumingDomainEvent<T> {

    private String eventId;

    private String type;

    private String handlerName;
    private Instant consumedAt;

    private T event;

    public ConsumingDomainEvent(String eventId, String eventType, T event) {
        requireNonBlank(eventId, "Event ID must not be blank.");
        requireNonBlank(eventType, "Event type must not be blank.");
        requireNonNull(event, "Event must not be null.");

        this.eventId = eventId;
        this.type = eventType;
        this.handlerName = null;
        this.consumedAt = Instant.now();
        this.event = event;
    }
}
