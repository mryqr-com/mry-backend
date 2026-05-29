package com.mryqr.core.qr.query.submission.list;

import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.permission.SubmissionPermissionChecker;
import com.mryqr.common.domain.permission.SubmissionPermissions;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.Pagination;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.operationmenu.SubmissionListType;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.qr.domain.AppedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.query.list.QListSubmission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.utils.CommonUtils.splitSearchBySpace;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoTextFieldOf;
import static com.mryqr.common.utils.MryConstants.SUBMISSION_COLLECTION;
import static com.mryqr.common.utils.Pagination.pagination;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.SUBMITTER_SUBMISSION;
import static com.mryqr.core.submission.domain.ApprovalStatus.statusOf;
import static java.time.LocalDate.parse;
import static java.time.ZoneId.systemDefault;
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
public class QrSubmissionQueryService {
    private static final String APPROVAL = "approval";
    private final QrRepository qrRepository;
    private final SubmissionPermissionChecker submissionPermissionChecker;
    private final MongoTemplate mongoTemplate;
    private final MryRateLimiter mryRateLimiter;
    private final MemberRepository memberRepository;

    public PagedList<QListSubmission> listQrSubmissions(String qrId, ListQrSubmissionsQuery queryCommand, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:ListSubmission", 50);

        AppedQr appedQr = qrRepository.appedQrByIdAndCheckTenantShip(qrId, user);
        QR qr = appedQr.getQr();
        App app = appedQr.getApp();
        String pageId = queryCommand.getPageId();

        SubmissionPermissions submissionPermissions = submissionPermissionChecker.permissionsFor(user, appedQr);

        SubmissionListType type = queryCommand.getType();
        Criteria baseCriteria = baseCriteria(qrId,
                user,
                type,
                pageId,
                queryCommand.getCreatedBy(),
                queryCommand.getStartDate(),
                queryCommand.getEndDate(),
                submissionPermissions);

        List<Control> allControls = app.allControls();
        Set<String> permissionedControlIds = permissionedControlIds(submissionPermissions, allControls, type);
        Criteria filterableCriteria = appendFilterableCriteria(baseCriteria,
                app,
                permissionedControlIds,
                queryCommand.getFilterables(),
                pageId);

        Criteria searchableCriteria = appendSearchableCriteria(filterableCriteria, queryCommand.getSearch());
        Query query = query(searchableCriteria);
        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());
        long count = mongoTemplate.count(query, SUBMISSION_COLLECTION);
        if (count == 0) {
            return pagedList(pagination, 0, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit())
                .with(sort(queryCommand.getSortedBy(), queryCommand.isAscSort(), app, pageId, permissionedControlIds));

        List<Submission> rawSubmissions = listRawSubmissions(query);
        Map<String, Control> controlMap = allControls.stream().collect(toImmutableMap(Control::getId, identity()));

        rawSubmissions.forEach(submission -> submission.getAnswers().entrySet().removeIf(entry -> {
            Control control = controlMap.get(entry.getKey());
            return control == null ||
                   !control.isSubmissionSummaryEligible() ||
                   !permissionedControlIds.contains(control.getId());
        }));

        SubmissionReferenceContext referenceContext = buildReferenceContext(rawSubmissions, app);
        List<QListSubmission> submissionList = rawSubmissions.stream().map(submission -> QListSubmission.builder()
                .id(submission.getId())
                .plateId(submission.getPlateId())
                .qrId(submission.getQrId())
                .qrName(qr.getName())
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
        return pagedList(pagination, (int) count, submissionList);
    }

    private PagedList<QListSubmission> pagedList(Pagination pagination, int count, List<QListSubmission> submissions) {
        return PagedList.<QListSubmission>builder()
                .totalNumber(count)
                .pageSize(pagination.getPageSize())
                .pageIndex(pagination.getPageIndex())
                .data(submissions)
                .build();
    }

    private Criteria baseCriteria(String qrId,
                                  User user,
                                  SubmissionListType type,
                                  String pageId,
                                  String createdBy,
                                  String startDate,
                                  String endDate,
                                  SubmissionPermissions submissionPermissions) {
        Criteria criteria = where("qrId").is(qrId);
        switch (type) {
            case ALL_SUBMIT_HISTORY -> {
                if (isBlank(pageId)) {
                    submissionPermissions.checkHasManagablePages();
                    criteria.and("pageId").in(submissionPermissions.getCanManageFillablePageIds());
                } else {
                    submissionPermissions.checkManagablePagePermission(pageId);
                    criteria.and("pageId").is(pageId);
                }

                if (isNotBlank(createdBy)) {
                    criteria.and("createdBy").is(createdBy);
                }
            }
            case SUBMITTER_SUBMISSION -> {
                if (isBlank(pageId)) {
                    submissionPermissions.checkHasViewablePages();
                    criteria.and("pageId").in(submissionPermissions.getCanViewFillablePageIds());
                } else {
                    submissionPermissions.checkViewablePagePermission(pageId);
                    criteria.and("pageId").is(pageId);
                }
                if (isNotBlank(user.getMemberId())) {
                    criteria.and("createdBy").is(user.getMemberId());
                }
            }
            case TO_BE_APPROVED -> {
                criteria.and("approval").is(null);

                if (isBlank(pageId)) {
                    submissionPermissions.checkHasApprovablePages();
                    criteria.and("pageId").in(submissionPermissions.getCanApproveFillablePageIds());
                } else {
                    submissionPermissions.checkApprovablePagePermission(pageId);
                    criteria.and("pageId").is(pageId);
                }
                if (isNotBlank(createdBy)) {
                    criteria.and("createdBy").is(createdBy);
                }
            }
        }

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

    private Set<String> permissionedControlIds(SubmissionPermissions submissionPermissions,
                                               List<Control> allControls,
                                               SubmissionListType type) {
        return allControls.stream().filter(control -> {
            if (submissionPermissions.hasManageQrPermission()) {
                return true;
            }

            if (type == SUBMITTER_SUBMISSION && control.isPermissionEnabled()) {
                return control.isAnswerViewableBySubmitter();
            }

            return true;
        }).map(Control::getId).collect(toImmutableSet());
    }

    private Criteria appendFilterableCriteria(Criteria criteria,
                                              App app,
                                              Set<String> permissionedControlIds,
                                              Map<String, Set<String>> filterables,
                                              String pageId) {
        if (isEmpty(filterables) || isBlank(pageId)) {
            return criteria;
        }

        List<Criteria> criteriaList = buildIndexedCriteria(app, permissionedControlIds, filterables, pageId);
        if (criteriaList.size() > 0) {
            criteria.andOperator(criteriaList.toArray(Criteria[]::new));
        }

        //审批过滤
        Set<String> approvalFilterables = filterables.get(APPROVAL);
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

    private List<Criteria> buildIndexedCriteria(App app,
                                                Set<String> permissionedControlIds,
                                                Map<String, Set<String>> allFilterables,
                                                String pageId) {
        return allFilterables.entrySet().stream()
                .filter(this::isIndexedEntry)
                .filter(entry -> permissionedControlIds.contains(entry.getKey()))
                .map(entry -> app.indexedFieldForControlOptional(pageId, entry.getKey())
                        .map(indexedField -> where(mongoTextFieldOf(indexedField)).in(entry.getValue())).orElse(null))
                .filter(Objects::nonNull)
                .collect(toImmutableList());
    }

    private boolean isIndexedEntry(Map.Entry<String, Set<String>> entry) {
        return !APPROVAL.equals(entry.getKey()) &&
               isNotEmpty(entry.getValue());
    }

    private Criteria appendSearchableCriteria(Criteria criteria, String search) {
        if (isBlank(search)) {
            return criteria;
        }

        return criteria.and("svs").all((Object[]) splitSearchBySpace(search));
    }

    private Sort sort(String sortedBy,
                      boolean ascSort,
                      App app,
                      String pageId,
                      Set<String> permissionedControlIds) {
        if (isBlank(sortedBy)) {
            return by(DESC, "createdAt");
        }

        Sort.Direction direction = ascSort ? ASC : DESC;

        if ("createdAt".equals(sortedBy)) {
            return by(direction, "createdAt");
        }

        if (isBlank(pageId)) {
            return unsorted();
        }

        if (!permissionedControlIds.contains(sortedBy)) {
            return unsorted();
        }

        return app.indexedFieldForControlOptional(pageId, sortedBy)
                .map(indexedField -> by(direction, mongoSortableFieldOf(indexedField)).and(by(DESC, "createdAt")))
                .orElse(unsorted());
    }

    private List<Submission> listRawSubmissions(Query query) {
        query.fields().include("plateId").include("qrId").include("appId").include("groupId").include("pageId")
                .include("answers").include("approval").include("createdBy").include("createdAt").include("referenceData")
                .include("creator");
        return mongoTemplate.find(query, Submission.class);
    }

    private SubmissionReferenceContext buildReferenceContext(List<Submission> submissions, App app) {
        Set<String> referencedMemberIds = submissions.stream()
                .flatMap(submission -> submission.getAnswers().values().stream()
                        .filter(answer -> answer instanceof MemberAware)
                        .map(answer -> ((MemberAware) answer).awaredMemberIds())
                        .flatMap(Collection::stream))
                .collect(toImmutableSet());

        Map<String, MemberReference> memberReferences = memberRepository.cachedMemberReferences(app.getTenantId(), referencedMemberIds);
        return SubmissionReferenceContext.builder().memberReferences(memberReferences).build();
    }

}
