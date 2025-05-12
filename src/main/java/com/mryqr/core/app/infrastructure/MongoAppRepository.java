package com.mryqr.core.app.infrastructure;

import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.TenantCachedApp;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.AR_NOT_FOUND;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MryConstants.APP_COLLECTION;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.*;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoAppRepository extends MongoBaseRepository<App> implements AppRepository {
    private static final Map<AttributeStatisticRange, String> rangeFieldMap = Map.of(
            THIS_WEEK, "hasWeeklyResetAttributes",
            THIS_MONTH, "hasMonthlyResetAttributes",
            THIS_SEASON, "hasSeasonlyResetAttributes",
            THIS_YEAR, "hasYearlyResetAttributes"
    );

    private final MongoCachedAppRepository cachedAppRepository;

    @Override
    public Set<String> allAppIdsOf(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("tenantId").is(tenantId));
        return mongoTemplate.findDistinct(query, "_id", APP_COLLECTION, String.class).stream().collect(toImmutableSet());
    }

    @Override
    public App cachedById(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        return cachedAppRepository.cachedById(appId);
    }

    @Override
    public App cachedByIdAndCheckTenantShip(String appId, User user) {
        requireNonBlank(appId, "App ID must not be blank.");

        App app = cachedAppRepository.cachedById(appId);
        checkTenantShip(app, user);
        return app;
    }

    @Override
    public Optional<App> cachedByIdOptional(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        try {
            App app = cachedAppRepository.cachedById(appId);
            return Optional.ofNullable(app);
        } catch (MryException ex) {
            if (ex.getCode() == AR_NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    @Override
    public List<TenantCachedApp> cachedTenantAllApps(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return cachedAppRepository.cachedTenantAllApps(tenantId).getApps();
    }

    @Override
    public boolean cachedExistsByName(String name, String tenantId) {
        requireNonBlank(name, "App name must not be blank.");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return cachedAppRepository.cachedTenantAllApps(tenantId).getApps()
                .stream().anyMatch(app -> Objects.equals(app.getName(), name));
    }

    @Override
    public App byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<App> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public App byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }

    @Override
    public void save(App app) {
        super.save(app);
        cachedAppRepository.evictAppCache(app.getId());
        cachedAppRepository.evictTenantAppsCache(app.getTenantId());
    }

    @Override
    public void delete(App app) {
        super.delete(app);
        cachedAppRepository.evictAppCache(app.getId());
        cachedAppRepository.evictTenantAppsCache(app.getTenantId());
    }

    @Override
    public void evictAppCache(String appId) {
        cachedAppRepository.evictAppCache(appId);
    }

    @Override
    public void evictTenantAppsCache(String tenantId) {
        cachedAppRepository.evictTenantAppsCache(tenantId);
    }

    @Override
    public int countApp(String tenantId) {
        return super.count(tenantId);
    }

    @Override
    public List<App> appsOfRange(AttributeStatisticRange range, String startId, int batchSize) {
        requireNonNull(range, "Range must not be null.");
        requireNonBlank(startId, "Start ID must not be blank.");

        return mongoTemplate.find(query(where(rangeFieldMap.get(range)).is(true)
                        .and("_id").lt(startId))
                        .with(by(DESC, "_id"))
                        .limit(batchSize),
                App.class);
    }

    @Override
    public int removeManagerFromAllApps(String memberId, String tenantId) {
        requireNonBlank(memberId, "Member ID must not be blank.");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("managers").is(memberId));
        Update update = new Update().pull("managers", memberId);
        UpdateResult result = mongoTemplate.updateMulti(query, update, App.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int appTemplateAppliedCountFor(String appTemplateId) {
        requireNonBlank(appTemplateId, "Template app ID must not be blank.");

        Query query = query(where("appTemplateId").is(appTemplateId));
        return (int) mongoTemplate.count(query, App.class);
    }
}
