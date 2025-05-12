package com.mryqr.core.app.domain.report;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.report.chart.ChartReportConfiguration;
import com.mryqr.core.app.domain.report.chart.ChartReportSetting;
import com.mryqr.core.app.domain.report.number.NumberReportConfiguration;
import com.mryqr.core.app.domain.report.number.NumberReportSetting;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class ReportSetting {

    @Valid
    @NotNull
    private final NumberReportSetting numberReportSetting;//数字报告设置

    @Valid
    @NotNull
    private final ChartReportSetting chartReportSetting;//图表报告设置

    public void correct() {
        this.numberReportSetting.correct();
        this.chartReportSetting.correct();
    }

    public void validate(AppSettingContext context) {
        this.numberReportSetting.validate(context);
        this.chartReportSetting.validate(context);
    }

    public static ReportSetting create() {
        NumberReportSetting numberReportSetting = NumberReportSetting.builder()
                .reports(new ArrayList<>())
                .configuration(NumberReportConfiguration.builder().gutter(20).reportPerLine(4).height(100).build())
                .build();

        ChartReportSetting chartReportSetting = ChartReportSetting.builder()
                .reports(new ArrayList<>())
                .configuration(ChartReportConfiguration.builder().gutter(20).build())
                .build();

        return ReportSetting.builder().numberReportSetting(numberReportSetting).chartReportSetting(chartReportSetting).build();
    }

    public void removePageAwareReports(Set<String> pageIds) {
        numberReportSetting.removePageAwareReports(pageIds);
        chartReportSetting.removePageAwareReports(pageIds);
    }

    public void removeControlAwareReports(Set<String> controlIds) {
        numberReportSetting.removeControlAwareReports(controlIds);
        chartReportSetting.removeControlAwareReports(controlIds);
    }

    public void removeAttributeAwareReports(Set<String> attributeIds) {
        numberReportSetting.removeAttributeAwareReports(attributeIds);
        chartReportSetting.removeAttributeAwareReports(attributeIds);
    }

}
