package com.mryqr.core.report.query.number.reporter.page;

import com.mryqr.common.domain.report.ReportRange;
import com.mryqr.core.app.domain.report.number.page.PageNumberReport;

import java.util.Set;

public interface PageNumberReporter {
    boolean supports(PageNumberReport report);

    Double report(String pageId, Set<String> groupIds, ReportRange range);
}
