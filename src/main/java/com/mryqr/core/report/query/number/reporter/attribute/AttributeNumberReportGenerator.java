package com.mryqr.core.report.query.number.reporter.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.app.domain.report.number.attribute.AttributeNumberReport;
import com.mryqr.core.report.query.number.reporter.NumberReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AttributeNumberReportGenerator implements NumberReportGenerator {
    private final AttributeNumberReporter attributeNumberReporter;

    @Override
    public boolean supports(NumberReport report) {
        return report instanceof AttributeNumberReport;
    }

    @Override
    public Double generate(NumberReport report, Set<String> groupIds, App app) {
        AttributeNumberReport theReport = (AttributeNumberReport) report;

        return attributeNumberReporter.report(theReport.getAttributeId(),
                theReport.getNumberAggregationType(),
                groupIds,
                theReport.getRange(),
                app);
    }
}
