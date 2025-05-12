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
// Can add more information(such as if the event is redelivered etc.) if required, but should not be coupled to a specific messaging middleware
@Getter
@FieldNameConstants
@NoArgsConstructor(access = PRIVATE)
@Document(CONSUMING_DOMAIN_EVENT_COLLECTION)
@TypeAlias("CONSUMING_DOMAIN_EVENT")
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
        this.consumedAt = Instant.now();
        this.event = event;
    }
}
