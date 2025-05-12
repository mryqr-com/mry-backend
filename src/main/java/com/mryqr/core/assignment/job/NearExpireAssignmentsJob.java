package com.mryqr.core.assignment.job;

import com.mryqr.common.notification.CompositeNotificationService;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.domain.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.assignment.domain.Assignment.newAssignmentId;
import static java.time.ZoneId.systemDefault;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class NearExpireAssignmentsJob {
    private static final int BATCH_SIZE = 100;
    private final AssignmentRepository assignmentRepository;
    private final AppRepository appRepository;
    private final CompositeNotificationService notificationService;

    public void run(LocalDateTime givenTime) {
        Instant nearExpireTime = givenTime.withMinute(0).withSecond(0).withNano(0).atZone(systemDefault()).toInstant();
        String timeString = MRY_DATE_TIME_FORMATTER.format(nearExpireTime);
        log.debug("Started near expire assignments job for time[{}].", timeString);

        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        String startId = newAssignmentId();

        try {
            while (true) {
                List<Assignment> assignments = assignmentRepository.nearExpiredAssignments(nearExpireTime, startId, BATCH_SIZE);
                if (isEmpty(assignments)) {
                    break;
                }

                forkJoinPool.submit(() -> assignments.parallelStream().forEach(assignment -> {
                    try {
                        assignment.calculateStatus(NO_USER);
                        log.info("Set assignment[{}] status to {}.", assignment.getId(), assignment.getStatus());
                        assignmentRepository.save(assignment);
                    } catch (Throwable t) {
                        log.error("Failed to try near expire assignment[{}].", assignment.getId(), t);
                    }
                })).join();

                startId = assignments.get(assignments.size() - 1).getId();//下一次直接从最后一条开始查询
            }
        } finally {
            forkJoinPool.shutdown();
        }

        log.debug("Finished near expire assignments for time[{}].", timeString);
    }

}
