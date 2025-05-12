package com.mryqr.common.event.publish;

import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import static com.mryqr.common.event.publish.DomainEventPublishStatus.CREATED;
import static com.mryqr.common.utils.MryConstants.PUBLISHING_DOMAIN_EVENT_COLLECTION;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

// Wrapper for DomainEvent for publishing, added status and publishCount to track the publishing process
@Getter
@FieldNameConstants
@NoArgsConstructor(access = PRIVATE)
@Document(PUBLISHING_DOMAIN_EVENT_COLLECTION)
@TypeAlias("PUBLISHING_DOMAIN_EVENT")
public class PublishingDomainEvent {

    private String id;
    private DomainEvent event;

    private DomainEventPublishStatus status;

    private int publishedCount;

    private Instant raisedAt;

    public PublishingDomainEvent(DomainEvent event) {
        requireNonNull(event, "Domain event must not be null.");

        this.id = event.getId();
        this.event = event;
        this.status = CREATED;
        this.publishedCount = 0;
        this.raisedAt = event.getRaisedAt();
    }
}
