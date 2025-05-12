package com.mryqr.core.app.command;

import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.common.utils.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateAppSettingCommand implements Command {
    @Valid
    @NotNull
    private final AppSetting setting;

    @NotNull
    @Size(max = 50)
    private final String version;

}
