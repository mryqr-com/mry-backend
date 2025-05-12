package com.mryqr.core.qr.query.stat;

import com.mryqr.common.domain.stat.NumberRangeSegment;
import com.mryqr.common.domain.stat.QrSegmentType;
import com.mryqr.common.domain.stat.StatRange;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.BucketOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.common.domain.stat.QrSegmentType.QR_COUNT_SUM;
import static com.mryqr.common.domain.stat.StatRange.timeRangeOf;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class QrNumberRangeSegmentStator {
    private static final int DEFAULT = Integer.MIN_VALUE;
    private static final String VALUE = "value";
    private static final String SEGMENT = "segment";
    private static final String BASED_FIELD = "basedField";
    private static final String TARGET_FIELD = "targetField";
    private final MongoTemplate mongoTemplate;

    public List<NumberRangeSegment> stat(Set<String> groupIds,
                                         QrSegmentType segmentType,
                                         String basedAttributeId,
                                         String targetAttributeId,
                                         StatRange range,
                                         List<Double> segments,
                                         App app) {
        requireNonNull(groupIds, "Group IDs must not be null.");
        requireNonNull(segmentType, "Segment type must not be null.");
        requireNonBlank(basedAttributeId, "Based attribute ID must not be blank.");
        requireNonNull(range, "Statistic range must not be null.");
        requireNonNull(segments, "Segments must not be null.");
        requireNonNull(app, "App must not be null.");

        if (segments.size() <= 1) {
            return List.of();
        }

        List<NumberRangeSegment> results = segmentType == QR_COUNT_SUM ?
                segmentForQrCount(groupIds, basedAttributeId, range, segments, app) :
                segmentForAttributeValue(groupIds, basedAttributeId, targetAttributeId, range, segments, app, segmentType);

        return finalResults(segments, results);
    }

    private List<NumberRangeSegment> segmentForQrCount(Set<String> groupIds,
                                                       String basedAttributeId,
                                                       StatRange range,
                                                       List<Double> segments,
                                                       App app) {
        Criteria criteria = baseCriteria(groupIds, range);
        String basedField = mongoSortableFieldOf(app.indexedFieldForAttribute(basedAttributeId));
        Aggregation aggregation = newAggregation(
                match(criteria),
                project().and(basedField).as(BASED_FIELD),
                bucket(BASED_FIELD)
                        .withBoundaries(segments.toArray(new Object[0]))
                        .withDefaultBucket(DEFAULT)
                        .andOutput("whatever").count().as(VALUE),
                project(VALUE).and("_id").as(SEGMENT)
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());
        return mongoTemplate.aggregate(aggregation, QR.class, NumberRangeSegment.class)
                .getMappedResults();
    }

    private List<NumberRangeSegment> segmentForAttributeValue(Set<String> groupIds,
                                                              String basedAttributeId,
                                                              String targetAttributeId,
                                                              StatRange range,
                                                              List<Double> segments,
                                                              App app,
                                                              QrSegmentType segmentType) {
        requireNonBlank(targetAttributeId, "Target attribute ID must not be blank.");

        Criteria criteria = baseCriteria(groupIds, range);
        String basedField = mongoSortableFieldOf(app.indexedFieldForAttribute(basedAttributeId));
        String targetField = mongoSortableFieldOf(app.indexedFieldForAttribute(targetAttributeId));
        Aggregation aggregation = newAggregation(
                match(criteria),
                project().and(basedField).as(BASED_FIELD).and(targetField).as(TARGET_FIELD),
                bucketSegmentType(segments, segmentType),
                project(VALUE).and("_id").as(SEGMENT)
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        List<NumberRangeSegment> results = mongoTemplate.aggregate(aggregation, QR.class, NumberRangeSegment.class)
                .getMappedResults();

        Attribute targetAttribute = app.attributeById(targetAttributeId);
        return results.stream().map(segment -> NumberRangeSegment.builder()
                .segment(segment.getSegment())
                .value(targetAttribute.format(segment.getValue()))
                .build()).collect(toImmutableList());
    }

    private BucketOperation bucketSegmentType(List<Double> segments, QrSegmentType segmentType) {
        switch (segmentType) {
            case ATTRIBUTE_VALUE_SUM -> {
                return bucket(BASED_FIELD)
                        .withBoundaries(segments.toArray(new Object[0]))
                        .withDefaultBucket(DEFAULT)
                        .andOutput(TARGET_FIELD).sum().as(VALUE);
            }
            case ATTRIBUTE_VALUE_AVG -> {
                return bucket(BASED_FIELD)
                        .withBoundaries(segments.toArray(new Object[0]))
                        .withDefaultBucket(DEFAULT)
                        .andOutput(TARGET_FIELD).avg().as(VALUE);
            }
            case ATTRIBUTE_VALUE_MAX -> {
                return bucket(BASED_FIELD)
                        .withBoundaries(segments.toArray(new Object[0]))
                        .withDefaultBucket(DEFAULT)
                        .andOutput(TARGET_FIELD).max().as(VALUE);
            }
            case ATTRIBUTE_VALUE_MIN -> {
                return bucket(BASED_FIELD)
                        .withBoundaries(segments.toArray(new Object[0]))
                        .withDefaultBucket(DEFAULT)
                        .andOutput(TARGET_FIELD).min().as(VALUE);
            }
            default ->
                    throw new IllegalStateException("Statistics segment type[" + segmentType.name() + "] not supported.");
        }
    }

    private Criteria baseCriteria(Set<String> groupIds, StatRange range) {
        Criteria criteria = where("groupId").in(groupIds);
        timeRangeOf(range).ifPresent(timeRange -> criteria.and("createdAt").gte(timeRange.getStartAt()).lt(timeRange.getEndAt()));
        return criteria;
    }

    private List<NumberRangeSegment> finalResults(List<Double> segments, List<NumberRangeSegment> dbSegments) {
        Map<Double, Double> dbResultsMap = dbSegments.stream()
                .filter(count -> count.getSegment() != DEFAULT)
                .collect(toImmutableMap(NumberRangeSegment::getSegment, NumberRangeSegment::getValue));

        Map<Double, Double> finalResultsMap = new HashMap<>(initialResults(segments));//将所有分段初始化为0
        finalResultsMap.putAll(dbResultsMap);//合并以获得所有的分段统计，包含count为0的

        return finalResultsMap.entrySet().stream()
                .map(entry -> NumberRangeSegment.builder().segment(entry.getKey()).value(entry.getValue()).build())
                .sorted(comparing(NumberRangeSegment::getSegment, naturalOrder())).collect(toImmutableList());
    }

    private Map<Double, Double> initialResults(List<Double> segments) {
        List<Double> copiedSegments = new ArrayList<>(segments);
        copiedSegments.remove(copiedSegments.size() - 1);
        return copiedSegments.stream().collect(toImmutableMap(aDouble -> aDouble, aDouble -> 0D));
    }
}
