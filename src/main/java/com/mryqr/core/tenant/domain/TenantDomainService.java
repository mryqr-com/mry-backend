package com.mryqr.core.tenant.domain;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.mryqr.core.common.exception.ErrorCode.TENANT_WITH_SUBDOMAIN_PREFIX_ALREADY_EXISTS;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class TenantDomainService {
    private final TenantRepository tenantRepository;

    public void updateSubdomain(Tenant tenant, String subdomainPrefix, User user) {
        if (isNotBlank(subdomainPrefix)
                && !Objects.equals(tenant.getSubdomainPrefix(), subdomainPrefix)
                && tenantRepository.existsBySubdomainPrefix(subdomainPrefix)) {
            throw new MryException(TENANT_WITH_SUBDOMAIN_PREFIX_ALREADY_EXISTS,
                    "更新失败，域名已经被占用。", mapOf("subdomainPrefix", subdomainPrefix));
        }

        tenant.updateSubdomain(subdomainPrefix, user);
    }

}
