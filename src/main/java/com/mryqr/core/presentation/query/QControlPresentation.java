package com.mryqr.core.presentation.query;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.presentation.query.answerreference.QAnswerReferencePresentation;
import com.mryqr.core.presentation.query.attributedashboard.QAttributeDashboardPresentation;
import com.mryqr.core.presentation.query.attributetable.QAttributeTablePresentation;
import com.mryqr.core.presentation.query.bar.QBarPresentation;
import com.mryqr.core.presentation.query.doughnut.QDoughnutPresentation;
import com.mryqr.core.presentation.query.instancelist.QInstanceListPresentation;
import com.mryqr.core.presentation.query.numberrangesegment.QNumberRangeSegmentPresentation;
import com.mryqr.core.presentation.query.pie.QPiePresentation;
import com.mryqr.core.presentation.query.submissionreference.QSubmissionReferencePresentation;
import com.mryqr.core.presentation.query.submithistory.QSubmitHistoryPresentation;
import com.mryqr.core.presentation.query.timesegment.QTimeSegmentPresentation;
import com.mryqr.core.presentation.query.trend.QTrendPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "controlType",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = QAnswerReferencePresentation.class, name = "ANSWER_REFERENCE"),
        @JsonSubTypes.Type(value = QAttributeDashboardPresentation.class, name = "ATTRIBUTE_DASHBOARD"),
        @JsonSubTypes.Type(value = QAttributeTablePresentation.class, name = "ATTRIBUTE_TABLE"),
        @JsonSubTypes.Type(value = QBarPresentation.class, name = "BAR"),
        @JsonSubTypes.Type(value = QDoughnutPresentation.class, name = "DOUGHNUT"),
        @JsonSubTypes.Type(value = QPiePresentation.class, name = "PIE"),
        @JsonSubTypes.Type(value = QSubmissionReferencePresentation.class, name = "SUBMISSION_REFERENCE"),
        @JsonSubTypes.Type(value = QSubmitHistoryPresentation.class, name = "SUBMIT_HISTORY"),
        @JsonSubTypes.Type(value = QInstanceListPresentation.class, name = "INSTANCE_LIST"),
        @JsonSubTypes.Type(value = QTimeSegmentPresentation.class, name = "TIME_SEGMENT"),
        @JsonSubTypes.Type(value = QTrendPresentation.class, name = "TREND"),
        @JsonSubTypes.Type(value = QNumberRangeSegmentPresentation.class, name = "NUMBER_RANGE_SEGMENT"),
})

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public abstract class QControlPresentation {
    private ControlType controlType;

    protected QControlPresentation(ControlType controlType) {
        this.controlType = controlType;
    }
}
