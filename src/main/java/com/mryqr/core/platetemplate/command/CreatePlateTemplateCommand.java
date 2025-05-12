package com.mryqr.core.platetemplate.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.id.app.AppId;
import com.mryqr.core.app.domain.plate.PlateSetting;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreatePlateTemplateCommand implements Command {

    @AppId
    @NotNull
    private final String appId;

    @Valid
    @NotNull
    private final PlateSetting plateSetting;
}
