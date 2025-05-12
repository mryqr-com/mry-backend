package com.mryqr.common.notification.inapp;

import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.notification.NotificationService;
import com.mryqr.common.properties.PropertyService;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.setting.notification.NotificationRole;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.inappnotification.domain.InAppNotification;
import com.mryqr.core.inappnotification.domain.InAppNotificationFactory;
import com.mryqr.core.inappnotification.domain.InAppNotificationRepository;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionApproval;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mryqr.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.app.domain.page.setting.notification.NotificationRole.*;
import static com.mryqr.management.crm.MryTenantManageApp.MRY_TENANT_MANAGE_APP_ID;
import static com.mryqr.management.crm.MryTenantManageApp.TENANT_SYNC_PAGE_ID;
import static java.util.Collections.singleton;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationService implements NotificationService {
    private final InAppNotificationRepository inAppNotificationRepository;
    private final InAppNotificationFactory inAppNotificationFactory;
    private final PropertyService propertyService;
    private final QrRepository qrRepository;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;

    @Override
    public void notifyOnSubmissionCreated(Submission submission, Page page, App app, List<NotificationRole> notifyRoles) {
        String createdBy = submission.getCreatedBy();
        Group group = groupRepository.cachedById(submission.getGroupId());
        Set<String> memberIds = toBeNotifiedMemberIdsForSubmission(app, group, notifyRoles, createdBy);
        if (isEmpty(memberIds)) {
            return;
        }

        String url = propertyService.submissionUrlOf(submission.getId(), page.getId(), submission.getPlateId());
        String qrName = qrRepository.qrNameOf(submission.getQrId());
        String submitterName = isNotBlank(createdBy) ? memberRepository.cachedMemberNameOf(createdBy) : "匿名";
        String content;
        if (app.getId().equals(MRY_TENANT_MANAGE_APP_ID) && page.getId().equals(TENANT_SYNC_PAGE_ID)) {
            content = "码如云有新租户注册了（租户名：" + qrName + "）";
        } else {
            content = "新表单提交（" + app.instanceDesignation() + "：" + qrName +
                      "，表单：" + page.pageName() +
                      "，" + page.submitterDesignation() + "：" + submitterName +
                      "）";
        }
        List<InAppNotification> notifications = memberIds.stream()
                .map(memberId -> inAppNotificationFactory.createInAppNotification(memberId, submission.getTenantId(), url, url, content))
                .toList();

        inAppNotificationRepository.insert(notifications);
    }

    @Override
    public void notifyOnSubmissionUpdated(Submission submission, Page page, App app, List<NotificationRole> notifyRoles) {
        String updatedBy = submission.getUpdatedBy();
        Group group = groupRepository.cachedById(submission.getGroupId());
        Set<String> memberIds = toBeNotifiedMemberIdsForSubmission(app, group, notifyRoles, updatedBy);
        if (isEmpty(memberIds)) {
            return;
        }

        String url = propertyService.submissionUrlOf(submission.getId(), page.getId(), submission.getPlateId());
        String updaterName = isNotBlank(updatedBy) ? memberRepository.cachedMemberNameOf(updatedBy) : "匿名";
        String qrName = qrRepository.qrNameOf(submission.getQrId());
        String content = "表单更新（" + app.instanceDesignation() + "：" + qrName +
                         "，表单：" + page.pageName() +
                         "，更新人" + "：" + updaterName +
                         "）";

        List<InAppNotification> notifications = memberIds.stream()
                .map(memberId -> inAppNotificationFactory.createInAppNotification(memberId, submission.getTenantId(), url, url, content))
                .toList();

        inAppNotificationRepository.insert(notifications);
    }

    @Override
    public void notifyApproverOnSubmissionCreated(Submission submission, Page page, App app) {
        String createdBy = submission.getCreatedBy();
        Permission requiredApprovalPermission = page.requiredApprovalPermission();
        Set<String> toBeNotifiedMemberIds = new HashSet<>();
        Group group = groupRepository.cachedById(submission.getGroupId());
        if (requiredApprovalPermission == CAN_MANAGE_GROUP) {
            toBeNotifiedMemberIds.addAll(group.getManagers());
            if (isEmpty(toBeNotifiedMemberIds)) {//如果没有分组管理员，则通知应用管理员
                toBeNotifiedMemberIds.addAll(app.getManagers());
            }
        } else if (requiredApprovalPermission == CAN_MANAGE_APP) {
            toBeNotifiedMemberIds.addAll(app.getManagers());
        }

        if (isEmpty(toBeNotifiedMemberIds)) {
            return;
        }

        String url = propertyService.submissionUrlOf(submission.getId(), page.getId(), submission.getPlateId());
        String qrName = qrRepository.qrNameOf(submission.getQrId());
        String submitterName = isNotBlank(createdBy) ? memberRepository.cachedMemberNameOf(createdBy) : "匿名";

        String content = "有新表单需要您审批（" + app.instanceDesignation() + "：" + qrName +
                         "，表单：" + page.pageName() +
                         "，" + page.submitterDesignation() + "：" + submitterName +
                         "）";

        List<InAppNotification> notifications = toBeNotifiedMemberIds.stream()
                .map(memberId -> inAppNotificationFactory.createInAppNotification(memberId, submission.getTenantId(), url, url, content))
                .toList();

        inAppNotificationRepository.insert(notifications);
    }

    @Override
    public void notifySubmitterOnSubmissionApproved(Submission submission, Page page, App app, SubmissionApproval approval) {
        String createdBy = submission.getCreatedBy();
        String approvedBy = approval.getApprovedBy();

        if (isBlank(createdBy)) {
            return;
        }

        String url = propertyService.submissionUrlOf(submission.getId(), page.getId(), submission.getPlateId());
        String approveResult = approval.isPassed() ? page.approvalPassText() : page.approvalNotPassText();
        String pageName = page.pageName();
        String approverName = isNotBlank(approvedBy) ? memberRepository.cachedMemberNameOf(approvedBy) : "匿名";
        String qrName = qrRepository.qrNameOf(submission.getQrId());
        String instanceDesignation = app.instanceDesignation();
        String content = "您提交的表单已完成审批（" + instanceDesignation + "：" + qrName +
                         "，表单：" + pageName +
                         "，审批人：" + approverName +
                         "，结果：" + approveResult +
                         "）";

        InAppNotification notification = inAppNotificationFactory.createInAppNotification(createdBy, submission.getTenantId(), url, url, content);
        inAppNotificationRepository.insert(notification);
    }

    @Override
    public void notifyOperatorsOnAssignmentCreated(Assignment assignment, App app) {
        List<String> operators = assignment.getOperators();
        if (isEmpty(operators)) {
            return;
        }

        String operatorNames = memberRepository.cachedAllMemberReferences(assignment.getTenantId()).stream()
                .filter(memberReference -> operators.contains(memberReference.getId()))
                .map(MemberReference::getName)
                .collect(Collectors.joining(","));

        Group group = groupRepository.cachedById(assignment.getGroupId());
        String startTime = MRY_DATE_TIME_FORMATTER.format(assignment.getStartAt());
        String endTime = MRY_DATE_TIME_FORMATTER.format(assignment.getExpireAt());
        String pcUrl = this.propertyService.pcAssignmentUrlOf(assignment);
        String mobileUrl = this.propertyService.mobileAssignmentUrlOf(assignment);
        String groupDesignation = app.groupDesignation();
        String content = "您有新任务需要完成（任务名称：" + assignment.getName() +
                         "，" + groupDesignation + "：" + group.getName() +
                         "，开始时间：" + startTime +
                         "，结束时间：" + endTime +
                         "，执行人：" + operatorNames +
                         "）";

        List<InAppNotification> notifications = operators.stream()
                .map(memberId -> inAppNotificationFactory.createInAppNotification(memberId, assignment.getTenantId(), pcUrl, mobileUrl, content))
                .toList();

        inAppNotificationRepository.insert(notifications);

        List<String> managers = group.getManagers();//通知分组管理员
        if (isEmpty(managers)) {
            return;
        }

        String contentForGroupManager = "新任务已创建（任务名称：" + assignment.getName() +
                                        "，" + groupDesignation + "：" + group.getName() +
                                        "，开始时间：" + startTime +
                                        "，结束时间：" + endTime +
                                        "，执行人：" + operatorNames +
                                        "）";

        List<InAppNotification> groupManagerNotifications = managers.stream()
                .map(memberId -> inAppNotificationFactory.createInAppNotification(memberId,
                        assignment.getTenantId(),
                        pcUrl,
                        mobileUrl,
                        contentForGroupManager))
                .toList();

        inAppNotificationRepository.insert(groupManagerNotifications);
    }

    @Override
    public void notifyOperatorsAssignmentNearExpire(Assignment assignment, App app) {
        List<String> memberIds = assignment.getOperators();
        if (isEmpty(memberIds)) {
            return;
        }

        String operatorNames = memberRepository.cachedAllMemberReferences(assignment.getTenantId()).stream()
                .filter(memberReference -> memberIds.contains(memberReference.getId()))
                .map(MemberReference::getName)
                .collect(Collectors.joining(","));

        Group group = groupRepository.cachedById(assignment.getGroupId());
        String startTime = MRY_DATE_TIME_FORMATTER.format(assignment.getStartAt());
        String endTime = MRY_DATE_TIME_FORMATTER.format(assignment.getExpireAt());
        String pcUrl = this.propertyService.pcAssignmentUrlOf(assignment);
        String mobileUrl = this.propertyService.mobileAssignmentUrlOf(assignment);
        String groupDesignation = app.groupDesignation();
        String content = "您有任务即将过期（任务名称：" + assignment.getName() +
                         "，" + groupDesignation + "：" + group.getName() +
                         "，开始时间：" + startTime +
                         "，结束时间：" + endTime +
                         "，执行人：" + operatorNames +
                         "）";

        List<InAppNotification> notifications = memberIds.stream()
                .map(memberId -> inAppNotificationFactory.createInAppNotification(memberId, assignment.getTenantId(), pcUrl, mobileUrl, content))
                .toList();

        inAppNotificationRepository.insert(notifications);
    }

    @Override
    public void notifyOnAssignmentFinished(Assignment assignment, App app) {
        Group group = groupRepository.cachedById(assignment.getGroupId());
        Set<String> toBeNotifiedMemberIds = new HashSet<>();
        toBeNotifiedMemberIds.addAll(assignment.getOperators());
        toBeNotifiedMemberIds.addAll(group.getManagers());//任务完成时除了通知执行人，还会同时通知分组管理员
        toBeNotifiedMemberIds.removeAll(singleton(null));
        if (isEmpty(toBeNotifiedMemberIds)) {
            return;
        }

        String operatorNames = memberRepository.cachedAllMemberReferences(assignment.getTenantId()).stream()
                .filter(memberReference -> assignment.getOperators().contains(memberReference.getId()))
                .map(MemberReference::getName)
                .collect(Collectors.joining(","));

        String startTime = MRY_DATE_TIME_FORMATTER.format(assignment.getStartAt());
        String endTime = MRY_DATE_TIME_FORMATTER.format(assignment.getExpireAt());
        String pcUrl = this.propertyService.pcAssignmentUrlOf(assignment);
        String mobileUrl = this.propertyService.mobileAssignmentUrlOf(assignment);
        String groupDesignation = app.groupDesignation();

        String content = "任务已按期完成（任务名称：" + assignment.getName() +
                         "，" + groupDesignation + "：" + group.getName() +
                         "，开始时间：" + startTime +
                         "，结束时间：" + endTime +
                         "，执行人：" + operatorNames +
                         "）";

        List<InAppNotification> notifications = toBeNotifiedMemberIds.stream()
                .map(memberId -> inAppNotificationFactory.createInAppNotification(memberId, assignment.getTenantId(), pcUrl, mobileUrl, content))
                .toList();

        inAppNotificationRepository.insert(notifications);
    }

    @Override
    public void notifyOnAssignmentFailed(Assignment assignment, App app) {
        Group group = groupRepository.cachedById(assignment.getGroupId());
        Set<String> toBeNotifiedMemberIds = new HashSet<>();
        toBeNotifiedMemberIds.addAll(assignment.getOperators());
        toBeNotifiedMemberIds.addAll(group.getManagers());//任务失败时除了通知执行人，还会同时通知分组管理员
        toBeNotifiedMemberIds.removeAll(singleton(null));
        if (isEmpty(toBeNotifiedMemberIds)) {
            return;
        }

        String operatorNames = memberRepository.cachedAllMemberReferences(assignment.getTenantId()).stream()
                .filter(memberReference -> assignment.getOperators().contains(memberReference.getId()))
                .map(MemberReference::getName)
                .collect(Collectors.joining(","));

        String startTime = MRY_DATE_TIME_FORMATTER.format(assignment.getStartAt());
        String endTime = MRY_DATE_TIME_FORMATTER.format(assignment.getExpireAt());
        String pcUrl = this.propertyService.pcAssignmentUrlOf(assignment);
        String mobileUrl = this.propertyService.mobileAssignmentUrlOf(assignment);
        String groupDesignation = app.groupDesignation();

        String content = "任务超期未完成（任务名称：" + assignment.getName() +
                         "，" + groupDesignation + "：" + group.getName() +
                         "，开始时间：" + startTime +
                         "，结束时间：" + endTime +
                         "，执行人：" + operatorNames +
                         "）";

        List<InAppNotification> notifications = toBeNotifiedMemberIds.stream()
                .map(memberId -> inAppNotificationFactory.createInAppNotification(memberId, assignment.getTenantId(), pcUrl, mobileUrl, content))
                .toList();

        inAppNotificationRepository.insert(notifications);
    }

    private Set<String> toBeNotifiedMemberIdsForSubmission(App app, Group group, List<NotificationRole> notifyRoles, String memberId) {
        Set<String> toBeNotifiedMemberIds = new HashSet<>();
        if (notifyRoles.contains(APP_MANAGER)) {
            toBeNotifiedMemberIds.addAll(app.getManagers());
        }

        if (notifyRoles.contains(GROUP_MANAGER)) {
            toBeNotifiedMemberIds.addAll(group.getMembers());
        }

        if (notifyRoles.contains(SUBMITTER) && isNotBlank(memberId)) {
            toBeNotifiedMemberIds.add(memberId);
        }

        toBeNotifiedMemberIds.removeAll(singleton(null));
        return toBeNotifiedMemberIds;
    }
}
