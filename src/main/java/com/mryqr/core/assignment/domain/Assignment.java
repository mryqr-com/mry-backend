package com.mryqr.core.assignment.domain;

import com.mryqr.core.assignment.event.AssignmentCreatedEvent;
import com.mryqr.core.assignmentplan.domain.AssignmentFrequency;
import com.mryqr.core.assignmentplan.domain.AssignmentPlan;
import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mryqr.core.assignment.domain.AssignmentStatus.FAILED;
import static com.mryqr.core.assignment.domain.AssignmentStatus.IN_PROGRESS;
import static com.mryqr.core.assignment.domain.AssignmentStatus.NEAR_EXPIRE;
import static com.mryqr.core.assignment.domain.AssignmentStatus.SUCCEED;
import static com.mryqr.core.common.exception.ErrorCode.ASSIGNMENT_CLOSED;
import static com.mryqr.core.common.utils.MryConstants.ASSIGNMENT_COLLECTION;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static java.time.ZoneId.systemDefault;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Getter
@Document(ASSIGNMENT_COLLECTION)
@TypeAlias(ASSIGNMENT_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class Assignment extends AggregateRoot {
    private String assignmentPlanId;
    private String name;
    private String appId;
    private String pageId;
    private String groupId;
    private Instant startAt;
    private Instant expireAt;
    private Instant nearExpireNotifyAt;
    private long cycleIndex;
    private Set<String> allQrIds;
    private int allQrCount;
    private AssignmentFrequency frequency;

    private Map<String, AssignmentFinishedQr> finishedQrs;
    private int finishedQrCount;
    private List<String> operators;
    private AssignmentStatus status;

    public Assignment(String groupId, Set<String> qrIds, LocalDateTime startTime, AssignmentPlan assignmentPlan, User user) {
        super(newAssignmentId(), assignmentPlan.getTenantId(), user);
        this.assignmentPlanId = assignmentPlan.getId();
        this.name = assignmentPlan.getName();
        this.appId = assignmentPlan.getAppId();
        this.pageId = assignmentPlan.getPageId();
        this.groupId = groupId;
        this.startAt = startTime.atZone(systemDefault()).toInstant();
        this.cycleIndex = assignmentPlan.cycleIndexOf(startTime);
        this.expireAt = assignmentPlan.expireAtFor(this.cycleIndex);
        this.nearExpireNotifyAt = assignmentPlan.nearExpireNotifyAtFor(this.cycleIndex);
        tryFixTime(assignmentPlan);
        this.allQrIds = qrIds;
        this.allQrCount = qrIds.size();
        this.frequency = assignmentPlan.getFrequency();
        this.finishedQrs = Map.of();
        this.finishedQrCount = 0;
        this.operators = assignmentPlan.operatorsForGroup(groupId);
        this.status = IN_PROGRESS;
        raiseEvent(new AssignmentCreatedEvent(this.getId(), user));
        addOpsLog("新建", user);
    }

    public static String newAssignmentId() {
        return "ASM" + newSnowflakeId();
    }

    private void tryFixTime(AssignmentPlan assignmentPlan) {//由于月份的天数不相同，有可能导致时间错乱，故修正
        if (!this.expireAt.isAfter(this.startAt)) {
            this.expireAt = this.startAt.plusSeconds(assignmentPlan.secondsBetweenStartAndExpire());
        }

        if (this.nearExpireNotifyAt != null) {
            if (!this.nearExpireNotifyAt.isAfter(this.startAt)) {
                this.nearExpireNotifyAt = this.startAt.plusSeconds(assignmentPlan.secondsBetweenStartAndNearEndNotify());
            }
            if (!this.nearExpireNotifyAt.isBefore(this.expireAt)) {
                this.nearExpireNotifyAt = null;
            }
        }
    }

    public void setOperators(List<String> operatorIds, User user) {
        if (isClosed()) {
            throw new MryException(ASSIGNMENT_CLOSED, "任务已关闭，无法设置执行人。", "assignmentId", this.getId());
        }

        this.operators = operatorIds;
        addOpsLog("设置执行人", user);
    }

    public boolean finishQr(AssignmentFinishedQr finishedQr, User user) {
        if (isClosed()) {
            log.warn("Assignment[{}] is already closed, cannot add finished QR[{}] further.", this.getId(), finishedQr.getQrId());
            return false;
        }

        if (!this.allQrIds.contains(finishedQr.getQrId())) {
            log.warn("Assignment[{}] does not contain QR[{}], cannot add it.", this.getId(), finishedQr.getQrId());
            return false;
        }

        if (isQrFallInRange(finishedQr)) {
            this.finishedQrs.put(finishedQr.getQrId(), finishedQr);
            this.finishedQrCount = this.finishedQrs.size();
            this.status = deriveStatus();
            addOpsLog("加入已完成实例[" + finishedQr.getSubmissionId() + "]", user);
            return true;
        } else {
            log.warn("QR[{}] finished time falls outside of assignment[{}] time range.", finishedQr.getQrId(), this.getId());
            return false;
        }
    }

    private boolean isQrFallInRange(AssignmentFinishedQr finishedQr) {
        Instant finishedAt = finishedQr.getFinishedAt();
        return finishedAt.isAfter(this.startAt) && finishedAt.isBefore(this.expireAt);
    }

    public void calculateStatus(User user) {
        this.status = deriveStatus();
        addOpsLog("设置状态为" + this.status.getName(), user);
    }

    private AssignmentStatus deriveStatus() {
        if (isClosed()) {
            return this.status;
        }

        if (isAllQrFinished()) {
            return SUCCEED;
        }

        if (isExpired()) {
            return FAILED;
        }

        if (isNearExpire()) {
            return NEAR_EXPIRE;
        }

        return IN_PROGRESS;
    }

    private boolean isClosed() {
        return this.status.isClosed();
    }

    private boolean isAllQrFinished() {
        return this.finishedQrs.keySet().containsAll(this.allQrIds);
    }

    private boolean isExpired() {
        return Instant.now().isAfter(this.expireAt);
    }

    private boolean isNearExpire() {
        return this.nearExpireNotifyAt != null && !isExpired() && Instant.now().isAfter(this.nearExpireNotifyAt);
    }
}
