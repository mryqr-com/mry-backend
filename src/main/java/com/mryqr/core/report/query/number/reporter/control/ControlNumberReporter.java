package com.mryqr.core.report.query.number.reporter.control;

import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.report.NumberAggregationType;
import com.mryqr.common.domain.report.ReportRange;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.report.query.number.NumberResult;
import com.mryqr.core.submission.domain.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.mryqr.common.domain.report.ReportRange.timeRangeOf;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class ControlNumberReporter {
    private final MongoTemplate mongoTemplate;

    public Double report(String pageId,
                         String controlId,
                         NumberAggregationType type,
                         Set<String> groupIds,
                         ReportRange range,
                         App app) {
        Criteria criteria = where("groupId").in(groupIds).and("pageId").is(pageId);
        timeRangeOf(range).ifPresent(timeRange -> criteria.and("createdAt").gte(timeRange.getStartAt()).lt(timeRange.getEndAt()));
        IndexedField indexedField = app.indexedFieldForControl(pageId, controlId);
        Control control = app.controlById(controlId);
        Aggregation aggregation = newAggregation(
                match(criteria),
                group(type, mongoSortableFieldOf(indexedField)),
                project("value")
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        List<NumberResult> results = mongoTemplate.aggregate(aggregation, Submission.class, NumberResult.class).getMappedResults();
        return isEmpty(results) ? null : control.format(results.get(0).getValue());
    }

    private GroupOperation group(NumberAggregationType type, String field) {
        switch (type) {
            case MAX -> {
                return Aggregation.group().max(field).as("value");
            }
            case MIN -> {
                return Aggregation.group().min(field).as("value");
            }
            case AVG -> {
                return Aggregation.group().avg(field).as("value");
            }
            case SUM -> {
                return Aggregation.group().sum(field).as("value");
            }
            default -> {
                throw new IllegalStateException("Number aggregation type[" + type.name() + "] not supported.");
            }
        }
    }

}
