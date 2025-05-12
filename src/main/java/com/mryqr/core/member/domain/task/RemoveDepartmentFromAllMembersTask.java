package com.mryqr.core.member.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.member.domain.TenantCachedMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveDepartmentFromAllMembersTask implements RepeatableTask {
    private final MemberRepository memberRepository;

    public void run(String departmentId, String tenantId) {
        List<TenantCachedMember> members = memberRepository.cachedTenantAllMembers(tenantId);
        members.stream().filter(member -> member.getDepartmentIds().contains(departmentId))
                .forEach(member -> memberRepository.evictMemberCache(member.getId()));

        int count = memberRepository.removeDepartmentFromAllMembers(departmentId, tenantId);
        log.info("Removed department[{}] from all {} members of tenant[{}].", departmentId, count, tenantId);

        if (count > 0) {
            memberRepository.evictTenantMembersCache(tenantId);
        }
    }
}
