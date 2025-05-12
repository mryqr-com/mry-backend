package com.mryqr.core.app.domain.report.chart.control.setting;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.PageAware;
import com.mryqr.core.app.domain.page.control.ControlAware;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.common.domain.report.ReportRange;
import com.mryqr.core.common.domain.report.SubmissionSegmentType;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.id.control.ControlId;
import com.mryqr.core.common.validation.id.page.PageId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_NUMBERED;
import static com.mryqr.core.common.exception.ErrorCode.REQUEST_VALIDATION_FAILED;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_CONTROL_NOT_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class ControlNumberRangeSegmentReportSetting implements ControlAware, PageAware {
    @NotNull
    private SubmissionSegmentType segmentType;

    @PageId
    @NotBlank
    private String pageId;

    @NotBlank
    @ControlId
    private String basedControlId;

    @NotBlank
    @Size(max = 100)
    private String numberRangesString;

    @ControlId
    private String targetControlId;

    @NotNull
    private ReportRange range;

    public void correct() {
        if (segmentType == SUBMIT_COUNT_SUM) {
            this.targetControlId = null;
        }
    }

    public void validate(AppSettingContext context) {
        if (context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_PAGE_NOT_EXIST, "引用的页面不存在。", mapOf("refPageId", pageId));
        }

        if (context.controlNotExists(pageId, basedControlId)) {
            throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "引用的基准控件不存在。",
                    mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
        }

        if (!context.controlTypeOf(basedControlId).isAnswerNumbered()) {
            throw new MryException(CONTROL_NOT_NUMBERED, "基准控件不支持数值。",
                    mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
        }

        if (segmentType != SUBMIT_COUNT_SUM) {
            if (isBlank(targetControlId)) {
                throw new MryException(REQUEST_VALIDATION_FAILED, "目标控件不能为空。", mapOf("refTargetControlId", targetControlId));
            }

            if (context.controlNotExists(pageId, targetControlId)) {
                throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "引用的目标控件不存在。",
                        mapOf("refPageId", pageId, "refTargetControlId", targetControlId));
            }

            ControlType valueControlType = context.controlTypeOf(targetControlId);
            if (!valueControlType.isAnswerNumbered()) {
                throw new MryException(CONTROL_NOT_NUMBERED, "目标控件不支持数值。",
                        mapOf("refPageId", pageId, "refTargetControlId", targetControlId));
            }
        }
    }

    @Override
    public Set<String> awaredControlIds() {
        return Stream.of(this.basedControlId, this.targetControlId)
                .filter(StringUtils::isNotBlank)
                .collect(toImmutableSet());
    }

    @Override
    public Set<String> awaredPageIds() {
        return isNotBlank(this.pageId) ? Set.of(this.pageId) : Set.of();
    }
}
