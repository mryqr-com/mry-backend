package com.mryqr.core.assignment.infrastructure;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.common.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.mryqr.core.assignment.domain.AssignmentStatus.IN_PROGRESS;
import static com.mryqr.core.assignment.domain.AssignmentStatus.NEAR_EXPIRE;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static java.time.ZoneId.systemDefault;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoAssignmentRepository extends MongoBaseRepository<Assignment> implements AssignmentRepository {
    private final MongoCachedAssignmentRepository mongoCachedAssignmentRepository;

    @Override
    public Optional<Assignment> latestForGroup(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        Query query = Query.query(where("groupId").is(groupId)).with(by(DESC, "createdAt"));
        return ofNullable(mongoTemplate.findOne(query, Assignment.class));
    }

    @Override
    public boolean exists(String assignmentPlanId, String groupId, LocalDateTime startAt) {
        requireNonBlank(assignmentPlanId, "Assignment plan ID must not be blank.");
        requireNonBlank(groupId, "Group ID must not be blank.");
        requireNonNull(startAt, "StartAt must not be null.");

        Query query = Query.query(where("groupId").is(groupId)
                .and("assignmentPlanId").is(assignmentPlanId)
                .and("startAt").is(startAt.atZone(systemDefault()).toInstant()));

        return mongoTemplate.exists(query, Assignment.class);
    }

    @Override
    public void save(Assignment it) {
        super.save(it);
    }

    @Override
    public void delete(Assignment it) {
        super.delete(it);
    }

    @Override
    public Assignment byId(String id) {
        return super.byId(id);
    }

    @Override
    public Assignment byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }

    @Override
    public List<Assignment> openAssignmentsFor(String qrId, String appId, String pageId) {
        requireNonBlank(qrId, "QR ID must not be blank.");
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");

        Query query = Query.query(where("appId").is(appId)
                .and("pageId").is(pageId)
                .and("status").in(IN_PROGRESS, NEAR_EXPIRE)
                .and("allQrIds").is(qrId));

        return mongoTemplate.find(query, Assignment.class);
    }

    @Override
    public List<String> cachedOpenAssignmentPages(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        return mongoCachedAssignmentRepository.cachedOpenAssignmentPages(appId);
    }

    @Override
    public List<Assignment> expiredAssignments(Instant expireTime, String startId, int batchSize) {
        requireNonNull(expireTime, "Expire time must not be null.");
        requireNonBlank(startId, "Start ID must not be blank.");

        Query query = Query.query(where("expireAt").lte(expireTime)
                        .and("status").in(IN_PROGRESS, NEAR_EXPIRE)
                        .and("_id").lt(startId))
                .with(by(DESC, "_id"))
                .limit(batchSize);

        return mongoTemplate.find(query, Assignment.class);
    }

    @Override
    public List<Assignment> nearExpiredAssignments(Instant nearExpireTime, String startId, int batchSize) {
        requireNonNull(nearExpireTime, "Near expire time must not be null.");
        requireNonBlank(startId, "Start ID must not be blank.");

        Query query = Query.query(where("nearExpireNotifyAt").is(nearExpireTime)
                        .and("status").is(IN_PROGRESS)
                        .and("_id").lt(startId))
                .with(by(DESC, "_id"))
                .limit(batchSize);

        return mongoTemplate.find(query, Assignment.class);
    }

    @Override
    public int removeAssignmentsUnderPage(String pageId, String appId) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");

        Query query = query(where("appId").is(appId).and("pageId").is(pageId));
        return (int) mongoTemplate.remove(query, Assignment.class).getDeletedCount();
    }

    @Override
    public int removeAssignmentsUnderApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId));
        DeleteResult result = mongoTemplate.remove(query, Assignment.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeAssignmentsUnderAssignmentPlan(String assignmentPlanId) {
        requireNonBlank(assignmentPlanId, "Assignment plan ID must not be blank.");

        Query query = query(where("assignmentPlanId").is(assignmentPlanId));
        DeleteResult result = mongoTemplate.remove(query, Assignment.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeAssignmentsUnderGroup(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        Query query = query(where("groupId").is(groupId));
        DeleteResult result = mongoTemplate.remove(query, Assignment.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeOperatorFromAllAssignments(String memberId, String tenantId) {
        requireNonBlank(memberId, "Member ID must not be blank.");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("operators").is(memberId));
        Update update = new Update().pull("operators", memberId);
        UpdateResult result = mongoTemplate.updateMulti(query, update, Assignment.class);
        return (int) result.getModifiedCount();
    }
}
