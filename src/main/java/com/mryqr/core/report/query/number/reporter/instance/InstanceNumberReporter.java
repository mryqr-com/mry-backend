package com.mryqr.core.report.query.number.reporter.instance;

import com.mryqr.common.domain.report.ReportRange;
import com.mryqr.core.app.domain.report.number.instance.InstanceNumberReport;

import java.util.Set;

public interface InstanceNumberReporter {
    boolean supports(InstanceNumberReport report);

    Double report(Set<String> groupIds, ReportRange range);
}
