package com.mryqr.integration.department.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.id.custom.CustomId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IntegrationUpdateDepartmentCustomIdCommand implements Command {
    @CustomId
    private final String customId;
}
