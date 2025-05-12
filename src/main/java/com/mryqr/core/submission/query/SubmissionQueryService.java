package com.mryqr.core.submission.query;

import com.google.common.collect.ImmutableSet;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.permission.AppOperatePermissionChecker;
import com.mryqr.core.common.domain.permission.AppOperatePermissions;
import com.mryqr.core.common.domain.permission.SubmissionPermissionChecker;
import com.mryqr.core.common.domain.permission.SubmissionPermissions;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.utils.EasyExcelResult;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.common.utils.Pagination;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionApproval;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.query.list.ListSubmissionsQuery;
import com.mryqr.core.submission.query.list.QListSubmission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.ALL_SUBMIT_HISTORY;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.TO_BE_APPROVED;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.core.common.exception.ErrorCode.BAD_REQUEST;
import static com.mryqr.core.common.utils.CommonUtils.splitSearchBySpace;
import static com.mryqr.core.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static com.mryqr.core.common.utils.MongoCriteriaUtils.mongoTextFieldOf;
import static com.mryqr.core.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.common.utils.MryConstants.SUBMISSION_COLLECTION;
import static com.mryqr.core.common.utils.Pagination.pagination;
import static com.mryqr.core.submission.domain.ApprovalStatus.statusOf;
import static com.mryqr.core.submission.domain.Submission.newSubmissionId;
import static java.time.LocalDate.parse;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.function.Function.identity;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isEmpty;
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
public class SubmissionQueryService {
    private static final String APPROVAL = "approval";
    private static final String CREATED_BY = "createdBy";
    private static final String GROUP_ID = "groupId";
    private final SubmissionRepository submissionRepository;
    private final MongoTemplate mongoTemplate;
    private final QrRepository qrRepository;
    private final SubmissionPermissionChecker submissionPermissionChecker;
    private final AppOperatePermissionChecker appOperatePermissionChecker;
    private final AppRepository appRepository;
    private final MemberRepository memberRepository;
    private final MryRateLimiter mryRateLimiter;
    private final GroupHierarchyRepository groupHierarchyRepository;

    public PagedList<QListSubmission> listSubmissions(ListSubmissionsQuery queryCommand, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Submission:List", 20);

        App app = appRepository.cachedByIdAndCheckTenantShip(queryCommand.getAppId(), user);
        AppOperatePermissions appOperatePermissions = appOperatePermissionChecker.permissionsFor(user, app);
        List<Control> allControls = app.allControls();
        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());

        Set<String> filterAndSortEligibleControlIds = filterAndSortEligibleControlIds(allControls, appOperatePermissions, queryCommand);
        Query query = buildListQuery(queryCommand, app, appOperatePermissions, user, filterAndSortEligibleControlIds);
        long count = mongoTemplate.count(query, SUBMISSION_COLLECTION);
        if (count == 0) {
            return pagedList(pagination, 0, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sort(queryCommand, app, filterAndSortEligibleControlIds));
        List<QListSubmission> finalSubmissions = fetchSubmissions(query, user, appOperatePermissions, allControls);
        return pagedList(pagination, (int) count, finalSubmissions);
    }

    private PagedList<QListSubmission> pagedList(Pagination pagination, int count, List<QListSubmission> submissions) {
        return PagedList.<QListSubmission>builder()
                .totalNumber(count)
                .pageSize(pagination.getPageSize())
                .pageIndex(pagination.getPageIndex())
                .data(submissions)
                .build();
    }

