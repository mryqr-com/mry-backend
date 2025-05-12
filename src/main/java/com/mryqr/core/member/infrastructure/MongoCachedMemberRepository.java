package com.mryqr.core.member.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.TenantCachedMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MryConstants.MEMBER_CACHE;
import static com.mryqr.core.common.utils.MryConstants.MEMBER_COLLECTION;
import static com.mryqr.core.common.utils.MryConstants.TENANT_MEMBERS_CACHE;
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

    //必须返回ArrayList而非List，否则缓存中由于没有ArrayList类型信息而失败
    @Cacheable(value = TENANT_MEMBERS_CACHE, key = "#tenantId")
    public ArrayList<TenantCachedMember> cachedTenantAllMembers(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId));
        query.fields().include("name", "role", "mobile", "email", "mobileWxOpenId", "customId", "departmentIds", "active");
        return new ArrayList<>(mongoTemplate.find(query, TenantCachedMember.class, MEMBER_COLLECTION));
    }

    @Caching(evict = {@CacheEvict(value = MEMBER_CACHE, key = "#memberId")})
    public void evictMemberCache(String memberId) {
        requireNonBlank(memberId, "Member ID must not be blank.");

        log.info("Evicted cache for member[{}].", memberId);
    }

    @Caching(evict = {@CacheEvict(value = TENANT_MEMBERS_CACHE, key = "#tenantId")})
    public void evictTenantMembersCache(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        log.info("Evicted all members cache for tenant[{}].", tenantId);
    }

}
