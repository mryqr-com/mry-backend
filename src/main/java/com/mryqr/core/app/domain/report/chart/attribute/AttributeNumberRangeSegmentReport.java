package com.mryqr.core.app.domain.report.chart.attribute;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.attribute.AttributeAware;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeNumberRangeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.style.NumberRangeSegmentReportStyle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class AttributeNumberRangeSegmentReport extends ChartReport implements AttributeAware {

    @Valid
    @NotNull
    private AttributeNumberRangeSegmentReportSetting setting;

    @Valid
    @NotNull
    private NumberRangeSegmentReportStyle style;

    @Override
    public void correct() {
        this.setting.correct();
    }

    @Override
    public void validate(AppSettingContext context) {
        this.setting.validate(context);
    }

    @Override
    public Set<String> awaredAttributeIds() {
        return this.setting.awaredAttributeIds();
    }
}
