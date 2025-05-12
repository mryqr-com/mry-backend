package com.mryqr.core.report.query.chart.reporter.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeBarReport;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeCategorizedReportSetting;
import com.mryqr.core.qr.query.report.QrCategorizedAttributeValueSegmentReporter;
import com.mryqr.core.report.query.chart.QCategorizedOptionSegmentReport;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.chart.reporter.ChartReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AttributeBarReportGenerator implements ChartReportGenerator {
    private final QrCategorizedAttributeValueSegmentReporter qrCategorizedAttributeValueSegmentReporter;

    @Override
    public boolean supports(ChartReport report) {
        return report instanceof AttributeBarReport;
    }

    @Override
    public QChartReport generate(ChartReport report, Set<String> groupIds, App app) {
        AttributeBarReport theReport = (AttributeBarReport) report;
        AttributeCategorizedReportSetting setting = theReport.getSetting();

        return new QCategorizedOptionSegmentReport(qrCategorizedAttributeValueSegmentReporter.reportMultiple(groupIds,
                setting.getSegmentType(),
                setting.getBasedAttributeId(),
                setting.getTargetAttributeIds(),
                setting.getRange(),
                app));
    }
}
