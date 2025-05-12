package com.mryqr.integration.group.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.id.app.AppId;
import com.mryqr.common.validation.id.custom.CustomId;
import com.mryqr.common.validation.id.group.GroupId;
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
public class IntegrationCreateGroupCommand implements Command {
    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @AppId
    @NotBlank
    private final String appId;

    @GroupId
    private final String parentGroupId;

    @CustomId
    private final String customId;

}
