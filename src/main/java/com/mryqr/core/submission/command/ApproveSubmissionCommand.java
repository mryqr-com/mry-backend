package com.mryqr.core.submission.command;

import com.mryqr.common.utils.Command;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ApproveSubmissionCommand implements Command {

    private final boolean passed;

    @Size(max = 1000)
    private final String note;

}
