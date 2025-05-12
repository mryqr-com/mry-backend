package com.mryqr.core.qr.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.id.plate.PlateId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ResetQrPlateCommand implements Command {
    @PlateId
    @NotBlank
    private final String plateId;

}
