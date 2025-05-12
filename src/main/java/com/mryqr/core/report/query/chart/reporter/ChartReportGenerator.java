package com.mryqr.core.report.query.chart.reporter;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.report.query.chart.QChartReport;

import java.util.Set;

public interface ChartReportGenerator {
    boolean supports(ChartReport report);

    QChartReport generate(ChartReport report, Set<String> groupIds, App app);
}
