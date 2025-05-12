package com.mryqr.core.app.domain.report.number;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.attribute.AttributeAware;
import com.mryqr.core.app.domain.page.PageAware;
import com.mryqr.core.app.domain.page.control.ControlAware;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Set;

import static com.mryqr.core.common.exception.ErrorCode.NUMBER_REPORT_ID_DUPLICATED;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static com.mryqr.core.common.utils.MryConstants.MAX_PER_APP_NUMBER_REPORT_SIZE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class NumberReportSetting {
    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_PER_APP_NUMBER_REPORT_SIZE)
    private final List<NumberReport> reports;//报告

    @Valid
    @NotNull
    private final NumberReportConfiguration configuration;//基本设置

    public void correct() {
    }

    public void validate(AppSettingContext context) {
        if (isDuplicated(reports)) {
            throw new MryException(NUMBER_REPORT_ID_DUPLICATED, "数字报表ID不能重复。");
        }

        reports.forEach(report -> report.validate(context));
    }

    public void removePageAwareReports(Set<String> pageIds) {
        reports.removeIf(report -> {
            if (report instanceof PageAware pageAware) {
                return pageAware.awaredPageIds().stream().anyMatch(pageIds::contains);
            }
            return false;
        });
    }

    public void removeControlAwareReports(Set<String> controlIds) {
        reports.removeIf(report -> {
            if (report instanceof ControlAware controlAware) {
                return controlAware.awaredControlIds().stream().anyMatch(controlIds::contains);
            }
            return false;
        });
    }

    public void removeAttributeAwareReports(Set<String> attributeIds) {
        reports.removeIf(report -> {
            if (report instanceof AttributeAware attributeAware) {
                return attributeAware.awaredAttributeIds().stream().anyMatch(attributeIds::contains);
            }
            return false;
        });
    }

}
