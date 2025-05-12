package com.mryqr.core.app.domain.report.number;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.common.domain.report.ReportRange;
import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.report.number.attribute.AttributeNumberReport;
import com.mryqr.core.app.domain.report.number.control.ControlNumberReport;
import com.mryqr.core.app.domain.report.number.instance.InstanceNumberReport;
import com.mryqr.core.app.domain.report.number.page.PageNumberReport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static lombok.AccessLevel.PROTECTED;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = AttributeNumberReport.class, name = "ATTRIBUTE_NUMBER_REPORT"),
        @JsonSubTypes.Type(value = InstanceNumberReport.class, name = "INSTANCE_NUMBER_REPORT"),
        @JsonSubTypes.Type(value = PageNumberReport.class, name = "PAGE_NUMBER_REPORT"),
        @JsonSubTypes.Type(value = ControlNumberReport.class, name = "CONTROL_NUMBER_REPORT"),
})

@Getter
@SuperBuilder
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public abstract class NumberReport implements Identified {

    @NotBlank
    @ShortUuid
    private String id;

    @NotNull
    private NumberReportType type;

    @NotBlank
    @Size(max = 20)
    private String name;

    @Size(max = 10)
    private String suffix;

    @NotNull
    private ReportRange range;

    @Override
    public String getIdentifier() {
        return id;
    }

    public abstract void validate(AppSettingContext context);
}
