package com.mryqr.core.report.query.number.reporter.instance;

import com.mryqr.common.domain.report.ReportRange;
import com.mryqr.core.app.domain.report.number.instance.InstanceNumberReport;
import com.mryqr.core.submission.domain.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.mryqr.common.domain.report.ReportRange.timeRangeOf;
import static com.mryqr.core.app.domain.report.number.instance.InstanceNumberReportType.INSTANCE_SUBMIT_COUNT;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class InstanceSubmitCountNumberReporter implements InstanceNumberReporter {
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean supports(InstanceNumberReport report) {
        return report.getInstanceNumberReportType() == INSTANCE_SUBMIT_COUNT;
    }

    @Override
    public Double report(Set<String> groupIds, ReportRange range) {
        Criteria criteria = where("groupId").in(groupIds);
        timeRangeOf(range).ifPresent(timeRange -> criteria.and("createdAt").gte(timeRange.getStartAt()).lt(timeRange.getEndAt()));
        long count = mongoTemplate.count(query(criteria), Submission.class);
        return (double) count;
    }

}
