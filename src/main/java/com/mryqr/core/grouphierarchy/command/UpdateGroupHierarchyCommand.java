package com.mryqr.core.grouphierarchy.command;

import com.mryqr.common.domain.idnode.IdTree;
import com.mryqr.common.utils.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateGroupHierarchyCommand implements Command {

    @Valid
    @NotNull
    private final IdTree idTree;
}
