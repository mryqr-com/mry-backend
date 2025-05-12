package com.mryqr.core.tenant.infrastructure;

import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.mryqr.core.common.exception.ErrorCode.AR_NOT_FOUND;
import static com.mryqr.core.common.exception.ErrorCode.TENANT_NOT_FOUND;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.TENANT_COLLECTION;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
@AllArgsConstructor
public class MongoTenantRepository extends MongoBaseRepository<Tenant> implements TenantRepository {
    private final MongoCachedTenantRepository cachedTenantRepository;

    @Override
    public Tenant bySubdomainPrefix(String subdomainPrefix) {
        requireNonBlank(subdomainPrefix, "Subdomain prefix must not be blank.");

        Query query = Query.query(where("subdomainPrefix").is(subdomainPrefix));
        Tenant tenant = mongoTemplate.findOne(query, Tenant.class);

        if (tenant == null) {
            throw new MryException(TENANT_NOT_FOUND, "没有找到子域名对应的租户。", mapOf("subdomainPrefix", subdomainPrefix));
        }

        return tenant;
    }

    @Override
    public boolean existsBySubdomainPrefix(String subdomainPrefix) {
        requireNonBlank(subdomainPrefix, "Subdomain prefix must not be blank.");

        Query query = Query.query(where("subdomainPrefix").is(subdomainPrefix));
        return mongoTemplate.exists(query, Tenant.class);
    }

    @Override
    public Tenant cachedByApiKey(String apiKey) {
        requireNonBlank(apiKey, "API Key must not be blank.");

        return cachedTenantRepository.cachedByApiKey(apiKey);
    }

    @Override
    public PackagesStatus packagesStatusOf(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("_id").is(tenantId));
        query.fields().include("packages").include("resourceUsage");
        PackagesStatus packagesStatus = mongoTemplate.findOne(query, PackagesStatus.class, TENANT_COLLECTION);

        if (packagesStatus == null) {
            throw new MryException(TENANT_NOT_FOUND, "没有找到租户。", mapOf("id", tenantId));
        }

        return packagesStatus;
    }

    @Override
    public List<String> allTenantIds() {
        Query query = new Query();
        return mongoTemplate.findDistinct(query, "_id", TENANT_COLLECTION, String.class);
    }

    @Override
    public Tenant cachedById(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return cachedTenantRepository.cachedById(tenantId);
    }

    @Override
    public Tenant cachedByIdAndCheckTenantShip(String tenantId, User user) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Tenant tenant = cachedTenantRepository.cachedById(tenantId);
        checkTenantShip(tenant, user);
        return tenant;
    }

    @Override
    public Optional<Tenant> cachedByIdOptional(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        try {
            Tenant tenant = cachedTenantRepository.cachedById(tenantId);
            return Optional.ofNullable(tenant);
        } catch (MryException ex) {
            if (ex.getCode() == AR_NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    @Override
    public Tenant byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<Tenant> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }

    @Override
    public void save(Tenant tenant) {
        super.save(tenant);
        cachedTenantRepository.evictTenantCache(tenant.getId());
        cachedTenantRepository.evictTenantCacheByApiKey(tenant.getApiSetting().getApiKey());
    }

    @Override
    public void evictTenantCache(String tenantId) {
        cachedTenantRepository.evictTenantCache(tenantId);
    }

    @Override
    public void evictTenantCacheByApiKey(String apiKey) {
        cachedTenantRepository.evictTenantCacheByApiKey(apiKey);
    }

    @Override
    public int deltaCountApp(String tenantId, int delta) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("_id").is(tenantId));
        Update update = new Update();
        update.inc("resourceUsage.appCount", delta);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Tenant.class);
        return (int) updateResult.getModifiedCount();
    }

    @Override
    public int deltaCountDepartment(String tenantId, int delta) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("_id").is(tenantId));
        Update update = new Update();
        update.inc("resourceUsage.departmentCount", delta);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Tenant.class);
        return (int) updateResult.getModifiedCount();
    }

    @Override
    public int deltaCountGroupForApp(String appId, String tenantId, int delta) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("_id").is(tenantId));
        Update update = new Update();
        update.inc("resourceUsage.groupCountPerApp." + appId, delta);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Tenant.class);
        return (int) updateResult.getModifiedCount();
    }

    @Override
    public int deltaCountMember(String tenantId, int delta) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("_id").is(tenantId));
        Update update = new Update();
        update.inc("resourceUsage.memberCount", delta);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Tenant.class);
        return (int) updateResult.getModifiedCount();
    }

    @Override
    public int deltaCountPlate(String tenantId, int delta) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("_id").is(tenantId));
        Update update = new Update();
        update.inc("resourceUsage.plateCount", delta);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Tenant.class);
        return (int) updateResult.getModifiedCount();
    }

    @Override
    public int deltaCountQrUnderApp(String appId, String tenantId, int delta) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("_id").is(tenantId));
        Update update = new Update();
        update.inc("resourceUsage.qrCountPerApp." + appId, delta);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Tenant.class);
        return (int) updateResult.getModifiedCount();
    }

    @Override
    public int deltaCountSubmissionForApp(String appId, String tenantId, int delta) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("_id").is(tenantId));
        Update update = new Update();
        update.inc("resourceUsage.submissionCountPerApp." + appId, delta);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Tenant.class);
        return (int) updateResult.getModifiedCount();
    }

}
