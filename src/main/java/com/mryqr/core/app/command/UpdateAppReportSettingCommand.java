package com.mryqr.core.app.command;

import com.mryqr.core.app.domain.report.ReportSetting;
import com.mryqr.core.common.utils.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateAppReportSettingCommand implements Command {
    @Valid
    @NotNull
    private final ReportSetting setting;

}
