package com.mryqr.core.qr.query.report;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.common.domain.report.CategorizedOptionSegment;
import com.mryqr.core.common.domain.report.QrSegmentType;
import com.mryqr.core.common.domain.report.ReportRange;
import com.mryqr.core.qr.domain.QR;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.common.domain.report.QrSegmentType.QR_COUNT_SUM;
import static com.mryqr.core.common.domain.report.ReportRange.timeRangeOf;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static com.mryqr.core.common.utils.MongoCriteriaUtils.mongoTextFieldOf;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class QrCategorizedAttributeValueSegmentReporter {
    private final MongoTemplate mongoTemplate;

    public List<List<CategorizedOptionSegment>> reportMultiple(Set<String> groupIds,
                                                               QrSegmentType segmentType,
                                                               String basedAttributeId,
                                                               List<String> targetAttributeIds,
                                                               ReportRange range,
                                                               App app) {
        if (segmentType == QR_COUNT_SUM) {
            return List.of(this.report(groupIds, segmentType, basedAttributeId, null, range, app));
        } else {
            return targetAttributeIds.stream()
                    .map(targetAttributeId -> report(groupIds, segmentType, basedAttributeId, targetAttributeId, range, app))
                    .collect(toImmutableList());
        }
    }

    public List<CategorizedOptionSegment> report(Set<String> groupIds,
                                                 QrSegmentType segmentType,
                                                 String basedAttributeId,
                                                 String targetAttributeId,
                                                 ReportRange range,
                                                 App app) {
        requireNonNull(groupIds, "Group IDs must not be blank.");
        requireNonNull(segmentType, "Segment type must not be null.");
        requireNonBlank(basedAttributeId, "Based attribute ID must not be blank.");
        requireNonNull(range, "Statistic range must not be null.");
        requireNonNull(app, "App must not be null.");

        return segmentType == QR_COUNT_SUM ?
                segmentForQrCount(groupIds, basedAttributeId, range, app) :
                segmentForAttributeValue(groupIds, basedAttributeId, targetAttributeId, range, app, segmentType);
    }

    private List<CategorizedOptionSegment> segmentForQrCount(Set<String> groupIds,
                                                             String basedAttributeId,
                                                             ReportRange range,
                                                             App app) {
        Criteria criteria = baseCriteria(groupIds, range);
        String basedAttributeField = mongoTextFieldOf(app.indexedFieldForAttribute(basedAttributeId));
        Aggregation aggregation = newAggregation(
                match(criteria),
                unwind(basedAttributeField),
                project().and(basedAttributeField).as("field"),
                group("field").count().as("value"),
                project("value").and("_id").as("option")
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        return mongoTemplate.aggregate(aggregation, QR.class, CategorizedOptionSegment.class)
                .getMappedResults();
    }

    private List<CategorizedOptionSegment> segmentForAttributeValue(Set<String> groupIds,
                                                                    String basedAttributeId,
                                                                    String targetAttributeId,
                                                                    ReportRange range,
                                                                    App app,
                                                                    QrSegmentType segmentType) {
        requireNonBlank(targetAttributeId, "Target Attribute ID must not be blank.");
        Criteria criteria = baseCriteria(groupIds, range);
        String optionFiled = mongoTextFieldOf(app.indexedFieldForAttribute(basedAttributeId));
        String sortableField = mongoSortableFieldOf(app.indexedFieldForAttribute(targetAttributeId));
        criteria.and(sortableField).ne(null);

        Aggregation aggregation = newAggregation(
                match(criteria),
                unwind(optionFiled),
                project().and(optionFiled).as("option").and(sortableField).as("number"),
                statisticsGroup(segmentType),
                project("value").and("_id").as("option")
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        List<CategorizedOptionSegment> results = mongoTemplate.aggregate(aggregation, QR.class, CategorizedOptionSegment.class)
                .getMappedResults();

        Attribute targetAttribute = app.attributeById(targetAttributeId);
        return results.stream().map(segment -> CategorizedOptionSegment.builder()
                        .option(segment.getOption())
                        .value(targetAttribute.format(segment.getValue()))
                        .build())
                .collect(toImmutableList());
    }

    private GroupOperation statisticsGroup(QrSegmentType segmentType) {
        switch (segmentType) {
            case ATTRIBUTE_VALUE_SUM -> {
                return group("option").sum("number").as("value");
            }
            case ATTRIBUTE_VALUE_AVG -> {
                return group("option").avg("number").as("value");
            }
            case ATTRIBUTE_VALUE_MAX -> {
                return group("option").max("number").as("value");
            }
            case ATTRIBUTE_VALUE_MIN -> {
                return group("option").min("number").as("value");
            }
            default -> {
                throw new IllegalStateException("Statistics segment type[" + segmentType.name() + "] not supported.");
            }
        }
    }

    private Criteria baseCriteria(Set<String> groupIds, ReportRange range) {
        Criteria criteria = where("groupId").in(groupIds);
        timeRangeOf(range).ifPresent(timeRange -> criteria.and("createdAt").gte(timeRange.getStartAt()).lt(timeRange.getEndAt()));
        return criteria;
    }
}
