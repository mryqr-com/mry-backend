package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.common.domain.AddressPrecisionType;
import com.mryqr.core.common.domain.report.ReportRange;
import com.mryqr.core.common.domain.report.SubmissionSegmentType;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
import com.mryqr.core.common.validation.color.Color;
import com.mryqr.core.common.validation.id.control.ControlId;
import com.mryqr.core.common.validation.id.page.PageId;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.core.common.exception.ErrorCode.NOT_SUPPORTED_BASED_CONTROL_BAR;
import static com.mryqr.core.common.exception.ErrorCode.NOT_SUPPORTED_TARGET_CONTROL_FOR_BAR;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_CONTROL_NOT_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("BAR_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PBarControl extends Control {
    public static final int MIN_MAX = 1;
    public static final int MAX_MAX = 20;

    @NotNull
    private SubmissionSegmentType segmentType;//统计类型

    @PageId
    private String pageId;//所引用的页面ID

    @ControlId
    private String basedControlId;//基准控件ID

    @Valid
    @NotNull
    @Size(max = 2)
    @NoNullElement
    private List<@ControlId String> targetControlIds;//目标控件ID，segmentType!=SUBMIT_COUNT_SUM时有效

    @NotNull
    private ReportRange range;//统计时间范围

    @Min(MIN_MAX)
    @Max(MAX_MAX)
    private int max;//最多显示的组元数

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String xTitle;

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String yTitle;

    private boolean horizontal;//是否水平显示

    private boolean hideGrid;//隐藏网格线

    @Valid
    @NotNull
    @Size(max = 2)
    private List<@Color String> colors;//条形颜色

    @Min(60)
    @Max(100)
    private int sizeRatio;//缩放比例

    private boolean hideControlIfNoData;//无数据时隐藏整个控件

    private boolean showNumber;//是否显示数值

    @NotNull
    private AddressPrecisionType addressPrecisionType;//地址单位，对Address和Geolocation控件有效

    @NotNull
    private MultiLevelSelectionPrecisionType multiLevelSelectionPrecisionType;//多级下拉对应的统计粒度

    @Override
    public void doCorrect(AppSettingContext context) {
        if (segmentType == SUBMIT_COUNT_SUM) {
            this.targetControlIds = List.of();
            this.complete = isNotBlank(pageId) && isNotBlank(basedControlId);
        } else {
            this.complete = isNotBlank(pageId) && isNotBlank(basedControlId) && isNotEmpty(targetControlIds);
        }

        this.colors = this.colors.stream().map(color -> isNotBlank(color) ? color : "#FF8C00").collect(toImmutableList());
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        if (!complete) {
            return;
        }

        if (context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_PAGE_NOT_EXIST, "条形图引用的页面不存在。", mapOf("refPageId", pageId));
        }

        if (context.controlNotExists(pageId, basedControlId)) {
            throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "条形图引用的基准控件不存在。",
                    mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
        }

        if (!context.controlTypeOf(basedControlId).isAnswerCategorized()) {
            throw new MryException(NOT_SUPPORTED_BASED_CONTROL_BAR, "基准控件不支持条形图。",
                    mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
        }

        if (segmentType != SUBMIT_COUNT_SUM) {
            targetControlIds.forEach(controlId -> {
                if (context.controlNotExists(pageId, controlId)) {
                    throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "条形图引用的目标控件不存在。",
                            mapOf("refPageId", pageId, "refTargetControlId", controlId));
                }

                ControlType valueControlType = context.controlTypeOf(controlId);
                if (!valueControlType.isAnswerNumbered()) {
                    throw new MryException(NOT_SUPPORTED_TARGET_CONTROL_FOR_BAR, "目标控件必须为数字输入类型。", mapOf(
                            "refPageId", pageId,
                            "refTargetControlId", controlId));

                }
            });
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

}
