package com.mryqr.core.app.domain.report.number.instance;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.report.number.NumberReport;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("INSTANCE_NUMBER_REPORT")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class InstanceNumberReport extends NumberReport {

    @NotNull
    private InstanceNumberReportType instanceNumberReportType;

    @Override
    public void validate(AppSettingContext context) {

    }
}
