package com.mryqr.core.qr.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ResetQrCirculationStatusCommand implements Command {

    @NotBlank
    @ShortUuid
    private final String circulationOptionId;

}
