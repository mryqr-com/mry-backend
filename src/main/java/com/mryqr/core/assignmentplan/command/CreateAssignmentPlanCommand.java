package com.mryqr.core.assignmentplan.command;

import com.mryqr.common.utils.Command;
import com.mryqr.core.assignmentplan.domain.AssignmentSetting;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateAssignmentPlanCommand implements Command {

    @Valid
    @NotNull
    private final AssignmentSetting setting;

    @Override
    public void correctAndValidate() {
        setting.correct();
    }
}
