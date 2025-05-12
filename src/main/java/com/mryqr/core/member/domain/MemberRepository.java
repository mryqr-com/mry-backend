package com.mryqr.core.member.domain;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.tenant.domain.Tenant;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface MemberRepository {
    Set<String> allMemberIdsOf(String tenantId);

    boolean existsByMobile(String mobile);

    boolean existsByEmail(String email);

    boolean existsByMobileOrEmail(String mobileOrEmail);

    boolean existsByWxUnionId(String wxUnionId);

    Optional<Member> byMobileOrEmailOptional(String mobileOrEmail);

    Optional<Member> byWxUnionIdOptional(String wxUnionId);

    Member byCustomIdAndCheckTenantShip(String tenantId, String customId, User user);

    List<TenantCachedMember> cachedTenantAllMembers(String tenantId);

    boolean cachedNotAllMembersExist(List<String> memberIds, String tenantId);

    Set<String> cachedMemberIdsOfDepartment(String tenantId, String departmentId);

    int cachedActiveTenantAdminCountFor(String tenantId);

    int cachedTenantAdminCountFor(String tenantId);

    String cachedMemberNameOf(String memberId);

    Map<String, MemberReference> cachedMemberReferences(String tenantId, Set<String> memberIds);

    List<String> cachedMemberIdsForCustomIds(String tenantId, List<String> customIds);

    boolean cachedExistsByCustomId(String customId, String tenantId);

    Map<String, String> cachedMobileWxOpenIdsOf(String tenantId, List<String> memberIds);

    Map<String, String> cachedEmailsOf(String tenantId, List<String> memberIds);

    List<TenantCachedMember> cachedAllActiveTenantAdmins(String tenantId);

    List<MemberReference> cachedAllMemberReferences(String tenantId);

    Member cachedById(String memberId);

    Member cachedByIdAndCheckTenantShip(String memberId, User user);

    Optional<Member> cachedByIdOptional(String memberId);

    List<TenantCachedMember> cachedByIds(Set<String> memberIds, String tenantId);

    Member byId(String id);

    Optional<Member> byIdOptional(String id);

    Member byIdAndCheckTenantShip(String id, User user);

    boolean exists(String arId);

    void save(Member member);

    void delete(Member member);

    default Function<TenantCachedMember, MemberReference> toMemberReference() {
        return cachedMember -> MemberReference.builder()
                .id(cachedMember.getId())
                .name(cachedMember.getName())
                .mobile(cachedMember.getMobile())
                .email(cachedMember.getEmail())
                .build();
    }

    void evictMemberCache(String memberId);

    void evictTenantMembersCache(String tenantId);

    int countMembersUnderTenant(String tenantId);

    int removeDepartmentFromAllMembers(String departmentId, String tenantId);

    void syncMemberNameToAllArs(Member member);

    int syncTenantStatusToAllMembers(Tenant tenant);
}
