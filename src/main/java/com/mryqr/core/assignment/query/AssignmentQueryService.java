package com.mryqr.core.assignment.query;

import com.mryqr.common.domain.Geolocation;
import com.mryqr.common.domain.Geopoint;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.permission.AppOperatePermissionChecker;
import com.mryqr.common.domain.permission.AppOperatePermissions;
import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.Pagination;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.domain.AssignmentFinishedQr;
import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.assignment.domain.AssignmentStatus;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.ACCESS_DENIED;
import static com.mryqr.common.exception.ErrorCode.REQUEST_VALIDATION_FAILED;
import static com.mryqr.common.utils.MongoCriteriaUtils.regexSearch;
import static com.mryqr.common.utils.MryConstants.ASSIGNMENT_COLLECTION;
import static com.mryqr.common.utils.MryConstants.QR_COLLECTION;
import static com.mryqr.common.utils.Pagination.pagination;
import static com.mryqr.common.validation.id.plate.PlateIdValidator.isPlateId;
import static java.util.Set.copyOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.domain.Sort.unsorted;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentQueryService {
    private final MryRateLimiter mryRateLimiter;
    private final MongoTemplate mongoTemplate;
    private final AppRepository appRepository;
    private final AppOperatePermissionChecker appOperatePermissionChecker;
    private final MemberRepository memberRepository;
    private final AssignmentRepository assignmentRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;

    public PagedList<QListAssignment> listMyManagedAssignments(ListMyManagedAssignmentsQuery queryCommand, User user) {
        String tenantId = user.getTenantId();
        mryRateLimiter.applyFor(tenantId, "Assignment:ListManaged", 20);

        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());
        String appId = queryCommand.getAppId();
        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        AppOperatePermissions operatePermissions = appOperatePermissionChecker.permissionsFor(user, app);

        Criteria criteria = where("appId").is(appId);
        String groupId = queryCommand.getGroupId();
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

        String assignmentPlanId = queryCommand.getAssignmentPlanId();
        if (isNotBlank(assignmentPlanId)) {
            criteria.and("assignmentPlanId").is(assignmentPlanId);
        }

        AssignmentStatus status = queryCommand.getStatus();
        if (status != null) {
            criteria.and("status").is(status);
        }

        String operatorId = queryCommand.getOperatorId();
        if (isNotBlank(operatorId)) {
            criteria.and("operators").is(operatorId);
        }

        return doQueryAssignments(criteria, pagination, queryCommand.getSortedBy(), queryCommand.isAscSort(), tenantId);
    }

    public PagedList<QListAssignment> listMyAssignments(ListMyAssignmentsQuery queryCommand, User user) {
        String tenantId = user.getTenantId();
        mryRateLimiter.applyFor(tenantId, "Assignment:ListMine", 20);

        App app = appRepository.cachedByIdAndCheckTenantShip(queryCommand.getAppId(), user);
        AppOperatePermissions operatePermissions = appOperatePermissionChecker.permissionsFor(user, app);
        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());
        Criteria criteria = where("appId").is(queryCommand.getAppId()).and("operators").is(user.getMemberId());
        String groupId = queryCommand.getGroupId();
        if (isNotBlank(groupId)) {
            operatePermissions.checkViewableGroupPermission(groupId);
            GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(queryCommand.getAppId());
            Set<String> withAllSubGroupIds = groupHierarchy.withAllSubGroupIdsOf(groupId);
            Set<String> viewableGroupIds = operatePermissions.getViewableGroupIds();
            Set<String> resultViewableGroupIds = withAllSubGroupIds.stream().filter(viewableGroupIds::contains).collect(toImmutableSet());
            criteria.and("groupId").in(resultViewableGroupIds);
        }

        AssignmentStatus status = queryCommand.getStatus();
        if (status != null) {
            criteria.and("status").is(status);
        }

        return doQueryAssignments(criteria, pagination, queryCommand.getSortedBy(), queryCommand.isAscSort(), tenantId);
    }

    private PagedList<QListAssignment> doQueryAssignments(Criteria criteria,
                                                          Pagination pagination,
                                                          String sortedBy,
                                                          boolean ascSort,
                                                          String tenantId) {
        Query query = Query.query(criteria);
        long count = mongoTemplate.count(query, ASSIGNMENT_COLLECTION);
        if (count == 0) {
            return pagedAssignmentList(pagination, 0, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sortAssignment(sortedBy, ascSort));
        query.fields().include("assignmentPlanId", "name", "groupId", "startAt", "expireAt",
                "allQrCount", "finishedQrCount", "operators", "status", "createdAt");

        List<RawAssignment> rawAssignments = mongoTemplate.find(query, RawAssignment.class, ASSIGNMENT_COLLECTION);
        return pagedAssignmentList(pagination, (int) count, transform(rawAssignments, tenantId));
    }

    private Sort sortAssignment(String sortedBy, boolean ascSort) {
        Sort.Direction direction = ascSort ? ASC : DESC;

        if ("startAt".equals(sortedBy) || "expireAt".equals(sortedBy)) {
            return by(direction, sortedBy);
        }

        return by(DESC, "startAt");
    }

    private List<QListAssignment> transform(List<RawAssignment> rawAssignments, String tenantId) {
        Set<String> memberIds = rawAssignments.stream()
                .flatMap(assignment -> assignment.getOperators().stream())
                .collect(toImmutableSet());

        Map<String, MemberReference> memberReferences = memberRepository.cachedMemberReferences(tenantId, memberIds);

        return rawAssignments.stream().map(assignment -> {
            List<MemberReference> references = assignment.getOperators().stream()
                    .map(memberReferences::get)
                    .filter(Objects::nonNull)
                    .collect(toImmutableList());

            return QListAssignment.builder()
                    .id(assignment.getId())
                    .assignmentPlanId(assignment.getAssignmentPlanId())
                    .name(assignment.getName())
                    .groupId(assignment.getGroupId())
                    .startAt(assignment.getStartAt())
                    .expireAt(assignment.getExpireAt())
                    .operators(references.stream().map(MemberReference::getId).collect(toImmutableList()))
                    .operatorNames(references.stream().map(MemberReference::getName).collect(toImmutableList()))
                    .status(assignment.getStatus())
                    .createdAt(assignment.getCreatedAt())
                    .allQrCount(assignment.getAllQrCount())
                    .finishedQrCount(assignment.getFinishedQrCount())
                    .build();
        }).collect(toImmutableList());
    }

    private PagedList<QListAssignment> pagedAssignmentList(Pagination pagination, int count, List<QListAssignment> assignments) {
        return PagedList.<QListAssignment>builder()
                .totalNumber(count)
                .pageSize(pagination.getPageSize())
                .pageIndex(pagination.getPageIndex())
                .data(assignments)
                .build();
    }

    public PagedList<QAssignmentListQr> listAssignmentQrs(String assignmentId, ListAssignmentQrsQuery queryCommand, User user) {
        String tenantId = user.getTenantId();
        mryRateLimiter.applyFor(tenantId, "Assignment:ListQrs", 20);

        Assignment assignment = assignmentRepository.byIdAndCheckTenantShip(assignmentId, user);
        if (assignment.getOperators().contains(user.getMemberId())) {
            return doQueryAssignmentQrs(queryCommand, assignment, tenantId);
        }

        Group group = groupRepository.cachedById(assignment.getGroupId());
        if (managePermissionChecker.canManageGroup(user, group)) {
            return doQueryAssignmentQrs(queryCommand, assignment, tenantId);
        }

        throw new MryException(ACCESS_DENIED, "无权访问，您不是任务执行人或者管理员。", "assignmentId", assignmentId);
    }

    private PagedList<QAssignmentListQr> doQueryAssignmentQrs(ListAssignmentQrsQuery queryCommand,
                                                              Assignment assignment,
                                                              String tenantId) {
        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());
        Set<String> eligibleQrIds = calculateEligibleQrIds(assignment, queryCommand.getFinished());
        Criteria criteria = where("_id").in(eligibleQrIds);
        appendSearchableCriteria(criteria, queryCommand.getSearch());

        if (shouldGeoSearch(queryCommand)) {
            appendGeoSearchCriteria(criteria, queryCommand.getCurrentPoint());
        }

        Query query = Query.query(criteria);
        long count = mongoTemplate.count(query, QR_COLLECTION);
        if (count == 0) {
            return pagedAssignmentQrList(pagination, 0, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sortAssignmentQr(queryCommand));
        query.fields().include("name", "plateId", "headerImage", "geolocation");
        List<RawQr> rawQrs = mongoTemplate.find(query, RawQr.class, QR_COLLECTION);

        Map<String, AssignmentFinishedQr> finishedQrs = assignment.getFinishedQrs();
        Set<String> allOperatorIds = rawQrs.stream().map(rawQr -> {
                    AssignmentFinishedQr finishedQr = finishedQrs.get(rawQr.getId());
                    return finishedQr != null ? finishedQr.getOperatorId() : null;
                }).filter(Objects::nonNull)
                .collect(toImmutableSet());

        Map<String, MemberReference> memberReferences = memberRepository.cachedMemberReferences(tenantId, allOperatorIds);

        List<QAssignmentListQr> qrs = rawQrs.stream().map(rawQr -> {
            AssignmentFinishedQr finishedQr = finishedQrs.get(rawQr.getId());
            if (finishedQr == null) {
                return QAssignmentListQr.builder()
                        .id(rawQr.getId())
                        .name(rawQr.getName())
                        .plateId(rawQr.getPlateId())
                        .headerImage(rawQr.getHeaderImage())
                        .geolocation(rawQr.getGeolocation())
                        .build();
            } else {
                MemberReference member = memberReferences.get(finishedQr.getOperatorId());
                String operatorName = member != null ? member.getName() : null;
                return QAssignmentListQr.builder()
                        .id(rawQr.getId())
                        .name(rawQr.getName())
                        .plateId(rawQr.getPlateId())
                        .headerImage(rawQr.getHeaderImage())
                        .geolocation(rawQr.getGeolocation())
                        .finished(true)
                        .submissionId(finishedQr.getSubmissionId())
                        .operatorId(finishedQr.getOperatorId())
                        .operatorName(operatorName)
                        .finishedAt(finishedQr.getFinishedAt())
                        .build();
            }
        }).collect(toImmutableList());

        return pagedAssignmentQrList(pagination, (int) count, qrs);
    }


    private void appendGeoSearchCriteria(Criteria criteria, Geopoint currentPoint) {
        criteria.and("geolocation.point").nearSphere(new Point(currentPoint.getLongitude(), currentPoint.getLatitude()));
    }

    private Set<String> calculateEligibleQrIds(Assignment assignment, Boolean finished) {
        if (finished == null) {
            return assignment.getAllQrIds();
        }

        if (finished) {
            return assignment.getFinishedQrs().keySet();
        }

        Set<String> finishedQrIds = assignment.getFinishedQrs().keySet();
        return assignment.getAllQrIds().stream().filter(id -> !finishedQrIds.contains(id)).collect(toImmutableSet());
    }

    private void appendSearchableCriteria(Criteria criteria, String search) {
        if (isBlank(search)) {
            return;
        }

        if (isPlateId(search)) {
            criteria.and("plateId").is(search);
            return;
        }

        criteria.orOperator(regexSearch("name", search), where("customId").is(search));
    }

    private boolean shouldGeoSearch(ListAssignmentQrsQuery queryCommand) {//sort和geo最近查询是互斥的，sortBy具有优先级
        Geopoint currentPoint = queryCommand.getCurrentPoint();
        return queryCommand.isNearestPointEnabled() &&
               currentPoint != null &&
               currentPoint.isPositioned() &&
               isBlank(queryCommand.getSortedBy());
    }

    private Sort sortAssignmentQr(ListAssignmentQrsQuery queryCommand) {
        if (shouldGeoSearch(queryCommand)) {
            return unsorted();
        }

        Sort.Direction direction = queryCommand.isAscSort() ? ASC : DESC;
        if ("name".equals(queryCommand.getSortedBy())) {
            return Sort.by(direction, "name");
        }

        if ("createdAt".equals(queryCommand.getSortedBy())) {
            return Sort.by(direction, "createdAt");
        }

        return Sort.by(DESC, "createdAt");
    }

    private PagedList<QAssignmentListQr> pagedAssignmentQrList(Pagination pagination,
                                                               int count,
                                                               List<QAssignmentListQr> data) {
        return PagedList.<QAssignmentListQr>builder()
                .totalNumber(count)
                .pageSize(pagination.getPageSize())
                .pageIndex(pagination.getPageIndex())
                .data(data)
                .build();
    }

    public QAssignmentDetail fetchAssignmentDetail(String assignmentId, User user) {
        String tenantId = user.getTenantId();
        mryRateLimiter.applyFor(tenantId, "Assignment:FetchDetail", 20);

        Assignment assignment = assignmentRepository.byIdAndCheckTenantShip(assignmentId, user);
        if (assignment.getOperators().contains(user.getMemberId())) {
            return toAssignmentDetail(tenantId, assignment);
        }

        Group group = groupRepository.cachedById(assignment.getGroupId());
        if (managePermissionChecker.canManageGroup(user, group)) {
            return toAssignmentDetail(tenantId, assignment);
        }

        throw new MryException(ACCESS_DENIED, "无权访问，您不是任务执行人或者管理员。", "assignmentId", assignmentId);
    }

    private QAssignmentDetail toAssignmentDetail(String tenantId, Assignment assignment) {
        List<String> operatorNames = memberRepository.cachedMemberReferences(tenantId, copyOf(assignment.getOperators()))
                .values().stream()
                .map(MemberReference::getName)
                .collect(toImmutableList());

        return QAssignmentDetail.builder()
                .id(assignment.getId())
                .name(assignment.getName())
                .operatorNames(operatorNames)
                .groupId(assignment.getGroupId())
                .pageId(assignment.getPageId())
                .startAt(assignment.getStartAt())
                .expireAt(assignment.getExpireAt())
                .allQrCount(assignment.getAllQrCount())
                .finishedQrCount(assignment.getFinishedQrCount())
                .status(assignment.getStatus())
                .build();
    }

    public QAssignmentQrDetail fetchAssignmentQrDetail(String assignmentId, String qrId, User user) {
        String tenantId = user.getTenantId();
        mryRateLimiter.applyFor(tenantId, "Assignment:FetchQrDetail", 20);

        Assignment assignment = assignmentRepository.byIdAndCheckTenantShip(assignmentId, user);

        if (assignment.getOperators().contains(user.getMemberId())) {
            return toAssignmentQrDetail(assignment, qrId);
        }

        Group group = groupRepository.cachedById(assignment.getGroupId());
        if (managePermissionChecker.canManageGroup(user, group)) {
            return toAssignmentQrDetail(assignment, qrId);
        }

        throw new MryException(ACCESS_DENIED, "无权访问，您不是任务执行人或者管理员。", "assignmentId", assignmentId);
    }

    private QAssignmentQrDetail toAssignmentQrDetail(Assignment assignment, String qrId) {
        if (!assignment.getAllQrIds().contains(qrId)) {
            throw new MryException(REQUEST_VALIDATION_FAILED, "实例不存在于任务中。", "assignmentId", assignment.getId(), "qrId", qrId);
        }

        AssignmentFinishedQr finishedQr = assignment.getFinishedQrs().get(qrId);
        if (finishedQr != null) {
            String operatorName = isNotBlank(finishedQr.getOperatorId()) ? memberRepository.cachedMemberNameOf(finishedQr.getOperatorId()) : null;
            return QAssignmentQrDetail.builder()
                    .assignmentId(assignment.getId())
                    .status(assignment.getStatus())
                    .allQrCount(assignment.getAllQrCount())
                    .finishedQrCount(assignment.getFinishedQrCount())
                    .qrId(qrId)
                    .finished(true)
                    .submissionId(finishedQr.getSubmissionId())
                    .operatorId(finishedQr.getOperatorId())
                    .operatorName(operatorName)
                    .finishedAt(finishedQr.getFinishedAt())
                    .build();
        } else {
            return QAssignmentQrDetail.builder()
                    .assignmentId(assignment.getId())
                    .status(assignment.getStatus())
                    .allQrCount(assignment.getAllQrCount())
                    .finishedQrCount(assignment.getFinishedQrCount())
                    .qrId(qrId)
                    .finished(false)
                    .build();
        }
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    private static class RawAssignment {
        private final String id;
        private final String assignmentPlanId;
        private final String name;
        private final String groupId;
        private final Instant startAt;
        private final Instant expireAt;
        private List<String> operators;
        private final AssignmentStatus status;
        private final Instant createdAt;
        private final int allQrCount;
        private final int finishedQrCount;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    private static class RawQr {
        private final String id;
        private final String name;
        private final String plateId;
        private final UploadedFile headerImage;
        private final Geolocation geolocation;
    }
}
