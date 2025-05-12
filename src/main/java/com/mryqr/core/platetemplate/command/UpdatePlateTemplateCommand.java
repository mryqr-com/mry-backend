package com.mryqr.core.platetemplate.command;

import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.utils.Command;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdatePlateTemplateCommand implements Command {

    @Valid
    private final UploadedFile image;

    private final int order;
}
