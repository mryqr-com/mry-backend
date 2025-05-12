package com.mryqr.core.qr.query;

import com.google.common.collect.ImmutableMap;
import com.mryqr.common.domain.Geopoint;
import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.permission.*;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.utils.EasyExcelResult;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.Pagination;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.group.domain.GroupAware;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.qr.domain.AppedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrReferenceContext;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.DoubleAttributeValue;
import com.mryqr.core.qr.domain.attribute.TextAttributeValue;
import com.mryqr.core.qr.domain.task.CountQrAccessTask;
import com.mryqr.core.qr.query.list.ListViewableQrsQuery;
import com.mryqr.core.qr.query.list.QViewableListQr;
import com.mryqr.core.qr.query.submission.QSubmissionAppDetail;
import com.mryqr.core.qr.query.submission.QSubmissionQr;
import com.mryqr.core.qr.query.submission.QSubmissionQrDetail;
import com.mryqr.core.qr.query.submission.QSubmissionQrMemberProfile;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import com.mryqr.core.tenant.domain.task.RecordTenantRecentAccessTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.PLATE_NOT_BOUND;
import static com.mryqr.common.exception.ErrorCode.QR_NOT_FOUND;
import static com.mryqr.common.utils.CommonUtils.splitSearchBySpace;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoTextFieldOf;
import static com.mryqr.common.utils.MryConstants.QR_COLLECTION;
import static com.mryqr.common.utils.Pagination.pagination;
import static com.mryqr.common.validation.id.plate.PlateIdValidator.isPlateId;
import static com.mryqr.common.validation.id.qr.QrIdValidator.isQrId;
import static com.mryqr.core.qr.domain.QR.newQrId;
import static java.time.LocalDate.parse;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.function.Function.identity;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.domain.Sort.unsorted;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrQueryService {
    private final AppRepository appRepository;
    private final QrRepository qrRepository;
    private final MongoTemplate mongoTemplate;
    private final MemberRepository memberRepository;
    private final TenantRepository tenantRepository;
    private final PlateRepository plateRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final AppOperatePermissionChecker appOperatePermissionChecker;
    private final SubmissionPermissionChecker submissionPermissionChecker;
    private final CountQrAccessTask countQrAccessTask;
    private final RecordTenantRecentAccessTask recordTenantRecentAccessTask;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;

    private final TaskExecutor taskExecutor;
    private final MryRateLimiter mryRateLimiter;

    public QSubmissionQr fetchSubmissionQr(String plateId, User user) {
        Query query = query(where("plateId").is(plateId));
        QR qr = mongoTemplate.findOne(query, QR.class);
        if (qr == null) {
            Plate plate = plateRepository.byId(plateId);
            if (!plate.isBound()) {
                //前端将导向绑定页面
                throw new MryException(PLATE_NOT_BOUND, "码牌尚未绑定。", mapOf("plateId", plateId));
            } else {
                throw new MryException(QR_NOT_FOUND, "未找到实例。", mapOf("plateId", plateId));
            }
        }

        App app = appRepository.cachedById(qr.getAppId());
        mryRateLimiter.applyFor(app.getTenantId(), "QR:FetchSubmissionQR", 50);

        SubmissionPermissions submissionPermissions = submissionPermissionChecker.permissionsFor(user, new AppedQr(qr, app));
        submissionPermissions.checkPermission(app.requiredPermission());

        //非APP管理员需要检查APP是否禁用,App管理员不检查主要用于他们预览
        if (!submissionPermissions.hasManageAppPermission()) {
            app.checkActive();
        }

        qr.checkActive(app);

        Member member = null;
        if (user.isLoggedIn()) {
            member = memberRepository.cachedById(user.getMemberId());
        }

        Tenant tenant = tenantRepository.cachedById(app.getTenantId());
        tenant.checkActive();

        QSubmissionQrMemberProfile submissionQrMemberProfile = QSubmissionQrMemberProfile.builder()
                .memberId(member != null ? member.getId() : null)
                .memberTenantId(member != null ? member.getTenantId() : null)
                .memberName(member != null ? member.getName() : null)
                .memberAvatar(member != null ? member.getAvatar() : null)
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .tenantLogo(tenant.getLogo())
                .subdomainPrefix(tenant.getSubdomainPrefix())
                .subdomainReady(tenant.isSubdomainReady())
                .hideBottomMryLogo(tenant.effectivePlan().isHideBottomMryLogo())
                .videoAudioAllowed(tenant.effectivePlan().isVideoAudioAllowed())
                .build();

        QSubmissionQrDetail qrDetail = QSubmissionQrDetail.builder()
                .id(qr.getId())
                .plateId(qr.getPlateId())
                .name(qr.getName())
                .appId(qr.getAppId())
                .groupId(qr.getGroupId())
                .tenantId(qr.getTenantId())
                .circulationOptionId(qr.getCirculationOptionId())
                .template(qr.isTemplate())
                .headerImage(qr.getHeaderImage())
                .geolocation(qr.getGeolocation())
                .description(qr.getDescription())
                .build();

        QSubmissionAppDetail appDetail = QSubmissionAppDetail.builder()
                .id(app.getId())
                .name(app.getName())
                .version(app.getVersion())
                .setting(app.getSetting())
                .build();

        taskExecutor.execute(() -> {
            countQrAccessTask.run(qr, app);
            recordTenantRecentAccessTask.run(app.getTenantId()); // 如果以后服务器负担过重，可以考虑去掉recordTenantRecentAccessTask
        });

        return QSubmissionQr.builder()
                .qr(qrDetail)
                .app(appDetail)
                .submissionQrMemberProfile(submissionQrMemberProfile)
                .permissions(submissionPermissions.getPermissions())
                .canViewFillablePageIds(submissionPermissions.getCanViewFillablePageIds())
                .canManageFillablePageIds(submissionPermissions.getCanManageFillablePageIds())
                .canApproveFillablePageIds(submissionPermissions.getCanApproveFillablePageIds())
                .canOperateApp(submissionPermissions.getPermissions().contains(app.getOperationPermission()))
                .build();
    }

    public QViewableListQr fetchViewableListQr(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:ListQr", 20);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        App app = appRepository.cachedById(qr.getAppId());
        SubmissionPermissions submissionPermissions = submissionPermissionChecker.permissionsFor(user, app, qr.getGroupId());
        submissionPermissions.checkPermissions(app.requiredPermission());

        Map<String, AttributeValue> fixedAttributeValues = app.fixedAttributeValues();
        Set<String> summaryEligibleAttributeIds = app.summaryEligibleAttributeIds();
        Map<String, AttributeValue> finalAttributeValues = Stream.concat(qr.getAttributeValues().values().stream(),
                        fixedAttributeValues.values().stream())
                .filter(attributeValue -> summaryEligibleAttributeIds.contains(attributeValue.getAttributeId()))
                .collect(toImmutableMap(AttributeValue::getAttributeId, identity()));

        QrReferenceContext referenceContext = buildListReferenceContext(app, List.of(qr));
        Map<String, DisplayValue> displayValues = finalAttributeValues.values().stream()
                .map(attributeValue -> attributeValue.toDisplayValue(referenceContext))
                .collect(toImmutableMap(DisplayValue::getKey, identity()));

        return QViewableListQr.builder()
                .id(qr.getId())
                .name(qr.getName())
                .plateId(qr.getPlateId())
                .appId(qr.getAppId())
                .groupId(qr.getGroupId())
                .template(qr.isTemplate())
                .circulationOptionId(qr.getCirculationOptionId())
                .createdAt(qr.getCreatedAt())
                .createdBy(qr.getCreatedBy())
                .creator(qr.getCreator())
                .geolocation(qr.getGeolocation())
                .headerImage(qr.getHeaderImage())
                .attributeDisplayValues(displayValues)
                .active(qr.isActive())
                .build();
    }

    public PagedList<QViewableListQr> listMyViewableQrs(ListViewableQrsQuery queryCommand, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:List", 20);

        App app = appRepository.cachedByIdAndCheckTenantShip(queryCommand.getAppId(), user);
        AppOperatePermissions appOperatePermissions = appOperatePermissionChecker.permissionsFor(user, app);
        Query query = buildQrListQuery(queryCommand, app, appOperatePermissions);
        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());

        long count = mongoTemplate.count(query, QR_COLLECTION);
        if (count == 0) {
            return pagedList(pagination, 0, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sort(queryCommand, app));

        List<QR> rawQrs = listRawQrs(query, app);
        Set<String> summaryEligibleAttributeIds = app.summaryEligibleAttributeIds();
        rawQrs.forEach(qr -> qr.getAttributeValues().entrySet()
                .removeIf(entry -> !summaryEligibleAttributeIds.contains(entry.getKey())));

        QrReferenceContext referenceContext = buildListReferenceContext(app, rawQrs);
        List<QViewableListQr> viewableQrs = rawQrs.stream().map(qr -> QViewableListQr.builder()
                .id(qr.getId())
                .name(qr.getName())
                .plateId(qr.getPlateId())
                .appId(qr.getAppId())
                .groupId(qr.getGroupId())
                .template(qr.isTemplate())
                .circulationOptionId(qr.getCirculationOptionId())
                .createdAt(qr.getCreatedAt())
                .createdBy(qr.getCreatedBy())
                .creator(qr.getCreator())
                .geolocation(qr.getGeolocation())
                .headerImage(qr.getHeaderImage())
                .attributeDisplayValues(qr.getAttributeValues().values().stream()
                        .map(attributeValue -> attributeValue.toDisplayValue(referenceContext))
                        .collect(toImmutableMap(DisplayValue::getKey, identity())))
                .customId(qr.getCustomId())
                .active(qr.isActive())
                .build()).collect(toImmutableList());

        return pagedList(pagination, (int) count, viewableQrs);
    }

    private PagedList<QViewableListQr> pagedList(Pagination pagination, int count, List<QViewableListQr> objects) {
        return PagedList.<QViewableListQr>builder()
                .totalNumber(count)
                .pageSize(pagination.getPageSize())
                .pageIndex(pagination.getPageIndex())
                .data(objects)
                .build();
    }

    public EasyExcelResult exportQrsToExcel(ListViewableQrsQuery queryCommand, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:Export", 1);

        App app = appRepository.cachedByIdAndCheckTenantShip(queryCommand.getAppId(), user);
        managePermissionChecker.checkCanManageApp(user, app);
        AppOperatePermissions appOperatePermissions = appOperatePermissionChecker.permissionsFor(user, app);

        Map<String, Control> allControls = app.allControls().stream().collect(toImmutableMap(Control::getId, identity()));
        List<Attribute> allExportableAttributes = app.allExportableAttributes();
        QrReferenceContext referenceContext = buildExportReferenceContext(app);

        List<List<String>> headers = exportHeaders(app, allExportableAttributes);
        List<List<Object>> records = new ArrayList<>();
        String startId = newQrId();

        while (true) {
            Query query = buildQrListQuery(queryCommand, app, appOperatePermissions);
            query.addCriteria(where("_id").lt(startId));
            query.with(by(DESC, "_id"));
            query.limit(500);

            List<QR> rawQrs = listRawQrs(query, app);
            if (isEmpty(rawQrs)) {
                break;
            }

            records.addAll(rawQrs.stream().map(qr -> transformToExcelObject(qr,
                    app,
                    allExportableAttributes,
                    allControls,
                    referenceContext)).collect(toImmutableList()));

            if (rawQrs.size() >= 10000) {//最大支持导出10000条数据
                break;
            }

            startId = rawQrs.get(rawQrs.size() - 1).getId();//下一次直接从最后一条开始查询
        }

        log.info("Exported qrs to excel for app[{}].", app.getId());

        return EasyExcelResult.builder()
                .headers(headers)
                .records(records)
                .fileName("Qrs_" + now().truncatedTo(SECONDS) + ".xlsx")
                .build();
    }

    private List<QR> listRawQrs(Query query, App app) {
        query.fields().include("plateId").include("name").include("appId").include("groupId").include("template").include("customId")
                .include("createdAt").include("createdBy").include("geolocation").include("headerImage").include("attributeValues")
                .include("active").include("creator").include("circulationOptionId");

        List<QR> qrs = mongoTemplate.find(query, QR.class);
        Map<String, AttributeValue> fixedAttributeValues = app.fixedAttributeValues();
        qrs.forEach(qr -> qr.getAttributeValues().putAll(fixedAttributeValues));
        return qrs;
    }

    private QrReferenceContext buildListReferenceContext(App app, List<QR> qrs) {
        Set<String> referencedMemberIds = new HashSet<>();
        Set<String> referencedGroupIds = new HashSet<>();

        qrs.forEach(qr -> {
            List<AttributeValue> attributeValues = qr.getAttributeValues().values().stream().collect(toImmutableList());

            referencedMemberIds.addAll(attributeValues.stream()
                    .filter(value -> value instanceof MemberAware)
                    .map(value -> ((MemberAware) value).awaredMemberIds())
                    .flatMap(Collection::stream)
                    .collect(toImmutableSet()));

            referencedGroupIds.addAll(attributeValues.stream()
                    .filter(value -> value instanceof GroupAware)
                    .map(value -> ((GroupAware) value).awaredGroupIds())
                    .flatMap(Collection::stream)
                    .collect(toImmutableSet()));
        });

        Map<String, MemberReference> memberReferences = memberRepository.cachedMemberReferences(app.getTenantId(), copyOf(referencedMemberIds));
        Map<String, String> groupFullNames = groupRepository.cachedGroupFullNamesOf(app.getId(), referencedGroupIds);

        return QrReferenceContext.builder()
                .app(app)
                .memberReferences(memberReferences)
                .groupFullNames(groupFullNames)
                .build();
    }

    private QrReferenceContext buildExportReferenceContext(App app) {
        Map<String, MemberReference> memberReferences = memberRepository.cachedAllMemberReferences(app.getTenantId())
                .stream().collect(toImmutableMap(MemberReference::getId, identity()));
        Map<String, String> groupFullNames = groupRepository.cachedAllGroupFullNames(app.getId());

        return QrReferenceContext.builder()
                .app(app)
                .memberReferences(memberReferences)
                .groupFullNames(groupFullNames)
                .build();
    }

    private List<Object> transformToExcelObject(QR qr,
                                                App app,
                                                List<Attribute> attributes,
                                                Map<String, Control> allControls,
                                                QrReferenceContext context) {
        List<Object> batchRecords = new ArrayList<>();
        batchRecords.add(qr.getId());
        batchRecords.add(qr.getName());

        if (app.isGeolocationEnabled()) {
            batchRecords.add(qr.getGeolocation() != null ? qr.getGeolocation().toText() : null);
        }

        attributes.forEach(attribute -> {
            AttributeValue attributeValue = qr.getAttributeValues().get(attribute.getId());
            if (attributeValue == null) {
                batchRecords.add(null);
            } else {
                Control control = isNotBlank(attribute.getControlId()) ? allControls.get(attribute.getControlId()) : null;
                batchRecords.add(attributeValue.toExportValue(attribute, context, control));
            }
        });

        batchRecords.add(qr.isActive() ? "是" : "否");
        batchRecords.add(qr.isTemplate() ? "是" : "否");
        batchRecords.add(qr.getPlateId());
        batchRecords.add(qr.getCustomId());
        batchRecords.add(qr.getGroupId());

        return batchRecords;
    }

    private List<List<String>> exportHeaders(App app, List<Attribute> allExportableAttributes) {
        List<List<String>> headers = new ArrayList<>();
        headers.add(List.of(app.instanceDesignation() + "ID"));
        headers.add(List.of(app.instanceDesignation() + "名称"));

        if (app.isGeolocationEnabled()) {
            headers.add(List.of("定位"));
        }

        allExportableAttributes.forEach(attribute -> headers.add(List.of(attribute.getName())));

        headers.add(List.of("是否启用"));
        headers.add(List.of("是否模板"));
        headers.add(List.of("码牌ID"));
        headers.add(List.of(app.customIdDesignation()));
        headers.add(List.of(app.groupDesignation() + "ID"));
        return headers;
    }

    private Query buildQrListQuery(ListViewableQrsQuery queryCommand,
                                   App app,
                                   AppOperatePermissions appOperatePermissions) {
        Criteria criteria = baseQrListCriteria(queryCommand, appOperatePermissions, app);
        Criteria filteredCriteria = appendQrListFilterableCriteria(criteria, queryCommand, app);
        Criteria searchedCriteria = appendQrListSearchableCriteria(filteredCriteria, queryCommand);
        return query(searchedCriteria);
    }

    private Criteria baseQrListCriteria(ListViewableQrsQuery queryCommand, AppOperatePermissions appOperatePermissions, App app) {
        Criteria criteria = where("appId").is(queryCommand.getAppId());
        if (queryCommand.isTemplateOnly()) {
            criteria.and("template").is(true);
        }

        String groupId = queryCommand.getGroupId();
        if (appOperatePermissions.isCanManageApp()) {
            if (queryCommand.isInactiveOnly()) {
                criteria.and("active").is(false);//App管理员不做active限制，除非显式指明inactiveOnly
            }
        } else {
            if (isBlank(groupId)) {
                criteria.and("active").is(true);//非App管理员看整个App的QR时，根本不考虑inactiveOnly，而是只能看到active的
            } else {
                if (appOperatePermissions.canManageGroup(groupId)) {
                    if (queryCommand.isInactiveOnly()) {
                        criteria.and("active").is(false);//对于某个group，非App管理员，但是为某个Group管理员时，不做active限制，除非显式指明inactiveOnly
                    }
                } else {
                    criteria.and("active").is(true);//对于某个group，非App管理员又非group管理员时，根本不考虑inactiveOnly，而是只能看到active的
                }
            }
        }

        if (isBlank(groupId)) {
            appOperatePermissions.checkHasViewableGroups();
            criteria.and("groupId").in(appOperatePermissions.getViewableGroupIds());
        } else {
            appOperatePermissions.checkViewableGroupPermission(groupId);

            GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(app.getId());
            Set<String> withAllSubGroupIds = groupHierarchy.withAllSubGroupIdsOf(groupId);
            Set<String> viewableGroupIds = appOperatePermissions.getViewableGroupIds();
            Set<String> resultViewableGroupIds = withAllSubGroupIds.stream().filter(viewableGroupIds::contains).collect(toImmutableSet());
            criteria.and("groupId").in(resultViewableGroupIds);
        }

        if (shouldGeoSearch(app, queryCommand)) {
            Geopoint currentPoint = queryCommand.getCurrentPoint();
            criteria.and("geolocation.point").nearSphere(new Point(currentPoint.getLongitude(), currentPoint.getLatitude()));
        }

        if (isNotBlank(queryCommand.getCreatedBy())) {
            criteria.and("createdBy").is(queryCommand.getCreatedBy());
        }

        String startDate = queryCommand.getStartDate();
        String endDate = queryCommand.getEndDate();
        if (isNotBlank(startDate) || isNotBlank(endDate)) {
            Criteria dateCriteria = criteria.and("createdAt");
            if (isNotBlank(startDate)) {
                dateCriteria.gte(parse(startDate).atStartOfDay(systemDefault()).toInstant());
            }

            if (isNotBlank(endDate)) {
                dateCriteria.lt(parse(endDate).plusDays(1).atStartOfDay(systemDefault()).toInstant());
            }
        }

        return criteria;
    }

    private boolean shouldGeoSearch(App app, ListViewableQrsQuery queryCommand) {//sort和geo最近查询是互斥的，sortBy具有优先级
        Geopoint currentPoint = queryCommand.getCurrentPoint();
        return app.isGeolocationEnabled() &&
               queryCommand.isNearestPointEnabled() &&
               currentPoint != null &&
               currentPoint.isPositioned() &&
               isBlank(queryCommand.getSortedBy());
    }

    private Criteria appendQrListFilterableCriteria(Criteria criteria, ListViewableQrsQuery queryCommand, App app) {
        Map<String, Set<String>> allFilterables = queryCommand.getFilterables();
        if (MapUtils.isEmpty(allFilterables)) {
            return criteria;
        }

        List<Criteria> criteriaList = allFilterables.entrySet().stream()
                .filter(entry -> isNotEmpty(entry.getValue()))
                .map(entry -> app.indexedFieldForAttributeOptional(entry.getKey())
                        .map(indexedField -> where(mongoTextFieldOf(indexedField)).in(entry.getValue())).orElse(null))
                .filter(Objects::nonNull)
                .collect(toImmutableList());

        if (criteriaList.size() > 0) {
            criteria.andOperator(criteriaList.toArray(Criteria[]::new));
        }

        return criteria;
    }

    private Criteria appendQrListSearchableCriteria(Criteria criteria, ListViewableQrsQuery queryCommand) {
        String search = queryCommand.getSearch();
        if (isBlank(search)) {
            return criteria;
        }

        if (isQrId(search)) {
            return criteria.and("_id").is(search);
        }

        if (isPlateId(search)) {
            return criteria.and("plateId").is(search);
        }

        Object[] terms = splitSearchBySpace(search);
        return criteria.orOperator(where("svs").all(terms),
                where("text").all(terms),
                where("name").is(search),
                where("customId").is(search));
    }

    private Sort sort(ListViewableQrsQuery queryCommand, App app) {
        if (shouldGeoSearch(app, queryCommand)) {
            return unsorted();
        }

        Sort baseSort = by(DESC, "active");
        String sortedBy = queryCommand.getSortedBy();
        Sort.Direction direction = queryCommand.isAscSort() ? ASC : DESC;

        if (isBlank(sortedBy)) {
            return baseSort.and(by(DESC, "createdAt"));
        }

        if ("createdAt".equals(sortedBy)) {
            return baseSort.and(by(direction, "createdAt"));
        }

        if ("name".equals(sortedBy)) {
            return baseSort.and(by(direction, "name"));
        }

        return baseSort.and(app.indexedFieldForAttributeOptional(sortedBy)
                .map(indexedField -> by(direction, mongoSortableFieldOf(indexedField)).and(by(DESC, "createdAt")))
                .orElse(unsorted()));
    }

    public QQrBaseSetting fetchQrBaseSetting(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:FetchBaseSetting", 20);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        App app = appRepository.cachedById(qr.getAppId());

        managePermissionChecker.checkCanManageQr(user, qr, app);
        Set<String> manualInputAttributeIds = app.manualInputAttributes().stream().map(Attribute::getId).collect(toImmutableSet());

        Map<String, String> manualAttributeValues = qr.getAttributeValues().values().stream()
                .filter(value -> manualInputAttributeIds.contains(value.getAttributeId()))
                .map(value -> {
                    if (value instanceof TextAttributeValue attributeValue) {
                        return isNotBlank(attributeValue.getText()) ?
                                ImmutableMap.of(value.getAttributeId(), attributeValue.getText()) :
                                null;
                    }

                    if (value instanceof DoubleAttributeValue attributeValue) {
                        return attributeValue.getNumber() != null ?
                                ImmutableMap.of(value.getAttributeId(), attributeValue.getNumber().toString()) :
                                null;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(map -> map.entrySet().stream())
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

        return QQrBaseSetting.builder()
                .id(qr.getId())
                .name(qr.getName())
                .description(qr.getDescription())
                .headerImage(qr.getHeaderImage())
                .manualAttributeValues(manualAttributeValues)
                .geolocation(qr.getGeolocation())
                .customId(qr.getCustomId())
                .build();
    }

    public QQrSummary fetchQrSummary(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:FetchSummary", 20);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        App app = appRepository.cachedById(qr.getAppId());

        //非APP管理员需要检查APP是否禁用,App管理员不检查主要用于他们预览
        if (!managePermissionChecker.canManageApp(user, app)) {
            app.checkActive();
        }

        qr.checkActive(app);

        return QQrSummary.builder()
                .id(qr.getId())
                .name(qr.getName())
                .plateId(qr.getPlateId())
                .appId(qr.getAppId())
                .groupId(qr.getGroupId())
                .template(qr.isTemplate())
                .build();
    }
}
