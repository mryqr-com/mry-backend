package com.mryqr.core.departmenthierarchy.eventhandler;


import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.TenantCachedApp;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.departmenthierarchy.domain.event.DepartmentHierarchyChangedEvent;
import com.mryqr.core.group.domain.task.SyncAllDepartmentsToGroupTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.DEPARTMENT_HIERARCHY_CHANGED;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentHierarchyChangedEventHandler implements DomainEventHandler {
    private final SyncAllDepartmentsToGroupTask syncAllDepartmentsToGroupTask;
    private final AppRepository appRepository;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == DEPARTMENT_HIERARCHY_CHANGED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        DepartmentHierarchyChangedEvent theEvent = (DepartmentHierarchyChangedEvent) domainEvent;

        appRepository.cachedTenantAllApps(theEvent.getTenantId()).stream()
                .filter(TenantCachedApp::isGroupSynced)
                .forEach(app -> taskRunner.run(() -> syncAllDepartmentsToGroupTask.run(app.getId())));
    }
}
