package com.mryqr.core.report.query.chart.reporter.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeTimeSegmentReport;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeTimeSegmentReportSetting;
import com.mryqr.core.app.domain.report.chart.style.TimeSegmentReportStyle;
import com.mryqr.core.qr.query.stat.QrTimeSegmentStator;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.chart.QTimeSegmentReport;
import com.mryqr.core.report.query.chart.reporter.ChartReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
@RequiredArgsConstructor
public class AttributeTimeSegmentReporter implements ChartReporter {
    private final QrTimeSegmentStator qrTimeSegmentStator;

    @Override
    public boolean supports(ChartReport report) {
        return report instanceof AttributeTimeSegmentReport;
    }

    @Override
    public QChartReport report(ChartReport report, Set<String> groupIds, App app) {
        AttributeTimeSegmentReport theReport = (AttributeTimeSegmentReport) report;
        AttributeTimeSegmentReportSetting setting = theReport.getSetting();
        TimeSegmentReportStyle style = theReport.getStyle();

        return new QTimeSegmentReport(setting.getInterval(), setting.getSegmentSettings().stream()
                .map(segmentSetting -> qrTimeSegmentStator.stat(groupIds,
                        segmentSetting.getSegmentType(),
                        segmentSetting.getBasedType(),
                        segmentSetting.getBasedAttributeId(),
                        segmentSetting.getTargetAttributeId(),
                        setting.getInterval(),
                        style.getMax(),
                        app))
                .collect(toImmutableList()));
    }
}