    public EasyExcelResult exportSubmissionsToExcel(ListSubmissionsQuery queryCommand, User user) {
        String pageId = queryCommand.getPageId();
        if (isBlank(pageId)) {
            throw new MryException(BAD_REQUEST, "下载失败，请提供页面ID。", "appId", queryCommand.getAppId());
        }

        if (queryCommand.getType() != ALL_SUBMIT_HISTORY) {
            throw new MryException(BAD_REQUEST, "下载失败，只允许ALL_SUBMIT_HISTORY类型。",
                    "appId", queryCommand.getAppId(), "pageId", pageId);
        }

        mryRateLimiter.applyFor(user.getTenantId(), "Submission:Export", 1);

        App app = appRepository.cachedByIdAndCheckTenantShip(queryCommand.getAppId(), user);
        AppOperatePermissions appOperatePermissions = appOperatePermissionChecker.permissionsFor(user, app);
        Page page = app.pageById(pageId);

        List<Control> exportableControls = page.allExportableControls();
        Map<String, String> qrNameMap = new HashMap<>();
        Map<String, MemberReference> memberReferences = memberRepository.cachedAllMemberReferences(app.getTenantId()).stream()
                .collect(toImmutableMap(MemberReference::getId, identity()));
        SubmissionReferenceContext referenceContext = SubmissionReferenceContext.builder().memberReferences(memberReferences).build();

        List<List<String>> headers = exportHeaders(app, page, exportableControls);
        List<Control> allControls = app.allControls();
        Set<String> filterAndSortEligibleControlIds = filterAndSortEligibleControlIds(allControls, appOperatePermissions, queryCommand);

        List<List<Object>> records = new ArrayList<>();
        String startId = newSubmissionId();
        while (true) {
            Query query = buildListQuery(queryCommand, app, appOperatePermissions, user, filterAndSortEligibleControlIds);
            query.addCriteria(where("_id").lt(startId));
            query.with(by(DESC, "_id"));
            query.limit(500);

            List<Submission> rawSubmissions = listRawSubmissions(query);
            if (rawSubmissions.isEmpty()) {
                break;
            }

            records.addAll(rawSubmissions.stream().map(submission ->
                            transformToExcelObject(submission,
                                    page,
                                    exportableControls,
                                    appOperatePermissions.getGroupFullNames(),
                                    qrNameMap,
                                    referenceContext,
                                    rawSubmissions))
                    .collect(toImmutableList()));
            if (records.size() >= 10000) {//最大导出10000条记录
                break;
            }

            startId = rawSubmissions.get(rawSubmissions.size() - 1).getId();//下一次直接从最后一条开始查询
        }

        log.info("Exported submission excel for app[{}].", queryCommand.getAppId());

        return EasyExcelResult.builder()
                .headers(headers)
                .records(records)
                .fileName("Submissions_" + now().truncatedTo(SECONDS) + ".xlsx")
                .build();
    }

    private List<Object> transformToExcelObject(Submission submission,
                                                Page page,
                                                List<Control> exportableControls,
                                                Map<String, String> groupNameMap,
                                                Map<String, String> qrNameMap,
                                                SubmissionReferenceContext referenceContext,
                                                List<Submission> submissions) {
        List<Object> batchRecords = new ArrayList<>();
        batchRecords.add(submission.getId());
        String qrName = qrNameMap.get(submission.getQrId());
        if (isBlank(qrName)) {//有一个qr名称为空，则加载该批次中所有尚未加载名称的qr名称
            Set<String> submissionQrIds = new HashSet<>(submissions.stream().map(Submission::getQrId).collect(toImmutableSet()));
            Set<String> existingQrIds = qrNameMap.keySet();
            submissionQrIds.removeAll(existingQrIds);
            qrNameMap.putAll(qrRepository.qrNamesOf(submissionQrIds));
        }
        batchRecords.add(qrNameMap.get(submission.getQrId()));
        batchRecords.add(groupNameMap.get(submission.getGroupId()));
        batchRecords.add(MRY_DATE_TIME_FORMATTER.format(submission.getCreatedAt()));

        if (page.requireLogin()) {
            batchRecords.add(submission.getCreator());
        }

        if (page.isApprovalEnabled()) {
            batchRecords.add(submission.getApproval() == null ? null :
                    submission.getApproval().isPassed() ? page.approvalPassText() : page.approvalNotPassText());
        }

        exportableControls.forEach(control -> {
            Answer answer = submission.getAnswers().get(control.getId());
            batchRecords.add(answer == null ? null : answer.toExportValue(control, referenceContext));
        });

        batchRecords.add(submission.getQrId());
        batchRecords.add(submission.getGroupId());
        return batchRecords;
    }

