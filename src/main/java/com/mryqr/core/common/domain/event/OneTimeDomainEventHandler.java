package com.mryqr.core.common.domain.event;

import com.mryqr.core.common.utils.MryTaskRunner;
import lombok.extern.slf4j.Slf4j;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;


@Slf4j
public abstract class OneTimeDomainEventHandler implements DomainEventHandler {
    private static final int VALID_HOURS = 48;

    @Override
    public int priority() {
        return 1000;//一次性handler默认优先级不高，因为通常用于处理notification等
    }

    @Override
    public final void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        if (domainEvent.isConsumedBefore()) {//如果事件已经被消费，无论是否消费成功，均不再进行处理
            log.warn("Domain event[{}:{}] is consumed before, skip consuming.", domainEvent.getType(), domainEvent.getId());
            return;
        }

        if (domainEvent.getRaisedAt().isBefore(now().minus(VALID_HOURS, HOURS))) {//超过时限则不再处理
            log.warn("Domain event[{}:{}] is more than {} hours old, skip.", domainEvent.getType(), domainEvent.getId(), VALID_HOURS);
            return;
        }

        try {
            doHandle(domainEvent);
        } catch (Throwable t) {//如果发生异常，自行吞掉，也即不会触发消息的兜底重发，因为即便重发了，由于先前已经处理过(isConsumedBefore)，也不会被处理
            log.error("Error while handle domain event[{}:{}].", domainEvent.getType(), domainEvent.getId(), t);
        }
    }

    protected abstract void doHandle(DomainEvent domainEvent);

}
