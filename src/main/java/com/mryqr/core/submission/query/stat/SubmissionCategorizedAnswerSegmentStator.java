package com.mryqr.core.submission.query.stat;

import com.mryqr.common.domain.stat.CategorizedOptionSegment;
import com.mryqr.common.domain.stat.StatRange;
import com.mryqr.common.domain.stat.SubmissionSegmentType;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.domain.stat.StatRange.timeRangeOf;
import static com.mryqr.common.domain.stat.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoTextFieldOf;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class SubmissionCategorizedAnswerSegmentStator {
    private final MongoTemplate mongoTemplate;

    public List<List<CategorizedOptionSegment>> statForQrMultiple(String qrId,
                                                                  SubmissionSegmentType segmentType,
                                                                  String pageId,
                                                                  String basedControlId,
                                                                  List<String> targetControlIds,
                                                                  StatRange range,
                                                                  App app) {
        if (segmentType == SUBMIT_COUNT_SUM) {
            return List.of(statForQr(qrId, segmentType, pageId, basedControlId, null, range, app));
        }

        return targetControlIds.stream()
                .map(targetControlId -> statForQr(qrId, segmentType, pageId, basedControlId, targetControlId, range, app))
                .collect(toImmutableList());
    }

    public List<CategorizedOptionSegment> statForQr(String qrId,
                                                    SubmissionSegmentType segmentType,
                                                    String pageId,
                                                    String basedControlId,
                                                    String targetControlId,
                                                    StatRange range,
                                                    App app) {
        requireNonBlank(qrId, "QR ID must not be blank.");
        requireNonNull(segmentType, "Segment type must not be null.");
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonBlank(basedControlId, "Based control ID must not be blank.");
        requireNonNull(range, "Statistic range must not be null.");
        requireNonNull(app, "App must not be null.");
        return this.stat(qrId, null, segmentType, pageId, basedControlId, targetControlId, range, app);
    }

    public List<List<CategorizedOptionSegment>> statForGroupsMultiple(Set<String> groupIds,
                                                                      SubmissionSegmentType segmentType,
                                                                      String pageId,
                                                                      String basedControlId,
                                                                      List<String> targetControlIds,
                                                                      StatRange range,
                                                                      App app) {
        if (segmentType == SUBMIT_COUNT_SUM) {
            return List.of(statForGroups(groupIds, segmentType, pageId, basedControlId, null, range, app));
        }

        return targetControlIds.stream()
                .map(targetControlId -> statForGroups(groupIds, segmentType, pageId, basedControlId, targetControlId, range, app))
                .collect(toImmutableList());
    }

    public List<CategorizedOptionSegment> statForGroups(Set<String> groupIds,
                                                        SubmissionSegmentType segmentType,
                                                        String pageId,
                                                        String basedControlId,
                                                        String targetControlId,
                                                        StatRange range,
                                                        App app) {
        requireNonNull(groupIds, "Group IDs must not be blank.");
        requireNonNull(segmentType, "Segment type must not be null.");
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonBlank(basedControlId, "Based control ID must not be blank.");
        requireNonNull(range, "Statistic range must not be null.");
        requireNonNull(app, "App must not be null.");
        return this.stat(null, groupIds, segmentType, pageId, basedControlId, targetControlId, range, app);
    }

    private List<CategorizedOptionSegment> stat(String qrId,
                                                Set<String> groupIds,
                                                SubmissionSegmentType segmentType,
                                                String pageId,
                                                String basedControlId,
                                                String targetControlId,
                                                StatRange range,
                                                App app) {
        return segmentType == SUBMIT_COUNT_SUM ?
                segmentForSubmitCount(qrId, groupIds, pageId, basedControlId, range, app) :
                segmentForControlValue(qrId, groupIds, pageId, basedControlId, targetControlId, range, app, segmentType);
    }

    private List<CategorizedOptionSegment> segmentForSubmitCount(String qrId,
                                                                 Set<String> groupIds,
                                                                 String pageId,
                                                                 String controlId,
                                                                 StatRange range,
                                                                 App app) {
        Criteria criteria = baseCriteria(qrId, groupIds, pageId, range, app);
        String optionField = mongoTextFieldOf(app.indexedFieldForControl(pageId, controlId));
        Aggregation aggregation = newAggregation(
                match(criteria),
                unwind(optionField),
                project().and(optionField).as("option"),
                group("option").count().as("value"),
                project("value").and("_id").as("option")
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        return mongoTemplate.aggregate(aggregation, Submission.class, CategorizedOptionSegment.class)
                .getMappedResults();
    }

    private List<CategorizedOptionSegment> segmentForControlValue(String qrId,
                                                                  Set<String> groupIds,
                                                                  String pageId,
                                                                  String basedControlId,
                                                                  String targetControlId,
                                                                  StatRange range,
                                                                  App app,
                                                                  SubmissionSegmentType segmentType) {
        requireNonBlank(targetControlId, "Target control ID must not be blank.");
        Criteria criteria = baseCriteria(qrId, groupIds, pageId, range, app);
        String optionFiled = mongoTextFieldOf(app.indexedFieldForControl(pageId, basedControlId));
        String sortableField = mongoSortableFieldOf(app.indexedFieldForControl(pageId, targetControlId));
        criteria.and(sortableField).ne(null);

        Aggregation aggregation = newAggregation(
                match(criteria),
                unwind(optionFiled),
                project().and(optionFiled).as("option").and(sortableField).as("number"),
                statisticsGroup(segmentType),
                project("value").and("_id").as("option")
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        List<CategorizedOptionSegment> results = mongoTemplate.aggregate(aggregation, Submission.class, CategorizedOptionSegment.class)
                .getMappedResults();

        Control targetControl = app.controlById(targetControlId);
        return results.stream().map(segment -> CategorizedOptionSegment.builder()
                .option(segment.getOption())
                .value(targetControl.format(segment.getValue()))
                .build()).collect(toImmutableList());
    }

    private GroupOperation statisticsGroup(SubmissionSegmentType segmentType) {
        switch (segmentType) {
            case CONTROL_VALUE_SUM -> {
                return group("option").sum("number").as("value");
            }
            case CONTROL_VALUE_AVG -> {
                return group("option").avg("number").as("value");
            }
            case CONTROL_VALUE_MAX -> {
                return group("option").max("number").as("value");
            }
            case CONTROL_VALUE_MIN -> {
                return group("option").min("number").as("value");
            }
            default -> {
                throw new IllegalStateException("Statistics segment type[" + segmentType.name() + "] not supported.");
            }
        }
    }

    private Criteria baseCriteria(String qrId, Set<String> groupIds, String pageId, StatRange range, App app) {
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

}
