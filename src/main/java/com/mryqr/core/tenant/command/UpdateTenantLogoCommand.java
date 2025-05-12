package com.mryqr.core.tenant.command;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.utils.Command;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateTenantLogoCommand implements Command {

    @Valid
    private final UploadedFile logo;
}
