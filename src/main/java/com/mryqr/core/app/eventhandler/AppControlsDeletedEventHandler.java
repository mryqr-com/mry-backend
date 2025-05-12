package com.mryqr.core.app.eventhandler;

import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.event.AppControlsDeletedEvent;
import com.mryqr.core.app.domain.event.DeletedControlInfo;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.submission.domain.task.RemoveAnswersForControlsFromAllSubmissionsTask;
import com.mryqr.core.submission.domain.task.RemoveIndexedValueFromAllSubmissionsTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.domain.event.DomainEventType.CONTROLS_DELETED;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppControlsDeletedEventHandler implements DomainEventHandler {
    private final AppRepository appRepository;
    private final RemoveAnswersForControlsFromAllSubmissionsTask removeAnswersForControlsFromAllSubmissionsTask;
    private final RemoveIndexedValueFromAllSubmissionsTask removeIndexedValueFromAllSubmissionsTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == CONTROLS_DELETED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        AppControlsDeletedEvent theEvent = (AppControlsDeletedEvent) domainEvent;
        String appId = theEvent.getAppId();
        Set<DeletedControlInfo> fillableControls = theEvent.getControls().stream()
                .filter(info -> info.getControlType().isFillable())
                .collect(toImmutableSet());

        Set<String> fillableControlIds = fillableControls.stream()
                .map(DeletedControlInfo::getControlId)
                .collect(toImmutableSet());
        taskRunner.run(() -> removeAnswersForControlsFromAllSubmissionsTask.run(fillableControlIds, appId));

        Set<DeletedControlInfo> valueIndexableControls = fillableControls.stream()
                .filter(info -> info.getControlType().isAnswerIndexable())
                .collect(toImmutableSet());

        if (isNotEmpty(valueIndexableControls)) {
            appRepository.byIdOptional(appId).ifPresent(app -> valueIndexableControls.forEach(info -> {
                if (!app.hasControlIndexField(info.getPageId(), info.getIndexedField())) {//如果字段尚未被别的属性占用，则直接删除
                    taskRunner.run(() -> removeIndexedValueFromAllSubmissionsTask.run(info.getIndexedField(),
                            info.getPageId(),
                            appId));
                } else {//否则需要加上controlId作为筛选条件
                    taskRunner.run(() -> removeIndexedValueFromAllSubmissionsTask.run(info.getIndexedField(),
                            info.getControlId(),
                            info.getPageId(),
                            appId));
                }
            }));
        }
    }
}
