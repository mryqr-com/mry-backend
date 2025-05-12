package com.mryqr.core.assignmentplan.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.collection.NoBlankString;
import com.mryqr.common.validation.collection.NoDuplicatedString;
import com.mryqr.common.validation.id.group.GroupId;
import com.mryqr.common.validation.id.member.MemberId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class SetGroupOperatorsCommand implements Command {

    @GroupId
    @NotBlank
    private final String groupId;

    @Valid
    @NotNull
    @NoBlankString
    @Size(max = 10)
    @NoDuplicatedString
    private final List<@MemberId String> memberIds;
}
