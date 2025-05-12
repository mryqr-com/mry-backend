package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.domain.AddressPrecisionType;
import com.mryqr.common.domain.report.ReportRange;
import com.mryqr.common.domain.report.SubmissionSegmentType;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.color.Color;
import com.mryqr.common.validation.id.control.ControlId;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.core.app.domain.AppSettingContext;
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

import static com.mryqr.common.domain.report.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("DOUGHNUT_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PDoughnutControl extends Control {
    public static final int MIN_MAX = 1;
    public static final int MAX_MAX = 20;

    @NotNull
    private SubmissionSegmentType segmentType;//统计类型

    @PageId
    private String pageId;//所引用的页面ID

    @ControlId
    private String basedControlId;//基准控件ID

    @ControlId
    private String targetControlId;//目标控件ID，segmentType==CONTROL_VALUE_SUM时有效

    @NotNull
    private ReportRange range;//统计时间范围

    @Min(MIN_MAX)
    @Max(MAX_MAX)
    private int max;//最多显示的组元数

    @Min(60)
    @Max(100)
    private int sizeRatio;//缩放比例

    private boolean hideControlIfNoData;//无数据时隐藏整个控件

    private boolean showValue;//是否显示数值

    private boolean showPercentage;//是否显示百分比

    private boolean showLabels;//显示标签

    private boolean showCenterTotal;//是否显示总数

    @NotNull
    private AddressPrecisionType addressPrecisionType;//地址单位，对Address和Geolocation控件有效

    @NotNull
    private MultiLevelSelectionPrecisionType multiLevelSelectionPrecisionType;//多级下拉对应的统计粒度

    @Valid
    @NotNull
    @Size(max = MAX_MAX)
    private List<@Color String> colors;//区域颜色

    @Override
    public void doCorrect(AppSettingContext context) {
        if (segmentType == SUBMIT_COUNT_SUM) {
            this.targetControlId = null;
            this.complete = isNotBlank(pageId) && isNotBlank(basedControlId);
        } else {
            this.complete = isNotBlank(pageId) && isNotBlank(basedControlId) && isNotBlank(targetControlId);
        }
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        if (!complete) {
            return;
        }

        if (context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_PAGE_NOT_EXIST, "圆环图引用的页面不存在。",
                    mapOf("refPageId", pageId));
        }

        if (context.controlNotExists(pageId, basedControlId)) {
            throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "圆环图引用的基准控件不存在。",
                    mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
        }

        if (!context.controlTypeOf(basedControlId).isAnswerCategorized()) {
            throw new MryException(NOT_SUPPORTED_BASED_CONTROL_FOR_DOUGHNUT, "基准控件不支持圆环图。",
                    mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
        }

        if (segmentType != SUBMIT_COUNT_SUM) {
            if (context.controlNotExists(pageId, targetControlId)) {
                throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "圆环图引用的目标控件不存在。",
                        mapOf("refPageId", pageId, "refTargetControlId", targetControlId));
            }

            ControlType valueControlType = context.controlTypeOf(targetControlId);
            if (!valueControlType.isAnswerNumbered()) {
                throw new MryException(NOT_SUPPORTED_TARGET_CONTROL_FOR_DOUGHNUT, "目标控件必须为数字输入类型。",
                        mapOf("refPageId", pageId, "refTargetControlId", targetControlId));
            }
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

}
