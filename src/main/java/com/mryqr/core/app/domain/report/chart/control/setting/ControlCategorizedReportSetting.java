package com.mryqr.core.app.domain.report.chart.control.setting;

import com.mryqr.common.domain.AddressPrecisionType;
import com.mryqr.common.domain.stat.StatRange;
import com.mryqr.common.domain.stat.SubmissionSegmentType;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.id.control.ControlId;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.PageAware;
import com.mryqr.core.app.domain.page.control.ControlAware;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.MultiLevelSelectionPrecisionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.domain.AddressPrecisionType.CITY;
import static com.mryqr.common.domain.stat.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.core.app.domain.page.control.MultiLevelSelectionPrecisionType.LEVEL2;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class ControlCategorizedReportSetting implements ControlAware, PageAware {
    @NotNull
    private SubmissionSegmentType segmentType;

    @PageId
    @NotBlank
    private String pageId;

    @NotBlank
    @ControlId
    private String basedControlId;

    private AddressPrecisionType addressPrecisionType;
    private MultiLevelSelectionPrecisionType multiLevelSelectionPrecisionType;

    @Valid
    @NotNull
    @Size(max = 2)
    @NoNullElement
    private List<@ControlId String> targetControlIds;

    @NotNull
    private StatRange range;

    public void correct() {
        if (segmentType == SUBMIT_COUNT_SUM) {
            this.targetControlIds = List.of();
        }

        if (addressPrecisionType == null) {
            addressPrecisionType = CITY;
        }

        if (multiLevelSelectionPrecisionType == null) {
            multiLevelSelectionPrecisionType = LEVEL2;
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

        if (!context.controlTypeOf(basedControlId).isAnswerCategorized()) {
            throw new MryException(CONTROL_NOT_CATEGORIZED, "基准控件不支持分类。",
                    mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
        }

        if (segmentType != SUBMIT_COUNT_SUM) {
            if (isEmpty(this.targetControlIds)) {
                throw new MryException(REQUEST_VALIDATION_FAILED, "未选择目标控件。");
            }

            this.targetControlIds.forEach(targetControlId -> {
                if (context.controlNotExists(pageId, targetControlId)) {
                    throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "引用的目标控件不存在。",
                            mapOf("refPageId", pageId, "refTargetControlId", targetControlId));
                }

                ControlType valueControlType = context.controlTypeOf(targetControlId);
                if (!valueControlType.isAnswerNumbered()) {
                    throw new MryException(CONTROL_NOT_NUMBERED, "目标控件不支持数值。", mapOf(
                            "refPageId", pageId,
                            "refTargetControlId", targetControlId));
                }
            });
        }
    }

    @Override
    public Set<String> awaredControlIds() {
        return Stream.concat(Stream.of(this.basedControlId), this.targetControlIds.stream())
                .filter(StringUtils::isNotBlank)
                .collect(toImmutableSet());
    }

    @Override
    public Set<String> awaredPageIds() {
        return isNotBlank(this.pageId) ? Set.of(this.pageId) : Set.of();
    }
}
