package com.mryqr.common.event.publish;

import java.util.List;

public interface DomainEventPublisher {
    void publish(List<String> eventIds);
}
