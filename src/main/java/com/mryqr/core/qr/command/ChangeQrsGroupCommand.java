package com.mryqr.core.qr.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.collection.NoBlankString;
import com.mryqr.common.validation.collection.NoDuplicatedString;
import com.mryqr.common.validation.id.group.GroupId;
import com.mryqr.common.validation.id.qr.QrId;
import jakarta.validation.constraints.NotBlank;
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
public class ChangeQrsGroupCommand implements Command {
    @NotNull
    @NoBlankString
    @Size(max = 100)
    @NoDuplicatedString
    private final Set<@QrId String> qrIds;

    @GroupId
    @NotBlank
    private final String groupId;

}
