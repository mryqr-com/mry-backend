package com.mryqr.core.member.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncMemberNameToAggregateRootsTask implements RetryableTask {
    private final MemberRepository memberRepository;

    public void run(String memberId) {
        memberRepository.byIdOptional(memberId).ifPresent(memberRepository::syncMemberNameToAllArs);
    }
}
