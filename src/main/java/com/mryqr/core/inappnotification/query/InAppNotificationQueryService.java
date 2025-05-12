package com.mryqr.core.inappnotification.query;

import com.mryqr.common.domain.AggregateRoot;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.Pagination;
import com.mryqr.core.inappnotification.domain.InAppNotification;
import com.mryqr.core.inappnotification.domain.QInAppNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.common.utils.MryConstants.IN_APP_NOTIFICATION_COLLECTION;
import static com.mryqr.common.utils.Pagination.pagination;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationQueryService {
    private final MongoTemplate mongoTemplate;
    private final MryRateLimiter mryRateLimiter;

    public PagedList<QInAppNotification> listMyInAppNotifications(ListInAppNotificationsQuery queryCommand,
                                                                  User user) {
        this.mryRateLimiter.applyFor(user.getTenantId(), "INA:ListMyNotifications", 20);

        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());
        if (pagination.getPageIndex() > 100) { // 太多时，不再显示老的通知
            return PagedList.<QInAppNotification>builder()
                    .totalNumber(-1)
                    .pageSize(pagination.getPageSize())
                    .pageIndex(pagination.getPageIndex())
                    .data(List.of())
                    .build();
        }

        Criteria criteria = where(InAppNotification.Fields.memberId).is(user.getMemberId());
        if (queryCommand.isUnViewedOnly()) {
            criteria = criteria.and(InAppNotification.Fields.viewed).is(false);
        }

        Query query = Query.query(criteria);
        query.skip(pagination.skip()).limit(pagination.limit()).with(by(DESC, AggregateRoot.Fields.createdAt));
        query.fields().include(InAppNotification.Fields.memberId,
                InAppNotification.Fields.viewed,
                InAppNotification.Fields.pcUrl,
                InAppNotification.Fields.mobileUrl,
                InAppNotification.Fields.content,
                AggregateRoot.Fields.createdAt);
        List<QInAppNotification> notifications = this.mongoTemplate.find(query, QInAppNotification.class, IN_APP_NOTIFICATION_COLLECTION);

        return PagedList.<QInAppNotification>builder()
                .totalNumber(-1)
                .pageSize(pagination.getPageSize())
                .pageIndex(pagination.getPageIndex())
                .data(notifications)
                .build();
    }

    public long countMyUnViewedInAppNotifications(User user) {
        this.mryRateLimiter.applyFor(user.getTenantId(), "INA:CountUnViewed", 20);

        Query query = Query.query(where(InAppNotification.Fields.memberId).is(user.getMemberId())
                .and(InAppNotification.Fields.viewed).is(false));

        return this.mongoTemplate.count(query, InAppNotification.class);
    }
}
