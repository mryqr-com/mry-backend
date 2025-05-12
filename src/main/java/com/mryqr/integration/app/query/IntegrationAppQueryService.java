package com.mryqr.integration.app.query;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.common.utils.MryConstants.APP_COLLECTION;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class IntegrationAppQueryService {
    private final AppRepository appRepository;
    private final MryRateLimiter mryRateLimiter;
    private final MongoTemplate mongoTemplate;

    public List<QIntegrationListApp> listApps(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:App:List", 10);

        Query query = Query.query(where("tenantId").is(user.getTenantId())).with(by(DESC, "createdAt"));
        query.fields().include("name", "active", "locked", "version", "permission", "operationPermission");

        return mongoTemplate.find(query, QIntegrationListApp.class, APP_COLLECTION);
    }

    public QIntegrationApp fetchApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:App:Fetch", 10);

        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);

        return QIntegrationApp.builder()
                .id(app.getId())
                .name(app.getName())
                .icon(app.getIcon())
                .active(app.isActive())
                .locked(app.isLocked())
                .version(app.getVersion())
                .setting(app.getSetting())
                .permission(app.getPermission())
                .operationPermission(app.getOperationPermission())
                .createdAt(app.getCreatedAt())
                .createdBy(app.getCreatedBy())
                .build();
    }
}