    private List<List<String>> exportHeaders(App app, Page page, List<Control> exportableControls) {
        List<List<String>> headers = new ArrayList<>();
        headers.add(List.of("提交ID"));
        headers.add(List.of(app.instanceDesignation() + "名称"));
        headers.add(List.of(app.groupDesignation() + "名称"));
        headers.add(List.of(page.submitAtDesignation()));

        if (page.requireLogin()) {
            headers.add(List.of(page.submitterDesignation()));
        }

        if (page.isApprovalEnabled()) {
            headers.add(List.of("审批结果"));
        }

        exportableControls.forEach(control -> headers.add(List.of(control.fieldName())));
        headers.add(List.of(app.instanceDesignation() + "ID"));
        headers.add(List.of(app.groupDesignation() + "ID"));
        return headers;
    }

    private List<QListSubmission> fetchSubmissions(Query query,
                                                   User user,
                                                   AppOperatePermissions appOperatePermissions,
                                                   List<Control> allControls) {
        List<Submission> rawSubmissions = listRawSubmissions(query);
        Map<String, String> qrNamesMap = qrNamesFor(rawSubmissions);
        Map<String, Control> controlMap = allControls.stream().collect(toImmutableMap(Control::getId, identity()));

        rawSubmissions.forEach(submission -> submission.getAnswers().entrySet().removeIf(entry -> {
            Control control = controlMap.get(entry.getKey());
            if (control == null) {
                return true;
            }

            if (appOperatePermissions.getManagablePageIds().contains(submission.getPageId())) {//管理员任何时候可以看到
                return false;
            }

            if (control.isPermissionEnabled()) {
                if (control.isAnswerViewableBySubmitter() && Objects.equals(user.getMemberId(), submission.getCreatedBy())) {
                    return false;
                }

                if (control.getPermission() == CAN_MANAGE_GROUP) {
                    return !appOperatePermissions.getManagableGroupIds().contains(user.getMemberId());
                }

                if (control.getPermission() == CAN_MANAGE_APP) {
                    return !appOperatePermissions.isCanManageApp();
                }
            }

            return false;
        }));

        SubmissionReferenceContext referenceContext = buildReferenceContext(rawSubmissions, user.getTenantId());
        return rawSubmissions.stream().map(submission -> QListSubmission.builder()
                .id(submission.getId())
                .plateId(submission.getPlateId())
                .qrId(submission.getQrId())
                .qrName(qrNamesMap.get(submission.getQrId()))
                .groupId(submission.getGroupId())
                .appId(submission.getAppId())
                .pageId(submission.getPageId())
                .displayAnswers(submission.getAnswers().values().stream()
                        .map(answer -> answer.toDisplayValue(referenceContext))
                        .filter(Objects::nonNull)
                        .collect(toImmutableMap(DisplayValue::getKey, identity())))
                .approvalStatus(statusOf(submission.getApproval()))
                .createdBy(submission.getCreatedBy())
                .creator(submission.getCreator())
                .createdAt(submission.getCreatedAt())
                .referenceData(submission.getReferenceData())
                .build()).collect(toImmutableList());
    }

    private List<Submission> listRawSubmissions(Query query) {
        query.fields().include("plateId").include("qrId").include("appId").include("groupId").include("pageId")
                .include("answers").include("approval").include("createdBy").include("createdAt").include("referenceData")
                .include("creator");
        return mongoTemplate.find(query, Submission.class);
    }

