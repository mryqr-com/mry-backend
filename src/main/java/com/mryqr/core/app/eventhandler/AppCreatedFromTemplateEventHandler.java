package com.mryqr.core.app.eventhandler;

import com.mryqr.core.app.domain.event.AppCreatedFromTemplateEvent;
import com.mryqr.core.appmanual.domain.task.CloneAppManualTask;
import com.mryqr.core.apptemplate.domain.task.CountAppTemplateAppliedTask;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.APP_CREATED_FROM_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppCreatedFromTemplateEventHandler implements DomainEventHandler {
    private final CountAppTemplateAppliedTask countAppTemplateAppliedTask;
    private final CloneAppManualTask cloneAppManualTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == APP_CREATED_FROM_TEMPLATE;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        AppCreatedFromTemplateEvent theEvent = (AppCreatedFromTemplateEvent) domainEvent;

        taskRunner.run(() -> countAppTemplateAppliedTask.run(theEvent.getAppTemplateId()));
        taskRunner.run(() -> cloneAppManualTask.run(theEvent.getSourceAppId(), theEvent.getAppId()));
    }
}
