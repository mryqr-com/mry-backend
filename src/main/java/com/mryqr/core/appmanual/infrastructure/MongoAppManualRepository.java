package com.mryqr.core.appmanual.infrastructure;

import com.mongodb.client.result.DeleteResult;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.appmanual.domain.AppManual;
import com.mryqr.core.appmanual.domain.AppManualRepository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static java.util.Optional.ofNullable;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class MongoAppManualRepository extends MongoBaseRepository<AppManual> implements AppManualRepository {

    @Override
    public Optional<AppManual> byAppIdOptional(String appId) {
        requireNonBlank(appId, "APP ID must not be blank.");

        Query query = Query.query(where("appId").is(appId));
        return ofNullable(mongoTemplate.findOne(query, AppManual.class));
    }

    @Override
    public void save(AppManual appManual) {
        super.save(appManual);
    }

    @Override
    public int removeAppManual(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId));
        DeleteResult result = mongoTemplate.remove(query, AppManual.class);
        return (int) result.getDeletedCount();
    }

}
