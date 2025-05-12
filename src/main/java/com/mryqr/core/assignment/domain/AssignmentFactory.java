package com.mryqr.core.assignment.domain;

import com.mryqr.core.assignmentplan.domain.AssignmentPlan;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

import static com.mryqr.core.common.exception.ErrorCode.ASSIGNMENT_ALREADY_EXISTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentFactory {
    private final AssignmentRepository assignmentRepository;

    public Assignment create(String groupId, Set<String> qrIds, LocalDateTime startTime, AssignmentPlan assignmentPlan, User user) {
        if (assignmentRepository.exists(assignmentPlan.getId(), groupId, startTime)) {
            throw new MryException(ASSIGNMENT_ALREADY_EXISTS, "任务已经存在。", "groupId", groupId, "startAt", startTime);
        }

        return new Assignment(groupId, qrIds, startTime, assignmentPlan, user);
    }
}
