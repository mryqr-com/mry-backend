package com.mryqr.core.member.command;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.password.MryPasswordEncoder;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.member.command.importmember.MemberImportResponse;
import com.mryqr.core.member.command.importmember.MemberImporter;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberDomainService;
import com.mryqr.core.member.domain.MemberFactory;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import com.mryqr.core.verification.domain.VerificationCodeChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

import static com.mryqr.common.domain.user.Role.TENANT_ADMIN;
import static com.mryqr.common.domain.user.Role.TENANT_MEMBER;
import static com.mryqr.common.exception.ErrorCode.MEMBER_NOT_FOUND_FOR_FINDBACK_PASSWORD;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.core.verification.domain.VerificationCodeType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCommandService {
    private final MemberRepository memberRepository;
    private final MemberFactory memberFactory;
    private final MemberDomainService memberDomainService;
    private final VerificationCodeChecker verificationCodeChecker;
    private final MryPasswordEncoder mryPasswordEncoder;
    private final TenantRepository tenantRepository;
    private final MryRateLimiter mryRateLimiter;
    private final MemberImporter memberImporter;

    @Transactional
    public String createMember(CreateMemberCommand command, User user) {
        user.checkIsTenantAdmin();
        String tenantId = user.getTenantId();
        mryRateLimiter.applyFor(tenantId, "Member:Create", 5);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(tenantId);
        packagesStatus.validateAddMember();

        Member member = memberFactory.create(command.getName(),
                command.getDepartmentIds(),
                command.getMobile(),
                command.getEmail(),
                mryPasswordEncoder.encode(command.getPassword()),
                user
        );

        memberRepository.save(member);
        log.info("Created member[{}].", member.getId());
        return member.getId();
    }

    public MemberImportResponse importMembers(InputStream inputStream, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Member:Import", 1);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateImportMember();
        int remainingCount = packagesStatus.validateImportMembers();

        MemberImportResponse response = memberImporter.importMembers(inputStream, remainingCount, user);
        log.info("Imported {} members for tenant[{}].", response.getImportedCount(), user.getTenantId());
        return response;
    }

    @Transactional
    public void updateMember(String memberId, UpdateMemberInfoCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Member:Update", 5);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);

        memberDomainService.updateMember(member,
                command.getName(),
                command.getDepartmentIds(),
                command.getMobile(),
                command.getEmail(),
                user);

        memberRepository.save(member);
        log.info("Updated detail for member[{}].", memberId);
    }

    @Transactional
    public void updateMemberRole(String memberId, UpdateMemberRoleCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Member:UpdateRole", 5);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        if (command.getRole() == member.getRole()) {
            return;
        }

        member.updateRole(command.getRole(), user);
        memberRepository.save(member);

        if (command.getRole() == TENANT_MEMBER) {
            memberDomainService.checkMinTenantAdminLimit(user.getTenantId());
        }

        if (command.getRole() == TENANT_ADMIN) {
            memberDomainService.checkMaxTenantAdminLimit(user.getTenantId());
        }

        log.info("Updated member[{}] role to {}.", memberId, command.getRole());
    }

    @Transactional
    public void deleteMember(String memberId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Member:Delete", 5);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        member.onDelete(user);
        memberRepository.delete(member);
        memberDomainService.checkMinTenantAdminLimit(member.getTenantId());
        log.info("Deleted member[{}].", memberId);
    }

    @Transactional
    public void resetPasswordForMember(String memberId, ResetMemberPasswordCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Member:ResetPassword", 5);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        member.changePassword(mryPasswordEncoder.encode(command.getPassword()), user);
        memberRepository.save(member);
        log.info("Reset password for member[{}].", memberId);
    }

    @Transactional
    public void unbindMemberWx(String memberId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Member:UnbindWx", 5);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        member.unbindWx(user);
        memberRepository.save(member);
        log.info("Unbound wx for member[{}].", memberId);
    }

    @Transactional
    public void updateMyBaseSetting(UpdateMyBaseSettingCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:UpdateMySetting", 5);

        Member member = memberRepository.byId(user.getMemberId());
        member.updateBaseSetting(command.getName(), user);
        memberRepository.save(member);
        log.info("Member base setting updated by member[{}].", member.getId());
    }

    @Transactional
    public void updateMyAvatar(UpdateMyAvatarCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:UpdateMyAvatar", 5);

        Member member = memberRepository.byId(user.getMemberId());
        member.updateAvatar(command.getAvatar(), user);
        memberRepository.save(member);
        log.info("Avatar updated by member[{}].", member.getId());
    }

    @Transactional
    public void deleteMyAvatar(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:DeleteMyAvatar", 5);

        Member member = memberRepository.byId(user.getMemberId());
        member.deleteAvatar(user);
        memberRepository.save(member);
        log.info("Avatar deleted by member[{}].", member.getId());
    }

    @Transactional
    public void changeMyPassword(ChangeMyPasswordCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:ChangeMyPassword", 5);

        Member member = memberRepository.byId(user.getMemberId());
        memberDomainService.changeMyPassword(member, command.getOldPassword(), command.getNewPassword());
        memberRepository.save(member);
        log.info("Password changed by member[{}].", member.getId());
    }

    @Transactional
    public void changeMyMobile(ChangeMyMobileCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:ChangeMyMobile", 5);

        String mobile = command.getMobile();
        verificationCodeChecker.check(mobile, command.getVerification(), CHANGE_MOBILE);

        Member member = memberRepository.byId(user.getMemberId());
        memberDomainService.changeMyMobile(member, mobile, command.getPassword());
        memberRepository.save(member);
        log.info("Mobile changed by member[{}].", member.getId());
    }

    @Transactional
    public void identifyMyMobile(IdentifyMyMobileCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:IdentifyMobile", 5);

        String mobile = command.getMobile();
        verificationCodeChecker.check(mobile, command.getVerification(), IDENTIFY_MOBILE);

        Member member = memberRepository.byId(user.getMemberId());
        memberDomainService.identifyMyMobile(member, mobile);
        memberRepository.save(member);
        log.info("Mobile identified by member[{}].", member.getId());
    }

    @Transactional
    public void unbindMyWx(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:UnbindMyWx", 5);

        Member member = memberRepository.byId(user.getMemberId());
        member.unbindWx(user);
        memberRepository.save(member);
        log.info("Wx unbound by member[{}].", member.getId());
    }

    @Transactional
    public void findBackPassword(FindbackPasswordCommand command) {
        mryRateLimiter.applyFor("Member:FindBackPassword:All", 5);

        String mobileOrEmail = command.getMobileOrEmail();
        verificationCodeChecker.check(mobileOrEmail, command.getVerification(), FINDBACK_PASSWORD);

        Member member = memberRepository.byMobileOrEmailOptional(mobileOrEmail)
                .orElseThrow(() -> new MryException(MEMBER_NOT_FOUND_FOR_FINDBACK_PASSWORD,
                        "没有找到手机号或密码对应用户。",
                        mapOf("mobileOrEmail", mobileOrEmail)));

        member.changePassword(mryPasswordEncoder.encode(command.getPassword()), member.toUser());
        memberRepository.save(member);
        log.info("Password found back by member[{}].", member.getId());
    }

    @Transactional
    public void topApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:TopApp", 5);

        Member member = memberRepository.byId(user.getMemberId());
        member.topApp(appId, user);
        memberRepository.save(member);
        log.info("Mark app[{}] as top by member[{}].", appId, member.getId());
    }

    @Transactional
    public void cancelTopApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:CancelTopApp", 5);

        Member member = memberRepository.byId(user.getMemberId());
        member.cancelTopApp(appId, user);
        memberRepository.save(member);
        log.info("Unmark app[{}] as top by member[{}].", appId, member.getId());
    }

    @Transactional
    public void activateMember(String memberId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Member:Activate", 5);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        member.activate(user);
        memberRepository.save(member);
        log.info("Activated member[{}].", memberId);
    }

    @Transactional
    public void deactivateMember(String memberId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Member:Deactivate", 5);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        member.deactivate(user);
        memberRepository.save(member);
        memberDomainService.checkMinTenantAdminLimit(member.getTenantId());
        log.info("Deactivated member[{}].", memberId);
    }
}
