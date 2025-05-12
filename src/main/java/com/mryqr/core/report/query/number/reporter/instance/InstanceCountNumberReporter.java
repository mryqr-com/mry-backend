package com.mryqr.core.report.query.number.reporter.instance;

import com.mryqr.core.app.domain.report.number.instance.InstanceNumberReport;
import com.mryqr.core.common.domain.report.ReportRange;
import com.mryqr.core.qr.domain.QR;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.mryqr.core.app.domain.report.number.instance.InstanceNumberReportType.INSTANCE_COUNT;
import static com.mryqr.core.common.domain.report.ReportRange.timeRangeOf;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class InstanceCountNumberReporter implements InstanceNumberReporter {
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean supports(InstanceNumberReport report) {
        return report.getInstanceNumberReportType() == INSTANCE_COUNT;
    }

    @Override
    public Double report(Set<String> groupIds, ReportRange range) {
        Criteria criteria = where("groupId").in(groupIds);
        timeRangeOf(range).ifPresent(timeRange -> criteria.and("createdAt").gte(timeRange.getStartAt()).lt(timeRange.getEndAt()));
        long count = mongoTemplate.count(query(criteria), QR.class);
        return (double) count;
    }

}
