package com.mryqr.core.plate.query;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.platebatch.domain.PlateBatch;
import com.mryqr.core.platebatch.domain.PlateBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class PlateQueryService {
    private final MongoTemplate mongoTemplate;
    private final PlateBatchRepository plateBatchRepository;
    private final AppRepository appRepository;
    private final MryRateLimiter mryRateLimiter;
    private final ManagePermissionChecker managePermissionChecker;

    public List<String> allPlateIdsUnderPlateBatch(String plateBatchId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Plate:AllPlateIds", 5);

        PlateBatch plateBatch = plateBatchRepository.byIdAndCheckTenantShip(plateBatchId, user);
        App app = appRepository.cachedById(plateBatch.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        Query query = Query.query(where("batchId").is(plateBatchId));
        return mongoTemplate.findDistinct(query, "_id", Plate.class, String.class);
    }

    public List<String> unusedPlateIdsUnderPlateBatch(String plateBatchId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Plate:AllPlateIds", 5);

        PlateBatch plateBatch = plateBatchRepository.byIdAndCheckTenantShip(plateBatchId, user);
        App app = appRepository.cachedById(plateBatch.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        Query query = Query.query(where("batchId").is(plateBatchId).and("qrId").is(null));
        return mongoTemplate.findDistinct(query, "_id", Plate.class, String.class);
    }

}
