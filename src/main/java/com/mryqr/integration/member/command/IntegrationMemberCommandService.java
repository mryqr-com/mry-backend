package com.mryqr.integration.member.command;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.password.MryPasswordEncoder;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberDomainService;
import com.mryqr.core.member.domain.MemberFactory;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationMemberCommandService {
    private final TenantRepository tenantRepository;
    private final MryRateLimiter mryRateLimiter;
    private final MemberFactory memberFactory;
    private final MryPasswordEncoder mryPasswordEncoder;
    private final MemberRepository memberRepository;
    private final MemberDomainService memberDomainService;

    @Transactional
    public String createMember(IntegrationCreateMemberCommand command, User user) {
        String tenantId = user.getTenantId();
        mryRateLimiter.applyFor(tenantId, "Integration:Member:Create", 10);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(tenantId);
        packagesStatus.validateAddMember();

        Member member = memberFactory.create(command.getName(),
                command.getDepartmentIds(),
                command.getMobile(),
                command.getEmail(),
                mryPasswordEncoder.encode(command.getPassword()),
                command.getCustomId(),
                user
        );

        memberRepository.save(member);
        log.info("Integration created member[{}].", member.getId());
        return member.getId();
    }

    @Transactional
    public void updateMemberCustomId(String memberId, IntegrationUpdateMemberCustomIdCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:UpdateCustomId", 10);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        memberDomainService.updateCustomId(member, command.getCustomId(), user);
        memberRepository.save(member);
        log.info("Integration updated custom ID[{}] for member[{}].", command.getCustomId(), memberId);
    }

    @Transactional
    public void deleteMember(String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:Delete", 10);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        member.onDelete(user);
        memberRepository.delete(member);
        memberDomainService.checkMinTenantAdminLimit(member.getTenantId());
        log.info("Integration deleted member[{}].", memberId);
    }

    @Transactional
    public void deleteMemberByCustomId(String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:Custom:Delete", 10);

        Member member = memberRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), customId, user);
        member.onDelete(user);
        memberRepository.delete(member);
        memberDomainService.checkMinTenantAdminLimit(member.getTenantId());
        log.info("Integration deleted member[customId={}].", customId);
    }

    @Transactional
    public void updateMemberInfo(String memberId, IntegrationUpdateMemberInfoCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:UpdateInfo", 10);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        memberDomainService.updateMember(member, command.getName(), command.getDepartmentIds(), command.getMobile(), command.getEmail(), user);
        memberRepository.save(member);
        log.info("Integration updated info for member[{}].", memberId);
    }

    @Transactional
    public void updateMemberInfoByCustomId(String customId, IntegrationUpdateMemberInfoCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:Custom:UpdateInfo", 10);

        Member member = memberRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), customId, user);
        memberDomainService.updateMember(member, command.getName(), command.getDepartmentIds(), command.getMobile(), command.getEmail(), user);
        memberRepository.save(member);
        log.info("Integration updated info for member[customId={}].", customId);
    }

    @Transactional
    public void activateMember(String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:Activate", 10);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        member.activate(user);
        memberRepository.save(member);
        log.info("Integration activated member[{}].", memberId);
    }

    @Transactional
    public void activateMemberByCustomId(String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:Custom:Activate", 10);

        Member member = memberRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), customId, user);
        member.activate(user);
        memberRepository.save(member);
        log.info("Integration activated  member[customId={}].", customId);
    }

    @Transactional
    public void deactivateMember(String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:Deactivate", 10);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        member.deactivate(user);
        memberRepository.save(member);
        memberDomainService.checkMinTenantAdminLimit(member.getTenantId());
        log.info("Integration deactivated member[{}].", memberId);
    }

    @Transactional
    public void deactivateMemberByCustomId(String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:Custom:Deactivate", 10);

        Member member = memberRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), customId, user);
        member.deactivate(user);
        memberRepository.save(member);
        memberDomainService.checkMinTenantAdminLimit(member.getTenantId());
        log.info("Integration deactivated  member[customId={}].", customId);
    }

}
