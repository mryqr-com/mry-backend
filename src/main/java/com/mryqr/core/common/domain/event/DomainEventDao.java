package com.mryqr.core.common.domain.event;

import java.util.List;

public interface DomainEventDao {
    void insert(List<DomainEvent> events);

    DomainEvent byId(String id);

    List<DomainEvent> byIds(List<String> ids);

    <T extends DomainEvent> T latestEventFor(String arId, DomainEventType type, Class<T> eventClass);

    void successPublish(DomainEvent event);

    void failPublish(DomainEvent event);

    void successConsume(DomainEvent event);

    void failConsume(DomainEvent event);

    List<DomainEvent> tobePublishedEvents(String startId, int limit);
}
