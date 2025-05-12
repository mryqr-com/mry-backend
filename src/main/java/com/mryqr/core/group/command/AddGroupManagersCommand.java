package com.mryqr.core.group.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.collection.NoBlankString;
import com.mryqr.core.common.validation.collection.NoDuplicatedString;
import com.mryqr.core.common.validation.id.member.MemberId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static com.mryqr.core.common.utils.MryConstants.MAX_GROUP_MANAGER_SIZE;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AddGroupManagersCommand implements Command {

    @Valid
    @NotNull
    @NoBlankString
    @NoDuplicatedString
    @Size(max = MAX_GROUP_MANAGER_SIZE)
    private final List<@MemberId String> memberIds;

}
