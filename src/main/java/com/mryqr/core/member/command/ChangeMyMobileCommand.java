package com.mryqr.core.member.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.mobile.Mobile;
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
public class ChangeMyMobileCommand implements Command {
    @Mobile
    @NotBlank
    private final String mobile;

    @NotBlank
    @VerificationCode
    private final String verification;

    @NotBlank
    @Password
    private final String password;

}
