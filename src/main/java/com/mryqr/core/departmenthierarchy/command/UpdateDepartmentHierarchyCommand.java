package com.mryqr.core.departmenthierarchy.command;

import com.mryqr.core.common.domain.idnode.IdTree;
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
public class UpdateDepartmentHierarchyCommand implements Command {

    @Valid
    @NotNull
    private final IdTree idTree;
}