    private SubmissionReferenceContext buildReferenceContext(List<Submission> submissions, String tenantId) {
        Set<String> referencedMemberIds = submissions.stream()
                .flatMap(submission -> submission.getAnswers().values().stream()
                        .filter(answer -> answer instanceof MemberAware)
                        .map(answer -> ((MemberAware) answer).awaredMemberIds())
                        .flatMap(Collection::stream))
                .collect(toImmutableSet());

        Map<String, MemberReference> memberReferences = memberRepository.cachedMemberReferences(tenantId, referencedMemberIds);
        return SubmissionReferenceContext.builder().memberReferences(memberReferences).build();
    }

    private Query buildListQuery(ListSubmissionsQuery queryCommand,
                                 App app,
                                 AppOperatePermissions appOperatePermissions,
                                 User user,
                                 Set<String> filterAndSortEligibleControlIds) {
        Criteria baseCriteria = baseCriteria(queryCommand, appOperatePermissions, user);
        Criteria filterableCriteria = appendFilterableCriteria(baseCriteria, queryCommand, app, filterAndSortEligibleControlIds);
        Criteria searchableCriteria = appendSearchableCriteria(filterableCriteria, queryCommand);
        return query(searchableCriteria);
    }

    private Criteria baseCriteria(ListSubmissionsQuery queryCommand, AppOperatePermissions operatePermissions, User user) {
        String appId = queryCommand.getAppId();
        String groupId = queryCommand.getGroupId();
        String qrId = queryCommand.getQrId();
        String pageId = queryCommand.getPageId();
        String createdBy = queryCommand.getCreatedBy();
        String startDate = queryCommand.getStartDate();
        String endDate = queryCommand.getEndDate();

        Criteria criteria = where("appId").is(appId);
        if (isNotBlank(startDate) || isNotBlank(endDate)) {
            Criteria dateCriteria = criteria.and("createdAt");
            if (isNotBlank(startDate)) {
                dateCriteria.gte(parse(startDate).atStartOfDay(systemDefault()).toInstant());
            }

            if (isNotBlank(endDate)) {
                dateCriteria.lt(parse(endDate).plusDays(1).atStartOfDay(systemDefault()).toInstant());
            }
        }

        switch (queryCommand.getType()) {
            case SUBMITTER_SUBMISSION -> {
                return appendSubmitterSubmissionCriteria(criteria, groupId, qrId, pageId, appId, user.getMemberId(), operatePermissions, user);
            }
            case ALL_SUBMIT_HISTORY -> {
                return appendSubmitHistoryCriteria(criteria, groupId, qrId, pageId, appId, createdBy, operatePermissions, user);
            }
            case TO_BE_APPROVED -> {
                return appendToBeApprovedCriteria(criteria, groupId, qrId, pageId, appId, createdBy, operatePermissions, user);
            }
            default -> {
                throw new IllegalStateException("Submission list type[" + queryCommand.getType().name() + "] not supported.");
            }
        }
    }

    private Set<String> filterAndSortEligibleControlIds(List<Control> allControls,
                                                        AppOperatePermissions appOperatePermissions,
                                                        ListSubmissionsQuery queryCommand) {
        return allControls.stream().filter(control -> {
            if (appOperatePermissions.isCanManageApp()) {
                return true;
            }

            if (queryCommand.getType() == ALL_SUBMIT_HISTORY || queryCommand.getType() == TO_BE_APPROVED) {
                return true;//此两种类型均表示能具有管理权限，管理员可以查看所有的控件值，无论控件的增强权限设置如何
            }

            if (control.isPermissionEnabled()) {
                return control.isAnswerViewableBySubmitter();
            }

            return true;
        }).map(Control::getId).collect(toImmutableSet());
    }

