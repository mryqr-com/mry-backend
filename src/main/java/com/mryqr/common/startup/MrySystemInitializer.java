package com.mryqr.common.startup;

import com.mryqr.common.domain.administrative.AdministrativeProvider;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.properties.CommonProperties;
import com.mryqr.common.wx.accesstoken.WxAccessTokenService;
import com.mryqr.common.wx.jssdk.WxJsSdkService;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.management.MryManageTenant;
import com.mryqr.management.apptemplate.MryAppTemplateManageApp;
import com.mryqr.management.apptemplate.MryAppTemplateTenant;
import com.mryqr.management.crm.MryTenantManageApp;
import com.mryqr.management.offencereport.MryOffenceReportApp;
import com.mryqr.management.operation.MryOperationApp;
import com.mryqr.management.order.MryOrderManageApp;
import com.mryqr.management.platform.domain.Platform;
import com.mryqr.management.platform.domain.PlatformFactory;
import com.mryqr.management.platform.domain.PlatformRepository;
import com.mryqr.management.printingproduct.PrintingProductApp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

import static com.mryqr.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoTextFieldOf;
import static com.mryqr.common.utils.MryConstants.*;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static com.mryqr.management.apptemplate.MryAppTemplateTenant.MRY_APP_TEMPLATE_TENANT_ID;
import static java.util.Locale.CHINESE;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.CollectionOptions.just;
import static org.springframework.data.mongodb.core.index.GeoSpatialIndexType.GEO_2DSPHERE;
import static org.springframework.data.mongodb.core.query.Collation.of;

@Slf4j
@Component
@RequiredArgsConstructor
public class MrySystemInitializer implements SmartLifecycle {
    private final MongoTemplate mongoTemplate;
    private final PrintingProductApp printingProductApp;
    private final MryManageTenant mryManageTenant;
    private final MryAppTemplateManageApp mryAppTemplateManageApp;
    private final MryAppTemplateTenant mryAppTemplateTenant;
    private final MryOrderManageApp mryOrderManageApp;
    private final MryTenantManageApp mryTenantManageApp;
    private final MryOperationApp mryOperationApp;
    private final MryOffenceReportApp mryOffenceReportApp;
    private final WxAccessTokenService wxAccessTokenService;
    private final WxJsSdkService wxJsSdkService;
    private final AdministrativeProvider administrativeProvider;
    private final PlatformRepository platformRepository;
    private final PlatformFactory platformFactory;
    private final AppRepository appRepository;
    private final CommonProperties commonProperties;
    private final CacheManager cacheManager;
    private volatile boolean running = false;

    @Override
    public void start() {
        clearCaches();
        ensureMongoCollectionExist();
        ensureMongoIndexExist();
        ensureMryManageAppsExist();
        ensurePlatformArExists();
        wxAccessTokenService.refreshAccessToken();
        wxJsSdkService.refreshJsApiTicket();
        administrativeProvider.init();
        log.info("Mr.Y system initialized.");
        running = true;
    }

    private void ensureMongoCollectionExist() {
        createCollection(APP_COLLECTION);
        createCollection(APP_MANUAL_COLLECTION);
        createCollection(ASSIGNMENT_COLLECTION);
        createCollection(ASSIGNMENT_PLAN_COLLECTION);
        createCollection(DEPARTMENT_COLLECTION);
        createCollection(DEPARTMENT_HIERARCHY_COLLECTION);
        createCollection(GROUP_COLLECTION);
        createCollection(GROUP_HIERARCHY_COLLECTION);
        createCollection(MEMBER_COLLECTION);
        createCollection(ORDER_COLLECTION);
        createCollection(PLATE_COLLECTION);
        createCollection(PLATE_BATCH_COLLECTION);
        createCollection(PLATE_TEMPLATE_COLLECTION);
        createCollection(QR_COLLECTION);
        createCollection(SUBMISSION_COLLECTION);
        createCollection(TENANT_COLLECTION);
        createCollection(VERIFICATION_COLLECTION);
        createCollection(PUBLISHING_DOMAIN_EVENT_COLLECTION);
        createCollection(CONSUMING_DOMAIN_EVENT_COLLECTION);
        createCollection(PLATFORM_COLLECTION);
        createCollection(IN_APP_NOTIFICATION_COLLECTION);
        createCollection(SHEDLOCK_COLLECTION);
    }

