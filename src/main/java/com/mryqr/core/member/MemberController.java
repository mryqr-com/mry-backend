package com.mryqr.core.member;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.ReturnId;
import com.mryqr.common.validation.id.app.AppId;
import com.mryqr.common.validation.id.member.MemberId;
import com.mryqr.common.validation.id.tenant.TenantId;
import com.mryqr.core.member.command.*;
import com.mryqr.core.member.command.importmember.MemberImportResponse;
import com.mryqr.core.member.query.*;
import com.mryqr.core.member.query.profile.MemberProfileQueryService;
import com.mryqr.core.member.query.profile.QClientMemberProfile;
import com.mryqr.core.member.query.profile.QConsoleMemberProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.mryqr.common.utils.ReturnId.returnId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/members")
public class MemberController {
    private final MemberProfileQueryService memberProfileQueryService;
    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    @PostMapping
    @ResponseStatus(CREATED)
    public ReturnId createMember(@RequestBody @Valid CreateMemberCommand command,
                                 @AuthenticationPrincipal User user) {
        String memberId = memberCommandService.createMember(command, user);
        return returnId(memberId);
    }

    @PostMapping(value = "/import", consumes = MULTIPART_FORM_DATA_VALUE)
    public MemberImportResponse importMembers(@RequestParam("file") @NotNull MultipartFile file,
                                              @AuthenticationPrincipal User user) throws IOException {
        return memberCommandService.importMembers(file.getInputStream(), user);
    }

    @PutMapping(value = "/{memberId}")
    public void updateMember(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                             @RequestBody @Valid UpdateMemberInfoCommand command,
                             @AuthenticationPrincipal User user) {
        memberCommandService.updateMember(memberId, command, user);
    }

    @PutMapping(value = "/{memberId}/role")
    public void updateMemberRole(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                                 @RequestBody @Valid UpdateMemberRoleCommand command,
                                 @AuthenticationPrincipal User user) {
        memberCommandService.updateMemberRole(memberId, command, user);
    }

    @DeleteMapping(value = "/{memberId}")
    public void deleteMember(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                             @AuthenticationPrincipal User user) {
        memberCommandService.deleteMember(memberId, user);
    }

    @PutMapping(value = "/{memberId}/activation")
    public void activateMember(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                               @AuthenticationPrincipal User user) {
        memberCommandService.activateMember(memberId, user);
    }

    @PutMapping(value = "/{memberId}/deactivation")
    public void deactivateMember(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                                 @AuthenticationPrincipal User user) {
        memberCommandService.deactivateMember(memberId, user);
    }

    @PutMapping(value = "/{memberId}/password")
    public void resetPasswordForMember(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                                       @RequestBody @Valid ResetMemberPasswordCommand command,
                                       @AuthenticationPrincipal User user) {
        memberCommandService.resetPasswordForMember(memberId, command, user);
    }

    @DeleteMapping(value = "/{memberId}/wx")
    public void unbindMemberWx(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                               @AuthenticationPrincipal User user) {
        memberCommandService.unbindMemberWx(memberId, user);
    }

    @PutMapping(value = "/me/password")
    public void changeMyPassword(@RequestBody @Valid ChangeMyPasswordCommand command,
                                 @AuthenticationPrincipal User user) {
        memberCommandService.changeMyPassword(command, user);
    }

    @PutMapping(value = "/me/mobile")
    public void changeMyMobile(@RequestBody @Valid ChangeMyMobileCommand command,
                               @AuthenticationPrincipal User user) {
        memberCommandService.changeMyMobile(command, user);
    }

    @PutMapping(value = "/me/mobile-identification")
    public void identifyMyMobile(@RequestBody @Valid IdentifyMyMobileCommand command,
                                 @AuthenticationPrincipal User user) {
        memberCommandService.identifyMyMobile(command, user);
    }

    @PutMapping(value = "/me/base-setting")
    public void updateMyBaseSetting(@RequestBody @Valid UpdateMyBaseSettingCommand command,
                                    @AuthenticationPrincipal User user) {
        memberCommandService.updateMyBaseSetting(command, user);
    }

    @PutMapping(value = "/me/avatar")
    public void updateMyAvatar(@RequestBody @Valid UpdateMyAvatarCommand command,
                               @AuthenticationPrincipal User user) {
        memberCommandService.updateMyAvatar(command, user);
    }

    @DeleteMapping(value = "/me/avatar")
    public void deleteMyAvatar(@AuthenticationPrincipal User user) {
        memberCommandService.deleteMyAvatar(user);
    }

    @DeleteMapping(value = "/me/wx")
    public void unbindMyWx(@AuthenticationPrincipal User user) {
        memberCommandService.unbindMyWx(user);
    }

    @PostMapping(value = "/findback-password")
    public void findBackPassword(@RequestBody @Valid FindbackPasswordCommand command) {
        memberCommandService.findBackPassword(command);
    }

    @PutMapping(value = "/me/top-apps/{appId}")
    public void topApp(@PathVariable("appId") @NotBlank @AppId String appId,
                       @AuthenticationPrincipal User user) {
        memberCommandService.topApp(appId, user);
    }

    @DeleteMapping(value = "/me/top-apps/{appId}")
    public void cancelTopApp(@PathVariable("appId") @NotBlank @AppId String appId,
                             @AuthenticationPrincipal User user) {
        memberCommandService.cancelTopApp(appId, user);
    }

    @GetMapping(value = "/me")
    public QConsoleMemberProfile fetchMyProfile(@AuthenticationPrincipal User user) {
        return memberProfileQueryService.fetchMyProfile(user);
    }

    @GetMapping(value = "/client/me")
    public QClientMemberProfile fetchMyClientProfile(@AuthenticationPrincipal User user) {
        return memberProfileQueryService.fetchMyClientMemberProfile(user);
    }

    @GetMapping(value = "/me/info")
    public QMemberInfo fetchMyMemberInfo(@AuthenticationPrincipal User user) {
        return memberQueryService.fetchMyMemberInfo(user);
    }

    @GetMapping(value = "/me/base-setting")
    public QMemberBaseSetting fetchMyBaseSetting(@AuthenticationPrincipal User user) {
        return memberQueryService.fetchMyBaseSetting(user);
    }

    @PostMapping(value = "/my-managed-members")
    public PagedList<QListMember> listMyManagedMembers(@RequestBody @Valid ListMyManagedMembersQuery queryCommand,
                                                       @AuthenticationPrincipal User user) {
        return memberQueryService.listMyManagedMembers(queryCommand, user);
    }

    @GetMapping(value = "/all-references")
    public List<QMemberReference> listMemberReferences(@AuthenticationPrincipal User user) {
        return memberQueryService.listMemberReferences(user);
    }

    @GetMapping(value = "/all-references/{tenantId}")
    public List<QMemberReference> listMemberReferencesForTenant(@PathVariable("tenantId") @NotBlank @TenantId String tenantId,
                                                                @AuthenticationPrincipal User user) {
        return memberQueryService.listMemberReferences(tenantId, user);
    }

}