    private Criteria appendSubmitterSubmissionCriteria(Criteria criteria,
                                                       String groupId,
                                                       String qrId,
                                                       String pageId,
                                                       String appId,
                                                       String createdBy,
                                                       AppOperatePermissions operatePermissions,
                                                       User user) {
        if (isNotBlank(qrId)) {
            QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
            operatePermissions.checkViewableGroupPermission(qr);
            criteria.and("qrId").is(qrId);
        } else {
            if (isBlank(groupId)) {
                operatePermissions.checkHasViewableGroups();
                criteria.and("groupId").in(operatePermissions.getViewableGroupIds());
            } else {
                operatePermissions.checkViewableGroupPermission(groupId);

                GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(appId);
                Set<String> withAllSubGroupIds = groupHierarchy.withAllSubGroupIdsOf(groupId);
                Set<String> viewableGroupIds = operatePermissions.getViewableGroupIds();
                Set<String> resultViewableGroupIds = withAllSubGroupIds.stream().filter(viewableGroupIds::contains).collect(toImmutableSet());
                criteria.and("groupId").in(resultViewableGroupIds);
            }
        }

        if (isBlank(pageId)) {
            operatePermissions.checkHasViewablePages();
            criteria.and("pageId").in(operatePermissions.getViewablePageIds());
        } else {
            operatePermissions.checkViewablePagePermission(pageId);
            criteria.and("pageId").is(pageId);
        }

        if (isNotBlank(createdBy)) {
            criteria.and("createdBy").is(createdBy);
        }

        return criteria;
    }

    private Criteria appendSubmitHistoryCriteria(Criteria criteria,
                                                 String groupId,
                                                 String qrId,
                                                 String pageId,
                                                 String appId,
                                                 String createdBy,
                                                 AppOperatePermissions operatePermissions,
                                                 User user) {
        if (isNotBlank(qrId)) {
            QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
            operatePermissions.checkManagableGroupPermission(qr);
            criteria.and("qrId").is(qrId);
        } else {
            if (isBlank(groupId)) {
                operatePermissions.checkHasManagableGroups();
                criteria.and("groupId").in(operatePermissions.getManagableGroupIds());
            } else {
                operatePermissions.checkManagableGroupPermission(groupId);

                GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(appId);
                Set<String> withAllSubGroupIds = groupHierarchy.withAllSubGroupIdsOf(groupId);
                Set<String> managableGroupIds = operatePermissions.getManagableGroupIds();
                Set<String> resultManagableGroupIds = withAllSubGroupIds.stream().filter(managableGroupIds::contains).collect(toImmutableSet());
                criteria.and("groupId").in(resultManagableGroupIds);
            }
        }

        if (isBlank(pageId)) {
            operatePermissions.checkHasManagablePages();
            criteria.and("pageId").in(operatePermissions.getManagablePageIds());
        } else {
            operatePermissions.checkManagablePagePermission(pageId);
            criteria.and("pageId").is(pageId);
        }

        if (isNotBlank(createdBy)) {
            criteria.and("createdBy").is(createdBy);
        }

        return criteria;
    }

    private Criteria appendToBeApprovedCriteria(Criteria criteria,
                                                String groupId,
                                                String qrId,
                                                String pageId,
                                                String appId,
                                                String createdBy,
                                                AppOperatePermissions operatePermissions,
                                                User user) {
        criteria.and("approval").is(null);
        if (isNotBlank(qrId)) {
            QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
            operatePermissions.checkApprovableGroupPermission(qr);
            criteria.and("qrId").is(qrId);
        } else {
            if (isBlank(groupId)) {
                operatePermissions.checkHasApprovableGroups();
                criteria.and("groupId").in(operatePermissions.getApprovableGroupIds());
            } else {
                operatePermissions.checkApprovableGroupPermission(groupId);

                GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(appId);
                Set<String> withAllSubGroupIds = groupHierarchy.withAllSubGroupIdsOf(groupId);
                Set<String> approvableGroupIds = operatePermissions.getApprovableGroupIds();
                Set<String> resultApprovableGroupIds = withAllSubGroupIds.stream().filter(approvableGroupIds::contains).collect(toImmutableSet());
                criteria.and("groupId").in(resultApprovableGroupIds);
            }
        }

        if (isBlank(pageId)) {
            operatePermissions.checkHasApprovablePages();
            criteria.and("pageId").in(operatePermissions.getApprovablePageIds());
        } else {
            operatePermissions.checkApprovablePagePermission(pageId);
            criteria.and("pageId").is(pageId);
        }

        if (isNotBlank(createdBy)) {
            criteria.and("createdBy").is(createdBy);
        }

        return criteria;
    }

