package com.mryqr.core.job;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.job.CreateAssignmentsJob;
import com.mryqr.core.assignment.job.NearExpireAssignmentsJob;
import com.mryqr.core.assignmentplan.AssignmentPlanApi;
import com.mryqr.core.assignmentplan.command.CreateAssignmentPlanCommand;
import com.mryqr.core.assignmentplan.domain.AssignmentSetting;
import com.mryqr.core.assignmentplan.domain.DateTime;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static com.mryqr.core.assignment.domain.AssignmentStatus.IN_PROGRESS;
import static com.mryqr.core.assignment.domain.AssignmentStatus.NEAR_EXPIRE;
import static com.mryqr.core.assignmentplan.domain.AssignmentFrequency.EVERY_MONTH;
import static com.mryqr.utils.RandomTestFixture.rAssignmentPlanName;
import static java.time.LocalDateTime.of;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class NearExpireAssignmentJobTest extends BaseApiTest {
    @Autowired
    private CreateAssignmentsJob createAssignmentsJob;

    @Autowired
    private NearExpireAssignmentsJob nearExpireAssignmentsJob;

    @Test
    public void should_near_expire_notify_assignments() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.setAppAssignmentEnabled(response.getJwt(), response.getAppId(), true);

        LocalDateTime nearExpireTime = LocalDateTime.now().withMinute(0);
        LocalDateTime startTime = nearExpireTime.minusHours(1);
        LocalDateTime expireTime = nearExpireTime.plusHours(1);

        DateTime startDateTime = DateTime.builder().date(startTime.toLocalDate().toString()).time(ofPattern("HH:mm").format(startTime)).build();
        DateTime expireDateTime = DateTime.builder().date(expireTime.toLocalDate().toString()).time(ofPattern("HH:mm").format(expireTime))
                .build();
        DateTime nearExpireDateTime = DateTime.builder().date(nearExpireTime.toLocalDate().toString())
                .time(ofPattern("HH:mm").format(nearExpireTime)).build();

        AssignmentSetting assignmentSetting = AssignmentSetting.builder()
                .name(rAssignmentPlanName())
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .frequency(EVERY_MONTH)
                .startTime(startDateTime)
                .expireTime(expireDateTime)
                .nearExpireNotifyEnabled(true)
                .nearExpireNotifyTime(nearExpireDateTime)
                .build();

        AssignmentPlanApi.createAssignmentPlan(response.getJwt(),
                CreateAssignmentPlanCommand.builder().setting(assignmentSetting).build());

        createAssignmentsJob.run(of(startTime.getYear(), startTime.getMonthValue(), startTime.getDayOfMonth(), startTime.getHour(), 0));
        Assignment assignment = assignmentRepository.latestForGroup(response.getDefaultGroupId()).get();
        assertEquals(IN_PROGRESS, assignment.getStatus());

        nearExpireAssignmentsJob.run(LocalDateTime.now());
        Assignment updated = assignmentRepository.byId(assignment.getId());
        assertEquals(NEAR_EXPIRE, updated.getStatus());
    }
}
