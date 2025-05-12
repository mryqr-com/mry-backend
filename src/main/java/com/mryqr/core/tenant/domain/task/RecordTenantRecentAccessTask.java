package com.mryqr.core.tenant.domain.task;

import com.mryqr.common.domain.task.NonRetryableTask;
import com.mryqr.core.tenant.domain.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.mryqr.core.tenant.domain.Tenant.Fields.recentAccessedAt;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordTenantRecentAccessTask implements NonRetryableTask {
    private final MongoTemplate mongoTemplate;
    private final SyncTenantToManagedQrTask syncTenantToManagedQrTask;

    public void run(String tenantId) {
        try {
            Update update = new Update();
            update.set(recentAccessedAt, Instant.now());
            mongoTemplate.updateFirst(Query.query(where("_id").is(tenantId)), update, Tenant.class);
            syncTenantToManagedQrTask.sync(tenantId);
        } catch (Throwable t) {
            log.warn("Error while record tenant[{}] recent access time.", tenantId, t);
        }
    }
}
