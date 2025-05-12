package com.mryqr.core.assignment.domain.task;


import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.domain.AssignmentFinishedQr;
import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.common.domain.task.RepeatableTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static com.mryqr.core.common.domain.user.User.NOUSER;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinishQrForAssignmentsTask implements RepeatableTask {
    private final AssignmentRepository assignmentRepository;

    public void run(String qrId, String submissionId, String appId, String pageId, String operatorId, Instant finishedAt) {
        //通过缓存快速短路
        if (!assignmentRepository.cachedOpenAssignmentPages(appId).contains(pageId)) {
            return;
        }

        List<Assignment> assignments = assignmentRepository.openAssignmentsFor(qrId, appId, pageId);
        assignments.forEach(assignment -> {
            AssignmentFinishedQr finishedQr = AssignmentFinishedQr.builder()
                    .qrId(qrId)
                    .submissionId(submissionId)
                    .operatorId(operatorId)
                    .finishedAt(finishedAt)
                    .build();
            boolean success = assignment.finishQr(finishedQr, NOUSER);
            if (success) {
                log.info("Finished QR[{}] for assignment[{}].", qrId, assignment.getId());
                assignmentRepository.save(assignment);
            }
        });
    }
}
