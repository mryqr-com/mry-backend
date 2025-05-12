package com.mryqr.core.login.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.mobileoremail.MobileOrEmail;
import com.mryqr.core.common.validation.verficationcode.VerificationCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class VerificationCodeLoginCommand implements Command {
    @NotBlank
    @MobileOrEmail
    private final String mobileOrEmail;

    @NotBlank
    @VerificationCode
    private final String verification;

    @Size(max = 1000)
    private final String wxIdInfo;

}
