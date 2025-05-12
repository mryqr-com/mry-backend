package com.mryqr.core.assignment.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.assignment.domain.Assignment;
import com.mryqr.core.assignment.domain.OpenAssignmentPages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MryConstants.OPEN_ASSIGNMENT_PAGES_CACHE;
import static com.mryqr.core.assignment.domain.AssignmentStatus.IN_PROGRESS;
import static com.mryqr.core.assignment.domain.AssignmentStatus.NEAR_EXPIRE;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoCachedAssignmentRepository extends MongoBaseRepository<Assignment> {

    @Cacheable(value = OPEN_ASSIGNMENT_PAGES_CACHE, key = "#appId")
    public OpenAssignmentPages cachedOpenAssignmentPages(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = Query.query(where("status").in(IN_PROGRESS, NEAR_EXPIRE).and("appId").is(appId));
        query.fields().include("pageId");

        List<String> pageIds = mongoTemplate.findDistinct(query, "pageId", Assignment.class, String.class);
        return OpenAssignmentPages.builder().pageIds(emptyIfNull(pageIds)).build();
    }

    @Caching(evict = {@CacheEvict(value = OPEN_ASSIGNMENT_PAGES_CACHE, key = "#appId")})
    public void evictOpenAssignmentPagesCache(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        log.debug("Evicted all open assignment pages cache for app[{}].", appId);
    }
}
