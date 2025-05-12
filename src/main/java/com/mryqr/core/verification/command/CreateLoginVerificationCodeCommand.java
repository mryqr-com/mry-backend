package com.mryqr.core.verification.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.mobileoremail.MobileOrEmail;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateLoginVerificationCodeCommand implements Command {

    @NotBlank
    @MobileOrEmail
    private final String mobileOrEmail;
}
