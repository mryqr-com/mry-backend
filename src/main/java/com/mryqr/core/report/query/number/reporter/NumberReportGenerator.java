package com.mryqr.core.report.query.number.reporter;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.number.NumberReport;

import java.util.Set;

public interface NumberReportGenerator {
    boolean supports(NumberReport report);

    Double generate(NumberReport report, Set<String> groupIds, App app);
}
