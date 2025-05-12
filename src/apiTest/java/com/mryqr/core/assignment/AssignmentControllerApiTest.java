package com.mryqr.core.assignment;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.assignment.command.SetAssignmentOperatorsCommand;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.domain.AssignmentFinishedQr;
import com.mryqr.core.assignment.job.CreateAssignmentsJob;
import com.mryqr.core.assignment.query.ListAssignmentQrsQuery;
import com.mryqr.core.assignment.query.ListMyAssignmentsQuery;
import com.mryqr.core.assignment.query.ListMyManagedAssignmentsQuery;
import com.mryqr.core.assignment.query.QAssignmentDetail;
import com.mryqr.core.assignment.query.QAssignmentListQr;
import com.mryqr.core.assignment.query.QAssignmentQrDetail;
import com.mryqr.core.assignment.query.QListAssignment;
import com.mryqr.core.assignmentplan.AssignmentPlanApi;
import com.mryqr.core.assignmentplan.command.CreateAssignmentPlanCommand;
import com.mryqr.core.assignmentplan.command.SetGroupOperatorsCommand;
import com.mryqr.core.assignmentplan.domain.AssignmentFrequency;
import com.mryqr.core.assignmentplan.domain.AssignmentSetting;
import com.mryqr.core.assignmentplan.domain.DateTime;
import com.mryqr.core.common.domain.Geolocation;
import com.mryqr.core.common.domain.Geopoint;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.command.UpdateQrBaseSettingCommand;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

