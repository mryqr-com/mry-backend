package com.mryqr.core.register.command;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.mobileoremail.MobileOrEmail;
import com.mryqr.common.validation.password.Password;
import com.mryqr.common.validation.verficationcode.VerificationCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.common.exception.ErrorCode.MUST_SIGN_AGREEMENT;
import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class RegisterCommand implements Command {
    @NotBlank
    @MobileOrEmail
    private final String mobileOrEmail;

//    @NotBlank
    @VerificationCode
    private final String verification;

    @NotBlank
    @Password
    private final String password;

    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String username;

    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String tenantName;

    private final boolean agreement;

    @Override
    public void correctAndValidate() {
        if (!agreement) {
            throw new MryException(MUST_SIGN_AGREEMENT, "请同意用户协议以完成注册。");
        }
    }
}
