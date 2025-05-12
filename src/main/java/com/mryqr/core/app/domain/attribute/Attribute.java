package com.mryqr.core.app.domain.attribute;


import com.mryqr.common.domain.ValueType;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.id.attribute.AttributeId;
import com.mryqr.common.validation.id.control.ControlId;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.common.validation.nospace.NoSpace;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

import static com.mryqr.common.domain.ValueType.DOUBLE_VALUE;
import static com.mryqr.common.domain.ValueType.TEXT_VALUE;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.*;
import static com.mryqr.core.app.domain.attribute.AttributeType.*;
import static java.lang.Double.valueOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class Attribute implements Identified {
    private static final Set<AttributeType> NUMBERED_CONTROL_TYPES = Set.of(CONTROL_SUM, CONTROL_AVERAGE, CONTROL_MAX, CONTROL_MIN);

    @NotBlank
    @AttributeId
    private final String id;//唯一标识

    @NoSpace
    @NotBlank
    @Size(max = MAX_SHORT_NAME_LENGTH)
    private final String name;//属性名称

    @NotNull
    private final AttributeType type;//属性类型

    @Size(max = 100)
    private String fixedValue;//固定值(只对固定值类型起作用)

    private final boolean manualInput;//是否在QR的基本信息编辑页面显示，只针对填入值类型

    @PageId
    private String pageId;//所引用的页面ID

    @ControlId
    private String controlId;//所引用的控件ID

    private AttributeStatisticRange range;//统计时间范围，仅对某些属性生效，比如总和、最大值等

    private final boolean pcListEligible;//是否显示在电脑端实例列表中

    private final boolean mobileListEligible;//是否显示在手机端实例列表中

    private boolean kanbanEligible;//是否显示为状态看板

    @NoSpace
    @Size(max = 10)
    private String suffix;//后缀，只对数字属性有效

    @Min(0)
    @Max(3)
    private int precision;//精度，只对数字属性有效

    private ValueType valueType;//属性值的类型

    public static String newAttributeId() {
        return "a_" + newShortUuid();
    }

    public void correct() {
        correctConfiguration();
    }

    private void correctConfiguration() {
        if (!type.isPageAware()) {
            this.pageId = null;
        }

        if (!type.isControlAware()) {
            this.controlId = null;
        }

        if (!type.isRangeAware()) {
            this.range = null;
        }

        if (type != FIXED) {
            this.fixedValue = null;
        }
    }

    public void validate(AppSettingContext context) {
        if (type.isPageAware()) {
            if (isBlank(pageId)) {
                throw new MryException(EMPTY_ATTRIBUTE_REF_PAGE_ID, "属性所引用的页面不能为空。", mapOf("attributeId", id));
            }

            if (context.pageNotExists(pageId)) {
                throw new MryException(VALIDATION_ATTRIBUTE_REF_PAGE_NOT_EXIST, "属性所引用的页面不存在。",
                        mapOf("attributeId", id, "refPageId", pageId));
            }
        }

        if (type.isControlAware()) {
            if (isBlank(controlId)) {
                throw new MryException(EMPTY_ATTRIBUTE_REF_CONTROL_ID, "属性所引用的控件不能为空。", mapOf("attributeId", id));
            }

            if (context.controlNotExists(pageId, controlId)) {
                throw new MryException(VALIDATION_ATTRIBUTE_REF_CONTROL_NOT_EXIST, "属性所引用的控件不存在。", mapOf(
                        "attributeId", id,
                        "refPageId", pageId,
                        "refControlId", controlId)
                );
            }

            ControlType controlType = context.controlTypeOf(controlId);
            if (!controlType.isFillable() || NUMBERED_CONTROL_TYPES.contains(this.type) && !controlType.isAnswerNumbered()) {
                throw new MryException(WRONG_ATTRIBUTE_REF_CONTROL_TYPE, "属性所引用控件不支持该属性类型。", mapOf(
                        "attributeId", id,
                        "refPageId", pageId,
                        "refControlId", controlId));
            }
        }

        if (type == FIXED && isBlank(fixedValue)) {
            throw new MryException(EMPTY_ATTRIBUTE_FIXED_VALUE, "固定值属性的值不能为空。", mapOf("attributeId", id));
        }

        if (type.isRangeAware() && range == null) {
            throw new MryException(ATTRIBUTE_RANGE_SHOULD_NOT_NULL, "属性统计时间范围不能为空。", mapOf("attributeId", id));
        }

        //在验证成功之后才做后续操作
        if (type.isControlAware()) {
            Control control = context.controlById(controlId);
            this.valueType = control.getType().getAnswerValueType();
            if (control instanceof FNumberInputControl numberInputControl) {
                this.precision = numberInputControl.getPrecision();
                this.suffix = numberInputControl.getSuffix();
            }
            return;
        }

        if (type == DIRECT_INPUT) {
            if (valueType != DOUBLE_VALUE) {
                this.valueType = TEXT_VALUE;//对于填入值，除了Double类型外全部为文本类型
            }
            return;
        }

        this.valueType = type.getValueType();
    }

    public final boolean isCalculated() {
        return this.type.isValueCalculated();
    }

    public final boolean shouldWeeklyReset() {
        return this.range == THIS_WEEK;
    }

    public final boolean shouldMonthlyReset() {
        return this.range == THIS_MONTH;
    }

    public final boolean shouldSeasonlyReset() {
        return this.range == THIS_SEASON;
    }

    public final boolean shouldYearlyReset() {
        return this.range == THIS_YEAR;
    }

    public boolean isFixed() {
        return this.type == FIXED;
    }

    public AttributeInfo toInfo() {
        return AttributeInfo.builder()
                .attributeId(id)
                .attributeType(type)
                .valueType(valueType)
                .build();
    }

    public boolean isSchemaDifferentFrom(Attribute other) {
        return !this.schema().equals(other.schema());
    }

    public boolean isSubmissionAware() {
        return type.isSubmissionAware();
    }

    public boolean isValueNumbered() {
        return this.valueType != null && this.valueType.isNumbered();
    }

    public boolean isValueCategorized() {
        return this.valueType != null && this.valueType.isCategorized();
    }

    public boolean isValueExportable() {
        return this.valueType != null && this.valueType.isExportable();
    }

    private AttributeSchema schema() {
        return AttributeSchema.builder()
                .id(this.id)
                .type(this.type)
                .pageId(this.pageId)
                .controlId(this.controlId)
                .range(this.range)
                .valueType(this.valueType)
                .build();
    }

    public Double format(Double number) {
        return number == null ? null : valueOf(String.format("%." + precision + "f", number));
    }

    public String formatToString(Double number) {
        return number == null ? null : String.format("%." + precision + "f", number);
    }

    public boolean isPageAware() {
        return this.type.isPageAware();
    }

    public boolean isControlAware() {
        return this.type.isControlAware();
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    private static class AttributeSchema {//用于检查attribute是否发生变更
        private String id;
        private AttributeType type;
        private String pageId;
        private String controlId;
        private AttributeStatisticRange range;
        private ValueType valueType;
    }

    @Override
    public String getIdentifier() {
        return id;
    }
}

