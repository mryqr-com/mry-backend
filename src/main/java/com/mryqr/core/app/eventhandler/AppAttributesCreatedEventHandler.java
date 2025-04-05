package com.mryqr.core.app.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.app.domain.attribute.AttributeInfo;
import com.mryqr.core.app.domain.event.AppAttributesCreatedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForAllQrsUnderAppTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.SetUtils.emptyIfNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppAttributesCreatedEventHandler extends AbstractDomainEventHandler<AppAttributesCreatedEvent> {
    private final SyncAttributeValuesForAllQrsUnderAppTask syncAttributeValuesForAllQrsUnderAppTask;

    @Override
    public void handle(AppAttributesCreatedEvent event) {
        Set<String> calculatedAttributeIds = emptyIfNull(event.getAttributes()).stream()
                .filter(it -> it.getAttributeType().isValueCalculated())
                .map(AttributeInfo::getAttributeId)
                .filter(Objects::nonNull)
                .collect(toImmutableSet());

        if (isNotEmpty(calculatedAttributeIds)) {
            MryTaskRunner.run(() -> syncAttributeValuesForAllQrsUnderAppTask.run(event.getAppId(), calculatedAttributeIds));
        }
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
