package com.mryqr.core.assignment.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.assignment.domain.Assignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

import static com.mryqr.core.assignment.domain.AssignmentStatus.IN_PROGRESS;
import static com.mryqr.core.assignment.domain.AssignmentStatus.NEAR_EXPIRE;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MryConstants.OPEN_ASSIGNMENT_PAGES_CACHE;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoCachedAssignmentRepository extends MongoBaseRepository<Assignment> {

    @Cacheable(value = OPEN_ASSIGNMENT_PAGES_CACHE, key = "#appId")
    public ArrayList<String> cachedOpenAssignmentPages(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = Query.query(where("status").in(IN_PROGRESS, NEAR_EXPIRE).and("appId").is(appId));
        query.fields().include("pageId");

        return new ArrayList<>(mongoTemplate.findDistinct(query, "pageId", Assignment.class, String.class));
    }

    @Caching(evict = {@CacheEvict(value = OPEN_ASSIGNMENT_PAGES_CACHE, key = "#appId")})
    public void evictOpenAssignmentPagesCache(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        log.info("Evicted all open assignment pages cache for app[{}].", appId);
    }
}
