package com.mryqr.integration.member.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.collection.NoBlankString;
import com.mryqr.common.validation.collection.NoDuplicatedString;
import com.mryqr.common.validation.email.Email;
import com.mryqr.common.validation.id.department.DepartmentId;
import com.mryqr.common.validation.mobile.Mobile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IntegrationUpdateMemberInfoCommand implements Command {
    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @Mobile
    private final String mobile;

    @Email
    private final String email;

    @Valid
    @NoBlankString
    @NoDuplicatedString
    @Size(max = 1000)
    private final List<@DepartmentId String> departmentIds;

}
