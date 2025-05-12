package com.mryqr.core.report.query.number.reporter.instance;

import com.mryqr.common.domain.report.ReportRange;
import com.mryqr.common.domain.report.TimeRange;
import com.mryqr.core.app.domain.report.number.instance.InstanceNumberReport;
import com.mryqr.core.qr.domain.QR;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static com.mryqr.common.domain.report.ReportRange.timeRangeOf;
import static com.mryqr.core.app.domain.report.number.instance.InstanceNumberReportType.ACCESSED_INSTANCE_COUNT;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class AccessedInstanceCountNumberReporter implements InstanceNumberReporter {
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean supports(InstanceNumberReport report) {
        return report.getInstanceNumberReportType() == ACCESSED_INSTANCE_COUNT;
    }

    @Override
    public Double report(Set<String> groupIds, ReportRange range) {
        Criteria criteria = where("groupId").in(groupIds);
        Optional<TimeRange> timeRangeOptional = timeRangeOf(range);
        if (timeRangeOptional.isPresent()) {
            TimeRange timeRange = timeRangeOptional.get();
            criteria.and("lastAccessedAt").gte(timeRange.getStartAt()).lt(timeRange.getEndAt());
        } else {
            criteria.and("lastAccessedAt").ne(null);
        }
        long count = mongoTemplate.count(query(criteria), QR.class);
        return (double) count;
    }

}
