package com.mryqr.core.assignmentplan.command;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.assignmentplan.domain.AssignmentPlan;
import com.mryqr.core.assignmentplan.domain.AssignmentPlanFactory;
import com.mryqr.core.assignmentplan.domain.AssignmentPlanRepository;
import com.mryqr.core.common.domain.permission.ManagePermissionChecker;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_GROUPS_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_MEMBERS_EXIST;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentPlanCommandService {
    private final AssignmentPlanFactory assignmentPlanFactory;
    private final MryRateLimiter mryRateLimiter;
    private final AppRepository appRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final TenantRepository tenantRepository;
    private final AssignmentPlanRepository assignmentPlanRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public String createAssignmentPlan(CreateAssignmentPlanCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AssignmentPlan:Create", 5);

        String appId = command.getSetting().getAppId();
        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);

        managePermissionChecker.checkCanManageApp(user, app);
        PackagesStatus packagesStatus = tenantRepository.cachedById(app.getTenantId()).packagesStatus();
        packagesStatus.validateAssignmentAllowed();

        AssignmentPlan assignmentPlan = assignmentPlanFactory.createAssignmentPlan(command.getSetting(), app, user);
        assignmentPlanRepository.save(assignmentPlan);
        log.info("Created assignment plan[{}] under app[{}].", assignmentPlan.getId(), command.getSetting().getAppId());
        return assignmentPlan.getId();
    }

    @Transactional
    public void updateAssignmentPlanSetting(String assignmentPlanId, UpdateAssignmentPlanSettingCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AssignmentPlan:Update", 5);

        AssignmentPlan assignmentPlan = assignmentPlanRepository.byIdAndCheckTenantShip(assignmentPlanId, user);

        String appId = assignmentPlan.getAppId();
        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);

        managePermissionChecker.checkCanManageApp(user, app);
        PackagesStatus packagesStatus = tenantRepository.cachedById(app.getTenantId()).packagesStatus();
        packagesStatus.validateAssignmentAllowed();

        assignmentPlan.updateSetting(command.getSetting(), app, user);
        assignmentPlanRepository.save(assignmentPlan);
        log.info("Updated setting for assignment plan[{}].", assignmentPlanId);
    }

    @Transactional
    public void excludeGroups(String assignmentPlanId, ExcludeGroupsCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AssignmentPlan:ExcludeGroups", 5);

        AssignmentPlan assignmentPlan = assignmentPlanRepository.byIdAndCheckTenantShip(assignmentPlanId, user);

        String appId = assignmentPlan.getAppId();
        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        if (!groupRepository.cachedAllGroupsExist(command.getExcludedGroups(), assignmentPlan.getAppId())) {
            throw new MryException(NOT_ALL_GROUPS_EXIST, "有" + app.groupDesignation() + "不存在。");
        }

        assignmentPlan.excludeGroups(command.getExcludedGroups(), user);
        assignmentPlanRepository.save(assignmentPlan);
        log.info("Excluded groups for assignment plan[{}].", assignmentPlanId);
    }

    @Transactional
    public void setGroupOperators(String assignmentPlanId, SetGroupOperatorsCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AssignmentPlan:SetGroupOperators", 5);

        String groupId = command.getGroupId();
        List<String> memberIds = command.getMemberIds();

        if (memberRepository.cachedNotAllMembersExist(memberIds, user.getTenantId())) {
            throw new MryException(NOT_ALL_MEMBERS_EXIST, "有成员不存在。", "tenantId", user.getTenantId());
        }

        AssignmentPlan assignmentPlan = assignmentPlanRepository.byIdAndCheckTenantShip(assignmentPlanId, user);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        managePermissionChecker.canManageGroup(user, group);

        if (!Objects.equals(assignmentPlan.getAppId(), group.getAppId())) {
            throw new RuntimeException("App ID not the same for assignment plan and group.");
        }

        assignmentPlan.setGroupOperators(groupId, memberIds, user);
        assignmentPlanRepository.save(assignmentPlan);
        log.info("Set operators{} for group[{}] for assignment plan[{}].", memberIds, command.getGroupId(), assignmentPlanId);
    }

    @Transactional
    public void deleteAssignmentPlan(String assignmentPlanId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AssignmentPlan:Delete", 5);

        AssignmentPlan assignmentPlan = assignmentPlanRepository.byIdAndCheckTenantShip(assignmentPlanId, user);

        String appId = assignmentPlan.getAppId();
        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        assignmentPlan.onDelete(user);
        assignmentPlanRepository.delete(assignmentPlan);
        log.info("Deleted assignment plan[{}].", assignmentPlanId);
    }

    @Transactional
    public void activate(String assignmentPlanId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AssignmentPlan:Activate", 5);

        AssignmentPlan assignmentPlan = assignmentPlanRepository.byIdAndCheckTenantShip(assignmentPlanId, user);

        String appId = assignmentPlan.getAppId();
        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        assignmentPlan.activate(user);
        assignmentPlanRepository.save(assignmentPlan);
        log.info("Activated assignment plan[{}].", assignmentPlanId);
    }

    @Transactional
    public void deactivate(String assignmentPlanId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AssignmentPlan:Deactivate", 5);

        AssignmentPlan assignmentPlan = assignmentPlanRepository.byIdAndCheckTenantShip(assignmentPlanId, user);
        App app = appRepository.cachedByIdAndCheckTenantShip(assignmentPlan.getAppId(), user);
        managePermissionChecker.checkCanManageApp(user, app);

        assignmentPlan.deactivate(user);
        assignmentPlanRepository.save(assignmentPlan);
        log.info("Deactivated assignment plan[{}].", assignmentPlanId);
    }
}
