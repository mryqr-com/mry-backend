package com.mryqr.core.app.domain.page.setting;

import com.mryqr.core.common.domain.permission.Permission;
import com.mryqr.core.common.exception.MryException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.core.common.exception.ErrorCode.APPROVAL_PERMISSION_NOT_ALLOWED;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class ApprovalSetting {
    private static final List<Permission> ALLOWED_APPROVAL_PERMISSIONS = List.of(CAN_MANAGE_GROUP, CAN_MANAGE_APP);
    private static final String PASS_TEXT = "通过";
    private static final String NOT_PASS_TEXT = "不通过";

    private boolean approvalEnabled;//是否启用审批

    @NotNull
    private Permission permission;//审批者所需权限

    @Size(max = MAX_SHORT_NAME_LENGTH)
    private String passText; //审批通过文本

    @Size(max = MAX_SHORT_NAME_LENGTH)
    private String notPassText;//审批不通过文本

    private boolean notifySubmitter;//审批完成通知提交者

    public void reset() {
        approvalEnabled = false;
        permission = CAN_MANAGE_APP;
        passText = PASS_TEXT;
        notPassText = NOT_PASS_TEXT;
        notifySubmitter = false;
    }

    public void correct() {
        if (isBlank(passText)) {
            this.passText = PASS_TEXT;
        }

        if (isBlank(notPassText)) {
            this.notPassText = NOT_PASS_TEXT;
        }
    }

    public void validate() {
        if (!ALLOWED_APPROVAL_PERMISSIONS.contains(permission)) {
            throw new MryException(APPROVAL_PERMISSION_NOT_ALLOWED, "审批权限不支持。", mapOf("permission", permission));
        }
    }

}
