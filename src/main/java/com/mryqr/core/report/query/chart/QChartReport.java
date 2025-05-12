package com.mryqr.core.report.query.chart;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = QCategorizedOptionSegmentReport.class, name = "CATEGORIZED_OPTION_SEGMENT"),
        @JsonSubTypes.Type(value = QNumberRangeSegmentReport.class, name = "NUMBER_RANGE_SEGMENT"),
        @JsonSubTypes.Type(value = QTimeSegmentReport.class, name = "TIME_SEGMENT"),
})

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public abstract class QChartReport {
    private QChartReportType type;
    private boolean hasData;

    protected QChartReport(QChartReportType type, boolean hasData) {
        this.type = type;
        this.hasData = hasData;
    }
}
