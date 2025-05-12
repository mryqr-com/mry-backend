package com.mryqr.core.report.query.chart.reporter.control;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.control.ControlBarReport;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlCategorizedReportSetting;
import com.mryqr.core.report.query.chart.QCategorizedOptionSegmentReport;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.chart.reporter.ChartReporter;
import com.mryqr.core.submission.query.stat.SubmissionCategorizedAnswerSegmentStator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ControlBarReporter implements ChartReporter {
    private final SubmissionCategorizedAnswerSegmentStator submissionCategorizedAnswerSegmentStator;

    @Override
    public boolean supports(ChartReport report) {
        return report instanceof ControlBarReport;
    }

    @Override
    public QChartReport report(ChartReport report, Set<String> groupIds, App app) {
        ControlBarReport theReport = (ControlBarReport) report;
        ControlCategorizedReportSetting setting = theReport.getSetting();

        return new QCategorizedOptionSegmentReport(submissionCategorizedAnswerSegmentStator.statForGroupsMultiple(
                groupIds,
                setting.getSegmentType(),
                setting.getPageId(),
                setting.getBasedControlId(),
                setting.getTargetControlIds(),
                setting.getRange(),
                app
        ));
    }
}