    private Criteria appendFilterableCriteria(Criteria criteria,
                                              ListSubmissionsQuery queryCommand,
                                              App app,
                                              Set<String> filterAndSortEligibleControlIds) {
        Map<String, Set<String>> allFilterables = queryCommand.getFilterables();
        String pageId = queryCommand.getPageId();

        if (isEmpty(allFilterables)) {
            return criteria;
        }

        List<Criteria> criteriaList = new ArrayList<>(buildIndexedCriterias(queryCommand, app, filterAndSortEligibleControlIds));

        Set<String> groupFilterables = allFilterables.get(GROUP_ID);
        if (isNotEmpty(groupFilterables) && isBlank(queryCommand.getGroupId())) {//只有在没有单独的groupId时，group多选过滤才生效
            criteriaList.add(where("groupId").in(groupFilterables));
        }

        Set<String> createdByFilterables = allFilterables.get(CREATED_BY);
        if (isNotEmpty(createdByFilterables) && isBlank(queryCommand.getCreatedBy())) {//只有在没有单独的createdBy时，createdBy多选过滤才生效
            criteriaList.add(where("createdBy").in(createdByFilterables));
        }

        if (criteriaList.size() > 0) {
            criteria.andOperator(criteriaList.toArray(Criteria[]::new));
        }

        if (isBlank(pageId)) {
            return criteria;
        }

        //审批过滤
        Set<String> approvalFilterables = allFilterables.get(APPROVAL);
        if (isNotEmpty(approvalFilterables)) {
            app.pageByIdOptional(pageId).ifPresent(page -> {
                if (page.isApprovalEnabled()) {
                    criteria.orOperator(approvalFilterables.stream().map(value -> {
                        switch (value) {
                            case "YES" -> {
                                return where("approval.passed").is(true);
                            }
                            case "NO" -> {
                                return where("approval.passed").is(false);
                            }
                        }
                        return where("approval").is(null);
                    }).toArray(Criteria[]::new));
                }
            });
        }

        return criteria;
    }

    private List<Criteria> buildIndexedCriterias(ListSubmissionsQuery queryCommand,
                                                 App app,
                                                 Set<String> filterAndSortEligibleControlIds) {
        Map<String, Set<String>> allFilterables = queryCommand.getFilterables();
        String pageId = queryCommand.getPageId();
        if (isBlank(pageId)) {//indexedField的过滤只针对页面有用，因为只有指定页面才有answer对应的indexed value
            return List.of();
        }

        return allFilterables.entrySet().stream()
                .filter(this::isIndexedEntry)
                .filter(entry -> filterAndSortEligibleControlIds.contains(entry.getKey()))
                .map(entry -> app.indexedFieldForControlOptional(pageId, entry.getKey())
                        .map(indexedField -> where(mongoTextFieldOf(indexedField)).in(entry.getValue())).orElse(null))
                .filter(Objects::nonNull)
                .collect(toImmutableList());
    }

    private boolean isIndexedEntry(Map.Entry<String, Set<String>> entry) {
        return !APPROVAL.equals(entry.getKey()) &&
                !CREATED_BY.equals(entry.getKey()) &&
                !GROUP_ID.equals(entry.getKey()) &&
                isNotEmpty(entry.getValue());
    }

    private Criteria appendSearchableCriteria(Criteria criteria, ListSubmissionsQuery queryCommand) {
        String search = queryCommand.getSearch();

        if (isBlank(search)) {
            return criteria;
        }

        return criteria.and("svs").all((Object[]) splitSearchBySpace(search));
    }

