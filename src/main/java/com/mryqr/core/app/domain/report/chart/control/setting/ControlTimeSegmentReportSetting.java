package com.mryqr.core.app.domain.report.chart.control.setting;

import com.mryqr.common.domain.stat.SubmissionSegmentType;
import com.mryqr.common.domain.stat.SubmissionTimeBasedType;
import com.mryqr.common.domain.stat.TimeSegmentInterval;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.id.control.ControlId;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.PageAware;
import com.mryqr.core.app.domain.page.control.ControlAware;
import com.mryqr.core.app.domain.page.control.ControlType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
import static com.mryqr.common.domain.stat.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.common.domain.stat.SubmissionTimeBasedType.CREATED_AT;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.Identified.isDuplicated;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static com.mryqr.core.app.domain.page.control.ControlType.TIMESTAMP_PROVIDING_CONTROLS;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class ControlTimeSegmentReportSetting implements ControlAware, PageAware {
    @Valid
    @NotNull
    @NotEmpty
    @NoNullElement
    @Size(max = 2)
    private List<TimeSegmentSetting> segmentSettings;

    @NotNull
    private TimeSegmentInterval interval;

    public void correct() {
        this.segmentSettings.forEach(TimeSegmentSetting::correct);
    }

    public void validate(AppSettingContext context) {
        if (isDuplicated(segmentSettings)) {
            throw new MryException(TIME_SEGMENT_ID_DUPLICATED, "统计项ID不能重复。");
        }

        this.segmentSettings.forEach(it -> it.validate(context));
    }

    @Override
    public Set<String> awaredControlIds() {
        return this.segmentSettings.stream().flatMap(setting -> Stream.of(setting.getBasedControlId(), setting.getTargetControlId()))
                .filter(StringUtils::isNotBlank)
                .collect(toImmutableSet());
    }

    @Override
    public Set<String> awaredPageIds() {
        return this.segmentSettings.stream().map(TimeSegmentSetting::getPageId)
                .filter(StringUtils::isNotBlank)
                .collect(toImmutableSet());
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
        private final SubmissionSegmentType segmentType;

        @NotNull
        private final SubmissionTimeBasedType basedType;

        @PageId
        @NotBlank
        private final String pageId;

        @ControlId
        private String basedControlId;

        @ControlId
        private String targetControlId;

        public void correct() {
            if (isBlank(name)) {
                this.name = "未命名";
            }

            if (this.segmentType == SUBMIT_COUNT_SUM) {
                this.targetControlId = null;
            }

            if (basedType == CREATED_AT) {
                this.basedControlId = null;
            }
        }

        public void validate(AppSettingContext context) {
            if (context.pageNotExists(pageId)) {
                throw new MryException(VALIDATION_PAGE_NOT_EXIST, "引用的页面不存在。", mapOf("refPageId", pageId));
            }

            if (basedType != CREATED_AT) {
                if (isBlank(basedControlId)) {
                    throw new MryException(REQUEST_VALIDATION_FAILED, "基准控件不能为空。", mapOf("refBasedControlId", basedControlId));
                }

                if (context.controlNotExists(pageId, basedControlId)) {
                    throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "引用的基准控件不存在。",
                            mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
                }

                if (!TIMESTAMP_PROVIDING_CONTROLS.contains(context.controlTypeOf(basedControlId))) {
                    throw new MryException(CONTROL_NOT_DATE, "基准控件必须为日期控件。",
                            mapOf("refPageId", pageId, "refBasedControlId", basedControlId));
                }
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
        public String getIdentifier() {
            return id;
        }
    }
}
