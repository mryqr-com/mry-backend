package com.mryqr.core.verification.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.mobile.Mobile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateChangeMobileVerificationCodeCommand implements Command {

    @Mobile
    @NotBlank
    @Size(max = 11)
    private final String mobile;
}
