package com.mryqr.core.assignment.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.collection.NoBlankString;
import com.mryqr.common.validation.collection.NoDuplicatedString;
import com.mryqr.common.validation.id.member.MemberId;
import jakarta.validation.Valid;
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
public class SetAssignmentOperatorsCommand implements Command {

    @Valid
    @NotNull
    @NoBlankString
    @Size(max = 10)
    @NoDuplicatedString
    private final List<@MemberId String> memberIds;
}
