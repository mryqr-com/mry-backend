package com.mryqr.core.app.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.app.domain.event.AppControlOptionsDeletedEvent;
import com.mryqr.core.app.domain.event.DeletedTextOptionInfo;
import com.mryqr.core.qr.domain.task.RemoveIndexedOptionUnderAllQrsTask;
import com.mryqr.core.submission.domain.task.RemoveSubmissionIndexedOptionForAppTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppControlOptionsDeletedEventHandler extends AbstractDomainEventHandler<AppControlOptionsDeletedEvent> {
    private final RemoveSubmissionIndexedOptionForAppTask removeSubmissionIndexedOptionForAppTask;
    private final RemoveIndexedOptionUnderAllQrsTask removeIndexedOptionUnderAllQrsTask;

    @Override
    public void handle(AppControlOptionsDeletedEvent event) {
        String appId = event.getAppId();
        Set<DeletedTextOptionInfo> options = event.getControlOptions();
        options.forEach(option ->
                MryTaskRunner.run(() -> removeSubmissionIndexedOptionForAppTask.run(appId,
                        option.getPageId(),
                        option.getControlId(),
                        option.getOptionId())));

        options.forEach(option ->
                MryTaskRunner.run(() -> removeIndexedOptionUnderAllQrsTask.run(appId,
                        option.getControlId(),
                        option.getOptionId())));

        //Nice to have: 考虑从submission中删除对应answer中的option，考虑从qr中删除对应attribute中的option
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
