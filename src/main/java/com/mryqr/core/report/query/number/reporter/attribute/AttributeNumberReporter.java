package com.mryqr.core.report.query.number.reporter.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.common.domain.report.NumberAggregationType;
import com.mryqr.core.common.domain.report.ReportRange;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.report.query.number.NumberResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.mryqr.core.common.domain.report.ReportRange.timeRangeOf;
import static com.mryqr.core.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class AttributeNumberReporter {
    private final MongoTemplate mongoTemplate;

    public Double report(String attributeId,
                         NumberAggregationType type,
                         Set<String> groupIds,
                         ReportRange range,
                         App app) {

        Criteria criteria = where("groupId").in(groupIds);
        timeRangeOf(range).ifPresent(timeRange -> criteria.and("createdAt").gte(timeRange.getStartAt()).lt(timeRange.getEndAt()));

        Attribute attribute = app.attributeById(attributeId);
        IndexedField indexedField = app.indexedFieldForAttribute(attributeId);
        Aggregation aggregation = newAggregation(
                match(criteria),
                group(type, mongoSortableFieldOf(indexedField)),
                project("value")
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        List<NumberResult> results = mongoTemplate.aggregate(aggregation, QR.class, NumberResult.class).getMappedResults();
        return isEmpty(results) ? null : attribute.format(results.get(0).getValue());
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
