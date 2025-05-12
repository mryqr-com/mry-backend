package com.mryqr.core.common.domain.event;

import com.mryqr.core.common.utils.MryTaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.common.utils.MryTaskRunner.newTaskRunner;
import static java.util.Comparator.comparingInt;

@Slf4j
@Component
public class DomainEventConsumer {
    private final List<DomainEventHandler> handlers;
    private final DomainEventDao domainEventDao;

    public DomainEventConsumer(List<DomainEventHandler> handlers, DomainEventDao domainEventDao) {
        this.handlers = handlers;
        this.handlers.sort(comparingInt(DomainEventHandler::priority));
        this.domainEventDao = domainEventDao;
    }

    //所有能处理事件的handler依次处理，全部处理成功记录消费成功，否则记录为消费失败；
    //消费失败后，兜底机制将重新发送事件，重新发送最多不超过3次
    public void consume(DomainEvent domainEvent) {
        log.info("Start consume domain event[{}:{}].", domainEvent.getType(), domainEvent.getId());

        boolean hasError = false;
        MryTaskRunner taskRunner = newTaskRunner();

        for (DomainEventHandler handler : handlers) {
            try {
                if (handler.canHandle(domainEvent)) {
                    handler.handle(domainEvent, taskRunner);
                }
            } catch (Throwable t) {
                hasError = true;
                log.error("Error while handle domain event[{}:{}] by [{}].",
                        domainEvent.getType(), domainEvent.getId(), handler.getClass().getSimpleName(), t);
            }
        }

        if (taskRunner.isHasError()) {
            hasError = true;
        }

        if (hasError) {
            domainEventDao.failConsume(domainEvent);
        } else {
            domainEventDao.successConsume(domainEvent);
        }
    }

}
