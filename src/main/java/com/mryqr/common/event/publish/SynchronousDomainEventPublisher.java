package com.mryqr.common.event.publish;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventConsumer;
import com.mryqr.core.common.domain.event.DomainEventDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("ci")
@RequiredArgsConstructor
public class SynchronousDomainEventPublisher implements DomainEventPublisher {
    private final DomainEventDao domainEventDao;
    private final DomainEventConsumer domainEventConsumer;

    @Override
    public void publish(List<String> eventIds) {
        List<DomainEvent> domainEvents = domainEventDao.byIds(eventIds);
        domainEvents.forEach(domainEvent -> {
            try {
                domainEventConsumer.consume(domainEvent);
            } catch (Throwable t) {
                log.error("Consume domain event[{}:{}] failed.", domainEvent.getType(), domainEvent.getId(), t);
            }
        });
    }

}
