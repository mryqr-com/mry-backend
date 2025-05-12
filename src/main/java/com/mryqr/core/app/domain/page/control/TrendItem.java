package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.domain.report.SubmissionReportTimeBasedType;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.id.control.ControlId;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.AppSettingContext;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.common.domain.report.SubmissionReportTimeBasedType.CREATED_AT;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static com.mryqr.core.app.domain.page.control.ControlType.DATE;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class TrendItem implements Identified {
    @NotNull
    @ShortUuid
    private final String id;

    @NotNull
    private SubmissionReportTimeBasedType basedType;

    @PageId
    private final String pageId;

    @ControlId
    private String basedControlId;

    @ControlId
    private final String targetControlId;

    @Size(max = MAX_SHORT_NAME_LENGTH)
    private String name;

    @EqualsAndHashCode.Exclude
    private boolean complete;

    public void correct() {
        if (isBlank(name)) {
            this.name = "未命名趋势项";
        }

        boolean pageComplete = isNotBlank(pageId);

        boolean basedComplete = true;
        if (basedType == CREATED_AT) {
            basedControlId = null;
        } else {
            basedComplete = isNotBlank(this.basedControlId);
        }

        boolean targetComplete = isNotBlank(targetControlId);

        this.complete = pageComplete && basedComplete && targetComplete;
    }

    public void validate(AppSettingContext context) {
        if (!complete) {
            return;
        }

        if (context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_PAGE_NOT_EXIST,
                    "趋势图项引用的页面不存在。",
                    mapOf("trendItemId", id,
                            "refPageId", pageId));
        }

        if (basedType != CREATED_AT) {
            if (context.controlNotExists(pageId, basedControlId)) {
                throw new MryException(VALIDATION_CONTROL_NOT_EXIST,
                        "趋势图项引用的基准控件不存在。",
                        mapOf("trendItemId", id,
                                "refPageId", pageId,
                                "refBasedControlId", basedControlId));
            }

            if (context.controlTypeOf(basedControlId) != DATE) {
                throw new MryException(CONTROL_NOT_DATE,
                        "趋势图项引用的基准控件必须为日期控件。",
                        mapOf("trendItemId", id,
                                "refPageId", pageId,
                                "refBasedControlId", basedControlId));
            }
        }

        if (context.controlNotExists(pageId, targetControlId)) {
            throw new MryException(VALIDATION_CONTROL_NOT_EXIST,
                    "趋势图项引用的目标控件不存在。",
                    mapOf("trendItemId", id,
                            "refPageId", pageId,
                            "refTargetControlId", targetControlId));
        }

        if (!context.controlTypeOf(targetControlId).isAnswerNumbered()) {
            throw new MryException(NOT_SUPPORTED_TARGET_CONTROL_FOR_TREND,
                    "趋势图项引用目标控件不支持趋势图。",
                    mapOf("trendItemId", id,
                            "refPageId", pageId,
                            "refTargetControlId", targetControlId));
        }
    }

    @Override
    public String getIdentifier() {
        return id;
    }
}
