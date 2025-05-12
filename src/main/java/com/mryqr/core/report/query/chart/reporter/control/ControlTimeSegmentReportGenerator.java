package com.mryqr.core.report.query.chart.reporter.control;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.control.ControlTimeSegmentReport;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlTimeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.style.TimeSegmentReportStyle;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.chart.QTimeSegmentReport;
import com.mryqr.core.report.query.chart.reporter.ChartReportGenerator;
import com.mryqr.core.submission.query.report.SubmissionTimeSegmentReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
@RequiredArgsConstructor
public class ControlTimeSegmentReportGenerator implements ChartReportGenerator {
    private final SubmissionTimeSegmentReporter submissionTimeSegmentReporter;

    @Override
    public boolean supports(ChartReport report) {
        return report instanceof ControlTimeSegmentReport;
    }

    @Override
    public QChartReport generate(ChartReport report, Set<String> groupIds, App app) {
        ControlTimeSegmentReport theReport = (ControlTimeSegmentReport) report;
        ControlTimeSegmentReportSetting setting = theReport.getSetting();
        TimeSegmentReportStyle style = theReport.getStyle();

        return new QTimeSegmentReport(setting.getInterval(), setting.getSegmentSettings().stream()
                .map(segmentSetting -> submissionTimeSegmentReporter.reportForGroups(groupIds,
                        segmentSetting.getSegmentType(),
                        segmentSetting.getBasedType(),
                        segmentSetting.getPageId(),
                        segmentSetting.getBasedControlId(),
                        segmentSetting.getTargetControlId(),
                        setting.getInterval(),
                        style.getMax(),
                        app))
                .collect(toImmutableList()));
    }
}
