package com.mryqr.core.presentation.query.trend;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PTrendControl;
import com.mryqr.core.app.domain.page.control.TrendItem;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.domain.stat.StatRange.timeRangeOf;
import static com.mryqr.common.domain.stat.SubmissionTimeBasedType.CREATED_AT;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static com.mryqr.common.utils.MryConstants.CHINA_TIME_ZONE;
import static com.mryqr.core.app.domain.page.control.ControlType.TREND;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToDate.toDate;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Timezone.valueOf;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.dateOf;
import static org.springframework.data.mongodb.core.aggregation.Fields.field;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class TrendPresentationer implements ControlPresentationer {
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == TREND;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PTrendControl theControl = (PTrendControl) control;
        List<TrendItem> completeItems = theControl.getTrendItems().stream().filter(TrendItem::isComplete).collect(toImmutableList());

        List<QTrendDataSet> dataSets = completeItems.stream().map(trendItem -> {
            Aggregation aggregation = trendItem.getBasedType() == CREATED_AT ?
                    aggregationByCreatedAt(qr, app, theControl, trendItem) :
                    aggregationByDateControl(qr, app, theControl, trendItem);

            List<QTrendRecord> results = mongoTemplate.aggregate(aggregation, Submission.class, QTrendRecord.class).getMappedResults()
                    .stream().filter(record -> isNotBlank(record.getDate()) && record.getNumber() != null)
                    .collect(toImmutableList());

            if (isEmpty(results)) {
                return null;
            }

            return QTrendDataSet.builder().label(trendItem.getName()).records(results).build();
        }).filter(Objects::nonNull).collect(toImmutableList());

        return new QTrendPresentation(dataSets);
    }

    private Aggregation aggregationByCreatedAt(QR qr, App app, PTrendControl theControl, TrendItem trendItem) {
        String targetField = mongoSortableFieldOf(app.indexedFieldForControl(trendItem.getPageId(), trendItem.getTargetControlId()));
        Criteria criteria = where("qrId").is(qr.getId()).and("pageId").is(trendItem.getPageId()).and(targetField).ne(null);

        timeRangeOf(theControl.getRange())
                .ifPresent(timeRange -> criteria.and("createdAt")
                        .gte(timeRange.getStartAt())
                        .lt(timeRange.getEndAt()));

        return newAggregation(
                match(criteria),
                project("createdAt")
                        .and(targetField).as("number")
                        .and(dateOf("createdAt").withTimezone(valueOf(CHINA_TIME_ZONE)).toString("%Y-%m-%d")).as("date"),
                sort(DESC, "createdAt"),
                group("date").first(ROOT).as("first"),//取一天中最后一条数据
                replaceRoot("first"),
                limit(theControl.getMaxPoints()),
                sort(ASC, "createdAt"),
                project("number", "date")
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());
    }


    private Aggregation aggregationByDateControl(QR qr, App app, PTrendControl theControl, TrendItem trendItem) {
        String basedField = mongoSortableFieldOf(app.indexedFieldForControl(trendItem.getPageId(), trendItem.getBasedControlId()));
        String targetField = mongoSortableFieldOf(app.indexedFieldForControl(trendItem.getPageId(), trendItem.getTargetControlId()));
        Criteria criteria = where("qrId").is(qr.getId()).and("pageId").is(trendItem.getPageId()).and(targetField).ne(null);

        timeRangeOf(theControl.getRange())
                .ifPresent(timeRange -> criteria.and(basedField)
                        .gte(timeRange.getStartAt().toEpochMilli())
                        .lt(timeRange.getEndAt().toEpochMilli()));

        return newAggregation(
                match(criteria),
                project("createdAt").and(basedField).as("basedTime")
                        .and(targetField).as("number")
                        .and(dateOf(toDate(field(basedField))).withTimezone(valueOf(CHINA_TIME_ZONE)).toString("%Y-%m-%d")).as("date"),
                sort(DESC, "createdAt"),
                group("date").first(ROOT).as("first"),//取一天中最后一条数据
                replaceRoot("first"),
                limit(theControl.getMaxPoints()),
                sort(ASC, "basedTime"),
                project("number", "date")
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());
    }
}
