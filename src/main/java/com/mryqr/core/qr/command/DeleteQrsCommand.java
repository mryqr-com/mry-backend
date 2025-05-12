package com.mryqr.core.qr.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.collection.NoBlankString;
import com.mryqr.core.common.validation.collection.NoDuplicatedString;
import com.mryqr.core.common.validation.id.qr.QrId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class DeleteQrsCommand implements Command {
    @NotNull
    @NoBlankString
    @Size(max = 100)
    @NoDuplicatedString
    private final Set<@QrId String> qrIds;

}
