package com.mryqr.core.report.query.number.reporter.page;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.app.domain.report.number.page.PageNumberReport;
import com.mryqr.core.report.query.number.reporter.NumberReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PageNumberReportGenerator implements NumberReportGenerator {
    private final List<PageNumberReporter> reporters;

    @Override
    public boolean supports(NumberReport report) {
        return report instanceof PageNumberReport;
    }

    @Override
    public Double generate(NumberReport report, Set<String> groupIds, App app) {
        PageNumberReport theReport = (PageNumberReport) report;
        PageNumberReporter reporter = getReporter(theReport);
        return reporter.report(theReport.getPageId(), groupIds, theReport.getRange());
    }

    private PageNumberReporter getReporter(PageNumberReport theReport) {
        return reporters.stream()
                .filter(reporter -> reporter.supports(theReport)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("No page number reporter found."));
    }
}
