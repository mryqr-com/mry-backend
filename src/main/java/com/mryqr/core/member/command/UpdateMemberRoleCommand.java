package com.mryqr.core.member.command;

import com.mryqr.core.common.domain.user.Role;
import com.mryqr.core.common.utils.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.common.domain.user.Role.ROBOT;
import static com.mryqr.core.common.exception.MryException.requestValidationException;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateMemberRoleCommand implements Command {
    @NotNull
    private final Role role;

    @Override
    public void correctAndValidate() {
        if (role == ROBOT) {
            throw requestValidationException("Role value not allowed.");
        }
    }
}
