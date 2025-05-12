package com.mryqr.common.notification.email;

import com.mryqr.common.notification.NotificationService;
import com.mryqr.common.profile.ProdProfile;
import com.mryqr.common.properties.PropertyService;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.setting.notification.NotificationRole;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionApproval;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mryqr.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.app.domain.page.setting.notification.NotificationRole.*;
import static com.mryqr.management.crm.MryTenantManageApp.MRY_TENANT_MANAGE_APP_ID;
import static java.util.Collections.singleton;
import static java.util.Set.copyOf;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@ProdProfile
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService {
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final QrRepository qrRepository;
    private final PropertyService propertyService;
    private final JavaMailSender mailSender;

    @Override
    public void notifyOnSubmissionCreated(Submission submission, Page page, App app, List<NotificationRole> notifyRoles) {
        Group group = groupRepository.cachedById(submission.getGroupId());
        Set<String> toBeNotifiedEmails = toBeNotifiedEmailsForSubmission(app, group, notifyRoles, submission.getCreatedBy());
        if (isEmpty(toBeNotifiedEmails)) {
            return;
        }

        String url = submissionUrlOf(submission.getId(), page.getId(), submission.getPlateId());
        String qrName = qrRepository.qrNameOf(submission.getQrId());
        String submittedBy = isNotBlank(submission.getCreatedBy()) ? memberRepository.cachedMemberNameOf(submission.getCreatedBy()) : "匿名";
        String createdAt = MRY_DATE_TIME_FORMATTER.format(submission.getCreatedAt());
        String subject = app.getId().equals(MRY_TENANT_MANAGE_APP_ID) && page.getId().equals("p_3nbM6aYj9y4FuaFTYrLADG") ?
                "码如云有新租户注册了！" :
                "请关注表单提交（" + qrName + "）";

        String content = "<div style=\"margin-bottom:12px;\">您有新的表单提交需关注，详情如下：</div>\n" +
                         "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">" + app.instanceDesignation() + "名称：</span>" + qrName + "\n" +
                         "</div>\n" +
                         "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">页面名称：</span>" + page.pageName() + "\n" +
                         "</div>\n" +
                         "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">所在" + app.groupDesignation() + "：</span>" + group.getName() +
                         "\n" +
                         "</div>\n" +
                         "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">所在应用：</span>" + app.getName() + "\n" +
                         "</div>\n" +
                         "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">提交时间：</span>" + createdAt + "\n" +
                         "</div>\n" +
                         "<div style=\"padding-left:16px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">提交人：</span>" + submittedBy + "\n" +
                         "</div>\n" +
                         "<div style=\"margin-top:12px;\">如需查看表单详情，请点击<a href=\"" + url + "\" target=\"_blank\">此链接</a>。</div>\n";

        toBeNotifiedEmails.forEach(mailTo -> sendMail(mailTo, subject, content));
    }

    @Override
    public void notifyOnSubmissionUpdated(Submission submission, Page page, App app, List<NotificationRole> notifyRoles) {
        Group group = groupRepository.cachedById(submission.getGroupId());
        Set<String> toBeNotifiedEmails = toBeNotifiedEmailsForSubmission(app, group, notifyRoles, submission.getUpdatedBy());
        if (isEmpty(toBeNotifiedEmails)) {
            return;
        }

        String url = submissionUrlOf(submission.getId(), page.getId(), submission.getPlateId());
        String qrName = qrRepository.qrNameOf(submission.getQrId());
        String updatedBy = isNotBlank(submission.getUpdatedBy()) ? memberRepository.cachedMemberNameOf(submission.getUpdatedBy()) : "匿名";
        String updatedAt = MRY_DATE_TIME_FORMATTER.format(submission.getUpdatedAt());
        String subject = "请关注表单更新（" + qrName + "）";

        String content = "<div style=\"margin-bottom:12px;\">您有新的表单更新需关注，详情如下：</div>\n" +
                         "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">" + app.instanceDesignation() + "名称：</span>" + qrName + "\n" +
                         "</div>\n" +
                         "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">页面名称：</span>" + page.pageName() + "\n" +
                         "</div>\n" +
                         "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">所在" + app.groupDesignation() + "：</span>" + group.getName() +
                         "\n" +
                         "</div>\n" +
                         "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">所在应用：</span>" + app.getName() + "\n" +
                         "</div>\n" +
                         "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">更新时间：</span>" + updatedAt + "\n" +
                         "</div>\n" +
                         "<div style=\"padding-left:16px;\">\n" +
                         "  <span style=\"color:#909399;padding-right:10px;\">更新人：</span>" + updatedBy + "\n" +
                         "</div>\n" +
                         "<div style=\"margin-top:12px;\">如需查看表单详情，请点击<a href=\"" + url + "\" target=\"_blank\">此链接</a>。</div>\n";

        toBeNotifiedEmails.forEach(mailTo -> sendMail(mailTo, subject, content));
    }

    private Set<String> toBeNotifiedEmailsForSubmission(App app, Group group, List<NotificationRole> notifyRoles, String memberId) {
        Set<String> toBeNotifiedEmails = new HashSet<>();
        if (notifyRoles.contains(APP_MANAGER)) {
            toBeNotifiedEmails.addAll(memberRepository.cachedEmailsOf(app.getTenantId(), app.getManagers()).values());
        }

        if (notifyRoles.contains(GROUP_MANAGER)) {
            toBeNotifiedEmails.addAll(memberRepository.cachedEmailsOf(app.getTenantId(), group.getManagers()).values());
        }

        if (notifyRoles.contains(SUBMITTER) && isNotBlank(memberId)) {
            toBeNotifiedEmails.addAll(memberRepository.cachedEmailsOf(app.getTenantId(), List.of(memberId)).values());
        }

        toBeNotifiedEmails.removeAll(singleton(null));
        return toBeNotifiedEmails;
    }

    @Override
    public void notifySubmitterOnSubmissionApproved(
            Submission submission,
            Page page,
            App app,
            SubmissionApproval approval) {
        if (isBlank(submission.getCreatedBy())) {
            return;
        }

        memberRepository.cachedByIdOptional(submission.getCreatedBy()).stream()
                .filter(member -> isNotBlank(member.getEmail()))
                .findFirst().ifPresent(member -> {
                    String url = submissionUrlOf(submission.getId(), page.getId(), submission.getPlateId());
                    String qrName = qrRepository.qrNameOf(submission.getQrId());
                    String approvedBy = isNotBlank(approval.getApprovedBy()) ? memberRepository.cachedMemberNameOf(approval.getApprovedBy()) : "匿名";
                    Group group = groupRepository.cachedById(submission.getGroupId());
                    String subject = "审批完成通知（" + qrName + "）";
                    String approveResult = approval.isPassed() ? page.approvalPassText() : page.approvalNotPassText();
                    String content = "<div style=\"margin-bottom:12px;\">您提交的表单已经完成审批，详情如下：</div>\n" +
                                     "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                                     "  <span style=\"color:#909399;padding-right:10px;\">" + app.instanceDesignation() +
                                     "名称：</span>" + qrName + "\n" +
                                     "</div>\n" +
                                     "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                                     "  <span style=\"color:#909399;padding-right:10px;\">页面名称：</span>" + page.pageName() + "\n" +
                                     "</div>\n" +
                                     "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                                     "  <span style=\"color:#909399;padding-right:10px;\">所在" + app.groupDesignation() +
                                     "：</span>" + group.getName() + "\n" +
                                     "</div>\n" +
                                     "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                                     "  <span style=\"color:#909399;padding-right:10px;\">所在应用：</span>" + app.getName() + "\n" +
                                     "</div>\n" +
                                     "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                                     "  <span style=\"color:#909399;padding-right:10px;\">审批人：</span>" + approvedBy + "\n" +
                                     "</div>\n" +
                                     "<div style=\"padding-left:16px;\">\n" +
                                     "  <span style=\"color:#909399;padding-right:10px;\">审批结果：</span>" + approveResult + "\n" +
                                     "</div>\n" +
                                     "<div style=\"margin-top:12px;\">如需查看表单详情，请点击<a href=\"" + url +
                                     "\" target=\"_blank\">此链接</a>。</div>\n";
                    sendMail(member.getEmail(), subject, content);
                });
    }

    @Override
    public void notifyOperatorsAssignmentNearExpire(Assignment assignment, App app) {
        List<String> operators = assignment.getOperators();
        Set<String> operatorEmails = copyOf(memberRepository.cachedEmailsOf(assignment.getTenantId(), operators).values());
        if (isEmpty(operatorEmails)) {
            return;
        }

        String url =
                propertyService.clientBaseUrl() + "/operations/" + assignment.getAppId() + "/my-assignments/" + assignment.getId() + "/qrs";
        String startTime = MRY_DATE_TIME_FORMATTER.format(assignment.getStartAt());
        String endTime = MRY_DATE_TIME_FORMATTER.format(assignment.getExpireAt());
        operatorEmails.forEach(emailTo -> {
            String content = "<div style=\"margin-bottom:12px;\">以下任务即将过期，请关注：</div>\n" +
                             "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                             "  <span style=\"color:#909399;padding-right:10px;\">任务名称：</span>" + assignment.getName() + "\n" +
                             "</div>\n" +
                             "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                             "  <span style=\"color:#909399;padding-right:10px;\">所在应用：</span>" + app.getName() + "\n" +
                             "</div>\n" +
                             "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                             "  <span style=\"color:#909399;padding-right:10px;\">开始时间：</span>" + startTime + "\n" +
                             "</div>\n" +
                             "<div style=\"padding-left:16px;\">\n" +
                             "  <span style=\"color:#909399;padding-right:10px;\">结束时间：</span>" + endTime + "\n" +
                             "</div>\n" +
                             "<div style=\"margin-top:12px;\">如需查看任务详情，请点击<a href=\"" + url +
                             "\" target=\"_blank\">此链接</a>。</div>\n";
            String subject = "任务即将过期提醒（" + assignment.getName() + "）";
            sendMail(emailTo, subject, content);
        });
    }

    @Override
    public void notifyOperatorsOnAssignmentCreated(Assignment assignment, App app) {
        List<String> operators = assignment.getOperators();
        Set<String> operatorEmails = copyOf(memberRepository.cachedEmailsOf(assignment.getTenantId(), operators).values());
        if (isEmpty(operatorEmails)) {
            return;
        }

        String url =
                propertyService.clientBaseUrl() + "/operations/" + assignment.getAppId() + "/my-assignments/" + assignment.getId() + "/qrs";
        String startTime = MRY_DATE_TIME_FORMATTER.format(assignment.getStartAt());
        String endTime = MRY_DATE_TIME_FORMATTER.format(assignment.getExpireAt());
        operatorEmails.forEach(emailTo -> {
            String content = "<div style=\"margin-bottom:12px;\">您有新任务需要完成，请关注：</div>\n" +
                             "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                             "  <span style=\"color:#909399;padding-right:10px;\">任务名称：</span>" + assignment.getName() + "\n" +
                             "</div>\n" +
                             "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                             "  <span style=\"color:#909399;padding-right:10px;\">所在应用：</span>" + app.getName() + "\n" +
                             "</div>\n" +
                             "<div style=\"padding-left:16px;margin-bottom:5px;\">\n" +
                             "  <span style=\"color:#909399;padding-right:10px;\">开始时间：</span>" + startTime + "\n" +
                             "</div>\n" +
                             "<div style=\"padding-left:16px;\">\n" +
                             "  <span style=\"color:#909399;padding-right:10px;\">结束时间：</span>" + endTime + "\n" +
                             "</div>\n" +
                             "<div style=\"margin-top:12px;\">如需查看任务详情，请点击<a href=\"" + url +
                             "\" target=\"_blank\">此链接</a>。</div>\n";
            String subject = "您有新任务需要完成（" + assignment.getName() + "）";
            sendMail(emailTo, subject, content);
        });
    }

    private String submissionUrlOf(String submissionId, String pageId, String plateId) {
        return propertyService.clientBaseUrl() + "/r/" + plateId + "/pages/" + pageId + "/" + submissionId;
    }

    private void sendMail(String mailTo, String subject, String content) {
        try {
            MimeMessage mailMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage, "utf-8");
            helper.setText(content, true);
            helper.setTo(mailTo);
            helper.setSubject(subject);
            helper.setFrom("码如云 <noreply@directmail.mryqr.com>");
            mailSender.send(mailMessage);
        } catch (Throwable t) {
            log.error("Failed to send notification email: ", t);
        }
    }
}
