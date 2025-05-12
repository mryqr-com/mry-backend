package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.common.domain.report.ReportRange;
import com.mryqr.core.common.domain.report.SubmissionSegmentType;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.color.Color;
import com.mryqr.core.common.validation.id.control.ControlId;
import com.mryqr.core.common.validation.id.page.PageId;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.text.DecimalFormat;
import java.util.List;

import static com.mryqr.core.common.domain.report.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_NUMBERED_FOR_NUMBER_RANGE_SEGMENT;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_SUPPORT_NUMBER_RANGE_SEGMENT;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_CONTROL_NOT_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.core.common.utils.CommonUtils.splitAndSortNumberSegment;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("NUMBER_RANGE_SEGMENT_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PNumberRangeSegmentControl extends Control {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#");

    @NotNull
    private SubmissionSegmentType segmentType;//统计类型

    @PageId
    private String pageId;//所引用的页面ID

    @ControlId
    private String basedControlId;//基准控件

    @ControlId
    private String targetControlId;//目标控件，segmentType==CONTROL_VALUE_SUM时有效

    @NotNull
    private ReportRange range;//统计时间范围

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String xTitle;//x轴名称

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String yTitle;//y轴名称

    @Min(60)
    @Max(100)
    private int sizeRatio;//缩放比例

    private boolean hideControlIfNoData;//无数据时隐藏整个控件

    private boolean hideGrid;//隐藏网格线

    @Color
    private String color;//条形图颜色

    private boolean showNumber;//是否显示数值

    @Size(max = 100)
    private String numberRangesString;//分段范围设置

    @EqualsAndHashCode.Exclude
    private List<Double> numberRanges;

    @Override
    public void doCorrect(AppSettingContext context) {
        this.numberRanges = splitAndSortNumberSegment(numberRangesString);
        this.numberRangesString = numberRanges.stream().map(DECIMAL_FORMAT::format).collect(joining(","));//规范化

        if (segmentType == SUBMIT_COUNT_SUM) {
            this.targetControlId = null;
            this.complete = isNotBlank(pageId) && isNotBlank(basedControlId) && numberRanges.size() >= 2;
        } else {
            this.complete = isNotBlank(pageId) && isNotBlank(basedControlId) && isNotBlank(targetControlId) && numberRanges.size() >= 2;
        }

    }

    @Override
    protected void doValidate(AppSettingContext context) {
        if (!complete) {
            return;
        }

        if (context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_PAGE_NOT_EXIST, "值域分布图引用的页面不存在。", mapOf("refPageId", pageId));
        }

        if (context.controlNotExists(pageId, basedControlId)) {
            throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "值域分布图引用的基准控件不存在。",
                    mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
        }

        if (!context.controlTypeOf(basedControlId).isAnswerNumbered()) {
            throw new MryException(CONTROL_NOT_SUPPORT_NUMBER_RANGE_SEGMENT, "基准控件不支持值域分布图。",
                    mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
        }

        if (segmentType != SUBMIT_COUNT_SUM) {
            if (context.controlNotExists(pageId, targetControlId)) {
                throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "值域分布图引用的目标控件不存在。",
                        mapOf("refPageId", pageId, "refTargetControlId", targetControlId));
            }

            ControlType valueControlType = context.controlTypeOf(targetControlId);
            if (!valueControlType.isAnswerNumbered()) {
                throw new MryException(CONTROL_NOT_NUMBERED_FOR_NUMBER_RANGE_SEGMENT, "目标控件必须为数字输入类型。",
                        mapOf("refPageId", pageId, "refTargetControlId", targetControlId));
            }
        }

    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

}
