package com.mryqr.core.app.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.id.app.AppId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CopyAppCommand implements Command {
    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @AppId
    @NotBlank
    private final String sourceAppId;
}
