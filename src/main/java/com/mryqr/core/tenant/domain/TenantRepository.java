package com.mryqr.core.tenant.domain;

import com.mryqr.core.common.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {
    Tenant bySubdomainPrefix(String subdomainPrefix);

    boolean existsBySubdomainPrefix(String subdomainPrefix);

    Tenant cachedByApiKey(String apiKey);

    PackagesStatus packagesStatusOf(String tenantId);

    List<String> allTenantIds();

    Tenant cachedById(String tenantId);

    Tenant cachedByIdAndCheckTenantShip(String tenantId, User user);

    Optional<Tenant> cachedByIdOptional(String tenantId);

    Tenant byId(String id);

    Optional<Tenant> byIdOptional(String id);

    boolean exists(String arId);

    void save(Tenant tenant);

    void evictTenantCache(String tenantId);

    void evictTenantCacheByApiKey(String apiKey);

    int deltaCountApp(String tenantId, int delta);

    int deltaCountDepartment(String tenantId, int delta);

    int deltaCountGroupForApp(String appId, String tenantId, int delta);

    int deltaCountMember(String tenantId, int delta);

    int deltaCountPlate(String tenantId, int delta);

    int deltaCountQrUnderApp(String appId, String tenantId, int delta);

    int deltaCountSubmissionForApp(String appId, String tenantId, int delta);
}
