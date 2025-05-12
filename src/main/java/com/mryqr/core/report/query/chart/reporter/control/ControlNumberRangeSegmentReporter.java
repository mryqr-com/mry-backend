package com.mryqr.core.report.query.chart.reporter.control;

import com.mryqr.common.domain.stat.NumberRangeSegment;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.control.ControlNumberRangeSegmentReport;
import com.mryqr.core.app.domain.report.chart.control.setting.ControlNumberRangeSegmentReportSetting;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.chart.QNumberRangeSegmentReport;
import com.mryqr.core.report.query.chart.reporter.ChartReporter;
import com.mryqr.core.submission.query.stat.SubmissionNumberRangeSegmentStator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.mryqr.common.utils.CommonUtils.splitAndSortNumberSegment;

@Component
@RequiredArgsConstructor
public class ControlNumberRangeSegmentReporter implements ChartReporter {
    private final SubmissionNumberRangeSegmentStator submissionNumberRangeSegmentStator;

    @Override
    public boolean supports(ChartReport report) {
        return report instanceof ControlNumberRangeSegmentReport;
    }

    @Override
    public QChartReport report(ChartReport report, Set<String> groupIds, App app) {
        ControlNumberRangeSegmentReport theReport = (ControlNumberRangeSegmentReport) report;
        ControlNumberRangeSegmentReportSetting setting = theReport.getSetting();
        List<Double> numberRanges = splitAndSortNumberSegment(setting.getNumberRangesString());

        List<NumberRangeSegment> segments = submissionNumberRangeSegmentStator.statForGroups(groupIds,
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
