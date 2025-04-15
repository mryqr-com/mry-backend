package com.mryqr.core.app.domain.page.setting;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.color.Color;
import com.mryqr.common.validation.nospace.NoSpace;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.setting.notification.NotificationSetting;
import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.shadow.Shadow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import static com.mryqr.common.domain.permission.Permission.*;
import static com.mryqr.common.exception.ErrorCode.MODIFY_PERMISSION_NOT_ALLOWED;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.*;
import static com.mryqr.core.app.domain.page.setting.AfterSubmitNavigationType.DEFAULT;
import static com.mryqr.core.app.domain.page.setting.SubmitType.NEW;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_MEMBER;
import static com.mryqr.core.app.domain.page.setting.SubmitterUpdateRange.IN_1_DAY;
import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class PageSetting {
    private static final List<Permission> ALLOWED_MODIFY_PERMISSIONS = List.of(CAN_MANAGE_GROUP, CAN_MANAGE_APP);

    @Size(max = MAX_SHORT_NAME_LENGTH)
    private String pageName;//页面名称

    @NotNull
    private SubmitType submitType;//提交类型

    @NotNull
    private Permission permission;//页面自身所需的权限，同时控制页面的查看和提交

    @NotNull
    private Permission modifyPermission;//修改权限

    private boolean submitterUpdatable;//是否允许提交者提交后修改

    @NotNull
    private SubmitterUpdateRange submitterUpdateRange;//在submitterUpdatable=true时，设置允许修改的期限

    @Valid
    @NotNull
    private final ApprovalSetting approvalSetting;//审批设置

    @Valid
    @NotNull
    private final NotificationSetting notificationSetting;

    @NotNull
    @NoNullElement
    @Size(max = 5)
    private final List<SubmissionWebhookType> submissionWebhookTypes;

    @Size(max = MAX_SHORT_NAME_LENGTH)
    private String actionName;//提交动作名称

    private boolean showAsterisk;//对必填项是否显示红色星号
    private final boolean showControlIndex;//为控件显示编号
    private final boolean hideProfileButton;//隐藏页面顶部登录按钮
    private final boolean hideTopBottomBlank;//为大屏设备隐藏上下留白
    private final boolean hideTopBar;//隐藏页面顶部导航栏
    private final boolean hideHeader;//隐藏页眉
    private final boolean hideTitle;//隐藏标题
    private final boolean hideMenu;//隐藏菜单

    @Min(650)
    @Max(2000)
    private final int pageMaxWidth;//页面最大宽度

    @Min(650)
    @Max(2000)
    private final int contentMaxWidth;//内容区域最大宽度

    @Color
    private final String pageBackgroundColor;//页面背景颜色

    @Min(MIN_BORDER_RADIUS)
    @Max(MAX_BORDER_RADIUS)
    private final int controlBorderRadius;//所有控件的圆角半径

    @Valid
    @NotNull
    private final Shadow shadow;//页边阴影

    @Valid
    @NotNull
    private final Border border;//页边边框

    @Color
    private final String viewPortBackgroundColor;//整屏背景颜色

    @Valid
    private final UploadedFile viewPortBackgroundImage;//整屏背景图片

    @Valid
    @NotNull
    private final AfterSubmitBehaviour afterSubmitBehaviour;//提交后行为

    @NoSpace
    @Size(max = 5)
    private final String submitterAlias;//提交人称号，用于替换"提交人"字样

    @NoSpace
    @Size(max = 5)
    private final String submitAtAlias;//提交时间称号，用于替换"提交时间"字样

    private void correctName() {
        if (isBlank(pageName)) {
            this.pageName = "未命名页面";
        }
    }

    public void correctForFillable(AppSettingContext context) {
        correctName();

        if (submitType == ONCE_PER_MEMBER && isPublic()) {//按成员提交页面必须保证登录
            this.permission = AS_TENANT_MEMBER;
        }

        if (isPublic()) {
            this.submitterUpdatable = false;
        }

        if (!submitterUpdatable) {
            submitterUpdateRange = IN_1_DAY;
        }

        approvalSetting.correct();
        notificationSetting.correct();
        afterSubmitBehaviour.correct(context);
    }

    public void correctForNonFillable() {
        correctName();
        submitType = NEW;
        modifyPermission = CAN_MANAGE_APP;
        submitterUpdatable = false;
        submitterUpdateRange = IN_1_DAY;
        approvalSetting.reset();
        notificationSetting.reset();
        actionName = null;
        afterSubmitBehaviour.reset();
    }

    public void validate() {
        if (!ALLOWED_MODIFY_PERMISSIONS.contains(modifyPermission)) {
            throw new MryException(MODIFY_PERMISSION_NOT_ALLOWED, "修改权限不支持。", mapOf("modifyPermission", permission));
        }
        approvalSetting.validate();
    }

    public boolean requireLogin() {
        return permission.requireLogin();
    }

    public boolean isPublic() {
        return permission.isPublic();
    }

    public void modify(SubmitType submitType, Permission permission) {
        this.submitType = submitType;
        this.permission = permission;
    }

    public static PageSetting defaultPageSetting() {
        return defaultPageSettingBuilder().build();
    }

    public static PageSettingBuilder defaultPageSettingBuilder() {
        return PageSetting.builder()
                .submitType(NEW)
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_APP)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_DAY)
                .approvalSetting(ApprovalSetting.builder()
                        .approvalEnabled(false)
                        .permission(CAN_MANAGE_APP)
                        .passText("通过")
                        .notPassText("不通过")
                        .build())
                .notificationSetting(NotificationSetting.builder()
                        .notificationEnabled(false)
                        .onCreateNotificationRoles(List.of())
                        .onUpdateNotificationRoles(List.of())
                        .build())
                .submissionWebhookTypes(List.of())
                .pageName("未命名页面")
                .actionName(null)
                .showAsterisk(true)
                .showControlIndex(false)
                .hideProfileButton(false)
                .hideTopBottomBlank(false)
                .hideTopBar(false)
                .hideHeader(false)
                .hideTitle(false)
                .hideMenu(false)
                .pageMaxWidth(650)
                .contentMaxWidth(650)
                .pageBackgroundColor("rgba(255, 255, 255, 1)")
                .controlBorderRadius(4)
                .shadow(Shadow.builder().width(6).color("rgba(0, 0, 0, .2)").build())
                .border(noBorder())
                .viewPortBackgroundColor(null)
                .viewPortBackgroundImage(null)
                .afterSubmitBehaviour(AfterSubmitBehaviour.builder()
                        .type(DEFAULT)
                        .internalPageId(null)
                        .externalUrl(null)
                        .build());
    }

}
