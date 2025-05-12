package com.mryqr.core.app.eventhandler;

import com.mryqr.core.app.domain.attribute.AttributeInfo;
import com.mryqr.core.app.domain.event.AppAttributesCreatedEvent;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForAllQrsUnderAppTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.domain.event.DomainEventType.ATTRIBUTES_CREATED;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.SetUtils.emptyIfNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppAttributesCreatedEventHandler implements DomainEventHandler {
    private final SyncAttributeValuesForAllQrsUnderAppTask syncAttributeValuesForAllQrsUnderAppTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == ATTRIBUTES_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        AppAttributesCreatedEvent event = (AppAttributesCreatedEvent) domainEvent;
        Set<String> calculatedAttributeIds = emptyIfNull(event.getAttributes()).stream()
                .filter(it -> it.getAttributeType().isValueCalculated())
                .map(AttributeInfo::getAttributeId)
                .filter(Objects::nonNull)
                .collect(toImmutableSet());

        if (isNotEmpty(calculatedAttributeIds)) {
            taskRunner.run(() -> syncAttributeValuesForAllQrsUnderAppTask.run(event.getAppId(), calculatedAttributeIds));
        }
    }
}
