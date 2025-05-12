package com.mryqr.core.member.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.mobile.Mobile;
import com.mryqr.core.common.validation.verficationcode.VerificationCode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IdentifyMyMobileCommand implements Command {

    @Mobile
    @NotBlank
    private final String mobile;

    @NotBlank
    @VerificationCode
    private final String verification;

}
