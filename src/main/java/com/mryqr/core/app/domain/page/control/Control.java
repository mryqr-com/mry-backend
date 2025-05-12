package com.mryqr.core.app.domain.page.control;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.ui.BoxedTextStyle;
import com.mryqr.core.common.domain.permission.Permission;
import com.mryqr.core.common.exception.ErrorCode;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.utils.Identified;
import com.mryqr.core.common.validation.id.control.ControlId;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.core.common.domain.permission.Permission.PUBLIC;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_PERMISSION_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.EMPTY_FILLABLE_SETTING;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = PSectionTitleViewControl.class, name = "SECTION_TITLE"),
        @JsonSubTypes.Type(value = PSeparatorControl.class, name = "SEPARATOR"),
        @JsonSubTypes.Type(value = PRichTextControl.class, name = "RICH_TEXT"),
        @JsonSubTypes.Type(value = PParagraphControl.class, name = "PARAGRAPH"),
        @JsonSubTypes.Type(value = PImageCardLinkControl.class, name = "IMAGE_CARD_LINK"),
        @JsonSubTypes.Type(value = PButtonPageLinkControl.class, name = "BUTTON_PAGE_LINK"),
        @JsonSubTypes.Type(value = PIconPageLinkControl.class, name = "ICON_PAGE_LINK"),
        @JsonSubTypes.Type(value = PImageViewControl.class, name = "IMAGE_VIEW"),
        @JsonSubTypes.Type(value = PVideoViewControl.class, name = "VIDEO_VIEW"),
        @JsonSubTypes.Type(value = PAttachmentViewControl.class, name = "ATTACHMENT_VIEW"),
        @JsonSubTypes.Type(value = PAttributeTableControl.class, name = "ATTRIBUTE_TABLE"),
        @JsonSubTypes.Type(value = PAttributeDashboardControl.class, name = "ATTRIBUTE_DASHBOARD"),
        @JsonSubTypes.Type(value = PSubmitHistoryControl.class, name = "SUBMIT_HISTORY"),
        @JsonSubTypes.Type(value = PInstanceListControl.class, name = "INSTANCE_LIST"),
        @JsonSubTypes.Type(value = PAnswerReferenceControl.class, name = "ANSWER_REFERENCE"),
        @JsonSubTypes.Type(value = PSubmissionReferenceControl.class, name = "SUBMISSION_REFERENCE"),
        @JsonSubTypes.Type(value = PTimeSegmentControl.class, name = "TIME_SEGMENT"),
        @JsonSubTypes.Type(value = PNumberRangeSegmentControl.class, name = "NUMBER_RANGE_SEGMENT"),
        @JsonSubTypes.Type(value = PBarControl.class, name = "BAR"),
        @JsonSubTypes.Type(value = PPieControl.class, name = "PIE"),
        @JsonSubTypes.Type(value = PDoughnutControl.class, name = "DOUGHNUT"),
        @JsonSubTypes.Type(value = PTrendControl.class, name = "TREND"),
        @JsonSubTypes.Type(value = FRadioControl.class, name = "RADIO"),
        @JsonSubTypes.Type(value = FCheckboxControl.class, name = "CHECKBOX"),
        @JsonSubTypes.Type(value = FDropdownControl.class, name = "DROPDOWN"),
        @JsonSubTypes.Type(value = FSingleLineTextControl.class, name = "SINGLE_LINE_TEXT"),
        @JsonSubTypes.Type(value = FMultiLineTextControl.class, name = "MULTI_LINE_TEXT"),
        @JsonSubTypes.Type(value = FRichTextInputControl.class, name = "RICH_TEXT_INPUT"),
        @JsonSubTypes.Type(value = FMemberSelectControl.class, name = "MEMBER_SELECT"),
        @JsonSubTypes.Type(value = FFileUploadControl.class, name = "FILE_UPLOAD"),
        @JsonSubTypes.Type(value = FImageUploadControl.class, name = "IMAGE_UPLOAD"),
        @JsonSubTypes.Type(value = FAddressControl.class, name = "ADDRESS"),
        @JsonSubTypes.Type(value = FGeolocationControl.class, name = "GEOLOCATION"),
        @JsonSubTypes.Type(value = FNumberInputControl.class, name = "NUMBER_INPUT"),
        @JsonSubTypes.Type(value = FNumberRankingControl.class, name = "NUMBER_RANKING"),
        @JsonSubTypes.Type(value = FMobileNumberControl.class, name = "MOBILE"),
        @JsonSubTypes.Type(value = FIdentifierControl.class, name = "IDENTIFIER"),
        @JsonSubTypes.Type(value = FPersonNameControl.class, name = "PERSON_NAME"),
        @JsonSubTypes.Type(value = FEmailControl.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = FDateControl.class, name = "DATE"),
        @JsonSubTypes.Type(value = FTimeControl.class, name = "TIME"),
        @JsonSubTypes.Type(value = FItemCountControl.class, name = "ITEM_COUNT"),
        @JsonSubTypes.Type(value = FItemStatusControl.class, name = "ITEM_STATUS"),
        @JsonSubTypes.Type(value = FPointCheckControl.class, name = "POINT_CHECK"),
        @JsonSubTypes.Type(value = FSignatureControl.class, name = "SIGNATURE"),
        @JsonSubTypes.Type(value = FMultiLevelSelectionControl.class, name = "MULTI_LEVEL_SELECTION"),
})

