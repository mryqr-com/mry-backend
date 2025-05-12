package com.mryqr.core.report.query.chart.reporter.control;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.control.ControlNumberRangeSegmentReport;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlNumberRangeSegmentReportSetting;
import com.mryqr.core.common.domain.report.NumberRangeSegment;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.chart.QNumberRangeSegmentReport;
import com.mryqr.core.report.query.chart.reporter.ChartReportGenerator;
import com.mryqr.core.submission.query.report.SubmissionNumberRangeSegmentReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.mryqr.core.common.utils.CommonUtils.splitAndSortNumberSegment;

@Component
@RequiredArgsConstructor
public class ControlNumberRangeSegmentReportGenerator implements ChartReportGenerator {
    private final SubmissionNumberRangeSegmentReporter submissionNumberRangeSegmentReporter;

    @Override
    public boolean supports(ChartReport report) {
        return report instanceof ControlNumberRangeSegmentReport;
    }

    @Override
    public QChartReport generate(ChartReport report, Set<String> groupIds, App app) {
        ControlNumberRangeSegmentReport theReport = (ControlNumberRangeSegmentReport) report;
        ControlNumberRangeSegmentReportSetting setting = theReport.getSetting();
        List<Double> numberRanges = splitAndSortNumberSegment(setting.getNumberRangesString());

        List<NumberRangeSegment> segments = submissionNumberRangeSegmentReporter.reportForGroups(groupIds,
                setting.getSegmentType(),
                setting.getPageId(),
                setting.getBasedControlId(),
                setting.getTargetControlId(),
                setting.getRange(),
                numberRanges,
                app);

        return new QNumberRangeSegmentReport(numberRanges, segments);
    }
}
