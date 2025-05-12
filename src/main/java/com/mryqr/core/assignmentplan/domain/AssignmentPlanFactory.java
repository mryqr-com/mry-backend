package com.mryqr.core.assignmentplan.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.exception.ErrorCode.APP_ASSIGNMENT_NOT_ENABLED;
import static com.mryqr.core.common.exception.ErrorCode.ASSIGNMENT_PLAN_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.MAX_ASSIGNMENT_PLAN_REACHED;
import static com.mryqr.core.common.utils.MapUtils.mapOf;

@Component
@RequiredArgsConstructor
public class AssignmentPlanFactory {
    private final AssignmentPlanRepository assignmentPlanRepository;

    public AssignmentPlan createAssignmentPlan(AssignmentSetting setting, App app, User user) {
        if (assignmentPlanRepository.assignmentPlanCount(app.getId()) >= 5) {
            throw new MryException(MAX_ASSIGNMENT_PLAN_REACHED, "一个应用最多只能创建5个任务计划。");
        }

        checkNameDuplication(setting.getName(), app.getId());
        checkAppAssignmentEnabled(app);

        setting.validate(app);
        return new AssignmentPlan(setting, user);
    }

    private void checkAppAssignmentEnabled(App app) {
        if (!app.isAssignmentEnabled()) {
            throw new MryException(APP_ASSIGNMENT_NOT_ENABLED, "创建失败，应用尚未开启任务管理功能。");
        }
    }

    private void checkNameDuplication(String name, String appId) {
        if (assignmentPlanRepository.existsByName(name, appId)) {
            throw new MryException(ASSIGNMENT_PLAN_WITH_NAME_ALREADY_EXISTS, "创建失败，名称已被占用。",
                    mapOf("name", name, "appId", appId));
        }
    }
}
