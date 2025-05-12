package com.mryqr.core.tenant.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.nospace.NoSpace;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.common.utils.MryConstants.MAX_SUBDOMAIN_LENGTH;
import static com.mryqr.core.common.utils.MryConstants.MIN_SUBDOMAIN_LENGTH;
import static com.mryqr.core.common.utils.MryRegexConstants.SUBDOMAIN_PATTERN;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateTenantSubdomainCommand implements Command {

    @NoSpace
    @Size(min = MIN_SUBDOMAIN_LENGTH, max = MAX_SUBDOMAIN_LENGTH)
    @Pattern(regexp = SUBDOMAIN_PATTERN, message = "子域名格式不正确")
    private final String subdomainPrefix;

}
