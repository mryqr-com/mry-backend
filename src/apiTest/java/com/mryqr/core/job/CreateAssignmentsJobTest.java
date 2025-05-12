package com.mryqr.core.job;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.event.AssignmentCreatedEvent;
import com.mryqr.core.assignment.job.CreateAssignmentsJob;
import com.mryqr.core.assignmentplan.AssignmentPlanApi;
import com.mryqr.core.assignmentplan.command.CreateAssignmentPlanCommand;
import com.mryqr.core.assignmentplan.command.ExcludeGroupsCommand;
import com.mryqr.core.assignmentplan.command.SetGroupOperatorsCommand;
import com.mryqr.core.assignmentplan.domain.AssignmentPlan;
import com.mryqr.core.assignmentplan.domain.AssignmentSetting;
import com.mryqr.core.assignmentplan.domain.DateTime;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.mryqr.common.event.DomainEventType.ASSIGNMENT_CREATED;
import static com.mryqr.core.assignment.domain.AssignmentStatus.IN_PROGRESS;
import static com.mryqr.core.assignmentplan.domain.AssignmentFrequency.*;
import static com.mryqr.utils.RandomTestFixture.rAssignmentPlanName;
import static java.time.LocalDateTime.of;
import static java.time.ZoneId.systemDefault;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)//由于一个测试的job运行可能也把其他测试方法的assignments创建了，所以采用SAME_THREAD一个一个挨着运行
public class CreateAssignmentsJobTest extends BaseApiTest {

    @Autowired
    private CreateAssignmentsJob createAssignmentsJob;

