package com.mryqr.core.assignment.domain;

import com.mryqr.common.domain.user.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository {
    Optional<Assignment> latestForGroup(String groupId);

    boolean exists(String assignmentPlanId, String groupId, LocalDateTime startAt);

    void save(Assignment it);

    void delete(Assignment it);

    Assignment byId(String id);

    Assignment byIdAndCheckTenantShip(String id, User user);

    boolean exists(String arId);

    List<Assignment> openAssignmentsFor(String qrId, String appId, String pageId);

    List<String> cachedOpenAssignmentPages(String appId);

    List<Assignment> expiredAssignments(Instant expireTime, String startId, int batchSize);

    List<Assignment> nearExpiredAssignments(Instant nearExpireTime, String startId, int batchSize);

    int removeAssignmentsUnderPage(String pageId, String appId);

    int removeAssignmentsUnderApp(String appId);

    int removeAssignmentsUnderAssignmentPlan(String assignmentPlanId);

    int removeAssignmentsUnderGroup(String groupId);

    int removeOperatorFromAllAssignments(String memberId, String tenantId);
}
