package com.mryqr.core.assignment.job;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.domain.AssignmentFactory;
import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.assignmentplan.domain.AssignmentPlan;
import com.mryqr.core.assignmentplan.domain.AssignmentPlanRepository;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.assignmentplan.domain.AssignmentPlan.newAssignmentPlanId;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateAssignmentsJob {
    private static final int BATCH_SIZE = 100;
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;
    private final GroupRepository groupRepository;
    private final AssignmentFactory assignmentFactory;
    private final AssignmentRepository assignmentRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final AssignmentPlanRepository assignmentPlanRepository;
    private final QrRepository qrRepository;

    public void run(LocalDateTime time) {
        LocalDateTime startTime = time.withMinute(0).withSecond(0).withNano(0);
        String timeString = MRY_DATE_TIME_FORMATTER.format(startTime);
        log.debug("Started create assignments from assignment plans for time[{}].", timeString);

        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        String startId = newAssignmentPlanId();

        try {
            while (true) {
                List<AssignmentPlan> assignmentPlans = assignmentPlanRepository.find(startTime, startId, BATCH_SIZE);

                if (isEmpty(assignmentPlans)) {
                    break;
                }

                forkJoinPool.submit(() -> assignmentPlans.parallelStream().forEach(assignmentPlan -> {
                    try {
                        createAssignment(assignmentPlan, startTime);
                    } catch (Throwable t) {
                        log.error("Failed to create assignments for assignment plan[{}].", assignmentPlan.getId(), t);
                    }
                })).join();

                startId = assignmentPlans.get(assignmentPlans.size() - 1).getId();//下一次直接从最后一条开始查询
            }
        } finally {
            forkJoinPool.shutdown();
        }

        log.debug("Finished create assignments from assignment plans for time[{}].", timeString);
    }

    private void createAssignment(AssignmentPlan assignmentPlan, LocalDateTime startTime) {
        if (!assignmentPlan.startTimeMatches(startTime)) {
            return;
        }

        App app = appRepository.cachedById(assignmentPlan.getAppId());
        if (!app.isAssignmentEnabled()) {
            return;
        }

        Tenant tenant = tenantRepository.cachedById(assignmentPlan.getTenantId());
        if (!tenant.isAssignmentAllowed()) {
            return;
        }

        List<String> excludedGroups = assignmentPlan.getExcludedGroups();
        Set<String> allExcludedGroupIds = isEmpty(excludedGroups) ? Set.of() : allExcludedGroupIds(excludedGroups, app.getId());
        Set<String> allVisibleGroupIds = groupRepository.cachedAllVisibleGroupIds(assignmentPlan.getAppId());
        allVisibleGroupIds.forEach(groupId -> {
            try {
                if (allExcludedGroupIds.contains(groupId)) {
                    log.info("Group[{}] is excluded from assignment plan[{}], skip creating assignment for it.",
                            groupId, assignmentPlan.getId());
                    return;
                }

                Set<String> qrIds = qrRepository.assignmentQrIdsOf(groupId);
                if (isEmpty(qrIds)) {
                    return;
                }

                Assignment assignment = assignmentFactory.create(groupId, qrIds, startTime, assignmentPlan, NOUSER);
                assignmentRepository.save(assignment);

                log.info("Created assignment[{}] for group[{}] from assignment plan[{}].",
                        assignment.getId(), groupId, assignmentPlan.getId());
            } catch (Throwable t) {
                log.warn("Failed to create assignment for group[{}] with startAt[{}].", groupId, startTime, t);
            }
        });
    }

    private Set<String> allExcludedGroupIds(List<String> excludedGroups, String appId) {
        GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(appId);
        return excludedGroups.stream().map(groupHierarchy::withAllSubGroupIdsOf)
                .flatMap(Collection::stream)
                .collect(toImmutableSet());
    }

}
