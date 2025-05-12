package com.mryqr.core.platebatch.infrastructure;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.platebatch.domain.PlateBatch;
import com.mryqr.core.platebatch.domain.PlateBatchRepository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class MongoPlateBatchRepository extends MongoBaseRepository<PlateBatch> implements PlateBatchRepository {

    @Override
    public boolean existsByName(String name, String appId) {
        requireNonBlank(name, "Batch name must not be blank.");
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId).and("name").is(name));
        return mongoTemplate.exists(query, PlateBatch.class);
    }

    @Override
    public void save(PlateBatch it) {
        super.save(it);
    }

    @Override
    public void delete(PlateBatch it) {
        super.delete(it);
    }

    @Override
    public PlateBatch byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<PlateBatch> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public PlateBatch byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public int count(String tenantId) {
        return super.count(tenantId);
    }

    @Override
    public int deltaCountUsedPlatesForPlateBatch(String plateBatchId, int delta) {
        requireNonBlank(plateBatchId, "Plate batch ID must not be blank.");

        Query query = Query.query(where("_id").is(plateBatchId));
        Update update = new Update();
        update.inc("usedCount", delta);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, PlateBatch.class);
        return (int) updateResult.getModifiedCount();
    }

    @Override
    public int removeAllPlateBatchUnderApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId));
        DeleteResult result = mongoTemplate.remove(query, PlateBatch.class);
        return (int) result.getDeletedCount();
    }

}
