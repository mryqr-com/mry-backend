package com.mryqr.core.kanban.query;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.permission.AppOperatePermissionChecker;
import com.mryqr.core.common.domain.permission.AppOperatePermissions;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.utils.MongoCriteriaUtils.mongoTextFieldOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class KanbanQueryService {
    private final AppRepository appRepository;
    private final AppOperatePermissionChecker appOperatePermissionChecker;
    private final MongoTemplate mongoTemplate;
    private final MryRateLimiter mryRateLimiter;
    private final TenantRepository tenantRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;

    public QAttributeKanban fetchKanban(FetchKanbanQuery queryCommand, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Kanban:Fetch", 20);

        Tenant tenant = tenantRepository.cachedByIdAndCheckTenantShip(user.getTenantId(), user);
        tenant.packagesStatus().validateKanban();

        App app = appRepository.cachedByIdAndCheckTenantShip(queryCommand.getAppId(), user);
        AppOperatePermissions appOperatePermissions = appOperatePermissionChecker.permissionsFor(user, app);

        Criteria criteria = where("appId").is(queryCommand.getAppId());
        String groupId = queryCommand.getGroupId();
        if (isBlank(groupId)) {
            appOperatePermissions.checkHasViewableGroups();
            criteria.and("groupId").in(appOperatePermissions.getViewableGroupIds());
        } else {
            appOperatePermissions.checkViewableGroupPermission(groupId);

            GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(app.getId());
            Set<String> withAllSubGroupIds = groupHierarchy.withAllSubGroupIdsOf(groupId);
            Set<String> viewableGroupIds = appOperatePermissions.getViewableGroupIds();
            Set<String> resultViewableGroupIds = withAllSubGroupIds.stream().filter(viewableGroupIds::contains).collect(toImmutableSet());
            criteria.and("groupId").in(resultViewableGroupIds);
        }

        String optionField = mongoTextFieldOf(app.indexedFieldForAttribute(queryCommand.getAttributeId()));
        Aggregation aggregation = newAggregation(
                match(criteria),
                unwind(optionField),
                project().and(optionField).as("value"),
                group("value").count().as("count"),
                project("count").and("_id").as("optionId")
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        List<QAttributeOptionCount> results = mongoTemplate.aggregate(aggregation, QR.class, QAttributeOptionCount.class)
                .getMappedResults();

        return QAttributeKanban.builder().counts(results).build();
    }
}