    private Sort sort(ListSubmissionsQuery queryCommand, App app, Set<String> filterAndSortEligibleControlIds) {
        String sortedBy = queryCommand.getSortedBy();

        if (isBlank(sortedBy)) {
            return by(DESC, "createdAt");
        }

        Sort.Direction direction = queryCommand.isAscSort() ? ASC : DESC;

        if ("createdAt".equals(sortedBy)) {
            return by(direction, "createdAt");
        }

        String pageId = queryCommand.getPageId();
        if (isBlank(pageId)) {
            return unsorted();
        }

        if (!filterAndSortEligibleControlIds.contains(sortedBy)) {
            return unsorted();
        }

        return app.indexedFieldForControlOptional(pageId, sortedBy)
                .map(indexedField -> by(direction, mongoSortableFieldOf(indexedField)).and(by(DESC, "createdAt")))
                .orElse(unsorted());
    }

    private Map<String, String> qrNamesFor(List<Submission> submissions) {
        Set<String> allQrIds = submissions.stream().map(Submission::getQrId).collect(toImmutableSet());
        return qrRepository.qrNamesOf(allQrIds);
    }

    public QDetailedSubmission fetchDetailedSubmission(String submissionId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Submission:FetchSubmission", 50);

        Submission submission = submissionRepository.byIdAndCheckTenantShip(submissionId, user);
        return toSubmissionDetail(submission, user);
    }

    public QListSubmission fetchListSubmission(String submissionId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Submission:FetchListedSubmission", 20);

        Submission submission = submissionRepository.byIdAndCheckTenantShip(submissionId, user);
        return toListSubmission(submission, user);
    }

    private QListSubmission toListSubmission(Submission submission, User user) {
        App app = appRepository.cachedById(submission.getAppId());
        SubmissionPermissions submissionPermissions = submissionPermissionChecker.permissionsFor(user, app, submission.getGroupId());

        Page page = app.pageById(submission.getPageId());
        submissionPermissions.checkCanViewSubmission(submission, page, app);
        Set<Answer> viewableAnswers = calculateViewableAnswers(submission, page, submissionPermissions);
        String qrName = qrRepository.qrNameOf(submission.getQrId());

        Set<String> referencedMemberIds = viewableAnswers.stream()
                .filter(answer -> answer instanceof MemberAware)
                .map(answer -> ((MemberAware) answer).awaredMemberIds())
                .flatMap(Collection::stream)
                .collect(toImmutableSet());

        Map<String, MemberReference> memberReferences = memberRepository.cachedMemberReferences(app.getTenantId(), referencedMemberIds);
        SubmissionReferenceContext referenceContext = SubmissionReferenceContext.builder()
                .memberReferences(memberReferences)
                .build();

        Map<String, DisplayValue> displayAnswers = viewableAnswers.stream()
                .map(answer -> answer.toDisplayValue(referenceContext))
                .collect(toImmutableMap(DisplayValue::getKey, identity()));

        return QListSubmission.builder()
                .id(submission.getId())
                .plateId(submission.getPlateId())
                .qrId(submission.getQrId())
                .qrName(qrName)
                .groupId(submission.getGroupId())
                .appId(submission.getAppId())
                .pageId(submission.getPageId())
                .displayAnswers(displayAnswers)
                .approvalStatus(statusOf(submission.getApproval()))
                .createdBy(submission.getCreatedBy())
                .creator(submission.getCreator())
                .createdAt(submission.getCreatedAt())
                .referenceData(submission.getReferenceData())
                .build();
    }

    public QDetailedSubmission tryFetchMyLastSubmission(String qrId, String pageId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Submission:FetchMyLast", 50);

        return submissionRepository.lastMemberSubmission(user.getMemberId(), qrId, pageId)
                .map(submission -> toSubmissionDetail(submission, user))
                .orElse(null);
    }

