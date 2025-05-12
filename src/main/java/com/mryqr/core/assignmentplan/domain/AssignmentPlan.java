package com.mryqr.core.assignmentplan.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.assignmentplan.domain.event.AssignmentPlanDeletedEvent;
import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mryqr.core.common.utils.MryConstants.ASSIGNMENT_PLAN_COLLECTION;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static java.time.ZoneId.systemDefault;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;
import static org.apache.commons.collections4.MapUtils.isEmpty;

@Getter
@Document(ASSIGNMENT_PLAN_COLLECTION)
@TypeAlias(ASSIGNMENT_PLAN_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class AssignmentPlan extends AggregateRoot {
    private String name;
    private AssignmentSetting setting;
    private List<String> excludedGroups;
    private Map<String, List<String>> groupOperators;//groupId -> memberIds
    private boolean active;

    public AssignmentPlan(AssignmentSetting setting, User user) {
        super(newAssignmentPlanId(), user);
        setSetting(setting);
        this.active = true;
        addOpsLog("新建", user);
    }

    public static String newAssignmentPlanId() {
        return "ASP" + newSnowflakeId();
    }

    public void updateSetting(AssignmentSetting setting, App app, User user) {
        setting.validate(app);
        setSetting(setting);
        addOpsLog("更新设置", user);
    }

    private void setSetting(AssignmentSetting setting) {
        this.setting = setting;
        this.name = setting.getName();
    }

    public void excludeGroups(List<String> excludedGroups, User user) {
        this.excludedGroups = excludedGroups;
        addOpsLog("排除分组", user);
    }

    public void setGroupOperators(String groupId, List<String> memberIds, User user) {
        if (isEmpty(groupOperators)) {
            groupOperators = new HashMap<>();
        }

        groupOperators.put(groupId, memberIds);
        addOpsLog("更新分组执行人", user);
    }

    public void removeOperator(String memberId, User user) {
        if (isEmpty(groupOperators)) {
            return;
        }

        groupOperators.values().forEach(operators -> operators.remove(memberId));
        addOpsLog("移除执行人[" + memberId + "]", user);
    }

    public void activate(User user) {
        if (this.active) {
            return;
        }

        this.active = true;
        addOpsLog("启用", user);
    }

    public void deactivate(User user) {
        if (!this.active) {
            return;
        }

        this.active = false;
        addOpsLog("禁用", user);
    }

    public boolean containsOperator(String memberId) {
        if (isEmpty(groupOperators)) {
            return false;
        }

        return groupOperators.values().stream().anyMatch(operators -> operators.contains(memberId));
    }

    public List<String> operatorsForGroup(String groupId) {
        return emptyIfNull(emptyIfNull(groupOperators).get(groupId));
    }

    public List<String> getExcludedGroups() {
        return emptyIfNull(excludedGroups);
    }

    public Map<String, List<String>> getGroupOperators() {
        return emptyIfNull(groupOperators);
    }

    public String getAppId() {
        return this.setting.getAppId();
    }

    public String getPageId() {
        return this.setting.getPageId();
    }

    public boolean startTimeMatches(LocalDateTime givenStartTime) {
        return this.setting.startTimeMatches(givenStartTime);
    }

    public String getName() {
        return setting.getName();
    }

    public AssignmentFrequency getFrequency() {
        return setting.getFrequency();
    }

    public long cycleIndexOf(LocalDateTime startTime) {
        return setting.cycleIndexOf(startTime);
    }

    public Instant expireAtFor(long cycleIndex) {
        return setting.expireAtFor(cycleIndex).atZone(systemDefault()).toInstant();
    }

    public Instant nearExpireNotifyAtFor(long cycleIndex) {
        if (!setting.isNearExpireNotifyEnabled()) {
            return null;
        }

        return setting.nearExpireNotifyAtFor(cycleIndex).atZone(systemDefault()).toInstant();
    }

    public long secondsBetweenStartAndExpire() {
        return setting.secondsBetweenStartAndExpire();
    }

    public long secondsBetweenStartAndNearEndNotify() {
        return setting.secondsBetweenStartAndNearEndNotify();
    }

    public Instant nextAssignmentStartAt() {
        return setting.nextAssignmentStartAt().atZone(systemDefault()).toInstant();
    }

    public void onDelete(User user) {
        raiseEvent(new AssignmentPlanDeletedEvent(this.getId(), user));
    }
}
