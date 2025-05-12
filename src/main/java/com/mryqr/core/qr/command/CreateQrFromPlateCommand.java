package com.mryqr.core.qr.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.id.group.GroupId;
import com.mryqr.common.validation.id.plate.PlateId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateQrFromPlateCommand implements Command {
    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @GroupId
    @NotBlank
    private final String groupId;

    @PlateId
    @NotBlank
    private final String plateId;
}