    public Set<Answer> tryFetchSubmissionAnswersForAutoFill(String qrId, String pageId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Submission:FetchForAutoFill", 50);

        return submissionRepository.lastMemberSubmission(user.getMemberId(), qrId, pageId)
                .map(submission -> {
                    App app = appRepository.cachedById(submission.getAppId());
                    Set<String> autoFillControlIds = app.pageById(pageId)
                            .getControls().stream()
                            .filter(Control::isAutoFill)
                            .map(Control::getId).collect(toImmutableSet());

                    return submission.getAnswers().values()
                            .stream().filter(answer -> autoFillControlIds.contains(answer.getControlId()))
                            .collect(toImmutableSet());
                }).orElse(ImmutableSet.of());
    }

    public QDetailedSubmission tryFetchInstanceLastSubmission(String qrId, String pageId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Submission:FetchInstanceLast", 50);

        return submissionRepository.lastInstanceSubmission(qrId, pageId)
                .map(submission -> toSubmissionDetail(submission, user))
                .orElse(null);
    }

    private QDetailedSubmission toSubmissionDetail(Submission submission, User user) {
        App app = appRepository.cachedById(submission.getAppId());

        SubmissionPermissions submissionPermissions = submissionPermissionChecker.permissionsFor(user, app, submission.getGroupId());

        Page page = app.pageById(submission.getPageId());
        submissionPermissions.checkCanViewSubmission(submission, page, app);

        SubmissionPermissions.CheckResult updateCheckResult = submissionPermissions.canUpdateSubmission(submission, page, app);
        SubmissionPermissions.CheckResult approvalCheckResult = submissionPermissions.canApproveSubmission(submission, page, app);
        return toSubmissionDetail(submission, page, submissionPermissions, updateCheckResult.isSuccess(), approvalCheckResult.isSuccess());
    }

    private QDetailedSubmission toSubmissionDetail(Submission submission,
                                                   Page page,
                                                   SubmissionPermissions submissionPermissions,
                                                   boolean canUpdate,
                                                   boolean canApprove) {
        //根据control权限过滤answer
        Set<Answer> viewableAnswers = calculateViewableAnswers(submission, page, submissionPermissions);

        return QDetailedSubmission.builder()
                .id(submission.getId())
                .tenantId(submission.getTenantId())
                .qrId(submission.getQrId())
                .groupId(submission.getGroupId())
                .appId(submission.getAppId())
                .pageId(submission.getPageId())
                .answers(viewableAnswers)
                .approval(toQSubmissionApproval(submission.getApproval()))
                .createdAt(submission.getCreatedAt())
                .createdBy(submission.getCreatedBy())
                .creatorName(submission.getCreator())
                .canUpdate(canUpdate)
                .canApprove(canApprove)
                .referenceData(submission.getReferenceData())
                .build();
    }

    private Set<Answer> calculateViewableAnswers(Submission submission, Page page, SubmissionPermissions submissionPermissions) {
        Map<String, Control> controlMap = page.getControls().stream().collect(toImmutableMap(Control::getId, identity()));
        return submission.allAnswers().values().stream().filter(answer -> {
            Control control = controlMap.get(answer.getControlId());
            if (control == null) {
                return false;
            }

            if (submissionPermissions.hasManageQrPermission()) {//管理员任何时候可以看到
                return true;
            }

            if (submissionPermissions.hasPermission(control.requiredPermission())) {
                return true;
            }

            return submissionPermissions.isSubmittedByCurrentUser(submission) && control.isAnswerViewableBySubmitter();

        }).collect(toImmutableSet());
    }

    private QSubmissionApproval toQSubmissionApproval(SubmissionApproval approval) {
        if (approval == null) {
            return null;
        }

        return QSubmissionApproval.builder()
                .passed(approval.isPassed())
                .approvedAt(approval.getApprovedAt())
                .approvedBy(approval.getApprovedBy())
                .note(approval.getNote())
                .approverName(isNotBlank(approval.getApprovedBy()) ? memberRepository.cachedMemberNameOf(approval.getApprovedBy()) : null)
                .build();
    }

}
