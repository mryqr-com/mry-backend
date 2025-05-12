package com.mryqr.common.event.publish;

import com.mryqr.common.event.DomainEventJobs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
@Profile("!ci")
@RequiredArgsConstructor
public class AsynchronousDomainEventPublisher implements DomainEventPublisher {
    private final TaskExecutor taskExecutor;
    private final DomainEventJobs domainEventJobs;

    @Override
    public void publish(List<String> eventIds) {
        if (isNotEmpty(eventIds)) {
            taskExecutor.execute(domainEventJobs::publishDomainEvents);
        }
    }

}
