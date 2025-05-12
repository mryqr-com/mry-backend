package com.mryqr.core.group.query;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.permission.ManagePermissionChecker;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.common.utils.Pagination;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.common.utils.CommonUtils.splitSearchBySpace;
import static com.mryqr.core.common.utils.MryConstants.QR_COLLECTION;
import static com.mryqr.core.common.utils.Pagination.pagination;
import static com.mryqr.core.common.validation.id.plate.PlateIdValidator.isPlateId;
import static com.mryqr.core.common.validation.id.qr.QrIdValidator.isQrId;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupQueryService {
    private final MongoTemplate mongoTemplate;
    private final GroupRepository groupRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final AppRepository appRepository;
    private final MryRateLimiter mryRateLimiter;

    public QGroupMembers listGroupMembers(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:FetchAllMembers", 20);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        return QGroupMembers.builder()
                .memberIds(group.getMembers())
                .managerIds(group.getManagers())
                .build();
    }

    public PagedList<QGroupQr> listGroupQrs(String groupId, ListGroupQrsQuery queryCommand, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:FetchQrs", 5);

        Group group = groupRepository.cachedByIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());
        Query query = query(listGroupQrsCriteria(groupId, queryCommand));

        long count = mongoTemplate.count(query, QR_COLLECTION);
        if (count == 0) {
            return pagedQrList(pagination, 0, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sort(queryCommand));
        query.fields().include("plateId").include("name").include("groupId")
                .include("createdAt").include("headerImage").include("active");

        List<QGroupQr> qrs = mongoTemplate.find(query, QGroupQr.class, QR_COLLECTION);
        return pagedQrList(pagination, (int) count, qrs);
    }

    private Criteria listGroupQrsCriteria(String groupId, ListGroupQrsQuery queryCommand) {
        //业务上只返回本group下的qr，而无需返回子group的qr
        Criteria criteria = where("groupId").is(groupId);

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

    private Sort sort(ListGroupQrsQuery queryCommand) {
        String sortedBy = queryCommand.getSortedBy();
        Sort.Direction direction = queryCommand.isAscSort() ? ASC : DESC;

        if (isBlank(sortedBy)) {
            return by(DESC, "createdAt");
        }

        if ("createdAt".equals(sortedBy)) {
            return by(direction, "createdAt");
        }

        if ("name".equals(sortedBy)) {
            return by(direction, "name");
        }

        if ("customId".equals(sortedBy)) {
            return by(direction, "customId");
        }

        return by(DESC, "createdAt");
    }

    private PagedList<QGroupQr> pagedQrList(Pagination pagination, int count, List<QGroupQr> qrs) {
        return PagedList.<QGroupQr>builder()
                .totalNumber(count)
                .pageSize(pagination.getPageSize())
                .pageIndex(pagination.getPageIndex())
                .data(qrs)
                .build();
    }
}
