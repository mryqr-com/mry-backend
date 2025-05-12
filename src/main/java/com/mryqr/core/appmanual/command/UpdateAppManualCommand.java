package com.mryqr.core.appmanual.command;

import com.mryqr.core.common.utils.Command;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.common.utils.MryConstants.MAX_PARAGRAPH_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateAppManualCommand implements Command {

    @Size(max = MAX_PARAGRAPH_LENGTH)
    private final String content;
}
