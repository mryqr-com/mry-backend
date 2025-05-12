package com.mryqr.core.app.domain.report.chart;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeBarReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeDoughnutReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeNumberRangeSegmentReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributePieReport;
import com.mryqr.core.app.domain.report.chart.attribute.AttributeTimeSegmentReport;
import com.mryqr.core.app.domain.report.chart.control.ControlBarReport;
import com.mryqr.core.app.domain.report.chart.control.ControlDoughnutReport;
import com.mryqr.core.app.domain.report.chart.control.ControlNumberRangeSegmentReport;
import com.mryqr.core.app.domain.report.chart.control.ControlPieReport;
import com.mryqr.core.app.domain.report.chart.control.ControlTimeSegmentReport;
import com.mryqr.core.common.utils.Identified;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import static lombok.AccessLevel.PROTECTED;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = ControlBarReport.class, name = "CONTROL_BAR_REPORT"),
        @JsonSubTypes.Type(value = ControlPieReport.class, name = "CONTROL_PIE_REPORT"),
        @JsonSubTypes.Type(value = ControlDoughnutReport.class, name = "CONTROL_DOUGHNUT_REPORT"),
        @JsonSubTypes.Type(value = ControlTimeSegmentReport.class, name = "CONTROL_TIME_SEGMENT_REPORT"),
        @JsonSubTypes.Type(value = ControlNumberRangeSegmentReport.class, name = "CONTROL_NUMBER_RANGE_REPORT"),
        @JsonSubTypes.Type(value = AttributeBarReport.class, name = "ATTRIBUTE_BAR_REPORT"),
        @JsonSubTypes.Type(value = AttributeDoughnutReport.class, name = "ATTRIBUTE_DOUGHNUT_REPORT"),
        @JsonSubTypes.Type(value = AttributePieReport.class, name = "ATTRIBUTE_PIE_REPORT"),
        @JsonSubTypes.Type(value = AttributeTimeSegmentReport.class, name = "ATTRIBUTE_TIME_SEGMENT_REPORT"),
        @JsonSubTypes.Type(value = AttributeNumberRangeSegmentReport.class, name = "ATTRIBUTE_NUMBER_RANGE_SEGMENT_REPORT"),
})

@Getter
@Document
@SuperBuilder
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public abstract class ChartReport implements Identified {

    @NotBlank
    @ShortUuid
    private String id;

    @NotNull
    private ChartReportType type;

    @NotBlank
    @Size(max = 20)
    private String name;

    @Min(6)
    @Max(24)
    private int span;

    @Min(30)
    @Max(100)
    private int aspectRatio;

    @Override
    public String getIdentifier() {
        return id;
    }

    public abstract void correct();

    public abstract void validate(AppSettingContext context);
}
