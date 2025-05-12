package com.mryqr.core.member.command;

import com.mryqr.core.common.domain.UploadedFile;
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
public class UpdateMyAvatarCommand implements Command {

    @Valid
    @NotNull
    private final UploadedFile avatar;
}
