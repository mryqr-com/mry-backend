package com.mryqr.core.inappnotification.infrastructure;

import com.mongodb.client.result.DeleteResult;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.inappnotification.domain.InAppNotification;
import com.mryqr.core.inappnotification.domain.InAppNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.inappnotification.domain.InAppNotification.Fields.viewed;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoInAppNotificationRepository extends MongoBaseRepository<InAppNotification> implements InAppNotificationRepository {

    @Override
    public void markAsViewed(String id, String memberId) {
        Query query = Query.query(where("_id").is(id).and(InAppNotification.Fields.memberId).is(memberId));
        Update update = new Update();
        update.set(viewed, true);
        mongoTemplate.updateFirst(query, update, InAppNotification.class);
    }

    @Override
    public void markAllAsViewed(String memberId) {
        Query query = Query.query(where(InAppNotification.Fields.memberId).is(memberId).and(viewed).is(false));
        Update update = new Update();
        update.set(viewed, true);
        mongoTemplate.updateMulti(query, update, InAppNotification.class);
    }

    @Override
    public int removeAllForMember(String memberId) {
        requireNonBlank(memberId, "memberId must not be blank.");

        Query query = query(where(InAppNotification.Fields.memberId).is(memberId));
        DeleteResult result = mongoTemplate.remove(query, InAppNotification.class);
        return (int) result.getDeletedCount();
    }
}
