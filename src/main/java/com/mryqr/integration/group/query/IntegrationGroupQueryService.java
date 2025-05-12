package com.mryqr.integration.group.query;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.common.utils.MryConstants.GROUP_COLLECTION;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class IntegrationGroupQueryService {
    private final GroupRepository groupRepository;
    private final MryRateLimiter mryRateLimiter;
    private final MongoTemplate mongoTemplate;

    public QIntegrationGroup fetchGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Fetch", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        return transform(group);
    }

    public QIntegrationGroup fetchGroupByCustomId(String appId, String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:Fetch", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        return transform(group);
    }

    public List<QIntegrationListGroup> listGroups(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:List", 10);

        Query query = Query.query(where("appId").is(appId)).with(by(DESC, "createdAt"));
        query.fields().include("appId", "name", "customId", "archived", "active");

        return mongoTemplate.find(query, QIntegrationListGroup.class, GROUP_COLLECTION);
    }

    private QIntegrationGroup transform(Group group) {
        return QIntegrationGroup.builder()
                .id(group.getId())
                .name(group.getName())
                .customId(group.getCustomId())
                .appId(group.getAppId())
                .managers(group.getManagers())
                .members(group.getMembers())
                .archived(group.isArchived())
                .active(group.isActive())
                .createdAt(group.getCreatedAt())
                .createdBy(group.getCreatedBy())
                .build();
    }
}
