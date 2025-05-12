package com.mryqr.management.platform.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.management.platform.domain.Platform;
import com.mryqr.management.platform.domain.PlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import static com.mryqr.management.platform.domain.Platform.Fields.mobileAccessCount;
import static com.mryqr.management.platform.domain.Platform.Fields.nonMobileAccessCount;
import static com.mryqr.management.platform.domain.Platform.PLATFORM_ID;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
@RequiredArgsConstructor
public class MongoPlatformRepository extends MongoBaseRepository<Platform> implements PlatformRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public Platform getPlatform() {
        return super.byId(PLATFORM_ID);
    }

    @Override
    public boolean platformExists() {
        return super.byIdOptional(PLATFORM_ID).isPresent();
    }

    @Override
    public void increaseMobileAccessCount() {
        Query query = Query.query(where("_id").is(PLATFORM_ID));
        Update update = new Update();
        update.inc(mobileAccessCount, 1);
        mongoTemplate.updateFirst(query, update, Platform.class);
    }

    @Override
    public void increaseNonMobileAccessCount() {
        Query query = Query.query(where("_id").is(PLATFORM_ID));
        Update update = new Update();
        update.inc(nonMobileAccessCount, 1);
        mongoTemplate.updateFirst(query, update, Platform.class);
    }
}
