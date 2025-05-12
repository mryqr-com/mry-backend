package com.mryqr.core.member.command.importmember;

import com.mryqr.common.password.MryPasswordEncoder;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberFactory;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberImportSaver {
    private final MemberFactory memberFactory;
    private final MryPasswordEncoder mryPasswordEncoder;
    private final MemberRepository memberRepository;

    @Transactional
    public void save(MemberImportRecord record, User user) {
        Member member = memberFactory.create(record.getName(),
                List.of(),
                record.getMobile(),
                record.getEmail(),
                mryPasswordEncoder.encode(record.getPassword()),
                record.getCustomId(),
                user);

        memberRepository.save(member);
    }
}