    @Test
    public void should_create_assignments_for_every_day_frequency() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-08").time("00:00").build();
        DateTime nearExpireNotifyDateTime = DateTime.builder().date("2020-07-08").time("22:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-08").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_DAY)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(nearExpireNotifyDateTime)
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        createAssignmentsJob.run(of(2020, 7, 8, 0, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        AssignmentCreatedEvent event = latestEventFor(assignment.getId(), ASSIGNMENT_CREATED, AssignmentCreatedEvent.class);
        assertEquals(assignment.getId(), event.getAssignmentId());

        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(assignmentPlan.getSetting().getName(), assignment.getName());
        assertEquals(assignmentPlan.getSetting().getAppId(), assignment.getAppId());
        assertEquals(response.getDefaultGroupId(), assignment.getGroupId());
        assertEquals(startDateTime.toInstant(), assignment.getStartAt());
        assertEquals(expireDateTime.toInstant(), assignment.getExpireAt());
        assertEquals(nearExpireNotifyDateTime.toInstant(), assignment.getNearExpireNotifyAt());
        assertEquals(EVERY_DAY, assignment.getFrequency());

        assertEquals(1, assignment.getAllQrIds().size());
        assertTrue(assignment.getAllQrIds().contains(response.getQrId()));
        assertTrue(assignment.getOperators().isEmpty());
        assertEquals(IN_PROGRESS, assignment.getStatus());

        createAssignmentsJob.run(of(2020, 7, 10, 0, 0));
        Assignment assignment1 = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(of(2020, 7, 10, 0, 0).atZone(systemDefault()).toInstant(), assignment1.getStartAt());
        assertEquals(of(2020, 7, 10, 23, 0).atZone(systemDefault()).toInstant(), assignment1.getExpireAt());
        assertEquals(of(2020, 7, 10, 22, 0).atZone(systemDefault()).toInstant(), assignment1.getNearExpireNotifyAt());
    }

    @Test
    public void should_create_assignment_for_every_week_frequency() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("02:00").build();
        DateTime nearExpireNotifyDateTime = DateTime.builder().date("2020-07-09").time("22:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-10").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_WEEK)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(nearExpireNotifyDateTime)
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        createAssignmentsJob.run(of(2020, 7, 9, 2, 1));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(assignmentPlan.getSetting().getName(), assignment.getName());
        assertEquals(assignmentPlan.getSetting().getAppId(), assignment.getAppId());
        assertEquals(response.getDefaultGroupId(), assignment.getGroupId());
        assertEquals(startDateTime.toInstant(), assignment.getStartAt());
        assertEquals(expireDateTime.toInstant(), assignment.getExpireAt());
        assertEquals(nearExpireNotifyDateTime.toInstant(), assignment.getNearExpireNotifyAt());

        assertEquals(1, assignment.getAllQrIds().size());
        assertTrue(assignment.getAllQrIds().contains(response.getQrId()));
        assertEquals(IN_PROGRESS, assignment.getStatus());

        createAssignmentsJob.run(of(2020, 7, 16, 2, 0));
        Assignment assignment1 = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(of(2020, 7, 16, 2, 0).atZone(systemDefault()).toInstant(), assignment1.getStartAt());
        assertEquals(of(2020, 7, 17, 23, 0).atZone(systemDefault()).toInstant(), assignment1.getExpireAt());
        assertEquals(of(2020, 7, 16, 22, 0).atZone(systemDefault()).toInstant(), assignment1.getNearExpireNotifyAt());
    }

    @Test
    public void should_create_assignment_for_every_month_frequency() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-10").time("03:00").build();
        DateTime nearExpireNotifyDateTime = DateTime.builder().date("2020-07-10").time("22:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-11").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(nearExpireNotifyDateTime)
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        createAssignmentsJob.run(of(2020, 7, 10, 3, 1));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(assignmentPlan.getSetting().getName(), assignment.getName());
        assertEquals(assignmentPlan.getSetting().getAppId(), assignment.getAppId());
        assertEquals(response.getDefaultGroupId(), assignment.getGroupId());
        assertEquals(startDateTime.toInstant(), assignment.getStartAt());
        assertEquals(expireDateTime.toInstant(), assignment.getExpireAt());
        assertEquals(nearExpireNotifyDateTime.toInstant(), assignment.getNearExpireNotifyAt());

        assertEquals(1, assignment.getAllQrIds().size());
        assertTrue(assignment.getAllQrIds().contains(response.getQrId()));
        assertEquals(IN_PROGRESS, assignment.getStatus());

        createAssignmentsJob.run(of(2020, 8, 10, 3, 0));

        Assignment assignment1 = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(of(2020, 8, 10, 3, 0).atZone(systemDefault()).toInstant(), assignment1.getStartAt());
        assertEquals(of(2020, 8, 11, 23, 0).atZone(systemDefault()).toInstant(), assignment1.getExpireAt());
        assertEquals(of(2020, 8, 10, 22, 0).atZone(systemDefault()).toInstant(), assignment1.getNearExpireNotifyAt());
    }

    @Test
    public void should_create_assignment_for_every_3_month_frequency() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-11").time("04:00").build();
        DateTime nearExpireNotifyDateTime = DateTime.builder().date("2020-07-11").time("22:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-12").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_THREE_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(nearExpireNotifyDateTime)
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        createAssignmentsJob.run(of(2020, 7, 11, 4, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(assignmentPlan.getSetting().getName(), assignment.getName());
        assertEquals(assignmentPlan.getSetting().getAppId(), assignment.getAppId());
        assertEquals(response.getDefaultGroupId(), assignment.getGroupId());
        assertEquals(startDateTime.toInstant(), assignment.getStartAt());
        assertEquals(expireDateTime.toInstant(), assignment.getExpireAt());
        assertEquals(nearExpireNotifyDateTime.toInstant(), assignment.getNearExpireNotifyAt());
        assertEquals(1, assignment.getAllQrIds().size());
        assertTrue(assignment.getAllQrIds().contains(response.getQrId()));
        assertEquals(IN_PROGRESS, assignment.getStatus());

        createAssignmentsJob.run(of(2020, 10, 11, 4, 0));
        Assignment assignment1 = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(of(2020, 10, 11, 4, 0).atZone(systemDefault()).toInstant(), assignment1.getStartAt());
        assertEquals(of(2020, 10, 12, 23, 0).atZone(systemDefault()).toInstant(), assignment1.getExpireAt());
        assertEquals(of(2020, 10, 11, 22, 0).atZone(systemDefault()).toInstant(), assignment1.getNearExpireNotifyAt());
    }

    @Test
    public void should_create_assignment_for_every_6_month_frequency() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-12").time("05:00").build();
        DateTime nearExpireNotifyDateTime = DateTime.builder().date("2020-07-12").time("22:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-13").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_SIX_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(nearExpireNotifyDateTime)
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        createAssignmentsJob.run(of(2020, 7, 12, 5, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(assignmentPlan.getSetting().getName(), assignment.getName());
        assertEquals(assignmentPlan.getSetting().getAppId(), assignment.getAppId());
        assertEquals(response.getDefaultGroupId(), assignment.getGroupId());
        assertEquals(startDateTime.toInstant(), assignment.getStartAt());
        assertEquals(expireDateTime.toInstant(), assignment.getExpireAt());
        assertEquals(nearExpireNotifyDateTime.toInstant(), assignment.getNearExpireNotifyAt());
        assertEquals(1, assignment.getAllQrIds().size());
        assertTrue(assignment.getAllQrIds().contains(response.getQrId()));
        assertEquals(IN_PROGRESS, assignment.getStatus());

        createAssignmentsJob.run(of(2021, 1, 12, 5, 0));
        Assignment assignment1 = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(of(2021, 1, 12, 5, 0).atZone(systemDefault()).toInstant(), assignment1.getStartAt());
        assertEquals(of(2021, 1, 13, 23, 0).atZone(systemDefault()).toInstant(), assignment1.getExpireAt());
        assertEquals(of(2021, 1, 12, 22, 0).atZone(systemDefault()).toInstant(), assignment1.getNearExpireNotifyAt());
    }

    @Test
    public void should_create_assignment_for_every_year_frequency() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-13").time("06:00").build();
        DateTime nearExpireNotifyDateTime = DateTime.builder().date("2020-07-13").time("22:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-14").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_YEAR)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(nearExpireNotifyDateTime)
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        createAssignmentsJob.run(of(2020, 7, 13, 6, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(assignmentPlan.getSetting().getName(), assignment.getName());
        assertEquals(assignmentPlan.getSetting().getAppId(), assignment.getAppId());
        assertEquals(response.getDefaultGroupId(), assignment.getGroupId());
        assertEquals(startDateTime.toInstant(), assignment.getStartAt());
        assertEquals(expireDateTime.toInstant(), assignment.getExpireAt());
        assertEquals(nearExpireNotifyDateTime.toInstant(), assignment.getNearExpireNotifyAt());
        assertEquals(1, assignment.getAllQrIds().size());
        assertTrue(assignment.getAllQrIds().contains(response.getQrId()));
        assertEquals(IN_PROGRESS, assignment.getStatus());

        createAssignmentsJob.run(of(2021, 7, 13, 6, 0));
        Assignment assignment1 = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(of(2021, 7, 13, 6, 0).atZone(systemDefault()).toInstant(), assignment1.getStartAt());
        assertEquals(of(2021, 7, 14, 23, 0).atZone(systemDefault()).toInstant(), assignment1.getExpireAt());
        assertEquals(of(2021, 7, 13, 22, 0).atZone(systemDefault()).toInstant(), assignment1.getNearExpireNotifyAt());
    }

    @Test
    public void should_create_assignments_for_each_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        String newGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        CreateQrResponse qr1 = QrApi.createQr(response.getJwt(), newGroupId);
        CreateQrResponse qr2 = QrApi.createQr(response.getJwt(), newGroupId);

        DateTime startDateTime = DateTime.builder().date("2020-07-14").time("07:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-14").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        createAssignmentsJob.run(of(2020, 7, 14, 7, 0));

        Assignment assignment = assignmentRepository.latestForGroup(newGroupId).get();
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(newGroupId, assignment.getGroupId());
        assertNull(assignment.getNearExpireNotifyAt());
        assertEquals(2, assignment.getAllQrIds().size());
        assertTrue(assignment.getAllQrIds().containsAll(List.of(qr1.getQrId(), qr2.getQrId())));
        assertEquals(IN_PROGRESS, assignment.getStatus());

        Assignment assignment2 = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignmentPlanId, assignment2.getAssignmentPlanId());
        assertEquals(response.getDefaultGroupId(), assignment2.getGroupId());
        assertEquals(1, assignment2.getAllQrIds().size());
        assertTrue(assignment2.getAllQrIds().contains(response.getQrId()));
        assertEquals(IN_PROGRESS, assignment2.getStatus());
        assertNull(assignment2.getNearExpireNotifyAt());
    }

    @Test
    public void should_copy_group_operators_of_assignment_plan_to_assignment() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-15").time("08:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-15").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId, SetGroupOperatorsCommand.builder()
                .groupId(response.getDefaultGroupId())
                .memberIds(List.of(response.getMemberId()))
                .build());

        createAssignmentsJob.run(of(2020, 7, 15, 8, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(1, assignment.getOperators().size());
        assertTrue(assignment.getOperators().contains(response.getMemberId()));
    }

    @Test
    public void should_deal_with_last_day_of_month() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-08-31").time("09:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-08-31").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        createAssignmentsJob.run(of(2020, 9, 30, 9, 1));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(of(2020, 9, 30, 9, 0).atZone(systemDefault()).toInstant(), assignment.getStartAt());
        assertEquals(of(2020, 9, 30, 23, 0).atZone(systemDefault()).toInstant(), assignment.getExpireAt());
    }

    @Test
    public void should_deal_with_non_last_day_of_month() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-08-30").time("10:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-08-30").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        createAssignmentsJob.run(of(2020, 9, 30, 10, 1));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(of(2020, 9, 30, 10, 0).atZone(systemDefault()).toInstant(), assignment.getStartAt());
        assertEquals(of(2020, 9, 30, 23, 0).atZone(systemDefault()).toInstant(), assignment.getExpireAt());
    }

    @Test
    public void should_fix_time_for_day_30_and_31() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-08-30").time("11:00").build();
        DateTime nearExpireNotifyDateTime = DateTime.builder().date("2020-08-31").time("02:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-08-31").time("03:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(nearExpireNotifyDateTime)
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        createAssignmentsJob.run(of(2020, 9, 30, 11, 1));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(of(2020, 9, 30, 11, 0).atZone(systemDefault()).toInstant(), assignment.getStartAt());
        assertEquals(of(2020, 10, 1, 2, 0).atZone(systemDefault()).toInstant(), assignment.getNearExpireNotifyAt());
        assertEquals(of(2020, 10, 1, 3, 0).atZone(systemDefault()).toInstant(), assignment.getExpireAt());
    }

    @Test
    public void should_fix_time_for_leap_year() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-01-30").time("12:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-01-31").time("03:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        createAssignmentsJob.run(of(2020, 2, 29, 12, 1));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(of(2020, 2, 29, 12, 0).atZone(systemDefault()).toInstant(), assignment.getStartAt());
        assertEquals(of(2020, 3, 1, 3, 0).atZone(systemDefault()).toInstant(), assignment.getExpireAt());
    }

    @Test
    public void should_not_create_assignments_if_time_not_match() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-16").time("13:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-16").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        createAssignmentsJob.run(of(2020, 7, 16, 12, 0));
        assertFalse(assignmentRepository.latestForGroup(response.getDefaultGroupId()).isPresent());
    }

    @Test
    public void should_not_create_assignment_if_package_not_allowed() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-17").time("14:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-17").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        Tenant theTenant = tenantRepository.byId(response.getTenantId());
        setupApi.updateTenantPlan(theTenant, theTenant.currentPlan().withAssignmentAllowed(false));
        createAssignmentsJob.run(of(2020, 7, 17, 14, 0));
        assertFalse(assignmentRepository.latestForGroup(response.getDefaultGroupId()).isPresent());

        Tenant theTenant2 = tenantRepository.byId(response.getTenantId());
        setupApi.updateTenantPlan(theTenant2, theTenant2.currentPlan().withAssignmentAllowed(true));
        createAssignmentsJob.run(of(2020, 7, 17, 14, 0));
        assertTrue(assignmentRepository.latestForGroup(response.getDefaultGroupId()).isPresent());
    }

    @Test
    public void should_not_create_assignment_if_app_assignment_not_enabled() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-18").time("15:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-18").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), false);
        createAssignmentsJob.run(of(2020, 7, 18, 15, 0));
        assertFalse(assignmentRepository.latestForGroup(response.getDefaultGroupId()).isPresent());

        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        createAssignmentsJob.run(of(2020, 7, 18, 15, 0));
        assertTrue(assignmentRepository.latestForGroup(response.getDefaultGroupId()).isPresent());
    }

    @Test
    public void should_not_create_assignment_if_group_excluded() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());
        QrApi.createQr(response.getJwt(), subGroupId);

        DateTime startDateTime = DateTime.builder().date("2020-07-19").time("16:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-19").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
        AssignmentPlan assignmentPlan = assignmentPlanRepository.byId(assignmentPlanId);
        assertEquals(assignmentSetting, assignmentPlan.getSetting());

        AssignmentPlanApi.excludeGroups(response.getJwt(), assignmentPlanId,
                ExcludeGroupsCommand.builder().excludedGroups(List.of(response.getDefaultGroupId())).build());
        createAssignmentsJob.run(of(2020, 7, 19, 16, 0));
        assertFalse(assignmentRepository.latestForGroup(response.getDefaultGroupId()).isPresent());
        assertFalse(assignmentRepository.latestForGroup(subGroupId).isPresent());

        AssignmentPlanApi.excludeGroups(response.getJwt(), assignmentPlanId, ExcludeGroupsCommand.builder().excludedGroups(List.of()).build());
        createAssignmentsJob.run(of(2020, 7, 19, 16, 0));
        assertTrue(assignmentRepository.latestForGroup(response.getDefaultGroupId()).isPresent());
        assertTrue(assignmentRepository.latestForGroup(subGroupId).isPresent());
    }

    @Test
    public void should_not_create_assignment_if_assignment_plan_deactivated() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-19").time("17:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-19").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        AssignmentPlanApi.deactivateAssignmentPlan(response.getJwt(), assignmentPlanId);
        createAssignmentsJob.run(of(2020, 7, 19, 17, 0));
        assertFalse(assignmentRepository.latestForGroup(response.getDefaultGroupId()).isPresent());

        AssignmentPlanApi.activateAssignmentPlan(response.getJwt(), assignmentPlanId);
        createAssignmentsJob.run(of(2020, 7, 19, 17, 0));
        assertTrue(assignmentRepository.latestForGroup(response.getDefaultGroupId()).isPresent());
    }

    @Test
    public void should_not_create_assignments_again_with_the_same_group_and_start_at() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        DateTime startDateTime = DateTime.builder().date("2020-07-19").time("18:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-19").time("23:00").build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        createAssignmentsJob.run(of(2020, 7, 19, 18, 0));
        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();

        createAssignmentsJob.run(of(2020, 7, 19, 18, 0));
        Assignment assignment2 = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(assignment.getId(), assignment2.getId());
    }
}
