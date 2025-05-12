package com.mryqr.core.report;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.report.query.chart.ChartReportQuery;
import com.mryqr.core.report.query.chart.ChartReportQueryService;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.number.NumberReportQuery;
import com.mryqr.core.report.query.number.NumberReportQueryService;
import com.mryqr.core.report.query.number.QNumberReport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/reports")
public class ReportController {
    private final NumberReportQueryService numberReportQueryService;
    private final ChartReportQueryService chartReportQueryService;

    @PostMapping(value = "/number")
    public QNumberReport fetchNumberReport(@RequestBody @Valid NumberReportQuery queryCommand,
                                           @AuthenticationPrincipal User user) {
        return numberReportQueryService.fetchNumberReport(queryCommand, user);
    }

    @PostMapping(value = "/chart")
    public QChartReport fetchChartReport(@RequestBody @Valid ChartReportQuery queryCommand,
                                         @AuthenticationPrincipal User user) {
        return chartReportQueryService.fetchChartReport(queryCommand, user);
    }

}
