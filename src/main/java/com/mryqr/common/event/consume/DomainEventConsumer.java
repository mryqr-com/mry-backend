package com.mryqr.common.event.consume;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final Map<String, Class<?>> handlerToEventMap = new ConcurrentHashMap<>();

    private final List<AbstractDomainEventHandler<T>> handlers;

    private final ConsumingDomainEventDao<T> consumingDomainEventDao;

    private final TransactionTemplate transactionTemplate;

    public void consume(ConsumingDomainEvent<T> consumingDomainEvent) {
        log.debug("Start consume domain event[{}:{}].", consumingDomainEvent.getType(), consumingDomainEvent.getEventId());
        this.handlers.stream()
                .filter(handler -> canHandle(handler, consumingDomainEvent.getEvent()))
                .sorted(comparingInt(AbstractDomainEventHandler::priority))
                .forEach(handler -> {
                    try {
                        if (handler.isTransactional()) {
                            this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                                @Override
                                protected void doInTransactionWithoutResult(TransactionStatus status) {
                                    recordAndHandle(handler, consumingDomainEvent);
                                }
                            });
                        } else {
                            recordAndHandle(handler, consumingDomainEvent);
                        }
                    } catch (Throwable t) {
                        log.error("Error occurred while handling domain event[{}:{}] by {}.",
                                consumingDomainEvent.getType(), consumingDomainEvent.getEventId(), handler.getClass().getName(), t);
                    }
                });
    }

    private void recordAndHandle(AbstractDomainEventHandler<T> handler, ConsumingDomainEvent<T> consumingDomainEvent) {
        if (handler.isIdempotent() || this.consumingDomainEventDao.recordAsConsumed(consumingDomainEvent, handler.getClass().getName())) {
            handler.handle(consumingDomainEvent.getEvent());
        } else {
            log.warn("Domain event[{}:{}] has already been consumed by handler[{}], skip handling.",
                    consumingDomainEvent.getEventId(), consumingDomainEvent.getType(), handler.getClass().getName());
        }
    }

    private boolean canHandle(AbstractDomainEventHandler<T> handler, T event) {
        String handlerClassName = handler.getClass().getName();

        if (!this.handlerToEventMap.containsKey(handlerClassName)) {
            Class<?> handlerEventClass = singleParameterizedArgumentClassOf(handler.getClass());
            this.handlerToEventMap.put(handlerClassName, handlerEventClass);
        }

        Class<?> finalHandlerEventClass = this.handlerToEventMap.get(handlerClassName);
        return finalHandlerEventClass != null && finalHandlerEventClass.isAssignableFrom(event.getClass());
    }
}
