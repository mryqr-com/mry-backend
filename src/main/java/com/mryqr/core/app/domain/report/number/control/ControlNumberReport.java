package com.mryqr.core.app.domain.report.number.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.PageAware;
import com.mryqr.core.app.domain.page.control.ControlAware;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.common.domain.report.NumberAggregationType;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.id.control.ControlId;
import com.mryqr.core.common.validation.id.page.PageId;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_NUMBER_VALUED;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_CONTROL_NOT_EXIST;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("CONTROL_NUMBER_REPORT")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class ControlNumberReport extends NumberReport implements PageAware, ControlAware {
    @PageId
    @NotNull
    private String pageId;//所引用的页面ID

    @NotNull
    @ControlId
    private String controlId;//所引用的控件ID

    @NotNull
    private NumberAggregationType numberAggregationType;

    @Override
    public void validate(AppSettingContext context) {
        if (context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_PAGE_NOT_EXIST, "报表所引用的页面不存在。",
                    mapOf("reportId", this.getId(), "refPageId", pageId));
        }

        if (context.controlNotExists(pageId, controlId)) {
            throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "报表所引用的控件不存在。",
                    mapOf("reportId", this.getId(), "refPageId", pageId, "refControlId", controlId));
        }

        ControlType controlType = context.controlTypeOf(this.controlId);
        if (!controlType.isAnswerNumbered()) {
            throw new MryException(CONTROL_NOT_NUMBER_VALUED, "所引用控件不支持数字报表。",
                    mapOf("reportId", this.getId(), "refPageId", pageId, "refControlId", controlId));
        }
    }

    @Override
    public Set<String> awaredPageIds() {
        return isNotBlank(this.pageId) ? Set.of(this.pageId) : Set.of();
    }

    @Override
    public Set<String> awaredControlIds() {
        return isNotBlank(this.controlId) ? Set.of(this.controlId) : Set.of();
    }
}
