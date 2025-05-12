package com.mryqr.core.job;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.job.CreateAssignmentsJob;
import com.mryqr.core.assignment.job.ExpireAssignmentsJob;
import com.mryqr.core.assignmentplan.AssignmentPlanApi;
import com.mryqr.core.assignmentplan.command.CreateAssignmentPlanCommand;
import com.mryqr.core.assignmentplan.domain.AssignmentSetting;
import com.mryqr.core.assignmentplan.domain.DateTime;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import static com.mryqr.core.assignment.domain.AssignmentStatus.FAILED;
import static com.mryqr.core.assignment.domain.AssignmentStatus.IN_PROGRESS;
import static com.mryqr.core.assignmentplan.domain.AssignmentFrequency.EVERY_MONTH;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.utils.RandomTestFixture.rAssignmentPlanName;
import static java.time.LocalDateTime.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class ExpireAssignmentJobTest extends BaseApiTest {
    @Autowired
    private CreateAssignmentsJob createAssignmentsJob;


    @Autowired
    private ExpireAssignmentsJob expireAssignmentsJob;

    @Test
    public void should_expire_assignments() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        DateTime startDateTime = DateTime.builder().date("2020-08-10").time("01:00").build();
        DateTime expireDateTime = DateTime.builder().date("2020-08-10").time("02:00").build();

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

        String assignmentPlanId = AssignmentPlanApi.createAssignmentPlan(response.getJwt(), CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        createAssignmentsJob.run(of(2020, 8, 10, 1, 1));
        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(IN_PROGRESS, assignment.getStatus());

        expireAssignmentsJob.run(of(2020, 8, 10, 2, 1));
        Assignment updated = assignmentRepository.byId(assignment.getId());
        assertEquals(FAILED, updated.getStatus());
    }
}
