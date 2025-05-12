package com.mryqr.core.app.domain.page;

import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlInfo;
import com.mryqr.core.app.domain.page.header.PageHeader;
import com.mryqr.core.app.domain.page.setting.PageSetting;
import com.mryqr.core.app.domain.page.setting.SubmissionWebhookType;
import com.mryqr.core.app.domain.page.setting.SubmitType;
import com.mryqr.core.app.domain.page.setting.SubmitterUpdateRange;
import com.mryqr.core.app.domain.page.setting.notification.NotificationRole;
import com.mryqr.core.app.domain.page.submitbutton.SubmitButton;
import com.mryqr.core.app.domain.page.title.PageTitle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.CONTROL_NOT_FOUND;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.MAX_PER_PAGE_CONTROL_SIZE;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.page.setting.SubmitType.*;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class Page implements Identified {

    @PageId
    @NotBlank
    private final String id;//页面ID

    @Valid
    @NotNull
    private final PageHeader header;//页眉

    @Valid
    @NotNull
    private final PageTitle title;//页面标题

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_PER_PAGE_CONTROL_SIZE)
    private final List<Control> controls;//控件列表

    @Valid
    @NotNull
    private final SubmitButton submitButton;//提交按钮

    @Valid
    @NotNull
    private final PageSetting setting;//页面设置

    public static String newPageId() {
        return "p_" + newShortUuid();
    }

    public SubmitType submitType() {
        return setting.getSubmitType();
    }

    public boolean isOncePerInstanceSubmitType() {
        return submitType() == ONCE_PER_INSTANCE;
    }

    public boolean isOncePerMemberSubmitType() {
        return submitType() == ONCE_PER_MEMBER;
    }

    public boolean isNewSubmitType() {
        return submitType() == NEW;
    }

    public boolean isPublic() {
        return setting.isPublic();
    }

    public boolean requireLogin() {
        return setting.requireLogin();
    }

    public Permission requiredPermission() {
        return setting.getPermission();
    }

    public Permission requiredModifyPermission() {
        return setting.getModifyPermission();
    }

    public Permission requiredApprovalPermission() {
        return setting.getApprovalSetting().getPermission();
    }

    public boolean isSubmitterUpdatable() {
        return setting.isSubmitterUpdatable();
    }

    public SubmitterUpdateRange submitterUpdateRange() {
        return setting.getSubmitterUpdateRange();
    }

    public boolean isApprovalEnabled() {
        return setting.getApprovalSetting().isApprovalEnabled();
    }

    public boolean isNotificationEnabled() {
        return this.setting.getNotificationSetting().isNotificationEnabled();
    }

    public String approvalPassText() {
        return setting.getApprovalSetting().getPassText();
    }

    public String approvalNotPassText() {
        return setting.getApprovalSetting().getNotPassText();
    }

    public void correct(AppSettingContext context) {
        if (isFillable()) {
            setting.correctForFillable(context);
        } else {
            setting.correctForNonFillable();
        }

        title.correct();
        submitButton.correct();
        controls.forEach(control -> control.correct(context));
    }

    public boolean isFillable() {
        return controls.stream().anyMatch(Control::isFillable);
    }

    public void validate(AppSettingContext context) {
        this.setting.validate();
        try {
            controls.forEach(control -> control.validate(context));
        } catch (MryException e) {
            e.addData("pageId", this.getId());
            throw e;
        }
    }

    public List<Control> allFillableControls() {
        return this.controls.stream()
                .filter(Control::isFillable)
                .collect(toImmutableList());
    }

    public List<Control> allExportableControls() {
        return this.controls.stream()
                .filter(Control::isAnswerExportable)
                .collect(toImmutableList());
    }

    public Control controlById(String controlId) {
        return this.controlByIdOptional(controlId)
                .orElseThrow(() -> new MryException(CONTROL_NOT_FOUND,
                        "控件未找到。", mapOf("controlId", controlId)));
    }

    public Optional<Control> controlByIdOptional(String controlId) {
        return this.controls.stream().filter(control -> control.getId().equals(controlId)).findFirst();
    }

    public Set<String> submissionSummaryEligibleControlIds() {
        return this.controls.stream().filter(Control::isSubmissionSummaryEligible).map(Control::getId)
                .collect(toImmutableSet());
    }

    @Override
    public String getIdentifier() {
        return this.id;
    }

    public String pageName() {
        return this.setting.getPageName();
    }

    public PageInfo toPageInfo() {
        Set<ControlInfo> controlInfos = this.controls.stream()
                .map(control -> ControlInfo.builder()
                        .pageId(getId())
                        .controlId(control.getId())
                        .controlType(control.getType())
                        .build())
                .collect(toImmutableSet());

        return PageInfo.builder().pageId(getId()).controlInfos(controlInfos).build();
    }

    public boolean shouldNotifySubmitterOnApproval() {
        return this.setting.getApprovalSetting().isNotifySubmitter();
    }

    public boolean shouldNotifyOnCreateSubmission() {
        return this.setting.getNotificationSetting().shouldNotifyOnCreateSubmission();
    }

    public boolean shouldNotifyOnUpdateSubmission() {
        return this.setting.getNotificationSetting().shouldNotifyOnUpdateSubmission();
    }

    public List<SubmissionWebhookType> submissionWebhookTypes() {
        return this.setting.getSubmissionWebhookTypes();
    }

    public List<NotificationRole> notifyOnCreateRoles() {
        return this.setting.getNotificationSetting().getOnCreateNotificationRoles();
    }

    public List<NotificationRole> notifyOnUpdateRoles() {
        return this.setting.getNotificationSetting().getOnUpdateNotificationRoles();
    }

    public void modify(SubmitType submitType, Permission permission) {
        this.setting.modify(submitType, permission);
    }

    public String submitterDesignation() {
        String submitterAlias = setting.getSubmitterAlias();
        return isNotBlank(submitterAlias) ? submitterAlias : "提交人";
    }

    public String submitAtDesignation() {
        String submitAtAlias = setting.getSubmitAtAlias();
        return isNotBlank(submitAtAlias) ? submitAtAlias : "提交时间";
    }
}
