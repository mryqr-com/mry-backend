package com.mryqr.core.report.query.chart.reporter.attribute;

import com.mryqr.common.domain.stat.NumberRangeSegment;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeNumberRangeSegmentReport;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeNumberRangeSegmentReportSetting;
import com.mryqr.core.qr.query.stat.QrNumberRangeSegmentStator;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.chart.QNumberRangeSegmentReport;
import com.mryqr.core.report.query.chart.reporter.ChartReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.mryqr.common.utils.CommonUtils.splitAndSortNumberSegment;

@Component
@RequiredArgsConstructor
public class AttributeNumberRangeSegmentReporter implements ChartReporter {
    private final QrNumberRangeSegmentStator qrNumberRangeSegmentStator;

    @Override
    public boolean supports(ChartReport report) {
        return report instanceof AttributeNumberRangeSegmentReport;
    }

    @Override
    public QChartReport report(ChartReport report, Set<String> groupIds, App app) {
        AttributeNumberRangeSegmentReport theReport = (AttributeNumberRangeSegmentReport) report;
        AttributeNumberRangeSegmentReportSetting setting = theReport.getSetting();
        List<Double> numberRanges = splitAndSortNumberSegment(setting.getNumberRangesString());

        List<NumberRangeSegment> segments = qrNumberRangeSegmentStator.stat(groupIds,
                setting.getSegmentType(),
                setting.getBasedAttributeId(),
                setting.getTargetAttributeId(),
                setting.getRange(),
                numberRanges,
                app);

        return new QNumberRangeSegmentReport(numberRanges, segments);
    }
}
