package com.mryqr.core.member.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.mobileoremail.MobileOrEmail;
import com.mryqr.common.validation.password.Password;
import com.mryqr.common.validation.verficationcode.VerificationCode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class FindbackPasswordCommand implements Command {
    @NotBlank
    @MobileOrEmail
    private final String mobileOrEmail;

    @NotBlank
    @VerificationCode
    private final String verification;

    @NotBlank
    @Password
    private final String password;

}
