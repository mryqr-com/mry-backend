package com.mryqr.core.login.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.mobileoremail.MobileOrEmail;
import com.mryqr.common.validation.password.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MobileOrEmailLoginCommand implements Command {
    @NotBlank
    @MobileOrEmail
    private final String mobileOrEmail;

    @NotBlank
    @Password
    private final String password;

    @Size(max = 1000)
    private final String wxIdInfo;

}
