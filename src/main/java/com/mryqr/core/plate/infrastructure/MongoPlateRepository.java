package com.mryqr.core.plate.infrastructure;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static java.util.Optional.ofNullable;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class MongoPlateRepository extends MongoBaseRepository<Plate> implements PlateRepository {

    @Override
    public Optional<Plate> byQrIdOptional(String qrId) {
        requireNonBlank(qrId, "QR ID must not be blank.");

        Query query = Query.query(where("qrId").is(qrId));
        return ofNullable(mongoTemplate.findOne(query, Plate.class));
    }

    @Override
    public Set<String> allPlateBatchIdsReferencingGroup(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        Query query = Query.query(where("groupId").is(groupId));
        List<String> batchIds = mongoTemplate.findDistinct(query, "batchId", Plate.class, String.class);
        return batchIds.stream().filter(Objects::nonNull).collect(toImmutableSet());
    }

    @Override
    public List<String> allPlateIdsUnderPlateBatch(String plateBatchId) {
        requireNonBlank(plateBatchId, "Plate batch ID must not be blank.");

        Query query = Query.query(where("batchId").is(plateBatchId));
        return List.copyOf(mongoTemplate.findDistinct(query, "_id", Plate.class, String.class));
    }

    @Override
    public void save(Plate it) {
        super.save(it);
    }

    @Override
    public void insert(List<Plate> plates) {
        super.insert(plates);
    }

    @Override
    public Plate byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<Plate> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public Plate byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public int count(String tenantId) {
        return super.count(tenantId);
    }

    @Override
    public int countPlateUnderTenant(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId));
        return (int) mongoTemplate.count(query, Plate.class);
    }

    @Override
    public int removeAllPlatesUnderApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId));
        DeleteResult result = mongoTemplate.remove(query, Plate.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int unbindAllPlatesUnderGroup(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        Update update = new Update();
        update.unset("groupId").unset("qrId");
        UpdateResult result = mongoTemplate.updateMulti(query(where("groupId").is(groupId)), update, Plate.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int unsetAllPlatesFromPlateBatch(String plateBatchId) {
        requireNonBlank(plateBatchId, "Plate batch ID must not be blank.");

        Update update = new Update();
        update.unset("batchId");
        Query query = query(where("batchId").is(plateBatchId));
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, Plate.class);
        return (int) updateResult.getModifiedCount();
    }

    @Override
    public int countUsedPlatesForPlateBatch(String plateBatchId) {
        requireNonBlank(plateBatchId, "Plate batch ID must not be blank.");

        Query query = query(where("batchId").is(plateBatchId).and("qrId").ne(null));
        return (int) mongoTemplate.count(query, Plate.class);
    }

}
