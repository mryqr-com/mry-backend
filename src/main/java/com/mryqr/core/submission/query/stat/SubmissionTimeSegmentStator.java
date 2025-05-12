package com.mryqr.core.submission.query.stat;

import com.mryqr.common.domain.stat.SubmissionSegmentType;
import com.mryqr.common.domain.stat.SubmissionTimeBasedType;
import com.mryqr.common.domain.stat.TimeSegment;
import com.mryqr.common.domain.stat.TimeSegmentInterval;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.submission.domain.Submission;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.domain.stat.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.common.domain.stat.SubmissionTimeBasedType.CREATED_AT;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static com.mryqr.common.utils.MryConstants.CHINA_TIME_ZONE;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Trunc.truncValueOf;
import static org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToDate.toDate;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Month.monthOf;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Timezone.valueOf;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Year.yearOf;
import static org.springframework.data.mongodb.core.aggregation.Fields.field;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class SubmissionTimeSegmentStator {
    private final MongoTemplate mongoTemplate;

    public List<TimeSegment> statForQr(String qrId,
                                       SubmissionSegmentType segmentType,
                                       SubmissionTimeBasedType basedType,
                                       String pageId,
                                       String basedControlId,
                                       String targetControlId,
                                       TimeSegmentInterval interval,
                                       int max,
                                       App app) {
        requireNonBlank(qrId, "QR ID must not be blank.");
        requireNonNull(segmentType, "Segment type must not be null.");
        requireNonNull(basedType, "Based type must not be null.");
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonNull(interval, "Time segment interval must not be null.");
        requireNonNull(app, "App must not be null.");

        return this.stat(qrId, null, segmentType, basedType, pageId, basedControlId, targetControlId, interval, max, app);
    }

    public List<TimeSegment> statForGroups(Set<String> groupIds,
                                           SubmissionSegmentType segmentType,
                                           SubmissionTimeBasedType basedType,
                                           String pageId,
                                           String basedControlId,
                                           String targetControlId,
                                           TimeSegmentInterval interval,
                                           int max,
                                           App app) {
        requireNonNull(groupIds, "Group IDs must not be null.");
        requireNonNull(segmentType, "Segment type must not be null.");
        requireNonNull(basedType, "Based type must not be null.");
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonNull(interval, "Time segment interval must not be null.");
        requireNonNull(app, "App must not be null.");

        return this.stat(null, groupIds, segmentType, basedType, pageId, basedControlId, targetControlId, interval, max, app);
    }

    private List<TimeSegment> stat(String qrId,
                                   Set<String> groupIds,
                                   SubmissionSegmentType segmentType,
                                   SubmissionTimeBasedType basedType,
                                   String pageId,
                                   String basedControlId,
                                   String targetControlId,
                                   TimeSegmentInterval interval,
                                   int max,
                                   App app) {
        return segmentType == SUBMIT_COUNT_SUM ?
                segmentForSubmitCount(qrId, groupIds, basedType, pageId, basedControlId, app, interval, max) :
                segmentForControlValue(qrId, groupIds, basedType, pageId, basedControlId, targetControlId, app, interval, max, segmentType);
    }

    private List<TimeSegment> segmentForSubmitCount(String qrId,
                                                    Set<String> groupIds,
                                                    SubmissionTimeBasedType basedType,
                                                    String pageId,
                                                    String basedControlId,
                                                    App app,
                                                    TimeSegmentInterval interval,
                                                    int max) {
        Aggregation aggregation = submitCountAggregation(qrId, groupIds, basedType, pageId, basedControlId, app, interval, max);
        return mongoTemplate.aggregate(aggregation, Submission.class, TimeSegment.class).getMappedResults();
    }

    private Aggregation submitCountAggregation(String qrId,
                                               Set<String> groupIds,
                                               SubmissionTimeBasedType basedType,
                                               String pageId,
                                               String basedControlId,
                                               App app,
                                               TimeSegmentInterval interval,
                                               int max) {
        Criteria criteria = baseCriteria(qrId, groupIds, pageId, app);

        switch (interval) {
            case PER_MONTH -> {
                Instant startAt = LocalDate.now()
                        .minusMonths(max - 1)
                        .with(firstDayOfMonth())
                        .atStartOfDay(systemDefault())
                        .toInstant();

                AggregationParameters parameters = buildAggregationParameters(basedType, pageId, basedControlId, app, criteria, startAt);

                return newAggregation(
                        match(parameters.criteria),
                        project().and(parameters.month).as("month")
                                .and(parameters.year).as("year"),
                        group("year", "month").count().as("value"),
                        project("value").and("_id.month").as("period").and("_id.year").as("year"),
                        sort(ASC, "year", "period")
                ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());
            }

            case PER_SEASON -> {
                Instant startAt = LocalDate.now()
                        .minusMonths(max * 3L - 1)
                        .with(firstDayOfMonth())
                        .atStartOfDay(systemDefault())
                        .toInstant();

                AggregationParameters parameters = buildAggregationParameters(basedType, pageId, basedControlId, app, criteria, startAt);

                return newAggregation(
                        match(parameters.criteria),
                        project().and(parameters.month).as("month")
                                .and(parameters.year).as("year"),
                        project("year").and("month").divide(3.1).as("float_season"),
                        project("year").and(truncValueOf("float_season")).as("lower_season"),
                        project("year").and("lower_season").plus(1).as("season"),
                        group("year", "season").count().as("value"),
                        project("value").and("_id.season").as("period").and("_id.year").as("year"),
                        sort(ASC, "year", "period")
                ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());
            }

            case PER_YEAR -> {
                Instant startAt = LocalDate.now()
                        .minusYears(max - 1)
                        .with(firstDayOfYear())
                        .atStartOfDay(systemDefault())
                        .toInstant();

                AggregationParameters parameters = buildAggregationParameters(basedType, pageId, basedControlId, app, criteria, startAt);

                return newAggregation(
                        match(parameters.criteria),
                        project().and(parameters.year).as("year"),
                        group("year").count().as("value"),
                        project("value").and("_id").as("period").and("_id").as("year"),
                        sort(ASC, "year")
                ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());
            }

            default -> {
                throw new IllegalStateException("Time segment interval[" + interval.name() + "] not supported.");
            }
        }
    }

    private List<TimeSegment> segmentForControlValue(String qrId,
                                                     Set<String> groupIds,
                                                     SubmissionTimeBasedType basedType,
                                                     String pageId,
                                                     String basedControlId,
                                                     String targetControlId,
                                                     App app,
                                                     TimeSegmentInterval interval,
                                                     int max,
                                                     SubmissionSegmentType segmentType) {
        requireNonBlank(targetControlId, "Target control ID must not be blank.");

        Aggregation aggregation = controlValueAggregation(qrId,
                groupIds,
                basedType,
                pageId,
                basedControlId,
                targetControlId,
                app,
                interval,
                max,
                segmentType);

        List<TimeSegment> results = mongoTemplate.aggregate(aggregation, Submission.class, TimeSegment.class).getMappedResults();

        Control targetControl = app.controlById(targetControlId);
        return results.stream().map(segment -> TimeSegment.builder()
                        .period(segment.getPeriod())
                        .year(segment.getYear())
                        .value(targetControl.format(segment.getValue()))
                        .build())
                .collect(toImmutableList());
    }

    private Aggregation controlValueAggregation(String qrId,
                                                Set<String> groupIds,
                                                SubmissionTimeBasedType basedType,
                                                String pageId,
                                                String basedControlId,
                                                String valueControlId,
                                                App app,
                                                TimeSegmentInterval interval,
                                                int max,
                                                SubmissionSegmentType segmentType) {
        String sortableField = mongoSortableFieldOf(app.indexedFieldForControl(pageId, valueControlId));
        Criteria criteria = baseCriteria(qrId, groupIds, pageId, app);
        criteria.and(sortableField).ne(null);

        switch (interval) {
            case PER_MONTH -> {
                Instant startAt = LocalDate.now()
                        .minusMonths(max - 1)
                        .with(firstDayOfMonth())
                        .atStartOfDay(systemDefault())
                        .toInstant();

                AggregationParameters parameters = buildAggregationParameters(basedType, pageId, basedControlId, app, criteria, startAt);

                return newAggregation(
                        match(parameters.criteria),
                        project().and(parameters.month).as("month")
                                .and(parameters.year).as("year")
                                .and(sortableField).as("number"),
                        statisticsGroup(group("year", "month"), segmentType),
                        project("value").and("_id.month").as("period").and("_id.year").as("year"),
                        sort(ASC, "year", "period")
                ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());
            }

            case PER_SEASON -> {
                Instant startAt = LocalDate.now()
                        .minusMonths(max * 3L - 1)
                        .with(firstDayOfMonth())
                        .atStartOfDay(systemDefault())
                        .toInstant();

                AggregationParameters parameters = buildAggregationParameters(basedType, pageId, basedControlId, app, criteria, startAt);

                return newAggregation(
                        match(parameters.criteria),
                        project().and(parameters.month).as("month")
                                .and(parameters.year).as("year")
                                .and(sortableField).as("number"),
                        project("year", "number").and("month").divide(3.1).as("float_season"),
                        project("year", "number").and(truncValueOf("float_season")).as("lower_season"),
                        project("year", "number").and("lower_season").plus(1).as("season"),
                        statisticsGroup(group("year", "season"), segmentType),
                        project("value").and("_id.season").as("period").and("_id.year").as("year"),
                        sort(ASC, "year", "period")
                ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());
            }

            case PER_YEAR -> {
                Instant startAt = LocalDate.now()
                        .minusYears(max - 1)
                        .with(firstDayOfYear())
                        .atStartOfDay(systemDefault())
                        .toInstant();

                AggregationParameters parameters = buildAggregationParameters(basedType, pageId, basedControlId, app, criteria, startAt);

                return newAggregation(
                        match(parameters.criteria),
                        project().and(parameters.year).as("year")
                                .and(sortableField).as("number"),
                        statisticsGroup(group("year"), segmentType),
                        project("value").and("_id").as("period").and("_id").as("year"),
                        sort(ASC, "year")
                ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());
            }

            default -> {
                throw new IllegalStateException("Time segment interval not supported [" + interval.name() + "].");
            }
        }
    }

    private AggregationParameters buildAggregationParameters(SubmissionTimeBasedType basedType,
                                                             String pageId,
                                                             String basedControlId,
                                                             App app,
                                                             Criteria criteria,
                                                             Instant startAt) {
        if (basedType == CREATED_AT) {
            criteria.and("createdAt").gte(startAt);
            return new AggregationParameters(criteria,
                    yearOf("createdAt").withTimezone(valueOf(CHINA_TIME_ZONE)),
                    monthOf("createdAt").withTimezone(valueOf(CHINA_TIME_ZONE)));
        } else {
            requireNonBlank(basedControlId, "Based control ID must not be blank.");

            String basedSortableField = mongoSortableFieldOf(app.indexedFieldForControl(pageId, basedControlId));
            ConvertOperators.ToDate basedDate = toDate(field(basedSortableField));
            criteria.and(basedSortableField).gte(startAt.toEpochMilli());
            return new AggregationParameters(criteria,
                    yearOf(basedDate).withTimezone(valueOf(CHINA_TIME_ZONE)),
                    monthOf(basedDate).withTimezone(valueOf(CHINA_TIME_ZONE)));
        }
    }

    private GroupOperation statisticsGroup(GroupOperation group, SubmissionSegmentType segmentType) {
        switch (segmentType) {
            case CONTROL_VALUE_SUM -> {
                return group.sum("number").as("value");
            }
            case CONTROL_VALUE_AVG -> {
                return group.avg("number").as("value");
            }
            case CONTROL_VALUE_MAX -> {
                return group.max("number").as("value");
            }
            case CONTROL_VALUE_MIN -> {
                return group.min("number").as("value");
            }
            default -> {
                throw new IllegalStateException("Statistics segment type not supported [" + segmentType.name() + "].");
            }
        }
    }

    private Criteria baseCriteria(String qrId, Set<String> groupIds, String pageId, App app) {
        Criteria criteria;
        if (isNotBlank(qrId)) {
            criteria = where("qrId").is(qrId);
        } else if (isNotEmpty(groupIds)) {
            criteria = where("groupId").in(groupIds);
        } else {
            criteria = where("appId").is(app.getId());
        }
        return criteria.and("pageId").is(pageId);
    }

    @AllArgsConstructor
    private static class AggregationParameters {
        Criteria criteria;
        DateOperators.Year year;
        DateOperators.Month month;
    }

}
