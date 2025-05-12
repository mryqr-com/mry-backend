package com.mryqr.integration.member.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.collection.NoBlankString;
import com.mryqr.core.common.validation.collection.NoDuplicatedString;
import com.mryqr.core.common.validation.email.Email;
import com.mryqr.core.common.validation.id.custom.CustomId;
import com.mryqr.core.common.validation.id.department.DepartmentId;
import com.mryqr.core.common.validation.mobile.Mobile;
import com.mryqr.core.common.validation.password.Password;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IntegrationCreateMemberCommand implements Command {
    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @Mobile
    private final String mobile;

    @Email
    private final String email;

    @NotBlank
    @Password
    private final String password;

    @CustomId
    private final String customId;

    @Valid
    @NoBlankString
    @NoDuplicatedString
    @Size(max = 1000)
    private final List<@DepartmentId String> departmentIds;
}
