package com.mryqr.core.group.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.id.app.AppId;
import com.mryqr.core.common.validation.id.group.GroupId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateGroupCommand implements Command {
    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @GroupId
    private final String parentGroupId;

    @AppId
    @NotBlank
    private final String appId;

}
