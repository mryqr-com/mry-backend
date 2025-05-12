package com.mryqr.core.assignmentplan.infrastructure;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.assignment.infrastructure.MongoCachedAssignmentRepository;
import com.mryqr.core.assignmentplan.domain.AssignmentPlan;
import com.mryqr.core.assignmentplan.domain.AssignmentPlanRepository;
import com.mryqr.core.common.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoAssignmentPlanRepository extends MongoBaseRepository<AssignmentPlan> implements AssignmentPlanRepository {
    private final MongoCachedAssignmentRepository mongoCachedAssignmentRepository;

    @Override
    public List<AssignmentPlan> allAssignmentPlansOf(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId));
        return mongoTemplate.find(query, AssignmentPlan.class);
    }

    @Override
    public boolean existsByName(String name, String appId) {
        requireNonBlank(name, "Assignment plan name must not be blank.");
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("setting.appId").is(appId).and("name").is(name));
        return mongoTemplate.exists(query, AssignmentPlan.class);
    }

    @Override
    public int assignmentPlanCount(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("setting.appId").is(appId));
        return (int) mongoTemplate.count(query, AssignmentPlan.class);
    }

    @Override
    public AssignmentPlan byId(String id) {
        return super.byId(id);
    }

    @Override
    public AssignmentPlan byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }

    @Override
    public void save(AssignmentPlan it) {
        super.save(it);
        mongoCachedAssignmentRepository.evictOpenAssignmentPagesCache(it.getAppId());
    }

    @Override
    public void delete(AssignmentPlan it) {
        super.delete(it);
        mongoCachedAssignmentRepository.evictOpenAssignmentPagesCache(it.getAppId());
    }

    @Override
    public List<AssignmentPlan> find(LocalDateTime startTime, String startId, int size) {
        requireNonNull(startTime, "Start time must not be null.");
        requireNonBlank(startId, "Start ID must not be blank.");

        Query query = Query.query(where("setting.startTime.time").is(startTime.format(ofPattern("HH:mm")))
                        .and("active").is(true)
                        .and("_id").lt(startId))
                .with(by(DESC, "_id"))
                .limit(size);

        return mongoTemplate.find(query, AssignmentPlan.class);
    }

    @Override
    public int removeAllAssignmentPlansUnderPage(String pageId, String appId) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");

        Query query = query(where("setting.appId").is(appId).and("setting.pageId").is(pageId));
        DeleteResult result = mongoTemplate.remove(query, AssignmentPlan.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeAllAssignmentPlansUnderApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("setting.appId").is(appId));
        DeleteResult result = mongoTemplate.remove(query, AssignmentPlan.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeGroupFromAssignmentPlanExcludedGroups(String groupId, String appId) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(groupId, "Group ID must not be blank.");

        Query query = query(where("setting.appId").is(appId).and("excludedGroups").is(groupId));
        Update update = new Update().pull("excludedGroups", groupId);
        UpdateResult result = mongoTemplate.updateMulti(query, update, AssignmentPlan.class);

        return (int) result.getModifiedCount();
    }

    @Override
    public int removeGroupFromAssignmentPlanGroupOperators(String groupId, String appId) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(groupId, "Group ID must not be blank.");

        Query query = query(where("setting.appId").is(appId));
        Update update = new Update().unset("groupOperators." + groupId);
        UpdateResult result = mongoTemplate.updateMulti(query, update, AssignmentPlan.class);
        return (int) result.getModifiedCount();
    }

}
