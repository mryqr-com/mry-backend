package com.mryqr.core.tenant.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.common.domain.user.User.NO_USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountMembersForTenantTask implements RetryableTask {
    private final TenantRepository tenantRepository;
    private final MemberRepository memberRepository;

    public void run(String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = memberRepository.countMembersUnderTenant(tenantId);
            tenant.setMemberCount(count, NO_USER);
            tenantRepository.save(tenant);
            log.info("Counted all {} members for tenant[{}].", count, tenantId);
        });
    }
}
