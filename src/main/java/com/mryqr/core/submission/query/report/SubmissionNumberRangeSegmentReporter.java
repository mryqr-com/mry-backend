package com.mryqr.core.submission.query.report;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.common.domain.report.NumberRangeSegment;
import com.mryqr.core.common.domain.report.ReportRange;
import com.mryqr.core.common.domain.report.SubmissionSegmentType;
import com.mryqr.core.submission.domain.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.BucketOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.core.common.domain.report.ReportRange.timeRangeOf;
import static com.mryqr.core.common.domain.report.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.bucket;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class SubmissionNumberRangeSegmentReporter {
    private static final int DEFAULT = Integer.MIN_VALUE;
    private static final String VALUE = "value";
    private static final String SEGMENT = "segment";
    private static final String BASED_FIELD = "basedField";
    private static final String TARGET_FIELD = "targetField";
    private final MongoTemplate mongoTemplate;

    public List<NumberRangeSegment> reportForQr(String qrId,
                                                SubmissionSegmentType segmentType,
                                                String pageId,
                                                String basedControlId,
                                                String targetControlId,
                                                ReportRange range,
                                                List<Double> segments,
                                                App app) {
        requireNonBlank(qrId, "QR ID must not be blank.");
        requireNonNull(segmentType, "Segment type must not be null.");
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonBlank(basedControlId, "Based control ID must not be blank.");
        requireNonNull(range, "Statistic range must not be null.");
        requireNonNull(segments, "Segments must not be null.");
        requireNonNull(app, "App must not be null.");
        return this.report(qrId, null, segmentType, pageId, basedControlId, targetControlId, range, segments, app);
    }

    public List<NumberRangeSegment> reportForGroups(Set<String> groupIds,
                                                    SubmissionSegmentType segmentType,
                                                    String pageId,
                                                    String basedControlId,
                                                    String targetControlId,
                                                    ReportRange range,
                                                    List<Double> segments,
                                                    App app) {
        requireNonNull(groupIds, "Group IDs must not be null.");
        requireNonNull(segmentType, "Segment type must not be null.");
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonBlank(basedControlId, "Based control ID must not be blank.");
        requireNonNull(range, "Statistic range must not be null.");
        requireNonNull(segments, "Segments must not be null.");
        requireNonNull(app, "App must not be null.");
        return this.report(null, groupIds, segmentType, pageId, basedControlId, targetControlId, range, segments, app);
    }

    private List<NumberRangeSegment> report(String qrId,
                                            Set<String> groupIds,
                                            SubmissionSegmentType segmentType,
                                            String pageId,
                                            String basedControlId,
                                            String targetControlId,
                                            ReportRange range,
                                            List<Double> segments,
                                            App app) {
        if (segments.size() <= 1) {
            return List.of();
        }

        List<NumberRangeSegment> results = segmentType == SUBMIT_COUNT_SUM ?
                segmentForSubmitCount(qrId, groupIds, pageId, basedControlId, range, segments, app) :
                segmentForControlValue(qrId, groupIds, pageId, basedControlId, targetControlId, range, segments, app, segmentType);

        return finalResults(segments, results);
    }

    private Criteria baseCriteria(String qrId, Set<String> groupIds, String pageId, ReportRange range, App app) {
        Criteria criteria;
        if (isNotBlank(qrId)) {
            criteria = where("qrId").is(qrId);
        } else if (isNotEmpty(groupIds)) {
            criteria = where("groupId").in(groupIds);
        } else {
            criteria = where("appId").is(app.getId());
        }

        criteria.and("pageId").is(pageId);
        timeRangeOf(range).ifPresent(timeRange -> criteria.and("createdAt").gte(timeRange.getStartAt()).lt(timeRange.getEndAt()));
        return criteria;
    }

    private List<NumberRangeSegment> segmentForSubmitCount(String qrId,
                                                           Set<String> groupIds,
                                                           String pageId,
                                                           String basedControlId,
                                                           ReportRange range,
                                                           List<Double> segments,
                                                           App app) {
        Criteria criteria = baseCriteria(qrId, groupIds, pageId, range, app);
        String basedField = mongoSortableFieldOf(app.indexedFieldForControl(pageId, basedControlId));
        Aggregation aggregation = newAggregation(
                match(criteria),
                project().and(basedField).as(BASED_FIELD),
                bucket(BASED_FIELD)
                        .withBoundaries(segments.toArray(new Object[0]))
                        .withDefaultBucket(DEFAULT)
                        .andOutput("whatever").count().as(VALUE),
                project(VALUE).and("_id").as(SEGMENT)
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());
        return mongoTemplate.aggregate(aggregation, Submission.class, NumberRangeSegment.class)
                .getMappedResults();
    }

    private List<NumberRangeSegment> segmentForControlValue(String qrId,
                                                            Set<String> groupIds,
                                                            String pageId,
                                                            String basedControlId,
                                                            String targetControlId,
                                                            ReportRange range,
                                                            List<Double> segments,
                                                            App app,
                                                            SubmissionSegmentType segmentType) {
        requireNonBlank(targetControlId, "Target control ID must not be blank.");
        Criteria criteria = baseCriteria(qrId, groupIds, pageId, range, app);
        String basedField = mongoSortableFieldOf(app.indexedFieldForControl(pageId, basedControlId));
        String targetField = mongoSortableFieldOf(app.indexedFieldForControl(pageId, targetControlId));
        Aggregation aggregation = newAggregation(
                match(criteria),
                project().and(basedField).as(BASED_FIELD).and(targetField).as(TARGET_FIELD),
                bucketSegmentType(segments, segmentType),
                project(VALUE).and("_id").as(SEGMENT)
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        List<NumberRangeSegment> results = mongoTemplate.aggregate(aggregation, Submission.class, NumberRangeSegment.class)
                .getMappedResults();

        Control targetControl = app.controlById(targetControlId);
        return results.stream().map(segment -> NumberRangeSegment.builder()
                .segment(segment.getSegment())
                .value(targetControl.format(segment.getValue()))
                .build()).collect(toImmutableList());
    }

    private BucketOperation bucketSegmentType(List<Double> segments, SubmissionSegmentType segmentType) {
        switch (segmentType) {
            case CONTROL_VALUE_SUM -> {
                return bucket(BASED_FIELD)
                        .withBoundaries(segments.toArray(new Object[0]))
                        .withDefaultBucket(DEFAULT)
                        .andOutput(TARGET_FIELD).sum().as(VALUE);
            }
            case CONTROL_VALUE_AVG -> {
                return bucket(BASED_FIELD)
                        .withBoundaries(segments.toArray(new Object[0]))
                        .withDefaultBucket(DEFAULT)
                        .andOutput(TARGET_FIELD).avg().as(VALUE);
            }
            case CONTROL_VALUE_MAX -> {
                return bucket(BASED_FIELD)
                        .withBoundaries(segments.toArray(new Object[0]))
                        .withDefaultBucket(DEFAULT)
                        .andOutput(TARGET_FIELD).max().as(VALUE);
            }
            case CONTROL_VALUE_MIN -> {
                return bucket(BASED_FIELD)
                        .withBoundaries(segments.toArray(new Object[0]))
                        .withDefaultBucket(DEFAULT)
                        .andOutput(TARGET_FIELD).min().as(VALUE);
            }
            default -> throw new IllegalStateException("Statistics segment type[" + segmentType.name() + "] not supported.");
        }
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
