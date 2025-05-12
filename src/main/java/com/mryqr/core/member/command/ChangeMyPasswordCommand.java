package com.mryqr.core.member.command;

import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.password.Password;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Objects;

import static com.mryqr.core.common.exception.ErrorCode.PASSWORD_CONFIRM_NOT_MATCH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ChangeMyPasswordCommand implements Command {
    @NotBlank
    @Password
    private final String oldPassword;

    @NotBlank
    @Password
    private final String newPassword;

    @NotBlank
    @Password
    private final String confirmNewPassword;

    @Override
    public void correctAndValidate() {
        if (!Objects.equals(newPassword, confirmNewPassword)) {
            throw new MryException(PASSWORD_CONFIRM_NOT_MATCH, "修改密码失败，确认密码和新密码不一致。");
        }
    }
}
