package com.mryqr.core.report.query.number.reporter.page;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.app.domain.report.number.page.PageNumberReport;
import com.mryqr.core.report.query.number.reporter.NumberReporter;
import com.mryqr.core.submission.domain.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.mryqr.common.domain.stat.StatRange.timeRangeOf;
import static com.mryqr.core.app.domain.report.number.page.PageNumberReportType.PAGE_SUBMIT_COUNT;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class PageSubmitCountNumberReporter implements NumberReporter {

    private final MongoTemplate mongoTemplate;

    @Override
    public boolean supports(NumberReport report) {
        if (report instanceof PageNumberReport pageNumberReport) {
            return pageNumberReport.getPageNumberReportType() == PAGE_SUBMIT_COUNT;
        }

        return false;
    }

    @Override
    public Double report(NumberReport report, Set<String> groupIds, App app) {
        PageNumberReport theReport = (PageNumberReport) report;
        Criteria criteria = where("groupId").in(groupIds).and("pageId").is(theReport.getPageId());
        timeRangeOf(theReport.getRange()).ifPresent(timeRange -> criteria.and("createdAt").gte(timeRange.getStartAt()).lt(timeRange.getEndAt()));
        long count = mongoTemplate.count(query(criteria), Submission.class);
        return (double) count;
    }

}
