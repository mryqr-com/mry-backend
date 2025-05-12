package com.mryqr.core.report.query.number.reporter.control;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.app.domain.report.number.control.ControlNumberReport;
import com.mryqr.core.report.query.number.reporter.NumberReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ControlNumberReportGenerator implements NumberReportGenerator {
    private final ControlNumberReporter controlNumberReporter;

    @Override
    public boolean supports(NumberReport report) {
        return report instanceof ControlNumberReport;
    }

    @Override
    public Double generate(NumberReport report, Set<String> groupIds, App app) {
        ControlNumberReport theReport = (ControlNumberReport) report;

        return controlNumberReporter.report(theReport.getPageId(),
                theReport.getControlId(),
                theReport.getNumberAggregationType(),
                groupIds,
                theReport.getRange(),
                app);
    }
}