@Getter
@Document
@SuperBuilder
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public abstract class Control implements Identified {
    public static final List<Permission> ALLOWED_PERMISSIONS = List.of(CAN_MANAGE_GROUP, CAN_MANAGE_APP);
    public final static int MAX_NAME_LENGTH = 100;
    public final static int MAX_DESCRIPTION_LENGTH = 500;

    @NotBlank
    @ControlId
    private String id;//控件ID

    @NotNull
    private ControlType type;//控件类型

    @Size(max = MAX_NAME_LENGTH)
    private String name;//名称

    @Valid
    @NotNull
    private ControlNameSetting nameSetting;//名称样式

    @Size(max = MAX_DESCRIPTION_LENGTH)
    private String description;//描述

    @Valid
    @NotNull
    private BoxedTextStyle descriptionStyle;//描述样式

    @Valid
    @NotNull
    private ControlStyleSetting styleSetting;//控件样式设置

    @Valid
    private ControlFillableSetting fillableSetting;//填值型控件的通用设置

    private boolean permissionEnabled;//是否启用权限

    @NotNull
    private Permission permission;//控件权限

    private boolean submitterViewable;//对原始提交者可见,仅作用于填值控件

    @EqualsAndHashCode.Exclude
    protected boolean complete;//主要针对具有外部引用的展示型控件，用于标记控件配置是否完整，前端可根据该值决定是否显示相关数据，后端可根据该值决定时候计算相关数据

    public static String newControlId() {
        return "c_" + newShortUuid();
    }

    public final void correct(AppSettingContext context) {
        if (isBlank(name)) {
            this.name = "未命名控件";
        }

        if (isFillable()) {
            Page page = context.pageForControl(this.id);
            if (fillableSetting != null) {
                fillableSetting.correct(this.type, page);
            }
            if (page.isPublic()) {
                submitterViewable = false;
            }
        } else {
            this.fillableSetting = null;
            submitterViewable = false;
        }

        if (!permissionEnabled) {
            submitterViewable = false;
        }

        this.complete = true;//先默认设置为true，对于需要重新计算的control，会在doCorrect()方法中重新计算
        doCorrect(context);
    }

    public final void validate(AppSettingContext context) {
        try {
            if (isFillable() && fillableSetting == null) {
                throw new MryException(EMPTY_FILLABLE_SETTING, "填值控件通用配置不能为空。");
            }

            if (!ALLOWED_PERMISSIONS.contains(permission)) {
                throw new MryException(CONTROL_PERMISSION_NOT_ALLOWED, "控件权限不支持。", mapOf("permission", permission));
            }

            doValidate(context);
        } catch (MryException e) {
            e.addData("controlId", this.getId());
            e.addData("controlType", this.getType());
            throw e;
        }
    }

    public boolean isAnswerViewableBySubmitter() {
        return isPermissionEnabled() && isSubmitterViewable();
    }

    public final void failAnswerValidation(ErrorCode errorCode, String detailMessage) {
        this.failAnswerValidation(errorCode, "提交数据验证失败。", detailMessage);
    }

    public final void failAnswerValidation(ErrorCode errorCode, String userMessage, String detailMessage) {
        throw new MryException(errorCode, userMessage,
                mapOf("controlId", this.getId(),
                        "controlType", this.getType().name(),
                        "detailMessage", detailMessage));
    }

    public final Permission requiredPermission() {
        //没有启用权限时，返回最低可设权限
        if (!permissionEnabled) {
            return PUBLIC;
        }

        return permission;
    }

    public final boolean isMandatory() {
        return this.fillableSetting != null && this.fillableSetting.isMandatory();
    }

    protected void setMandatory(boolean value) {
        this.fillableSetting.setMandatory(value);
    }

    public final boolean isAutoFill() {
        return this.fillableSetting != null && this.fillableSetting.isAutoFill();
    }

    protected void setAutoFill(boolean value) {
        this.fillableSetting.setAutoFill(value);
    }

    public final boolean isAnswerExportable() {
        return this.type.isAnswerExportable();
    }

    public final String fieldName() {
        if (fillableSetting == null) {
            return this.name;
        }
        String settingFieldName = this.fillableSetting.getFieldName();
        return isNotBlank(settingFieldName) ? settingFieldName : this.name;
    }

    public final boolean isFillable() {
        return this.type.isFillable();
    }

    public final boolean isAnswerNumerical() {
        return this.type.isAnswerNumerical();
    }

    public final boolean isSubmissionSummaryEligible() {
        return isFillable() && fillableSetting != null && fillableSetting.isSubmissionSummaryEligible();
    }

    protected abstract void doCorrect(AppSettingContext context);

    protected abstract void doValidate(AppSettingContext context);

    @Override
    public final String getIdentifier() {
        return this.id;
    }

    public Double format(Double number) {
        return number;
    }

    public Answer createAnswerFrom(String value) {
        if (isBlank(value)) {
            return null;
        }

        return doCreateAnswerFrom(value);
    }

    protected abstract Answer doCreateAnswerFrom(String value);

}
