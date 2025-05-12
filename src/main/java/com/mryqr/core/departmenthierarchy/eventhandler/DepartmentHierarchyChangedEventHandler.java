package com.mryqr.core.departmenthierarchy.eventhandler;


import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.TenantCachedApp;
import com.mryqr.core.departmenthierarchy.domain.event.DepartmentHierarchyChangedEvent;
import com.mryqr.core.group.domain.task.SyncAllDepartmentsToGroupTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentHierarchyChangedEventHandler extends AbstractDomainEventHandler<DepartmentHierarchyChangedEvent> {
    private final SyncAllDepartmentsToGroupTask syncAllDepartmentsToGroupTask;
    private final AppRepository appRepository;

    @Override
    protected void doHandle(DepartmentHierarchyChangedEvent event) {
        appRepository.cachedTenantAllApps(event.getTenantId()).stream()
                .filter(TenantCachedApp::isGroupSynced)
                .forEach(app -> MryTaskRunner.run(() -> syncAllDepartmentsToGroupTask.run(app.getId())));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
