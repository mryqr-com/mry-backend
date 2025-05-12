package com.mryqr.common.event.publish;

import com.mryqr.common.event.DomainEvent;

import java.util.List;

public interface PublishingDomainEventDao {
    void stage(List<DomainEvent> events);

    List<DomainEvent> stagedEvents(String startId, int limit);

    List<DomainEvent> byIds(List<String> ids);

    void successPublish(String eventId);

    void failPublish(String eventId);
}
