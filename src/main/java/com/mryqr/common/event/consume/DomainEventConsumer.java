package com.mryqr.common.event.consume;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mryqr.common.utils.CommonUtils.singleParameterizedArgumentClassOf;
import static java.util.Comparator.comparingInt;

// The entry point for handling a domain event, it finds all eligible handlers and call them one by one
// If multiple handlers are eligible for handling one domain event, each handler does its job individually
// without been impacted by exceptions thrown from other handlers

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventConsumer<T> {
    private final Map<String, Class<?>> handlerEventClassMap = new ConcurrentHashMap<>();

    private final List<DomainEventHandler<T>> handlers;

    public void consume(ConsumingDomainEvent<T> consumingDomainEvent) {
        log.debug("Start consume domain event[{}:{}].", consumingDomainEvent.getType(), consumingDomainEvent.getEventId());
        this.handlers.stream()
                .filter(handler -> canHandle(handler, consumingDomainEvent.getEvent()))
                .sorted(comparingInt(DomainEventHandler::priority))
                .forEach(handler -> {
                    try {
                        handler.handle(consumingDomainEvent);
                    } catch (Throwable t) {
                        log.error("Error occurred while handling domain event[{}:{}] by {}.",
                                consumingDomainEvent.getType(), consumingDomainEvent.getEventId(), handler.getClass().getName(), t);
                    }
                });
    }

    private boolean canHandle(DomainEventHandler<T> handler, T event) {
        String handlerClassName = handler.getClass().getName();

        if (!this.handlerEventClassMap.containsKey(handlerClassName)) {
            Class<?> handlerEventClass = singleParameterizedArgumentClassOf(handler.getClass());
            this.handlerEventClassMap.put(handlerClassName, handlerEventClass);
        }

        Class<?> finalHandlerEventClass = this.handlerEventClassMap.get(handlerClassName);
        return finalHandlerEventClass != null && finalHandlerEventClass.isAssignableFrom(event.getClass());
    }
}
