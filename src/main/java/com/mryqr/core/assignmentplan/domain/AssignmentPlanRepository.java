package com.mryqr.core.assignmentplan.domain;

import com.mryqr.core.common.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface AssignmentPlanRepository {
    List<AssignmentPlan> allAssignmentPlansOf(String tenantId);

    boolean existsByName(String name, String appId);

    int assignmentPlanCount(String appId);

    AssignmentPlan byId(String id);

    AssignmentPlan byIdAndCheckTenantShip(String id, User user);

    boolean exists(String arId);

    void save(AssignmentPlan it);

    void delete(AssignmentPlan it);

    List<AssignmentPlan> find(LocalDateTime startTime, String startId, int size);

    int removeAllAssignmentPlansUnderPage(String pageId, String appId);

    int removeAllAssignmentPlansUnderApp(String appId);

    int removeGroupFromAssignmentPlanExcludedGroups(String groupId, String appId);

    int removeGroupFromAssignmentPlanGroupOperators(String groupId, String appId);

}
