package com.mryqr.core.member.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.password.Password;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ResetMemberPasswordCommand implements Command {
    @NotNull
    @Password
    private final String password;
}
