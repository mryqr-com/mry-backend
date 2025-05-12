package com.mryqr.core.assignmentplan.query;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.assignmentplan.domain.AssignmentPlan;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.REQUEST_VALIDATION_FAILED;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.ASSIGNMENT_PLAN_COLLECTION;
import static java.util.Objects.requireNonNull;
import static java.util.Set.copyOf;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentPlanQueryService {
    private final MongoTemplate mongoTemplate;
    private final MryRateLimiter mryRateLimiter;
    private final AppRepository appRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;

    public List<QAssignmentPlan> listAssignmentPlans(String appId, String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AssignmentPlan:List", 10);

        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        if (isBlank(groupId)) {
            managePermissionChecker.checkCanManageApp(user, app);
        } else {
            Group group = groupRepository.cachedByIdAndCheckTenantShip(groupId, user);
            if (!Objects.equals(group.getAppId(), appId)) {
                throw new MryException(REQUEST_VALIDATION_FAILED, app.groupDesignation() + "ID和应用ID不一致。",
                        mapOf("groupId", groupId, "groupAppId", group.getAppId(), "appId", appId));
            }
            managePermissionChecker.checkCanManageGroup(user, group, app);
        }

        Query query = Query.query(where("setting.appId").is(appId)).with(by(DESC, "createdAt"));
        List<AssignmentPlan> assignmentPlans = mongoTemplate.find(query, AssignmentPlan.class);

        if (isBlank(groupId)) {
            return assignmentPlans.stream()
                    .map(toAppLevelQAssignmentPlan())
                    .collect(toImmutableList());
        } else {
            GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(appId);
            return assignmentPlans.stream()
                    .filter(assignmentPlan -> {
                        List<String> excludedGroups = assignmentPlan.getExcludedGroups();
                        Set<String> allExcludedGroupIds = excludedGroups.stream().map(groupHierarchy::withAllSubGroupIdsOf)
                                .flatMap(Collection::stream)
                                .collect(toImmutableSet());
                        return !allExcludedGroupIds.contains(groupId) && assignmentPlan.isActive();
                    }).map(toGroupLevelQAssignmentPlan(groupId))
                    .collect(toImmutableList());
        }
    }

    private Function<AssignmentPlan, QAssignmentPlan> toAppLevelQAssignmentPlan() {
        return plan -> QAssignmentPlan.builder()
                .id(plan.getId())
                .setting(plan.getSetting())
                .name(plan.getName())
                .createdAt(plan.getCreatedAt())
                .creator(plan.getCreator())
                .excludedGroups(plan.getExcludedGroups())
                .nextAssignmentStartAt(plan.nextAssignmentStartAt())
                .active(plan.isActive())
                .build();
    }

    private Function<AssignmentPlan, QAssignmentPlan> toGroupLevelQAssignmentPlan(String groupId) {
        return plan -> {
            List<String> operators = plan.operatorsForGroup(groupId);

            List<String> finalOperatorIds = new ArrayList<>();
            List<String> operatorNames = new ArrayList<>();
            if (isNotEmpty(operators)) {
                Map<String, MemberReference> members = memberRepository.cachedMemberReferences(plan.getTenantId(), copyOf(operators));
                operators.forEach(memberId -> {
                    MemberReference memberReference = members.get(memberId);
                    if (memberReference != null) {
                        finalOperatorIds.add(memberReference.getId());//只有人员的确存在才返回
                        operatorNames.add(memberReference.getName());
                    }
                });
            }

            return QAssignmentPlan.builder()
                    .id(plan.getId())
                    .name(plan.getName())
                    .setting(plan.getSetting())
                    .createdAt(plan.getCreatedAt())
                    .creator(plan.getCreator())
                    .operators(finalOperatorIds)
                    .operatorNames(operatorNames)
                    .nextAssignmentStartAt(plan.nextAssignmentStartAt())
                    .active(plan.isActive())
                    .build();
        };
    }

    public List<QAssignmentPlanSummary> listAssignmentPlanSummaries(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AssignmentPlan:ListSummaries", 10);

        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        requireNonNull(app);

        Query query = Query.query(where("setting.appId").is(appId).and("active").is(true)).with(by(DESC, "createdAt"));
        query.fields().include("name");
        return mongoTemplate.find(query, QAssignmentPlanSummary.class, ASSIGNMENT_PLAN_COLLECTION);
    }

    public List<QAssignmentPlanSummary> listAssignmentPlanSummariesForGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AssignmentPlan:ListSummariesForGroup", 10);

        Group group = groupRepository.cachedByIdAndCheckTenantShip(groupId, user);
        GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(group.getAppId());
        Set<String> withAllParentGroupIds = groupHierarchy.withAllParentGroupIdsOf(groupId);

        Query query = Query.query(where("setting.appId").is(group.getAppId())
                        .and("active").is(true)
                        .and("excludedGroups").nin(withAllParentGroupIds))
                .with(by(DESC, "createdAt"));
        query.fields().include("name");
        return mongoTemplate.find(query, QAssignmentPlanSummary.class, ASSIGNMENT_PLAN_COLLECTION);
    }
}
