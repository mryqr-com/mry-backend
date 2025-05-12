package com.mryqr.core.member.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncTenantActiveStatusToMembersTask implements RepeatableTask {
    private final TenantRepository tenantRepository;
    private final MemberRepository memberRepository;

    public void run(String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = memberRepository.syncTenantStatusToAllMembers(tenant);
            log.info("Sync tenant status[{}] to all {} members under tenant[{}].", tenant.isActive(), count, tenantId);
            memberRepository.allMemberIdsOf(tenantId).forEach(memberRepository::evictMemberCache);
            memberRepository.evictTenantMembersCache(tenantId);
        });
    }
}
