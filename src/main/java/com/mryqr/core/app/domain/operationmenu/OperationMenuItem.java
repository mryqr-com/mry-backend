package com.mryqr.core.app.domain.operationmenu;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.utils.Identified;
import com.mryqr.core.common.validation.id.page.AllablePageId;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.ALL_SUBMIT_HISTORY;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.SUBMITTER_SUBMISSION;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.TO_BE_APPROVED;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_OPERATION_MENU_REF_PAGE_NOT_EXIST;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.ALL;
import static com.mryqr.core.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class OperationMenuItem implements Identified {
    @NotBlank
    @ShortUuid
    private final String id;//唯一标识

    @NotNull
    private final SubmissionListType type;//提交列表类型

    @NotBlank
    @Size(max = MAX_SHORT_NAME_LENGTH)
    private final String name;//名称

    @NotBlank
    @AllablePageId
    private final String pageId;//引用页面的ID

    public void validate(AppSettingContext context) {
        if (!pageId.equalsIgnoreCase(ALL) && context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_OPERATION_MENU_REF_PAGE_NOT_EXIST, "运营菜单所引用的页面不存在。",
                    mapOf("menuItemId", id, "refPageId", pageId));
        }
    }

    public String schema() {
        return this.pageId + this.type.name();
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    public static List<OperationMenuItem> defaultOperationMenuItems() {
        OperationMenuItem submitHistoryMenuItem = OperationMenuItem.builder()
                .id(newShortUuid()).name("所有提交").pageId(ALL).type(ALL_SUBMIT_HISTORY).build();

        OperationMenuItem submitterSubmissionMenuItem = OperationMenuItem.builder()
                .id(newShortUuid()).name("我的提交").pageId(ALL).type(SUBMITTER_SUBMISSION).build();

        OperationMenuItem tobeApprovedMenuItem = OperationMenuItem.builder()
                .id(newShortUuid()).name("待我审批").pageId(ALL).type(TO_BE_APPROVED).build();

        return List.of(submitHistoryMenuItem, submitterSubmissionMenuItem, tobeApprovedMenuItem);
    }
}
