package com.mryqr.integration.group.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.collection.NoBlankString;
import com.mryqr.common.validation.collection.NoDuplicatedString;
import com.mryqr.common.validation.id.custom.CustomId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IntegrationCustomRemoveGroupMembersCommand implements Command {

    @Valid
    @NotNull
    @NoBlankString
    @NoDuplicatedString
    @Size(max = 1000)
    private final List<@CustomId String> memberCustomIds;

}
