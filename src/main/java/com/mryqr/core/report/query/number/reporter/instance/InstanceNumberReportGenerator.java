package com.mryqr.core.report.query.number.reporter.instance;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.app.domain.report.number.instance.InstanceNumberReport;
import com.mryqr.core.report.query.number.reporter.NumberReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class InstanceNumberReportGenerator implements NumberReportGenerator {
    private final List<InstanceNumberReporter> reporters;

    @Override
    public boolean supports(NumberReport report) {
        return report instanceof InstanceNumberReport;
    }

    @Override
    public Double generate(NumberReport report, Set<String> groupIds, App app) {
        InstanceNumberReport theReport = (InstanceNumberReport) report;
        InstanceNumberReporter instanceNumberReporter = getReporter(theReport);
        return instanceNumberReporter.report(groupIds, theReport.getRange());
    }

    private InstanceNumberReporter getReporter(InstanceNumberReport theReport) {
        return reporters.stream()
                .filter(reporter -> reporter.supports(theReport)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("No instance number reporter found."));
    }
}
