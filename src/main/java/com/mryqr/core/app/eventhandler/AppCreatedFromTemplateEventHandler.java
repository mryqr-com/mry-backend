package com.mryqr.core.app.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.app.domain.event.AppCreatedFromTemplateEvent;
import com.mryqr.core.appmanual.domain.task.CloneAppManualTask;
import com.mryqr.core.apptemplate.domain.task.CountAppTemplateAppliedTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppCreatedFromTemplateEventHandler extends AbstractDomainEventHandler<AppCreatedFromTemplateEvent> {
    private final CountAppTemplateAppliedTask countAppTemplateAppliedTask;
    private final CloneAppManualTask cloneAppManualTask;

    @Override
    protected void doHandle(AppCreatedFromTemplateEvent event) {
        MryTaskRunner.run(() -> countAppTemplateAppliedTask.run(event.getAppTemplateId()));
        MryTaskRunner.run(() -> cloneAppManualTask.run(event.getSourceAppId(), event.getAppId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