import static com.mryqr.core.assignment.domain.AssignmentStatus.IN_PROGRESS;
import static com.mryqr.core.assignment.domain.AssignmentStatus.SUCCEED;
import static com.mryqr.core.assignmentplan.domain.AssignmentFrequency.EVERY_DAY;
import static com.mryqr.core.assignmentplan.domain.AssignmentFrequency.EVERY_MONTH;
import static com.mryqr.core.common.exception.ErrorCode.ACCESS_DENIED;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.utils.RandomTestFixture.defaultPage;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControl;
import static com.mryqr.utils.RandomTestFixture.rAddress;
import static com.mryqr.utils.RandomTestFixture.rAssignmentPlanName;
import static com.mryqr.utils.RandomTestFixture.rQrName;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.of;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class AssignmentControllerApiTest extends BaseApiTest {

    @Autowired
    private CreateAssignmentsJob createAssignmentsJob;

    @Test
    public void should_list_managed_assignments() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2020-07-08").time("17:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-08").time("23:00").build();

        createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_DAY);

        IntStream.range(8, 20).forEach(value -> createAssignmentsJob.run(of(2020, 7, value, 17, 0)));

        PagedList<QListAssignment> firstPage = AssignmentApi.listManagedAssignments(response.getJwt(), ListMyManagedAssignmentsQuery.builder()
                .appId(response.getAppId())
                .pageIndex(1)
                .pageSize(10)
                .build());

        assertEquals(10, firstPage.getData().size());
        assertEquals(12, firstPage.getTotalNumber());

        PagedList<QListAssignment> secondPage = AssignmentApi.listManagedAssignments(response.getJwt(), ListMyManagedAssignmentsQuery.builder()
                .appId(response.getAppId())
                .pageIndex(2)
                .pageSize(10)
                .build());

        assertEquals(2, secondPage.getData().size());
        assertEquals(12, secondPage.getTotalNumber());
        assertTrue(secondPage.getData().get(0).getCreatedAt().isAfter(secondPage.getData().get(1).getCreatedAt()));
    }

    @Test
    public void should_list_managed_assignments_with_correct_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2021-07-08").time("01:00").build();
        DateTime expireDateTime = DateTime.builder().date("2021-07-08").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);
        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId, SetGroupOperatorsCommand.builder().groupId(response.getDefaultGroupId()).memberIds(List.of(response.getMemberId())).build());
        createAssignmentsJob.run(of(2021, 7, 8, 1, 0));
        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();

        PagedList<QListAssignment> assignments = AssignmentApi.listManagedAssignments(response.getJwt(), ListMyManagedAssignmentsQuery.builder()
                .appId(response.getAppId())
                .pageIndex(1)
                .pageSize(20)
                .build());

        assertEquals(1, assignments.getData().size());
        QListAssignment qAssignment = assignments.getData().get(0);
        assertEquals(assignmentPlanId, assignment.getAssignmentPlanId());
        assertEquals(assignmentPlanId, qAssignment.getAssignmentPlanId());
        assertEquals(assignment.getId(), qAssignment.getId());
        assertEquals(assignment.getGroupId(), qAssignment.getGroupId());
        assertEquals(assignment.getName(), qAssignment.getName());
        assertEquals(assignment.getCreatedAt(), qAssignment.getCreatedAt());
        assertEquals(assignment.getExpireAt(), qAssignment.getExpireAt());
        assertEquals(assignment.getStartAt(), qAssignment.getStartAt());
        assertEquals(assignment.getStatus(), qAssignment.getStatus());
        assertEquals(0, qAssignment.getFinishedQrCount());
        assertEquals(1, qAssignment.getAllQrCount());
        Member member = memberRepository.byId(response.getMemberId());
        assertTrue(qAssignment.getOperatorNames().contains(member.getName()));
        assertTrue(qAssignment.getOperators().contains(member.getId()));
        assertEquals(1, qAssignment.getOperators().size());
        assertEquals(1, qAssignment.getOperatorNames().size());
    }

    @Test
    public void should_list_managed_assignments_filtered_by_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        String groupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        QrApi.createQr(response.getJwt(), groupId);

        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), groupId);
        QrApi.createQr(response.getJwt(), subGroupId);

        DateTime startDateTime = DateTime.builder().date("2021-07-08").time("02:00").build();
        DateTime expireDateTime = DateTime.builder().date("2021-07-08").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_DAY);
        createAssignmentsJob.run(of(2021, 7, 8, 2, 0));

        PagedList<QListAssignment> assignments = AssignmentApi.listManagedAssignments(response.getJwt(), ListMyManagedAssignmentsQuery.builder()
                .appId(response.getAppId())
                .pageIndex(1)
                .pageSize(20)
                .build());
        assertEquals(3, assignments.getData().size());

        PagedList<QListAssignment> groupFiltered = AssignmentApi.listManagedAssignments(response.getJwt(), ListMyManagedAssignmentsQuery.builder()
                .appId(response.getAppId())
                .groupId(groupId)
                .pageIndex(1)
                .pageSize(20)
                .build());
        assertEquals(2, groupFiltered.getData().size());
        List<String> groupIds = groupFiltered.getData().stream().map(QListAssignment::getGroupId).collect(toList());
        assertTrue(groupIds.contains(groupId));
        assertTrue(groupIds.contains(subGroupId));
    }

    @Test
    public void should_list_managed_assignments_filtered_by_assignment_plan() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2021-07-08").time("03:00").build();
        DateTime expireDateTime = DateTime.builder().date("2021-07-08").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);
        String assignmentPlanId2 = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(2021, 7, 8, 3, 0));

        PagedList<QListAssignment> assignments = AssignmentApi.listManagedAssignments(response.getJwt(), ListMyManagedAssignmentsQuery.builder()
                .appId(response.getAppId())
                .pageIndex(1)
                .pageSize(20)
                .build());
        assertEquals(2, assignments.getData().size());

        PagedList<QListAssignment> groupFiltered = AssignmentApi.listManagedAssignments(response.getJwt(), ListMyManagedAssignmentsQuery.builder()
                .appId(response.getAppId())
                .assignmentPlanId(assignmentPlanId)
                .pageIndex(1)
                .pageSize(20)
                .build());
        assertEquals(1, groupFiltered.getData().size());
        assertEquals(assignmentPlanId, groupFiltered.getData().get(0).getAssignmentPlanId());
    }

    @Test
    public void should_delete_assignment() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("04:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(2020, 7, 9, 4, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertNotNull(assignment);

        AssignmentApi.deleteAssignment(response.getJwt(), assignment.getId());

        assertFalse(assignmentRepository.exists(assignment.getId()));
    }

    @Test
    public void should_set_operator_for_assignment() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("05:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(2020, 7, 9, 5, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertTrue(assignment.getOperators().isEmpty());

        AssignmentApi.setOperators(response.getJwt(), assignment.getId(), SetAssignmentOperatorsCommand.builder().memberIds(List.of(response.getMemberId())).build());

        Assignment updated = assignmentRepository.byId(assignment.getId());
        assertEquals(1, updated.getOperators().size());
        assertEquals(response.getMemberId(), updated.getOperators().get(0));
    }

    @Test
    public void delete_app_should_also_delete_assignment_under_it() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("06:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(2020, 7, 9, 6, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertTrue(assignment.getOperators().isEmpty());

        AppApi.deleteApp(response.getJwt(), response.getAppId());
        assertFalse(assignmentRepository.exists(assignment.getId()));
    }

    @Test
    public void delete_group_should_also_delete_assignment_for_it() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        String newGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        QrApi.createQr(response.getJwt(), newGroupId);

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("07:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(2020, 7, 9, 7, 0));
        Assignment assignment = assignmentRepository.latestForGroup(newGroupId).get();
        assertNotNull(assignment);

        GroupApi.deleteGroup(response.getJwt(), newGroupId);
        assertFalse(assignmentRepository.exists(assignment.getId()));
    }

    @Test
    public void delete_page_should_also_delete_assignments_for_it() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("08:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(2021, 7, 9, 8, 0));
        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertNotNull(assignment);

        String appId = response.getAppId();
        Page newPage = defaultPage(defaultRadioControl());
        AppApi.updateAppPage(response.getJwt(), appId, newPage);

        assertFalse(assignmentRepository.exists(assignment.getId()));
    }

    @Test
    public void delete_member_should_delete_operator_for_assignments() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        String memberId = MemberApi.createMember(response.getJwt());

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("09:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(2020, 7, 9, 9, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertTrue(assignment.getOperators().isEmpty());

        AssignmentApi.setOperators(response.getJwt(), assignment.getId(), SetAssignmentOperatorsCommand.builder().memberIds(List.of(memberId)).build());

        Assignment updated = assignmentRepository.byId(assignment.getId());
        assertEquals(1, updated.getOperators().size());
        assertEquals(memberId, updated.getOperators().get(0));

        MemberApi.deleteMember(response.getJwt(), memberId);
        assertTrue(assignmentRepository.byId(assignment.getId()).getOperators().isEmpty());
    }

    @Test
    public void should_finish_qr_for_assignment() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        LocalDateTime now = LocalDateTime.now().withMinute(0);
        String startTime = DateTimeFormatter.ofPattern("HH:mm").format(now);

        LocalDateTime expire = now.plusHours(2);
        String expireTime = DateTimeFormatter.ofPattern("HH:mm").format(expire);

        DateTime startDateTime = DateTime.builder().date(now.toLocalDate().toString()).time(startTime).build();
        DateTime expireDateTime = DateTime.builder().date(expire.toLocalDate().toString()).time(expireTime).build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(1, assignment.getAllQrCount());
        assertEquals(0, assignment.getFinishedQrCount());

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        Assignment updated = assignmentRepository.byId(assignment.getId());
        assertEquals(1, updated.getFinishedQrCount());
        assertEquals(1, updated.getFinishedQrs().size());
        AssignmentFinishedQr finishedQr = updated.getFinishedQrs().get(response.getQrId());
        assertEquals(submissionId, finishedQr.getSubmissionId());
        assertEquals(response.getQrId(), finishedQr.getQrId());
        assertEquals(response.getMemberId(), finishedQr.getOperatorId());
        assertNotNull(finishedQr.getFinishedAt());
    }

    @Test
    public void should_not_finished_qr_for_assignment_if_submission_not_fall_in_range() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("10:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(2020, 7, 9, 10, 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        Assignment updated = assignmentRepository.byId(assignment.getId());
        assertEquals(0, updated.getFinishedQrs().size());
    }

    @Test
    public void should_list_assignment_managed_qrs() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        CreateQrResponse newQrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        LocalDateTime now = LocalDateTime.now().withMinute(0);
        String startTime = DateTimeFormatter.ofPattern("HH:mm").format(now);

        LocalDateTime expire = now.plusHours(2);
        String expireTime = DateTimeFormatter.ofPattern("HH:mm").format(expire);

        DateTime startDateTime = DateTime.builder().date(now.toLocalDate().toString()).time(startTime).build();
        DateTime expireDateTime = DateTime.builder().date(expire.toLocalDate().toString()).time(expireTime).build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        AssignmentApi.setOperators(response.getJwt(), assignment.getId(), SetAssignmentOperatorsCommand.builder().memberIds(List.of(response.getMemberId())).build());

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        PagedList<QAssignmentListQr> qrs = AssignmentApi.listAssignmentQrs(response.getJwt(),
                assignment.getId(),
                ListAssignmentQrsQuery.builder()
                        .pageIndex(1)
                        .pageSize(20)
                        .build());
        assertEquals(2, qrs.getData().size());

        QR dbFirstQr = qrRepository.byId(newQrResponse.getQrId());
        QAssignmentListQr firstQr = qrs.getData().get(0);
        assertEquals(newQrResponse.getQrId(), firstQr.getId());
        assertEquals(newQrResponse.getPlateId(), firstQr.getPlateId());
        assertEquals(dbFirstQr.getName(), firstQr.getName());
        assertNull(firstQr.getFinishedAt());
        assertNull(firstQr.getOperatorName());
        assertNull(firstQr.getSubmissionId());

        QR dbSecondQr = qrRepository.byId(response.getQrId());
        Member member = memberRepository.byId(response.getMemberId());
        QAssignmentListQr secondQr = qrs.getData().get(1);
        assertEquals(dbSecondQr.getId(), secondQr.getId());
        assertEquals(dbSecondQr.getPlateId(), secondQr.getPlateId());
        assertEquals(dbSecondQr.getName(), secondQr.getName());
        assertNotNull(secondQr.getFinishedAt());
        assertEquals(member.getName(), secondQr.getOperatorName());
        assertEquals(submissionId, secondQr.getSubmissionId());
        assertEquals(response.getMemberId(), secondQr.getOperatorId());
    }

    @Test
    public void should_search_assignment_qrs() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        CreateQrResponse newQrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        LocalDateTime now = LocalDateTime.now().withMinute(0);
        String startTime = DateTimeFormatter.ofPattern("HH:mm").format(now);

        LocalDateTime expire = now.plusHours(2);
        String expireTime = DateTimeFormatter.ofPattern("HH:mm").format(expire);

        DateTime startDateTime = DateTime.builder().date(now.toLocalDate().toString()).time(startTime).build();
        DateTime expireDateTime = DateTime.builder().date(expire.toLocalDate().toString()).time(expireTime).build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        QR qr = qrRepository.byId(response.getQrId());
        PagedList<QAssignmentListQr> qrs = AssignmentApi.listAssignmentQrs(response.getJwt(),
                assignment.getId(),
                ListAssignmentQrsQuery.builder()
                        .pageIndex(1)
                        .pageSize(20)
                        .search(qr.getName())
                        .build());

        assertEquals(1, qrs.getData().size());
        assertEquals(response.getQrId(), qrs.getData().get(0).getId());

        QR qr2 = qrRepository.byId(newQrResponse.getQrId());
        PagedList<QAssignmentListQr> qrs2 = AssignmentApi.listAssignmentQrs(response.getJwt(),
                assignment.getId(),
                ListAssignmentQrsQuery.builder()
                        .pageIndex(1)
                        .pageSize(20)
                        .search(qr2.getPlateId())
                        .build());
        assertEquals(1, qrs2.getData().size());
        assertEquals(qr2.getId(), qrs2.getData().get(0).getId());

        PagedList<QAssignmentListQr> qrs3 = AssignmentApi.listAssignmentQrs(response.getJwt(),
                assignment.getId(),
                ListAssignmentQrsQuery.builder()
                        .pageIndex(1)
                        .pageSize(20)
                        .finished(true)
                        .build());
        assertEquals(1, qrs3.getData().size());
        assertEquals(qr.getId(), qrs3.getData().get(0).getId());

        PagedList<QAssignmentListQr> qrs4 = AssignmentApi.listAssignmentQrs(response.getJwt(),
                assignment.getId(),
                ListAssignmentQrsQuery.builder()
                        .pageIndex(1)
                        .pageSize(20)
                        .finished(false)
                        .build());
        assertEquals(1, qrs4.getData().size());
        assertEquals(qr2.getId(), qrs4.getData().get(0).getId());
    }

    @Test
    public void should_list_my_assignments() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("11:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_DAY);

        IntStream.range(9, 20).forEach(value -> {
            createAssignmentsJob.run(of(2020, 7, value, 11, 1));
            Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
            AssignmentApi.setOperators(response.getJwt(), assignment.getId(), SetAssignmentOperatorsCommand.builder().memberIds(List.of(response.getMemberId())).build());
        });

        IntStream.range(21, 22).forEach(value -> {
            createAssignmentsJob.run(of(2020, 7, value, 11, 1));
        });

        PagedList<QListAssignment> firstPage = AssignmentApi.listMyAssignments(response.getJwt(), ListMyAssignmentsQuery.builder()
                .appId(response.getAppId())
                .pageIndex(1)
                .pageSize(10)
                .build());

        assertEquals(10, firstPage.getData().size());
        assertEquals(11, firstPage.getTotalNumber());

        PagedList<QListAssignment> secondPage = AssignmentApi.listMyAssignments(response.getJwt(), ListMyAssignmentsQuery.builder()
                .appId(response.getAppId())
                .pageIndex(2)
                .pageSize(10)
                .build());

        assertEquals(1, secondPage.getData().size());
        assertEquals(11, secondPage.getTotalNumber());
    }

    @Test
    public void should_list_my_assignments_filtered_by_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        String groupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        QrApi.createQr(response.getJwt(), groupId);
        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), groupId);
        QrApi.createQr(response.getJwt(), subGroupId);

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("12:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_DAY);
        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId, SetGroupOperatorsCommand.builder().groupId(response.getDefaultGroupId()).memberIds(List.of(response.getMemberId())).build());
        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId, SetGroupOperatorsCommand.builder().groupId(groupId).memberIds(List.of(response.getMemberId())).build());
        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId, SetGroupOperatorsCommand.builder().groupId(subGroupId).memberIds(List.of(response.getMemberId())).build());
        createAssignmentsJob.run(of(2020, 7, 9, 12, 1));

        assertEquals(3, AssignmentApi.listMyAssignments(response.getJwt(), ListMyAssignmentsQuery.builder()
                .appId(response.getAppId())
                .pageIndex(1)
                .pageSize(10)
                .build()).getData().size());

        PagedList<QListAssignment> result = AssignmentApi.listMyAssignments(response.getJwt(), ListMyAssignmentsQuery.builder()
                .appId(response.getAppId())
                .groupId(groupId)
                .pageIndex(1)
                .pageSize(10)
                .build());
        assertEquals(2, result.getData().size());
        List<String> groupIds = result.getData().stream().map(QListAssignment::getGroupId).toList();
        assertTrue(groupIds.contains(groupId));
        assertTrue(groupIds.contains(subGroupId));
    }

    @Test
    public void should_list_my_assignment_qrs() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        IntStream.range(1, 15).forEach(value -> QrApi.createQr(response.getJwt(), response.getDefaultGroupId()));

        LocalDateTime now = LocalDateTime.now().withMinute(0);
        String startTime = DateTimeFormatter.ofPattern("HH:mm").format(now);

        LocalDateTime expire = now.plusHours(2);
        String expireTime = DateTimeFormatter.ofPattern("HH:mm").format(expire);

        DateTime startDateTime = DateTime.builder().date(now.toLocalDate().toString()).time(startTime).build();
        DateTime expireDateTime = DateTime.builder().date(expire.toLocalDate().toString()).time(expireTime).build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        AssignmentApi.setOperators(response.getJwt(), assignment.getId(), SetAssignmentOperatorsCommand.builder().memberIds(List.of(response.getMemberId(), memberResponse.getMemberId())).build());

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        PagedList<QAssignmentListQr> pagedList1 = AssignmentApi.listAssignmentQrs(response.getJwt(),
                assignment.getId(),
                ListAssignmentQrsQuery.builder()
                        .pageIndex(1)
                        .pageSize(10)
                        .build());
        assertEquals(15, pagedList1.getTotalNumber());
        PagedList<QAssignmentListQr> pagedList2 = AssignmentApi.listAssignmentQrs(memberResponse.getJwt(),
                assignment.getId(),
                ListAssignmentQrsQuery.builder()
                        .pageIndex(2)
                        .pageSize(10)
                        .build());

        QR dbSecondQr = qrRepository.byId(response.getQrId());
        Member member = memberRepository.byId(response.getMemberId());
        QAssignmentListQr secondQr = pagedList2.getData().get(4);
        assertTrue(secondQr.isFinished());
        assertEquals(dbSecondQr.getId(), secondQr.getId());
        assertEquals(dbSecondQr.getPlateId(), secondQr.getPlateId());
        assertEquals(dbSecondQr.getName(), secondQr.getName());
        assertNotNull(secondQr.getFinishedAt());
        assertEquals(member.getName(), secondQr.getOperatorName());
        assertEquals(submissionId, secondQr.getSubmissionId());
    }

    @Test
    public void should_list_my_assignment_qrs_by_geolocation() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr3Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        Geolocation geolocation1 = Geolocation.builder()
                .address(rAddress())
                .point(Geopoint.builder().longitude(120f).latitude(31f).build())
                .build();
        Geolocation geolocation2 = Geolocation.builder()
                .address(rAddress())
                .point(Geopoint.builder().longitude(120f).latitude(33f).build())
                .build();
        Geolocation geolocation3 = Geolocation.builder()
                .address(rAddress())
                .point(Geopoint.builder().longitude(120f).latitude(30f).build())
                .build();
        QrApi.updateQrBaseSetting(response.getJwt(), qr1Response.getQrId(), UpdateQrBaseSettingCommand.builder().name(rQrName()).geolocation(geolocation1).build());
        QrApi.updateQrBaseSetting(response.getJwt(), qr2Response.getQrId(), UpdateQrBaseSettingCommand.builder().name(rQrName()).geolocation(geolocation2).build());
        QrApi.updateQrBaseSetting(response.getJwt(), qr3Response.getQrId(), UpdateQrBaseSettingCommand.builder().name(rQrName()).geolocation(geolocation3).build());

        Geopoint currentPoint = Geopoint.builder().longitude(120f).latitude(29f).build();

        LocalDateTime now = LocalDateTime.now().withMinute(0);
        String startTime = DateTimeFormatter.ofPattern("HH:mm").format(now);

        LocalDateTime expire = now.plusHours(2);
        String expireTime = DateTimeFormatter.ofPattern("HH:mm").format(expire);

        DateTime startDateTime = DateTime.builder().date(now.toLocalDate().toString()).time(startTime).build();
        DateTime expireDateTime = DateTime.builder().date(expire.toLocalDate().toString()).time(expireTime).build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        AssignmentApi.setOperators(response.getJwt(), assignment.getId(), SetAssignmentOperatorsCommand.builder().memberIds(List.of(response.getMemberId())).build());

        PagedList<QAssignmentListQr> pagedList = AssignmentApi.listAssignmentQrs(response.getJwt(),
                assignment.getId(),
                ListAssignmentQrsQuery.builder()
                        .pageIndex(1)
                        .pageSize(10)
                        .nearestPointEnabled(true)
                        .currentPoint(currentPoint)
                        .build());

        assertEquals(3, pagedList.getData().size());
        assertEquals(qr3Response.getQrId(), pagedList.getData().get(0).getId());
        assertEquals(qr1Response.getQrId(), pagedList.getData().get(1).getId());
        assertEquals(qr2Response.getQrId(), pagedList.getData().get(2).getId());
    }

    @Test
    public void should_list_my_assignment_qrs_if_member_is_not_assignment_operator() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        LocalDateTime now = LocalDateTime.now().withMinute(0);
        String startTime = DateTimeFormatter.ofPattern("HH:mm").format(now);

        LocalDateTime expire = now.plusHours(2);
        String expireTime = DateTimeFormatter.ofPattern("HH:mm").format(expire);

        DateTime startDateTime = DateTime.builder().date(now.toLocalDate().toString()).time(startTime).build();
        DateTime expireDateTime = DateTime.builder().date(expire.toLocalDate().toString()).time(expireTime).build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();

        assertError(() -> AssignmentApi.listAssignmentQrsRaw(memberResponse.getJwt(),
                assignment.getId(),
                ListAssignmentQrsQuery.builder()
                        .pageIndex(1)
                        .pageSize(10)
                        .build()), ACCESS_DENIED);
    }

    @Test
    public void manager_should_fetch_assignment_detail() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("13:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);
        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId, SetGroupOperatorsCommand.builder().groupId(response.getDefaultGroupId()).memberIds(List.of(response.getMemberId())).build());
        createAssignmentsJob.run(of(2020, 7, 9, 13, 1));
        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();

        QAssignmentDetail detail = AssignmentApi.fetchAssignmentDetail(response.getJwt(), assignment.getId());
        assertEquals(assignment.getId(), detail.getId());
        assertEquals(assignment.getName(), detail.getName());
        assertEquals(assignment.getGroupId(), detail.getGroupId());
        assertEquals(assignment.getPageId(), detail.getPageId());
        assertEquals(assignment.getAllQrCount(), detail.getAllQrCount());
        assertEquals(assignment.getFinishedQrCount(), detail.getFinishedQrCount());
        assertEquals(assignment.getStatus(), detail.getStatus());
        assertEquals(assignment.getStartAt(), detail.getStartAt());
        assertEquals(assignment.getExpireAt(), detail.getExpireAt());
        Member member = memberRepository.byId(response.getMemberId());
        assertTrue(detail.getOperatorNames().contains(member.getName()));
    }

    @Test
    public void assignment_operator_should_fetch_assignment_detail() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("14:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);
        AssignmentPlanApi.setGroupOperators(response.getJwt(), assignmentPlanId, SetGroupOperatorsCommand.builder().groupId(response.getDefaultGroupId()).memberIds(List.of(memberResponse.getMemberId())).build());
        createAssignmentsJob.run(of(2020, 7, 9, 14, 1));
        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();

        QAssignmentDetail detail = AssignmentApi.fetchAssignmentDetail(memberResponse.getJwt(), assignment.getId());
        assertEquals(assignment.getId(), detail.getId());
    }

    @Test
    public void non_assignment_operator_should_fail_fetch_assignment_detail() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        DateTime startDateTime = DateTime.builder().date("2020-07-09").time("14:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-09").time("23:00").build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);
        createAssignmentsJob.run(of(2020, 7, 9, 14, 1));
        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();

        assertError(() -> AssignmentApi.fetchAssignmentDetailRaw(memberResponse.getJwt(), assignment.getId()), ACCESS_DENIED);
    }

    @Test
    public void should_fetch_assignment_qr_detail() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        LocalDateTime now = LocalDateTime.now().withMinute(0);
        String startTime = DateTimeFormatter.ofPattern("HH:mm").format(now);

        LocalDateTime expire = now.plusHours(2);
        String expireTime = DateTimeFormatter.ofPattern("HH:mm").format(expire);

        DateTime startDateTime = DateTime.builder().date(now.toLocalDate().toString()).time(startTime).build();
        DateTime expireDateTime = DateTime.builder().date(expire.toLocalDate().toString()).time(expireTime).build();

        String assignmentPlanId = createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_MONTH);

        createAssignmentsJob.run(of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), 0));

        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        AssignmentApi.setOperators(response.getJwt(), assignment.getId(), SetAssignmentOperatorsCommand.builder().memberIds(List.of(memberResponse.getMemberId())).build());

        assertEquals(1, assignment.getAllQrCount());
        assertEquals(0, assignment.getFinishedQrCount());
        QAssignmentQrDetail qrDetail = AssignmentApi.fetchAssignmentQrDetail(response.getJwt(), assignment.getId(), response.getQrId());
        assertEquals(assignment.getId(), qrDetail.getAssignmentId());
        assertEquals(IN_PROGRESS, qrDetail.getStatus());
        assertEquals(1, qrDetail.getAllQrCount());
        assertEquals(0, qrDetail.getFinishedQrCount());
        assertEquals(response.getQrId(), qrDetail.getQrId());
        assertFalse(qrDetail.isFinished());

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        QAssignmentQrDetail updatedQrDetail = AssignmentApi.fetchAssignmentQrDetail(response.getJwt(), assignment.getId(), response.getQrId());
        assertEquals(assignment.getId(), updatedQrDetail.getAssignmentId());
        assertEquals(SUCCEED, updatedQrDetail.getStatus());
        assertEquals(1, updatedQrDetail.getAllQrCount());
        assertEquals(1, updatedQrDetail.getFinishedQrCount());
        assertEquals(response.getQrId(), updatedQrDetail.getQrId());
        assertTrue(updatedQrDetail.isFinished());
        assertEquals(submissionId, updatedQrDetail.getSubmissionId());
        assertEquals(response.getMemberId(), updatedQrDetail.getOperatorId());
        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(member.getName(), updatedQrDetail.getOperatorName());
        assertNotNull(updatedQrDetail.getFinishedAt());

        QAssignmentQrDetail newMemberQrDetail = AssignmentApi.fetchAssignmentQrDetail(memberResponse.getJwt(), assignment.getId(), response.getQrId());
        assertEquals(assignment.getId(), newMemberQrDetail.getAssignmentId());
        assertEquals(member.getName(), newMemberQrDetail.getOperatorName());
    }

    @Test
    public void should_cache_open_assignment_app_pages() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2020-07-08").time("15:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-07-08").time("23:00").build();

        createAssignmentPlan(startDateTime, expireDateTime, response.getAppId(), response.getHomePageId(), response.getJwt(), EVERY_DAY);

        createAssignmentsJob.run(of(2020, 7, 8, 15, 0));

        List<String> pageIds = assignmentRepository.cachedOpenAssignmentPages(response.getAppId());
        assertTrue(pageIds.contains(response.getHomePageId()));
        String key = "Cache:OPEN_ASSIGNMENT_PAGES::" + response.getAppId();
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));
    }

    private String createAssignmentPlan(DateTime startDateTime, DateTime expireDateTime, String appId, String pageId, String jwt, AssignmentFrequency frequency) {
        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(appId)
                .pageId(pageId)
                .frequency(frequency)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(false)
                .nearExpireNotifyTime(DateTime.builder().build())
                .build();

        return AssignmentPlanApi.createAssignmentPlan(jwt, CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());
    }
}
