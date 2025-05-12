package com.mryqr.core.verification.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.mobile.Mobile;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IdentifyMobileVerificationCodeCommand implements Command {

    @Mobile
    @NotBlank
    private final String mobile;
}
