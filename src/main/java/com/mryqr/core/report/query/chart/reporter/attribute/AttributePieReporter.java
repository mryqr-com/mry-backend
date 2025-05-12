package com.mryqr.core.report.query.chart.reporter.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributePieReport;
import com.mryqr.core.app.domain.report.chart.attribute.setting.AttributeCategorizedReportSetting;
import com.mryqr.core.qr.query.stat.QrCategorizedAttributeValueSegmentStator;
import com.mryqr.core.report.query.chart.QCategorizedOptionSegmentReport;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.chart.reporter.ChartReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AttributePieReporter implements ChartReporter {
    private final QrCategorizedAttributeValueSegmentStator qrCategorizedAttributeValueSegmentStator;

    @Override
    public boolean supports(ChartReport report) {
        return report instanceof AttributePieReport;
    }

    @Override
    public QChartReport report(ChartReport report, Set<String> groupIds, App app) {
        AttributePieReport theReport = (AttributePieReport) report;
        AttributeCategorizedReportSetting setting = theReport.getSetting();

        return new QCategorizedOptionSegmentReport(qrCategorizedAttributeValueSegmentStator.statMultiple(groupIds,
                setting.getSegmentType(),
                setting.getBasedAttributeId(),
                setting.getTargetAttributeIds(),
                setting.getRange(),
                app));
    }
}
