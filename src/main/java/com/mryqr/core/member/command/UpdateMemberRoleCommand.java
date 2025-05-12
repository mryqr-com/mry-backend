package com.mryqr.core.member.command;

import com.mryqr.common.domain.user.Role;
import com.mryqr.common.utils.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateMemberRoleCommand implements Command {
    @NotNull
    private final Role role;

}
