package com.mryqr.core.qr.query.report;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.common.domain.report.QrReportTimeBasedType;
import com.mryqr.core.common.domain.report.QrSegmentType;
import com.mryqr.core.common.domain.report.TimeSegment;
import com.mryqr.core.common.domain.report.TimeSegmentInterval;
import com.mryqr.core.qr.domain.QR;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.common.domain.report.QrReportTimeBasedType.CREATED_AT;
import static com.mryqr.core.common.domain.report.QrSegmentType.QR_COUNT_SUM;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static com.mryqr.core.common.utils.MryConstants.CHINA_TIME_ZONE;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Trunc.truncValueOf;
import static org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToDate.toDate;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Month.monthOf;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Timezone.valueOf;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Year.yearOf;
import static org.springframework.data.mongodb.core.aggregation.Fields.field;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class QrTimeSegmentReporter {
    private final MongoTemplate mongoTemplate;

    public List<TimeSegment> report(Set<String> groupIds,
                                    QrSegmentType segmentType,
                                    QrReportTimeBasedType basedType,
                                    String basedAttributeId,
                                    String targetAttributeId,
                                    TimeSegmentInterval interval,
                                    int max,
                                    App app) {
        requireNonNull(groupIds, "Group IDs must not be null.");
        requireNonNull(segmentType, "Segment type must not be null.");
        requireNonNull(basedType, "Based type must not be null.");
        requireNonNull(interval, "Time segment interval must not be null.");
        requireNonNull(app, "App must not be null.");

        return segmentType == QR_COUNT_SUM ?
                segmentForQrCount(groupIds, basedType, basedAttributeId, app, interval, max) :
                segmentForAttributeValue(groupIds, basedType, basedAttributeId, targetAttributeId, app, interval, max, segmentType);
    }

    private List<TimeSegment> segmentForQrCount(Set<String> groupIds,
                                                QrReportTimeBasedType basedType,
                                                String basedAttributeId,
                                                App app,
                                                TimeSegmentInterval interval,
                                                int max) {
        Aggregation aggregation = qrCountAggregation(groupIds, basedType, basedAttributeId, app, interval, max);
        return mongoTemplate.aggregate(aggregation, QR.class, TimeSegment.class).getMappedResults();
    }

    private Aggregation qrCountAggregation(Set<String> groupIds,
                                           QrReportTimeBasedType basedType,
                                           String basedAttributeId,
                                           App app,
                                           TimeSegmentInterval interval,
                                           int max) {
        Criteria criteria = baseCriteria(groupIds);

        switch (interval) {
            case PER_MONTH -> {
                Instant startAt = LocalDate.now()
                        .minusMonths(max - 1)
                        .with(firstDayOfMonth())
                        .atStartOfDay(systemDefault())
                        .toInstant();

                AggregationParameters parameters = buildAggregationParameters(basedType, basedAttributeId, app, criteria, startAt);

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

                AggregationParameters parameters = buildAggregationParameters(basedType, basedAttributeId, app, criteria, startAt);

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

                AggregationParameters parameters = buildAggregationParameters(basedType, basedAttributeId, app, criteria, startAt);

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

    private List<TimeSegment> segmentForAttributeValue(Set<String> groupIds,
                                                       QrReportTimeBasedType basedType,
                                                       String basedAttributeId,
                                                       String targetAttributeId,
                                                       App app,
                                                       TimeSegmentInterval interval,
                                                       int max,
                                                       QrSegmentType segmentType) {
        requireNonBlank(targetAttributeId, "Target attribute ID must not be blank.");

        Aggregation aggregation = attributeValueAggregation(groupIds,
                basedType,
                basedAttributeId,
                targetAttributeId,
                app,
                interval,
                max,
                segmentType);

        List<TimeSegment> results = mongoTemplate.aggregate(aggregation, QR.class, TimeSegment.class).getMappedResults();

        Attribute targetAttribute = app.attributeById(targetAttributeId);
        return results.stream().map(segment -> TimeSegment.builder()
                .period(segment.getPeriod())
                .year(segment.getYear())
                .value(targetAttribute.format(segment.getValue()))
                .build()).collect(toImmutableList());

    }

    private Aggregation attributeValueAggregation(Set<String> groupIds,
                                                  QrReportTimeBasedType basedType,
                                                  String basedAttributeId,
                                                  String targetAttributeId,
                                                  App app,
                                                  TimeSegmentInterval interval,
                                                  int max,
                                                  QrSegmentType segmentType) {
        String sortableField = mongoSortableFieldOf(app.indexedFieldForAttribute(targetAttributeId));
        Criteria criteria = baseCriteria(groupIds);
        criteria.and(sortableField).ne(null);

        switch (interval) {
            case PER_MONTH -> {
                Instant startAt = LocalDate.now()
                        .minusMonths(max - 1)
                        .with(firstDayOfMonth())
                        .atStartOfDay(systemDefault())
                        .toInstant();

                AggregationParameters parameters = buildAggregationParameters(basedType, basedAttributeId, app, criteria, startAt);

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

                AggregationParameters parameters = buildAggregationParameters(basedType, basedAttributeId, app, criteria, startAt);

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

                AggregationParameters parameters = buildAggregationParameters(basedType, basedAttributeId, app, criteria, startAt);

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

    private GroupOperation statisticsGroup(GroupOperation group, QrSegmentType segmentType) {
        switch (segmentType) {
            case ATTRIBUTE_VALUE_SUM -> {
                return group.sum("number").as("value");
            }
            case ATTRIBUTE_VALUE_AVG -> {
                return group.avg("number").as("value");
            }
            case ATTRIBUTE_VALUE_MAX -> {
                return group.max("number").as("value");
            }
            case ATTRIBUTE_VALUE_MIN -> {
                return group.min("number").as("value");
            }
            default -> {
                throw new IllegalStateException("Statistics segment type not supported [" + segmentType.name() + "].");
            }
        }
    }

    private Criteria baseCriteria(Set<String> groupIds) {
        return where("groupId").in(groupIds);
    }

    private AggregationParameters buildAggregationParameters(QrReportTimeBasedType basedType,
                                                             String basedAttributeId,
                                                             App app,
                                                             Criteria criteria,
                                                             Instant startAt) {
        if (basedType == CREATED_AT) {
            criteria.and("createdAt").gte(startAt);
            return new AggregationParameters(criteria,
                    yearOf("createdAt").withTimezone(valueOf(CHINA_TIME_ZONE)),
                    monthOf("createdAt").withTimezone(valueOf(CHINA_TIME_ZONE)));
        } else {
            requireNonBlank(basedAttributeId, "Based attribute ID must not be blank.");

            String basedSortableField = mongoSortableFieldOf(app.indexedFieldForAttribute(basedAttributeId));
            ConvertOperators.ToDate basedDate = toDate(field(basedSortableField));
            criteria.and(basedSortableField).gte(startAt.toEpochMilli());
            return new AggregationParameters(criteria,
                    yearOf(basedDate).withTimezone(valueOf(CHINA_TIME_ZONE)),
                    monthOf(basedDate).withTimezone(valueOf(CHINA_TIME_ZONE)));
        }
    }

    @AllArgsConstructor
    private static class AggregationParameters {
        Criteria criteria;
        DateOperators.Year year;
        DateOperators.Month month;
    }

}
