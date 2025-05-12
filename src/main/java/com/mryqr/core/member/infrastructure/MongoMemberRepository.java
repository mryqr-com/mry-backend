package com.mryqr.core.member.infrastructure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.member.domain.TenantCachedMember;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.platebatch.domain.PlateBatch;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.tenant.domain.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.AR_NOT_FOUND;
import static com.mryqr.common.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.MEMBER_COLLECTION;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoMemberRepository extends MongoBaseRepository<Member> implements MemberRepository {
    private final MongoCachedMemberRepository cachedMemberRepository;

    @Override
    public Set<String> allMemberIdsOf(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId));
        query.fields().include("_id");
        return mongoTemplate.findDistinct(query, "_id", MEMBER_COLLECTION, String.class).stream().collect(toImmutableSet());
    }

    @Override
    public boolean existsByMobile(String mobile) {
        requireNonBlank(mobile, "Mobile must not be blank.");

        Query query = query(where("mobile").is(mobile));
        return mongoTemplate.exists(query, Member.class);
    }

    @Override
    public boolean existsByEmail(String email) {
        requireNonBlank(email, "Email must not be blank.");

        Query query = query(where("email").is(email));
        return mongoTemplate.exists(query, Member.class);
    }

    @Override
    public boolean existsByMobileOrEmail(String mobileOrEmail) {
        requireNonBlank(mobileOrEmail, "Mobile or email must not be blank.");

        Criteria criteria = new Criteria();
        criteria.orOperator(where("mobile").is(mobileOrEmail), where("email").is(mobileOrEmail));
        return mongoTemplate.exists(new Query(criteria), Member.class);
    }

    @Override
    public boolean existsByWxUnionId(String wxUnionId) {
        requireNonBlank(wxUnionId, "WxUnionId must not be blank.");

        Query query = query(where("wxUnionId").is(wxUnionId));
        return mongoTemplate.exists(query, Member.class);
    }

    @Override
    public Optional<Member> byMobileOrEmailOptional(String mobileOrEmail) {
        requireNonBlank(mobileOrEmail, "Mobile or email must not be blank.");

        Criteria criteria = new Criteria();
        criteria.orOperator(where("mobile").is(mobileOrEmail), where("email").is(mobileOrEmail));
        return ofNullable(mongoTemplate.findOne(query(criteria), Member.class));
    }

    @Override
    public Optional<Member> byWxUnionIdOptional(String wxUnionId) {
        requireNonBlank(wxUnionId, "WxUnionId must not be blank.");

        Query query = query(where("wxUnionId").is(wxUnionId));
        return ofNullable(mongoTemplate.findOne(query, Member.class));
    }

    @Override
    public Member byCustomIdAndCheckTenantShip(String tenantId, String customId, User user) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");
        requireNonBlank(customId, "Custom ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId).and("customId").is(customId));
        Member member = mongoTemplate.findOne(query, Member.class);

        if (member == null) {
            throw new MryException(MEMBER_NOT_FOUND, "未找到成员。", mapOf("tenantId", tenantId, "customId", customId));
        }

        checkTenantShip(member, user);
        return member;
    }

    @Override
    public List<TenantCachedMember> cachedTenantAllMembers(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers();
    }

    @Override
    public boolean cachedNotAllMembersExist(List<String> memberIds, String tenantId) {
        requireNonNull(memberIds, "Member IDs must not be null");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        if (isEmpty(memberIds)) {
            return false;
        }

        Set<String> allMemberIds = cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers().stream()
                .map(TenantCachedMember::getId)
                .collect(toImmutableSet());
        return !allMemberIds.containsAll(memberIds);
    }

    @Override
    public Set<String> cachedMemberIdsOfDepartment(String tenantId, String departmentId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");
        requireNonBlank(departmentId, "Department ID must not be blank.");

        return cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers().stream()
                .filter(member -> member.getDepartmentIds().contains(departmentId))
                .map(TenantCachedMember::getId)
                .collect(toImmutableSet());
    }

    @Override
    public int cachedActiveTenantAdminCountFor(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return (int) cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers()
                .stream()
                .filter(member -> member.isTenantAdmin() && member.isActive())
                .count();
    }

    @Override
    public int cachedTenantAdminCountFor(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return (int) cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers()
                .stream().filter(TenantCachedMember::isTenantAdmin)
                .count();
    }

    @Override
    public String cachedMemberNameOf(String memberId) {
        requireNonBlank(memberId, "Member ID must not be blank.");

        Member member = cachedMemberRepository.cachedById(memberId);
        return member != null ? member.getName() : null;
    }

    @Override
    public Map<String, MemberReference> cachedMemberReferences(String tenantId, Set<String> memberIds) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        if (isEmpty(memberIds)) {
            return ImmutableMap.of();
        }

        return cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers().stream()
                .filter(member -> memberIds.contains(member.getId()))
                .map(toMemberReference())
                .collect(toImmutableMap(MemberReference::getId, identity()));
    }

    @Override
    public List<String> cachedMemberIdsForCustomIds(String tenantId, List<String> customIds) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        if (isEmpty(customIds)) {
            return ImmutableList.of();
        }

        return cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers().stream()
                .filter(member -> customIds.contains(member.getCustomId()))
                .map(TenantCachedMember::getId)
                .collect(toImmutableList());
    }

    @Override
    public boolean cachedExistsByCustomId(String customId, String tenantId) {
        requireNonBlank(customId, "Member custom ID must not be blank.");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers()
                .stream().anyMatch(member -> Objects.equals(member.getCustomId(), customId));
    }

    @Override
    public Map<String, String> cachedMobileWxOpenIdsOf(String tenantId, List<String> memberIds) {
        requireNonBlank(tenantId, "TenantId must not be blank.");
        requireNonNull(memberIds, "Member IDs must not be blank.");

        if (isEmpty(memberIds)) {
            return ImmutableMap.of();
        }

        return cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers().stream()
                .filter(member -> memberIds.contains(member.getId()))
                .filter(member -> isNotBlank(member.getMobileWxOpenId()))
                .collect(toImmutableMap(TenantCachedMember::getId, TenantCachedMember::getMobileWxOpenId));
    }

    @Override
    public Map<String, String> cachedEmailsOf(String tenantId, List<String> memberIds) {
        requireNonBlank(tenantId, "TenantId must not be blank.");
        requireNonNull(memberIds, "Member IDs must not be blank.");

        if (isEmpty(memberIds)) {
            return ImmutableMap.of();
        }
        return cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers().stream()
                .filter(member -> memberIds.contains(member.getId()))
                .filter(member -> isNotBlank(member.getEmail()))
                .collect(toImmutableMap(TenantCachedMember::getId, TenantCachedMember::getEmail));
    }

    @Override
    public List<TenantCachedMember> cachedAllActiveTenantAdmins(String tenantId) {
        requireNonBlank(tenantId, "TenantId must not be blank.");

        return cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers().stream()
                .filter(member -> member.isTenantAdmin() && member.isActive())
                .collect(toImmutableList());
    }

    @Override
    public List<MemberReference> cachedAllMemberReferences(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers().stream()
                .map(toMemberReference())
                .collect(toImmutableList());
    }

    @Override
    public Member cachedById(String memberId) {
        requireNonBlank(memberId, "Member ID must not be blank.");

        return cachedMemberRepository.cachedById(memberId);
    }

    @Override
    public Member cachedByIdAndCheckTenantShip(String memberId, User user) {
        requireNonBlank(memberId, "Member ID must not be blank.");

        Member member = cachedMemberRepository.cachedById(memberId);
        checkTenantShip(member, user);
        return member;
    }

    @Override
    public Optional<Member> cachedByIdOptional(String memberId) {
        requireNonBlank(memberId, "Member ID must not be blank.");

        try {
            Member member = cachedMemberRepository.cachedById(memberId);
            return Optional.ofNullable(member);
        } catch (MryException ex) {
            if (ex.getCode() == AR_NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    @Override
    public List<TenantCachedMember> cachedByIds(Set<String> memberIds, String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");
        requireNonNull(memberIds, "Member IDs must not be null.");

        if (isEmpty(memberIds)) {
            return List.of();
        }

        return cachedMemberRepository.cachedTenantAllMembers(tenantId).getMembers().stream()
                .filter(member -> memberIds.contains(member.getId())).collect(toImmutableList());
    }

    @Override
    public Member byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<Member> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public Member byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }

    @Override
    public void save(Member member) {
        super.save(member);
        cachedMemberRepository.evictMemberCache(member.getId());
        cachedMemberRepository.evictTenantMembersCache(member.getTenantId());
    }

    @Override
    public void delete(Member member) {
        super.delete(member);
        cachedMemberRepository.evictMemberCache(member.getId());
        cachedMemberRepository.evictTenantMembersCache(member.getTenantId());
    }

    @Override
    public void evictMemberCache(String memberId) {
        cachedMemberRepository.evictMemberCache(memberId);
    }

    @Override
    public void evictTenantMembersCache(String tenantId) {
        cachedMemberRepository.evictTenantMembersCache(tenantId);
    }

    @Override
    public int countMembersUnderTenant(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId));
        return (int) mongoTemplate.count(query, Member.class);
    }

    @Override
    public int removeDepartmentFromAllMembers(String departmentId, String tenantId) {
        requireNonBlank(departmentId, "Department ID must not be blank.");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId).and("departmentIds").is(departmentId));
        Update update = new Update().pull("departmentIds", departmentId);
        UpdateResult result = mongoTemplate.updateMulti(query, update, Member.class);

        return (int) result.getModifiedCount();
    }

    @Override
    public void syncMemberNameToAllArs(Member member) {
        requireNonNull(member, "Member must not be null.");

        Query query = Query.query(where("createdBy").is(member.getId()));
        Update update = new Update();
        update.set("creator", member.getName());
        mongoTemplate.updateMulti(query, update, App.class);
        mongoTemplate.updateMulti(query, update, Group.class);
        mongoTemplate.updateMulti(query, update, Order.class);
        mongoTemplate.updateMulti(query, update, PlateBatch.class);
        mongoTemplate.updateMulti(query, update, QR.class);
        mongoTemplate.updateMulti(query, update, Submission.class);
        mongoTemplate.updateMulti(query, update, Tenant.class);
    }

    @Override
    public int syncTenantStatusToAllMembers(Tenant tenant) {
        requireNonNull(tenant, "Tenant must not be null.");

        Query query = Query.query(where("tenantId").is(tenant.getId()));
        Update update = new Update();
        update.set("tenantActive", tenant.isActive());

        UpdateResult result = mongoTemplate.updateMulti(query, update, Member.class);
        return (int) result.getModifiedCount();
    }
}
