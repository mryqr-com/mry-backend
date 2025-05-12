package com.mryqr.core.app.query;

import com.mryqr.common.domain.permission.AppOperatePermissionChecker;
import com.mryqr.common.domain.permission.AppOperatePermissions;
import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.utils.EasyExcelResult;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.Pagination;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.TenantCachedApp;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.tenant.domain.ResourceUsage;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.domain.permission.Permission.*;
import static com.mryqr.common.exception.ErrorCode.APP_ALREADY_LOCKED;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MongoCriteriaUtils.regexSearch;
import static com.mryqr.common.utils.MryConstants.*;
import static com.mryqr.common.utils.Pagination.pagination;
import static com.mryqr.common.validation.id.app.AppIdValidator.isAppId;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class AppQueryService {
    private final static Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "createdAt", "active");
    private final AppRepository appRepository;
    private final MongoTemplate mongoTemplate;
    private final ManagePermissionChecker managePermissionChecker;
    private final AppOperatePermissionChecker appOperatePermissionChecker;
    private final MryRateLimiter mryRateLimiter;
    private final TenantRepository tenantRepository;

    public PagedList<QManagedListApp> listMyManagedApps(ListMyManagedAppsQuery queryCommand, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:ListMyManagedApps", 5);

        String search = queryCommand.getSearch();
        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());

        Query query = query(where("tenantId").is(user.getTenantId()));

        if (isNotBlank(search)) {
            if (isAppId(search)) {
                query.addCriteria(where("_id").is(search));
            } else {
                query.addCriteria(regexSearch("name", search));
            }
        }

        if (!user.isTenantAdmin()) {
            query.addCriteria(where("managers").is(user.getMemberId()));
        }

        long count = mongoTemplate.count(query, App.class);
        if (count == 0) {
            return pagedList(pagination, 0, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.getPageSize()).with(sort(queryCommand));
        query.fields().include("name", "createdAt", "createdBy", "creator", "active", "locked", "icon");
        List<QManagedListApp> listApps = mongoTemplate.find(query, QManagedListApp.class, APP_COLLECTION);
        return pagedList(pagination, (int) count, listApps);
    }

    private PagedList<QManagedListApp> pagedList(Pagination pagination, int count, List<QManagedListApp> apps) {
        return PagedList.<QManagedListApp>builder()
                .totalNumber(count)
                .pageIndex(pagination.getPageIndex())
                .pageSize(pagination.getPageSize())
                .data(apps)
                .build();
    }

    private Sort sort(ListMyManagedAppsQuery queryCommand) {
        String sortedBy = queryCommand.getSortedBy();

        if (isBlank(sortedBy) || !ALLOWED_SORT_FIELDS.contains(sortedBy)) {
            return by(DESC, "createdAt");
        }

        Sort.Direction direction = queryCommand.isAscSort() ? ASC : DESC;
        if (Objects.equals(sortedBy, "createdAt")) {
            return by(direction, "createdAt");
        }

        return by(direction, sortedBy).and(by(DESC, "createdAt"));
    }

    public List<QViewableListApp> listMyViewableApps(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:ListMyViewableApps", 20);

        String tenantId = user.getTenantId();
        String memberId = user.getMemberId();
        List<TenantCachedApp> cachedApps = appRepository.cachedTenantAllApps(tenantId);

        if (user.isTenantAdmin()) {
            return cachedApps.stream().map(toViewableListApp()).collect(toImmutableList());
        }

        //可以看到自己作为App管理员的App，不排除禁用的(为了支持编辑时预览)
        Set<String> asAppManagerAppIds = cachedApps.stream()
                .filter(app -> app.getManagers().contains(memberId))
                .map(TenantCachedApp::getId)
                .collect(toImmutableSet());

        Map<Permission, List<TenantCachedApp>> permissionedApps = cachedApps.stream()
                .filter(TenantCachedApp::isActive)//如果不是App管理员，则排除禁用的
                .collect(groupingBy(TenantCachedApp::getOperationPermission));

        //可以看到权限为AS_TENANT_MEMBER和PUBLIC的App
        Set<String> commonAppIds = Stream
                .concat(emptyIfNull(permissionedApps.get(AS_TENANT_MEMBER)).stream(), emptyIfNull(permissionedApps.get(PUBLIC)).stream())
                .map(TenantCachedApp::getId)
                .collect(toImmutableSet());

        //可以看到作为group普通成员的App
        Set<String> asGroupMemberAppIds = Set.of();
        List<TenantCachedApp> groupMemberRequiredApps = permissionedApps.get(AS_GROUP_MEMBER);
        if (isNotEmpty(groupMemberRequiredApps)) {
            Set<String> groupMemberRequiredAppIds = groupMemberRequiredApps.stream()
                    .map(TenantCachedApp::getId)
                    .collect(toImmutableSet());
            Query asGroupMemberQuery = new Query(where("appId").in(groupMemberRequiredAppIds).and("members").is(memberId));
            asGroupMemberAppIds = mongoTemplate.findDistinct(asGroupMemberQuery, "appId", GROUP_COLLECTION, String.class)
                    .stream().collect(toImmutableSet());
        }

        //可以看到作为group管理员的App
        Set<String> asGroupManagerAppIds = Set.of();
        List<TenantCachedApp> groupManagerRequiredApps = permissionedApps.get(CAN_MANAGE_GROUP);
        if (isNotEmpty(groupManagerRequiredApps)) {
            Set<String> groupManagerRequiredAppIds = groupManagerRequiredApps.stream()
                    .map(TenantCachedApp::getId)
                    .collect(toImmutableSet());
            Query asGroupManagerQuery = new Query(where("appId").in(groupManagerRequiredAppIds).and("managers").is(memberId));
            asGroupManagerAppIds = mongoTemplate.findDistinct(asGroupManagerQuery, "appId", GROUP_COLLECTION, String.class)
                    .stream().collect(toImmutableSet());
        }

        //合并所有可见的App
        Set<String> eligibleAppIds = Stream.of(asAppManagerAppIds, commonAppIds, asGroupMemberAppIds, asGroupManagerAppIds)
                .flatMap(Collection::stream)
                .collect(toImmutableSet());

        return cachedApps.stream()
                .filter(app -> eligibleAppIds.contains(app.getId()))
                .map(toViewableListApp())
                .collect(toImmutableList());
    }

    private Function<TenantCachedApp, QViewableListApp> toViewableListApp() {
        return cachedApp -> QViewableListApp.builder()
                .id(cachedApp.getId())
                .name(cachedApp.getName())
                .icon(cachedApp.getIcon())
                .locked(cachedApp.isLocked())
                .build();
    }

    public QOperationalApp fetchOperationalApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:FetchOperationApp", 20);

        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        AppOperatePermissions operatePermissions = appOperatePermissionChecker.permissionsFor(user, app);
        if (!operatePermissions.isCanManageApp()) {//App管理员无需检查禁用状态(为了支持编辑时预览)
            app.checkActive();
        }

        operatePermissions.checkHasPermissions();

        return QOperationalApp.builder()
                .id(appId)
                .name(app.getName())
                .icon(app.getIcon())
                .locked(app.isLocked())
                .setting(app.getSetting())
                .reportSetting(app.getReportSetting())
                .groupSynced(app.isGroupSynced())
                .canManageApp(operatePermissions.isCanManageApp())
                .groupFullNames(operatePermissions.getGroupFullNames())
                .viewableGroupIds(operatePermissions.getViewableGroupIds())
                .viewablePageIds(operatePermissions.getViewablePageIds())
                .managableGroupIds(operatePermissions.getManagableGroupIds())
                .managablePageIds(operatePermissions.getManagablePageIds())
                .approvableGroupIds(operatePermissions.getApprovableGroupIds())
                .approvablePageIds(operatePermissions.getApprovablePageIds())
                .build();
    }

    public QUpdatableApp fetchUpdatableApp(String appId, User user) {
        if (user.isMryManageTenantUser()) {
            App app = appRepository.byId(appId);
            return QUpdatableApp.builder()
                    .id(app.getId())
                    .name(app.getName())
                    .tenantId(app.getTenantId())
                    .version(app.getVersion())
                    .webhookEnabled(app.isWebhookEnabled())
                    .setting(app.getSetting())
                    .build();
        }

        mryRateLimiter.applyFor(user.getTenantId(), "App:FetchUpdatableApp", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        if (app.isLocked()) {
            throw new MryException(APP_ALREADY_LOCKED, "应用已经锁定，无法编辑，请解除锁定后再编辑。", mapOf("appId", app.getId()));
        }

        return QUpdatableApp.builder()
                .id(app.getId())
                .name(app.getName())
                .tenantId(app.getTenantId())
                .version(app.getVersion())
                .webhookEnabled(app.isWebhookEnabled())
                .setting(app.getSetting())
                .build();
    }

    public List<String> listAppManagers(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:FetchAppManagers", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);
        return app.getManagers();
    }

    public QAppResourceUsages fetchAppResourceUsages(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:FetchAppResourceUsages", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        Tenant tenant = tenantRepository.byId(app.getTenantId());
        ResourceUsage resourceUsage = tenant.getResourceUsage();

        return QAppResourceUsages.builder()
                .usedQrCount(resourceUsage.getQrCountForApp(appId))
                .usedGroupCount(resourceUsage.getGroupCountForApp(appId))
                .usedSubmissionCount(resourceUsage.getSubmissionCountForApp(appId))
                .build();
    }

    public EasyExcelResult fetchQrImportTemplateForApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:FetchQrImportTemplate", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        List<Attribute> attributes = app.qrImportableAttributes();

        List<List<String>> headers = new ArrayList<>();
        headers.add(List.of(app.qrImportNameFieldName()));
        headers.add(List.of(app.qrImportCustomIdFieldName()));
        attributes.forEach(attribute -> headers.add(List.of(attribute.getName())));

        return EasyExcelResult.builder()
                .fileName(app.getName() + "-" + app.instanceDesignation() + "导入模板.xlsx")
                .headers(headers)
                .records(List.of(List.of()))
                .build();
    }

    public QAppWebhookSetting fetchAppWebhookSetting(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:FetchAppWebhookSetting", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        return QAppWebhookSetting.builder()
                .webhookSetting(app.getWebhookSetting())
                .build();
    }

    public QAppFirstQr fetchAppFirstQr(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:FetchFirstQr", 20);

        Query query = query(where("tenantId").is(user.getTenantId()).and("appId").is(appId))
                .with(by(ASC, "createdAt"))
                .limit(1);

        return mongoTemplate.findOne(query, QAppFirstQr.class, QR_COLLECTION);
    }
}
