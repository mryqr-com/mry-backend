package com.mryqr.core.app.eventhandler;

import com.mryqr.core.app.domain.event.AppControlOptionsDeletedEvent;
import com.mryqr.core.app.domain.event.DeletedTextOptionInfo;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.task.RemoveIndexedOptionUnderAllQrsTask;
import com.mryqr.core.submission.domain.task.RemoveSubmissionIndexedOptionForAppTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.mryqr.core.common.domain.event.DomainEventType.CONTROL_OPTIONS_DELETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppControlOptionsDeletedEventHandler implements DomainEventHandler {
    private final RemoveSubmissionIndexedOptionForAppTask removeSubmissionIndexedOptionForAppTask;
    private final RemoveIndexedOptionUnderAllQrsTask removeIndexedOptionUnderAllQrsTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == CONTROL_OPTIONS_DELETED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        AppControlOptionsDeletedEvent theEvent = (AppControlOptionsDeletedEvent) domainEvent;
        String appId = theEvent.getAppId();
        Set<DeletedTextOptionInfo> options = theEvent.getControlOptions();
        options.forEach(option ->
                taskRunner.run(() -> removeSubmissionIndexedOptionForAppTask.run(appId,
                        option.getPageId(),
                        option.getControlId(),
                        option.getOptionId())));

        options.forEach(option ->
                taskRunner.run(() -> removeIndexedOptionUnderAllQrsTask.run(appId,
                        option.getControlId(),
                        option.getOptionId())));

        //Nice to have: 考虑从submission中删除对应answer中的option，考虑从qr中删除对应attribute中的option
    }

}
