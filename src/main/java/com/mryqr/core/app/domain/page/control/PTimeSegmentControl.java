package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.domain.report.SubmissionReportTimeBasedType;
import com.mryqr.common.domain.report.SubmissionSegmentType;
import com.mryqr.common.domain.report.TimeSegmentInterval;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.color.Color;
import com.mryqr.common.validation.id.control.ControlId;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.domain.report.SubmissionReportTimeBasedType.CREATED_AT;
import static com.mryqr.common.domain.report.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.Identified.isDuplicated;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static com.mryqr.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static com.mryqr.core.app.domain.page.control.ControlType.DATE;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("TIME_SEGMENT_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PTimeSegmentControl extends Control {
    public static final int MIN_MAX = 1;
    public static final int MAX_MAX = 20;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = 2)
    private List<TimeSegmentSetting> segmentSettings;

    @NotNull
    private TimeSegmentInterval interval;//时段间隔

    @Min(MIN_MAX)
    @Max(MAX_MAX)
    private int max;//最多显示的条数

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String xTitle;//x轴名称

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String yTitle;//y轴名称

    @Min(60)
    @Max(100)
    private int sizeRatio;//缩放比例

    @Valid
    @NotNull
    @Size(max = 2)
    private List<@Color String> colors;//条形颜色

    private boolean horizontal;//是否水平显示

    private boolean hideGrid;//隐藏网格线

    private boolean hideControlIfNoData;//无数据时隐藏整个控件

    private boolean showNumber;//是否显示数值

    @Override
    public void doCorrect(AppSettingContext context) {
        this.segmentSettings.forEach(TimeSegmentSetting::correct);
        this.colors = this.colors.stream().map(color -> isNotBlank(color) ? color : "#FF8C00").collect(toImmutableList());
        this.complete = this.segmentSettings.stream().anyMatch(TimeSegmentSetting::isComplete);
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        if (isDuplicated(segmentSettings)) {
            throw new MryException(TIME_SEGMENT_ID_DUPLICATED, "统计项ID不能重复。");
        }

        segmentSettings.forEach(item -> item.validate(context));
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    @Getter
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor(access = PRIVATE)
    public static class TimeSegmentSetting implements Identified {
        @NotNull
        @ShortUuid
        private final String id;

        @Size(max = MAX_SHORT_NAME_LENGTH)
        private String name;

        @NotNull
        private SubmissionSegmentType segmentType;//统计类型

        @PageId
        private String pageId;//所引用的页面ID

        @NotNull
        private SubmissionReportTimeBasedType basedType;//基准类型

        @ControlId
        private String basedControlId;//基准控件ID

        @ControlId
        private String targetControlId;//目标控件ID

        @EqualsAndHashCode.Exclude
        private boolean complete;

        public void correct() {
            if (isBlank(name)) {
                this.name = "未命名";
            }

            boolean pageComplete = isNotBlank(this.pageId);

            boolean targetComplete = true;
            if (this.segmentType == SUBMIT_COUNT_SUM) {
                this.targetControlId = null;
            } else {
                targetComplete = isNotBlank(targetControlId);
            }

            boolean basedComplete = true;
            if (basedType == CREATED_AT) {
                this.basedControlId = null;
            } else {
                basedComplete = isNotBlank(this.basedControlId);
            }

            this.complete = pageComplete && targetComplete && basedComplete;
        }

        public void validate(AppSettingContext context) {
            if (!complete) {
                return;
            }

            if (context.pageNotExists(pageId)) {
                throw new MryException(VALIDATION_PAGE_NOT_EXIST, "分时统计控件引用的页面不存在。", mapOf("refPageId", pageId));
            }

            if (basedType != CREATED_AT) {
                if (context.controlNotExists(pageId, basedControlId)) {
                    throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "分时统计控件引用的基准控件不存在。",
                            mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
                }

                if (context.controlTypeOf(basedControlId) != DATE) {
                    throw new MryException(NOT_SUPPORTED_BASED_CONTROL_FOR_TIME_SEGMENT, "基准控件必须为日期控件。",
                            mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
                }
            }

            if (segmentType != SUBMIT_COUNT_SUM) {
                if (context.controlNotExists(pageId, targetControlId)) {
                    throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "分时统计控件引用的目标控件不存在。",
                            mapOf("refPageId", pageId, "refTargetControlId", targetControlId));
                }

                if (!context.controlTypeOf(targetControlId).isAnswerNumbered()) {
                    throw new MryException(NOT_SUPPORTED_TARGET_CONTROL_FOR_TIME_SEGMENT, "目标控件不支持分时统计控件。",
                            mapOf("refPageId", pageId, "refTargetControlId", targetControlId));
                }
            }
        }

        @Override
        public String getIdentifier() {
            return id;
        }
    }
}
