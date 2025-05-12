package com.mryqr.core.tenant.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.user.User.NOUSER;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountMembersForTenantTask implements RepeatableTask {
    private final TenantRepository tenantRepository;
    private final MemberRepository memberRepository;

    public void run(String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = memberRepository.countMembersUnderTenant(tenantId);
            tenant.setMemberCount(count, NOUSER);
            tenantRepository.save(tenant);
            log.info("Counted all {} members for tenant[{}].", count, tenantId);
        });
    }
}
