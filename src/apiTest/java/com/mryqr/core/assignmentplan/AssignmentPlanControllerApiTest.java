package com.mryqr.core.assignmentplan;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.job.CreateAssignmentsJob;
import com.mryqr.core.assignmentplan.command.CreateAssignmentPlanCommand;
import com.mryqr.core.assignmentplan.command.ExcludeGroupsCommand;
import com.mryqr.core.assignmentplan.command.SetGroupOperatorsCommand;
import com.mryqr.core.assignmentplan.command.UpdateAssignmentPlanSettingCommand;
import com.mryqr.core.assignmentplan.domain.AssignmentPlan;
import com.mryqr.core.assignmentplan.domain.AssignmentSetting;
import com.mryqr.core.assignmentplan.domain.DateTime;
import com.mryqr.core.assignmentplan.domain.event.AssignmentPlanDeletedEvent;
import com.mryqr.core.assignmentplan.query.QAssignmentPlan;
import com.mryqr.core.assignmentplan.query.QAssignmentPlanSummary;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.domain.Member;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mryqr.core.assignmentplan.domain.AssignmentFrequency.EVERY_MONTH;
import static com.mryqr.core.common.domain.event.DomainEventType.ASSIGNMENT_PLAN_DELETED;
import static com.mryqr.core.common.exception.ErrorCode.APP_ASSIGNMENT_NOT_ENABLED;
import static com.mryqr.core.common.exception.ErrorCode.ASSIGNMENT_DURATION_EXCEED_FREQUENCY;
import static com.mryqr.core.common.exception.ErrorCode.ASSIGNMENT_NOTIFY_TIME_OVERFLOW;
import static com.mryqr.core.common.exception.ErrorCode.ASSIGNMENT_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.ASSIGNMENT_PLAN_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.ASSIGNMENT_START_TIME_AFTER_END_TIME;
import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_GROUPS_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_MEMBERS_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.PAGE_NOT_FOUND;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.utils.RandomTestFixture.defaultPage;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControl;
import static com.mryqr.utils.RandomTestFixture.rAssignmentPlanName;
import static java.time.LocalDateTime.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssignmentPlanControllerApiTest extends BaseApiTest {

    @Autowired
    private CreateAssignmentsJob createAssignmentsJob;

    @Test
    public void should_create_assignment_plan() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());
    }

    @Test
    public void should_fail_create_if_name_duplicates() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        assertError(() -> AssignmentPlanApi.createAssignmentPlanRaw(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build()), ASSIGNMENT_PLAN_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_create_if_assignment_not_enabled() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        assertError(() -> AssignmentPlanApi.createAssignmentPlanRaw(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build()), APP_ASSIGNMENT_NOT_ENABLED);
    }

    @Test
    public void should_fail_create_if_packages_too_low() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        assertError(() -> AssignmentPlanApi.createAssignmentPlanRaw(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build()), ASSIGNMENT_NOT_ALLOWED);
    }

    @Test
    public void should_fail_create_if_page_not_found() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(Page.newPageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        assertError(() -> AssignmentPlanApi.createAssignmentPlanRaw(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build()), PAGE_NOT_FOUND);
    }

    @Test
    public void should_fail_create_if_start_time_after_end_time() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-19").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        assertError(() -> AssignmentPlanApi.createAssignmentPlanRaw(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build()), ASSIGNMENT_START_TIME_AFTER_END_TIME);
    }

    @Test
    public void should_fail_create_if_time_duration_exceed_frequency() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-15").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-04-17").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-18").time("09:00").build())
                .build();

        assertError(() -> AssignmentPlanApi.createAssignmentPlanRaw(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build()), ASSIGNMENT_DURATION_EXCEED_FREQUENCY);
    }


    @Test
    public void should_fail_create_if_time_near_end_notify_overflow() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-15").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-17").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-18").time("09:00").build())
                .build();

        assertError(() -> AssignmentPlanApi.createAssignmentPlanRaw(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build()), ASSIGNMENT_NOTIFY_TIME_OVERFLOW);
    }

    @Test
    public void should_update_assignment_plan_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        assertEquals(assignmentSetting, assignmentPlanRepository.byId(assignmentPlanId).getSetting());

        AssignmentSetting updateAssignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        AssignmentPlanApi.updateAssignmentPlanSetting(response.getJwt(), assignmentPlanId, UpdateAssignmentPlanSettingCommand.builder().setting(updateAssignmentSetting).build());
        assertEquals(updateAssignmentSetting, assignmentPlanRepository.byId(assignmentPlanId).getSetting());
    }

    @Test
    public void update_assignment_plan_setting_should_do_validation() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        assertEquals(assignmentSetting, assignmentPlanRepository.byId(assignmentPlanId).getSetting());

        AssignmentSetting updateAssignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-04-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        assertError(() -> AssignmentPlanApi.updateAssignmentPlanSettingRaw(response.getJwt(), assignmentPlanId, UpdateAssignmentPlanSettingCommand.builder().setting(updateAssignmentSetting).build()), ASSIGNMENT_DURATION_EXCEED_FREQUENCY);
    }

    @Test
    public void should_exclude_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        List<String> excludedGroups = List.of(response.getDefaultGroupId());
        AssignmentPlanApi.excludeGroups(response.getJwt(), assignmentPlanId, ExcludeGroupsCommand.builder().excludedGroups(excludedGroups).build());
        assertEquals(excludedGroups, assignmentPlanRepository.byId(assignmentPlanId).getExcludedGroups());
    }

    @Test
    public void should_fail_exclude_group_if_not_all_groups_exists() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        List<String> excludedGroups = List.of(response.getDefaultGroupId(), Group.newGroupId());
        assertError(() -> AssignmentPlanApi.excludeGroupsRaw(response.getJwt(), assignmentPlanId, ExcludeGroupsCommand.builder().excludedGroups(excludedGroups).build()), NOT_ALL_GROUPS_EXIST);
    }

    @Test
    public void should_set_group_operators() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId, SetGroupOperatorsCommand.builder()
                .groupId(response.getDefaultGroupId())
                .memberIds(List.of(response.getMemberId()))
                .build());

        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        List<String> operators = assignmentPlan.getGroupOperators().get(response.getDefaultGroupId());
        assertEquals(List.of(response.getMemberId()), operators);
    }

    @Test
    public void should_remove_operator_after_member_deleted() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        String memberId = MemberApi.createMember(response.getJwt());

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId, SetGroupOperatorsCommand.builder()
                .groupId(response.getDefaultGroupId())
                .memberIds(List.of(memberId))
                .build());

        assertTrue(assignmentPlanRepository.byId(assignmentPlanId).getGroupOperators().get(response.getDefaultGroupId()).contains(memberId));
        MemberApi.deleteMember(response.getJwt(), memberId);
        assertFalse(assignmentPlanRepository.byId(assignmentPlanId).getGroupOperators().get(response.getDefaultGroupId()).contains(memberId));
    }

    @Test
    public void should_fail_set_group_operators_if_not_all_members_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        SetGroupOperatorsCommand setGroupOperatorsCommand = SetGroupOperatorsCommand.builder()
                .groupId(response.getDefaultGroupId())
                .memberIds(List.of(response.getMemberId(), Member.newMemberId()))
                .build();

        assertError(() -> AssignmentPlanApi.setGroupOperatorsRaw(response.getJwt(), assignmentPlanId, setGroupOperatorsCommand), NOT_ALL_MEMBERS_EXIST);
    }

    @Test
    public void should_list_assignment_plans() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        String name = rAssignmentPlanName();
        String assignmentPlanId1 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(name)
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build()).build());
        List<String> excludedGroups = List.of(response.getDefaultGroupId());
        AssignmentPlanApi.excludeGroups(response.getJwt(), assignmentPlanId1, ExcludeGroupsCommand.builder().excludedGroups(excludedGroups).build());

        String assignmentPlanId2 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-10").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build()).build());

        List<QAssignmentPlan> plans = AssignmentPlanApi.listAssignmentPlans(response.getJwt(), response.getAppId());
        assertEquals(2, plans.size());
        assertEquals(assignmentPlanId2, plans.get(0).getId());
        assertEquals(assignmentPlanId1, plans.get(1).getId());
        assertEquals(name, plans.get(1).getName());
        assertTrue(plans.get(1).getExcludedGroups().contains(response.getDefaultGroupId()));
        assertNull(plans.get(0).getOperators());

        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId1);
        assertEquals(assignmentPlan.getSetting(), plans.get(1).getSetting());
    }

    @Test
    public void should_list_assignment_plan_with_excluded_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId1 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        List<String> excludedGroups = List.of(response.getDefaultGroupId());
        AssignmentPlanApi.excludeGroups(response.getJwt(), assignmentPlanId1, ExcludeGroupsCommand.builder().excludedGroups(excludedGroups).build());

        String assignmentPlanId2 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-10").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build()).build());

        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId2, SetGroupOperatorsCommand.builder()
                .groupId(response.getDefaultGroupId())
                .memberIds(List.of(response.getMemberId()))
                .build());

        List<QAssignmentPlan> plans = AssignmentPlanApi.listAssignmentPlans(response.getJwt(), response.getAppId(), response.getDefaultGroupId());
        assertEquals(1, plans.size());
        assertEquals(assignmentPlanId2, plans.get(0).getId());
        assertTrue(plans.get(0).getOperators().contains(response.getMemberId()));
        String memberName = memberRepository.cachedMemberNameOf(response.getMemberId());
        assertTrue(plans.get(0).getOperatorNames().contains(memberName));
        assertNull(plans.get(0).getExcludedGroups());

        List<QAssignmentPlan> subGroupPlans = AssignmentPlanApi.listAssignmentPlans(response.getJwt(), response.getAppId(), subGroupId);
        assertEquals(1, subGroupPlans.size());
        assertEquals(assignmentPlanId2, subGroupPlans.get(0).getId());
    }

    @Test
    public void should_delete_assignment_plan() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        assertTrue(assignmentPlanRepository.exists(assignmentPlanId));

        AssignmentPlanApi.deleteAssignmentPlan(response.getJwt(), assignmentPlanId);
        assertFalse(assignmentPlanRepository.exists(assignmentPlanId));

        AssignmentPlanDeletedEvent deletedEvent = domainEventDao.latestEventFor(assignmentPlanId, ASSIGNMENT_PLAN_DELETED, AssignmentPlanDeletedEvent.class);
        assertEquals(assignmentPlanId, deletedEvent.getAssignmentPlanId());
    }

    @Test
    public void delete_assignment_plan_should_also_delete_assignments_under_it() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2020-05-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2020-05-09").time("23:00").build())
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        assertTrue(assignmentPlanRepository.exists(assignmentPlanId));

        createAssignmentsJob.run(of(2021, 5, 9, 9, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertNotNull(assignment);

        AssignmentPlanApi.deleteAssignmentPlan(response.getJwt(), assignmentPlanId);
        assertFalse(assignmentPlanRepository.exists(assignmentPlanId));

        AssignmentPlanDeletedEvent deletedEvent = domainEventDao.latestEventFor(assignmentPlanId, ASSIGNMENT_PLAN_DELETED, AssignmentPlanDeletedEvent.class);
        assertEquals(assignmentPlanId, deletedEvent.getAssignmentPlanId());

        assertFalse(assignmentRepository.exists(assignment.getId()));
    }

    @Test
    public void delete_app_should_also_delete_assignment_plans_under_it() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        assertTrue(assignmentPlanRepository.exists(assignmentPlanId));

        AppApi.deleteApp(response.getJwt(), response.getAppId());
        assertFalse(assignmentPlanRepository.exists(assignmentPlanId));
    }

    @Test
    public void delete_page_should_also_delete_assignment_plans_for_it() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        assertTrue(assignmentPlanRepository.exists(assignmentPlanId));

        String appId = response.getAppId();
        Page newPage = defaultPage(defaultRadioControl());
        AppApi.updateAppPage(response.getJwt(), appId, newPage);

        assertFalse(assignmentPlanRepository.exists(assignmentPlanId));
    }

    @Test
    public void delete_group_should_also_delete_it_from_all_assignment_plans() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        String newGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId, SetGroupOperatorsCommand.builder()
                .groupId(response.getDefaultGroupId())
                .memberIds(List.of(response.getMemberId()))
                .build());

        List<String> excludedGroups = List.of(response.getDefaultGroupId(), newGroupId);
        AssignmentPlanApi.excludeGroups(response.getJwt(), assignmentPlanId, ExcludeGroupsCommand.builder().excludedGroups(excludedGroups).build());

        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertTrue(assignmentPlan.getExcludedGroups().containsAll(List.of(response.getDefaultGroupId(), newGroupId)));
        assertTrue(assignmentPlan.getGroupOperators().containsKey(response.getDefaultGroupId()));

        GroupApi.deleteGroup(response.getJwt(), response.getDefaultGroupId());
        AssignmentPlan updated = assignmentPlanRepository.byId(assignmentPlanId);
        assertTrue(updated.getExcludedGroups().contains(newGroupId));
        assertFalse(updated.getExcludedGroups().contains(response.getDefaultGroupId()));
        assertFalse(updated.getGroupOperators().containsKey(response.getDefaultGroupId()));
    }

    @Test
    public void should_deactivate_and_activate_assignment_plans() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        assertTrue(assignmentPlanRepository.byId(assignmentPlanId).isActive());

        AssignmentPlanApi.deactivateAssignmentPlan(response.getJwt(), assignmentPlanId);
        assertFalse(assignmentPlanRepository.byId(assignmentPlanId).isActive());

        AssignmentPlanApi.activateAssignmentPlan(response.getJwt(), assignmentPlanId);
        assertTrue(assignmentPlanRepository.byId(assignmentPlanId).isActive());
    }

    @Test
    public void should_not_list_deactivated_assignment_plan_for_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        String assignmentPlanId1 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build()).build());

        String assignmentPlanId2 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-03-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-03-15").time("23:00").build())
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(DateTime.builder().date("2000-03-14").time("09:00").build())
                .build()).build());

        AssignmentPlanApi.deactivateAssignmentPlan(response.getJwt(), assignmentPlanId1);
        assertEquals(2, AssignmentPlanApi.listAssignmentPlans(response.getJwt(), response.getAppId()).size());
        assertEquals(1, AssignmentPlanApi.listAssignmentPlans(response.getJwt(), response.getAppId(), response.getDefaultGroupId()).size());
    }

    @Test
    public void should_list_assignment_plan_summaries() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        String assignmentPlanId1 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-04-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-04-15").time("23:00").build())
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build()).build());

        String assignmentPlanId2 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-04-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-04-15").time("23:00").build())
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build()).build());

        String assignmentPlanId3 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-04-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-04-15").time("23:00").build())
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build()).build());

        AssignmentPlanApi.deactivateAssignmentPlan(response.getJwt(), assignmentPlanId1);

        List<QAssignmentPlanSummary> summaries = AssignmentPlanApi.listAssignmentPlanSummaries(response.getJwt(), response.getAppId());
        assertEquals(2, summaries.size());
        Set<String> ids = summaries.stream().map(QAssignmentPlanSummary::getId).collect(Collectors.toSet());
        assertFalse(ids.contains(assignmentPlanId1));
        assertTrue(ids.contains(assignmentPlanId2));
        assertTrue(ids.contains(assignmentPlanId3));
    }

    @Test
    public void should_list_assignment_plan_summaries_for_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());

        String assignmentPlanId1 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-04-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-04-15").time("23:00").build())
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build()).build());

        String assignmentPlanId2 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-04-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-04-15").time("23:00").build())
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build()).build());

        String assignmentPlanId3 = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(DateTime.builder().date("2000-04-09").time("09:00").build())
                .expireTime(DateTime.builder().date("2000-04-15").time("23:00").build())
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build()).build());

        assertEquals(3, AssignmentPlanApi.listAssignmentPlanSummariesForGroup(response.getJwt(), response.getDefaultGroupId()).size());
        assertEquals(3, AssignmentPlanApi.listAssignmentPlanSummariesForGroup(response.getJwt(), subGroupId).size());

        AssignmentPlanApi.deactivateAssignmentPlan(response.getJwt(), assignmentPlanId1);
        AssignmentPlanApi.excludeGroups(response.getJwt(), assignmentPlanId2, ExcludeGroupsCommand.builder().excludedGroups(List.of(response.getDefaultGroupId())).build());

        List<QAssignmentPlanSummary> summaries = AssignmentPlanApi.listAssignmentPlanSummariesForGroup(response.getJwt(), response.getDefaultGroupId());
        assertEquals(1, summaries.size());
        Set<String> ids = summaries.stream().map(QAssignmentPlanSummary::getId).collect(Collectors.toSet());
        assertFalse(ids.contains(assignmentPlanId1));
        assertFalse(ids.contains(assignmentPlanId2));
        assertTrue(ids.contains(assignmentPlanId3));

        QAssignmentPlanSummary summary = summaries.get(0);
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId3);
        assertEquals(assignmentPlan.getName(), summary.getName());

        assertEquals(1, AssignmentPlanApi.listAssignmentPlanSummariesForGroup(response.getJwt(), subGroupId).size());
    }
}