    private void createCollection(String collectionName) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName, just(of(CHINESE).numericOrderingEnabled()));
        }
    }

    private void ensureMongoIndexExist() {
        ensureAppIndex();
        ensureAppManualIndex();
        ensureAssignmentIndex();
        ensureAssignmentPlanIndex();
        ensureDepartmentIndex();
        ensureDepartmentHierarchyIndex();
        ensureGroupIndex();
        ensureGroupHierarchyIndex();
        ensureMemberIndex();
        ensureOrderIndex();
        ensurePlateIndex();
        ensurePlateBatchIndex();
        ensureQrIndex();
        ensureSubmissionIndex();
        ensureTenantIndex();
        ensureVerificationIndex();
        ensureInAppNotificationIndex();
        ensurePublishingDomainEventIndex();
        ensureConsumingDomainEventIndex();
    }

    private void ensureAppIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(APP_COLLECTION);
        indexOperations.createIndex(new Index().on("tenantId", DESC));
        indexOperations.createIndex(new Index().on("managers", DESC));
        indexOperations.createIndex(new Index().on("name", DESC));
        indexOperations.createIndex(new Index().on("createdAt", DESC));
        indexOperations.createIndex(new Index().on("appTemplateId", DESC).sparse());
        indexOperations.createIndex(new Index().on("createdBy", DESC));
    }

    private void ensureAppManualIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(APP_MANUAL_COLLECTION);
        indexOperations.createIndex(new Index().on("appId", DESC));
    }

    private void ensureAssignmentIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(ASSIGNMENT_COLLECTION);
        indexOperations.createIndex(new Index().on("expireAt", DESC));
        indexOperations.createIndex(new Index().on("nearExpireNotifyAt", DESC));
        indexOperations.createIndex(new Index().on("appId", DESC));
        indexOperations.createIndex(new Index().on("pageId", DESC));
        indexOperations.createIndex(new Index().on("allQrIds", DESC));
        indexOperations.createIndex(new Index().on("assignmentPlanId", DESC));
        indexOperations.createIndex(new Index().on("groupId", DESC));
        indexOperations.createIndex(new Index().on("operators", DESC));
        indexOperations.createIndex(new Index().on("startAt", DESC));
        indexOperations.createIndex(new Index().on("status", DESC));
    }

    private void ensureAssignmentPlanIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(ASSIGNMENT_PLAN_COLLECTION);
        indexOperations.createIndex(new Index().on("setting.startTime.time", DESC));
        indexOperations.createIndex(new Index().on("setting.appId", DESC));
        indexOperations.createIndex(new Index().on("setting.pageId", DESC));
        indexOperations.createIndex(new Index().on("excludedGroups", DESC));
        indexOperations.createIndex(new Index().on("tenantId", DESC));
        indexOperations.createIndex(new Index().on("name", DESC));
        indexOperations.createIndex(new Index().on("createdAt", DESC));
    }

    private void ensureDepartmentIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(DEPARTMENT_COLLECTION);
        indexOperations.createIndex(new Index().on("tenantId", DESC));
        indexOperations.createIndex(new Index().on("managers", DESC));
        indexOperations.createIndex(new Index().on("customId", DESC));
    }

    private void ensureDepartmentHierarchyIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(DEPARTMENT_HIERARCHY_COLLECTION);
        indexOperations.createIndex(new Index().on("tenantId", DESC).unique());
    }

    private void ensureGroupIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(GROUP_COLLECTION);
        indexOperations.createIndex(new Index().on("appId", DESC));
        indexOperations.createIndex(new Index().on("managers", DESC));
        indexOperations.createIndex(new Index().on("members", DESC));
        indexOperations.createIndex(new Index().on("customId", DESC).sparse());
        indexOperations.createIndex(new Index().on("createdAt", DESC));
        indexOperations.createIndex(new Index().on("createdBy", DESC));
        indexOperations.createIndex(new Index().on("departmentId", DESC).sparse());
    }

    private void ensureGroupHierarchyIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(GROUP_HIERARCHY_COLLECTION);
        indexOperations.createIndex(new Index().on("appId", DESC));
    }

    private void ensureMemberIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(MEMBER_COLLECTION);
        indexOperations.createIndex(new Index().on("tenantId", DESC));
        indexOperations.createIndex(new Index().on("mobile", DESC).sparse().unique());
        indexOperations.createIndex(new Index().on("email", DESC).sparse().unique());
        indexOperations.createIndex(new Index().on("wxUnionId", DESC).sparse().unique());
        indexOperations.createIndex(new Index().on("name", DESC));
        indexOperations.createIndex(new Index().on("customId", DESC).sparse());
        indexOperations.createIndex(new Index().on("createdAt", DESC));
        indexOperations.createIndex(new Index().on("departmentIds", DESC));
    }

    private void ensureOrderIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(ORDER_COLLECTION);
        indexOperations.createIndex(new Index().on("createdBy", DESC));
        indexOperations.createIndex(new Index().on("createdAt", DESC));
        indexOperations.createIndex(new Index().on("tenantId", DESC));
        indexOperations.createIndex(new Index().on("status", DESC));
        indexOperations.createIndex(new Index().on("wxTxnId", DESC).sparse());
        indexOperations.createIndex(new Index().on("bankTransferCode", DESC).sparse());
        indexOperations.createIndex(new Index().on("bankTransferAccountId", DESC).sparse());
    }

    private void ensurePlateIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(PLATE_COLLECTION);
        indexOperations.createIndex(new Index().on("tenantId", DESC));
        indexOperations.createIndex(new Index().on("appId", DESC));
        indexOperations.createIndex(new Index().on("groupId", DESC).sparse());
        indexOperations.createIndex(new Index().on("batchId", DESC).sparse());
        indexOperations.createIndex(new Index().on("qrId", DESC).sparse());
    }

    private void ensurePlateBatchIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(PLATE_BATCH_COLLECTION);
        indexOperations.createIndex(new Index().on("appId", DESC));
        indexOperations.createIndex(new Index().on("name", DESC));
        indexOperations.createIndex(new Index().on("createdBy", DESC));
        indexOperations.createIndex(new Index().on("createdAt", DESC));
        indexOperations.createIndex(new Index().on("totalCount", DESC));
    }

    private void ensureQrIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(QR_COLLECTION);
        indexOperations.createIndex(new Index().on("tenantId", DESC));
        indexOperations.createIndex(new Index().on("appId", DESC));
        indexOperations.createIndex(new Index().on("name", DESC));
        indexOperations.createIndex(new Index().on("text", DESC));
        indexOperations.createIndex(new Index().on("plateId", DESC));
        indexOperations.createIndex(new Index().on("groupId", DESC));
        indexOperations.createIndex(new Index().on("customId", DESC).sparse());
        indexOperations.createIndex(new Index().on("createdBy", DESC));
        indexOperations.createIndex(new Index().on("createdAt", DESC));
        indexOperations.createIndex(new Index().on("svs", DESC).sparse());
        indexOperations.createIndex(new Index().on("lastAccessedAt", DESC));
        indexOperations.createIndex(new GeospatialIndex("geolocation.point").typed(GEO_2DSPHERE));

        for (IndexedField indexedField : IndexedField.values()) {
            indexOperations.createIndex(new Index().on(mongoTextFieldOf(indexedField), DESC).sparse());
            indexOperations.createIndex(new Index().on(mongoSortableFieldOf(indexedField), DESC).sparse());
        }
    }

    private void ensureSubmissionIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(SUBMISSION_COLLECTION);
        indexOperations.createIndex(new Index().on("tenantId", DESC));
        indexOperations.createIndex(new Index().on("appId", DESC));
        indexOperations.createIndex(new Index().on("groupId", DESC));
        indexOperations.createIndex(new Index().on("qrId", DESC));
        indexOperations.createIndex(new Index().on("pageId", DESC));
        indexOperations.createIndex(new Index().on("createdAt", DESC));
        indexOperations.createIndex(new Index().on("createdBy", DESC).sparse());
        indexOperations.createIndex(new Index().on("svs", DESC).sparse());

        for (IndexedField indexedField : IndexedField.values()) {
            indexOperations.createIndex(new Index().on(mongoTextFieldOf(indexedField), DESC).sparse());
            indexOperations.createIndex(new Index().on(mongoSortableFieldOf(indexedField), DESC).sparse());
        }
    }

    private void ensureTenantIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(TENANT_COLLECTION);
        indexOperations.createIndex(new Index().on("createdBy", DESC));
        indexOperations.createIndex(new Index().on("apiSetting.apiKey", DESC).unique());
        indexOperations.createIndex(new Index().on("createdAt", DESC));
    }

    private void ensureVerificationIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(VERIFICATION_COLLECTION);
        indexOperations.createIndex(new Index().on("code", DESC));
        indexOperations.createIndex(new Index().on("mobileOrEmail", DESC));
        indexOperations.createIndex(new Index().on("type", DESC));
        indexOperations.createIndex(new Index().on("usedCount", DESC));
        indexOperations.createIndex(new Index().on("createdAt", DESC));
    }

    private void ensureInAppNotificationIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(IN_APP_NOTIFICATION_COLLECTION);
        indexOperations.createIndex(new Index().on("memberId", DESC));
        indexOperations.createIndex(new Index().on("viewed", DESC));
        indexOperations.createIndex(new Index().on("createdAt", DESC));
    }

    private void ensurePublishingDomainEventIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(PUBLISHING_DOMAIN_EVENT_COLLECTION);
        indexOperations.createIndex(new Index().on("status", DESC));
        indexOperations.createIndex(new Index().on("publishedCount", DESC));
        indexOperations.createIndex(new Index().on("raisedAt", DESC));
    }

    private void ensureConsumingDomainEventIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(CONSUMING_DOMAIN_EVENT_COLLECTION);
        indexOperations.createIndex(new Index().on("eventId", DESC));
    }

    private void ensurePlatformArExists() {
        if (!platformRepository.platformExists()) {
            Platform platform = platformFactory.createPlatform();
            platformRepository.save(platform);
        }
    }

    private void ensureMryManageAppsExist() {
        //码如云管理
        mryManageTenant.init();
        mryTenantManageApp.init();
        mryOperationApp.init();

        if (commonProperties.isCreateOrderManageApp()) {
            mryOrderManageApp.init();
        }

        if (commonProperties.isCreatePrintingProductManageApp()) {
            printingProductApp.init();
        }

        if (commonProperties.isCreateOffenceManageApp()) {
            mryOffenceReportApp.init();
        }

        //应用模板
        mryAppTemplateTenant.init();
        mryAppTemplateManageApp.init();

        appRepository.evictTenantAppsCache(MRY_MANAGE_TENANT_ID);
        appRepository.evictTenantAppsCache(MRY_APP_TEMPLATE_TENANT_ID);
    }

    private void clearCaches() {
        this.cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        log.info("Cleared all application caches.");
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 0;
    }
}
