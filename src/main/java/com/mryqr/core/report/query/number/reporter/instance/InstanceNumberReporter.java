package com.mryqr.core.report.query.number.reporter.instance;

import com.mryqr.core.app.domain.report.number.instance.InstanceNumberReport;
import com.mryqr.core.common.domain.report.ReportRange;

import java.util.Set;

public interface InstanceNumberReporter {
    boolean supports(InstanceNumberReport report);

    Double report(Set<String> groupIds, ReportRange range);
}
