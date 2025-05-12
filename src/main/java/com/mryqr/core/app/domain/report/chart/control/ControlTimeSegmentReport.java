package com.mryqr.core.app.domain.report.chart.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.PageAware;
import com.mryqr.core.app.domain.page.control.ControlAware;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlTimeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.style.TimeSegmentReportStyle;
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
@TypeAlias("CONTROL_TIME_SEGMENT_REPORT")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class ControlTimeSegmentReport extends ChartReport implements ControlAware, PageAware {

    @Valid
    @NotNull
    private ControlTimeSegmentReportSetting setting;

    @Valid
    @NotNull
    private TimeSegmentReportStyle style;

    @Override
    public void correct() {
        this.setting.correct();
    }

    @Override
    public void validate(AppSettingContext context) {
        this.setting.validate(context);
    }

    @Override
    public Set<String> awaredControlIds() {
        return this.setting.awaredControlIds();
    }

    @Override
    public Set<String> awaredPageIds() {
        return this.setting.awaredPageIds();
    }
}
