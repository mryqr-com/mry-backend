package com.mryqr.integration.department.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.id.custom.CustomId;
import com.mryqr.common.validation.id.department.DepartmentId;
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
public class IntegrationCreateDepartmentCommand implements Command {

    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @DepartmentId
    private final String parentDepartmentId;

    @CustomId
    private final String customId;
}
