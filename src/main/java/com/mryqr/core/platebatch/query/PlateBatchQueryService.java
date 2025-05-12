package com.mryqr.core.platebatch.query;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.Pagination;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.mryqr.common.utils.MongoCriteriaUtils.regexSearch;
import static com.mryqr.common.utils.MryConstants.PLATE_BATCH_COLLECTION;
import static com.mryqr.common.utils.Pagination.pagination;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class PlateBatchQueryService {
    private final static Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "createdAt", "totalCount");

    private final AppRepository appRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final MongoTemplate mongoTemplate;
    private final MryRateLimiter mryRateLimiter;

    public PagedList<QManagedListPlateBatch> listManagedPlateBatches(ListMyManagedPlateBatchesQuery queryCommand, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "PlateBatch:List", 10);
        String appId = queryCommand.getAppId();
        String search = queryCommand.getSearch();
        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());

        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        Query query = query(where("appId").is(appId));
        if (isNotBlank(search)) {
            query.addCriteria(regexSearch("name", search));
        }

        long count = mongoTemplate.count(query, PLATE_BATCH_COLLECTION);
        if (count == 0) {
            return pagedList(pagination, 0, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sort(queryCommand));
        query.fields().include("name", "totalCount", "usedCount", "createdAt", "createdBy", "creator");
        List<QManagedListPlateBatch> items = mongoTemplate.find(query, QManagedListPlateBatch.class, PLATE_BATCH_COLLECTION);

        return pagedList(pagination, (int) count, items);
    }

    private PagedList<QManagedListPlateBatch> pagedList(Pagination pagination, int count, List<QManagedListPlateBatch> batches) {
        return PagedList.<QManagedListPlateBatch>builder()
                .totalNumber(count)
                .pageSize(pagination.getPageSize())
                .pageIndex(pagination.getPageIndex())
                .data(batches)
                .build();
    }

    private Sort sort(ListMyManagedPlateBatchesQuery queryCommand) {
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

}
