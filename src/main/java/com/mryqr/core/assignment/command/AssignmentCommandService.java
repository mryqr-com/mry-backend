package com.mryqr.core.assignment.command;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mryqr.common.exception.ErrorCode.NOT_ALL_MEMBERS_EXIST;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentCommandService {
    private final MryRateLimiter mryRateLimiter;
    private final AssignmentRepository assignmentRepository;
    private final AppRepository appRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public void deleteAssignment(String assignmentId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Assignment:Delete", 5);

        Assignment assignment = assignmentRepository.byIdAndCheckTenantShip(assignmentId, user);
        App app = appRepository.cachedByIdAndCheckTenantShip(assignment.getAppId(), user);
        managePermissionChecker.checkCanManageApp(user, app);

        assignmentRepository.delete(assignment);
        log.info("Deleted assignment[{}].", assignmentId);
    }

    @Transactional
    public void setAssignmentOperators(String assignmentPlanId, SetAssignmentOperatorsCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Assignment:SetOperators", 5);

        List<String> memberIds = command.getMemberIds();
        if (memberRepository.cachedNotAllMembersExist(memberIds, user.getTenantId())) {
            throw new MryException(NOT_ALL_MEMBERS_EXIST, "有成员不存在。", "tenantId", user.getTenantId());
        }

        Assignment assignment = assignmentRepository.byIdAndCheckTenantShip(assignmentPlanId, user);
        Group group = groupRepository.cachedById(assignment.getGroupId());
        managePermissionChecker.canManageGroup(user, group);

        assignment.setOperators(memberIds, user);
        assignmentRepository.save(assignment);
        log.info("Set operators {} for assignment[{}].", command.getMemberIds(), assignment.getId());
    }
}
