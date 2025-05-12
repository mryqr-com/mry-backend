package com.mryqr.core.report.query.number.reporter.page;

import com.mryqr.core.app.domain.report.number.page.PageNumberReport;
import com.mryqr.core.common.domain.report.ReportRange;
import com.mryqr.core.submission.domain.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.mryqr.core.app.domain.report.number.page.PageNumberReportType.PAGE_SUBMIT_COUNT;
import static com.mryqr.core.common.domain.report.ReportRange.timeRangeOf;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class PageSubmitCountNumberReporter implements PageNumberReporter {
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean supports(PageNumberReport report) {
        return report.getPageNumberReportType() == PAGE_SUBMIT_COUNT;
    }

    @Override
    public Double report(String pageId, Set<String> groupIds, ReportRange range) {
        Criteria criteria = where("groupId").in(groupIds).and("pageId").is(pageId);
        timeRangeOf(range).ifPresent(timeRange -> criteria.and("createdAt").gte(timeRange.getStartAt()).lt(timeRange.getEndAt()));
        long count = mongoTemplate.count(query(criteria), Submission.class);
        return (double) count;
    }
}
