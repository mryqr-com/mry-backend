package com.mryqr.core.report.query.chart.reporter.control;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.control.ControlPieReport;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlCategorizedReportSetting;
import com.mryqr.core.report.query.chart.QCategorizedOptionSegmentReport;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.chart.reporter.ChartReportGenerator;
import com.mryqr.core.submission.query.report.SubmissionCategorizedAnswerSegmentReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ControlPieReportGenerator implements ChartReportGenerator {
    private final SubmissionCategorizedAnswerSegmentReporter submissionCategorizedAnswerSegmentReporter;

    @Override
    public boolean supports(ChartReport report) {
        return report instanceof ControlPieReport;
    }

    @Override
    public QChartReport generate(ChartReport report, Set<String> groupIds, App app) {
        ControlPieReport theReport = (ControlPieReport) report;
        ControlCategorizedReportSetting setting = theReport.getSetting();

        return new QCategorizedOptionSegmentReport(submissionCategorizedAnswerSegmentReporter.reportForGroupsMultiple(
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
