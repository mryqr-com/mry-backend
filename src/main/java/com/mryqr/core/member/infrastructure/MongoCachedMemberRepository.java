package com.mryqr.core.member.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.TenantCachedMember;
import com.mryqr.core.member.domain.TenantCachedMembers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MryConstants.*;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

//为了绕开Spring AOP必须从外部调用才生效的限制，否则方法可以直接放到MemberRepository中
//不要直接使用，而是使用MemberRepository中同名方法
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoCachedMemberRepository extends MongoBaseRepository<Member> {

    @Cacheable(value = MEMBER_CACHE, key = "#memberId")
    public Member cachedById(String memberId) {
        requireNonBlank(memberId, "Member ID must not be blank.");

        return super.byId(memberId);
    }

    @Cacheable(value = TENANT_MEMBERS_CACHE, key = "#tenantId")
    public TenantCachedMembers cachedTenantAllMembers(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId));
        query.fields().include("name", "role", "mobile", "email", "mobileWxOpenId", "customId", "departmentIds", "active");
        List<TenantCachedMember> tenantCachedMembers = mongoTemplate.find(query, TenantCachedMember.class, MEMBER_COLLECTION);
        return TenantCachedMembers.builder().members(emptyIfNull(tenantCachedMembers)).build();
    }

    @Caching(evict = {@CacheEvict(value = MEMBER_CACHE, key = "#memberId")})
    public void evictMemberCache(String memberId) {
        requireNonBlank(memberId, "Member ID must not be blank.");

        log.debug("Evicted cache for member[{}].", memberId);
    }

    @Caching(evict = {@CacheEvict(value = TENANT_MEMBERS_CACHE, key = "#tenantId")})
    public void evictTenantMembersCache(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        log.debug("Evicted all members cache for tenant[{}].", tenantId);
    }
}
