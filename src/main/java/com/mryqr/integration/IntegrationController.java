package com.mryqr.integration;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.utils.ReturnId;
import com.mryqr.common.validation.id.app.AppId;
import com.mryqr.common.validation.id.custom.CustomId;
import com.mryqr.common.validation.id.department.DepartmentId;
import com.mryqr.common.validation.id.group.GroupId;
import com.mryqr.common.validation.id.member.MemberId;
import com.mryqr.common.validation.id.qr.QrId;
import com.mryqr.common.validation.id.submission.SubmissionId;
import com.mryqr.integration.app.command.IntegrationAppCommandService;
import com.mryqr.integration.app.query.IntegrationAppQueryService;
import com.mryqr.integration.app.query.QIntegrationApp;
import com.mryqr.integration.app.query.QIntegrationListApp;
import com.mryqr.integration.department.command.IntegrationCreateDepartmentCommand;
import com.mryqr.integration.department.command.IntegrationDepartmentCommandService;
import com.mryqr.integration.department.command.IntegrationUpdateDepartmentCustomIdCommand;
import com.mryqr.integration.department.query.IntegrationDepartmentQueryService;
import com.mryqr.integration.department.query.QIntegrationDepartment;
import com.mryqr.integration.department.query.QIntegrationListDepartment;
import com.mryqr.integration.group.command.*;
import com.mryqr.integration.group.query.IntegrationGroupQueryService;
import com.mryqr.integration.group.query.QIntegrationGroup;
import com.mryqr.integration.group.query.QIntegrationListGroup;
import com.mryqr.integration.member.command.IntegrationCreateMemberCommand;
import com.mryqr.integration.member.command.IntegrationMemberCommandService;
import com.mryqr.integration.member.command.IntegrationUpdateMemberCustomIdCommand;
import com.mryqr.integration.member.command.IntegrationUpdateMemberInfoCommand;
import com.mryqr.integration.member.query.IntegrationMemberQueryService;
import com.mryqr.integration.member.query.QIntegrationListMember;
import com.mryqr.integration.member.query.QIntegrationMember;
import com.mryqr.integration.qr.command.*;
import com.mryqr.integration.qr.query.IntegrationQrQueryService;
import com.mryqr.integration.qr.query.QIntegrationQr;
import com.mryqr.integration.submission.command.IntegrationNewSubmissionCommand;
import com.mryqr.integration.submission.command.IntegrationSubmissionCommandService;
import com.mryqr.integration.submission.command.IntegrationUpdateSubmissionCommand;
import com.mryqr.integration.submission.query.IntegrationSubmissionQueryService;
import com.mryqr.integration.submission.query.QIntegrationSubmission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mryqr.common.utils.ReturnId.returnId;
import static org.springframework.http.HttpStatus.CREATED;


@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/integration")
public class IntegrationController {
    private final IntegrationAppQueryService integrationAppQueryService;
    private final IntegrationSubmissionQueryService integrationSubmissionQueryService;
    private final IntegrationQrCommandService integrationQrCommandService;
    private final IntegrationQrQueryService integrationQrQueryService;
    private final IntegrationGroupCommandService integrationGroupCommandService;
    private final IntegrationGroupQueryService integrationGroupQueryService;
    private final IntegrationMemberCommandService integrationMemberCommandService;
    private final IntegrationMemberQueryService integrationMemberQueryService;
    private final IntegrationSubmissionCommandService integrationSubmissionCommandService;
    private final IntegrationAppCommandService integrationAppCommandService;
    private final IntegrationDepartmentCommandService integrationDepartmentCommandService;
    private final IntegrationDepartmentQueryService integrationDepartmentQueryService;

    @GetMapping(value = "/apps")
    public List<QIntegrationListApp> listApps(@AuthenticationPrincipal User user) {
        return integrationAppQueryService.listApps(user);
    }

    @GetMapping(value = "/apps/{appId}")
    public QIntegrationApp fetchApp(@PathVariable("appId") @NotBlank @AppId String appId,
                                    @AuthenticationPrincipal User user) {
        return integrationAppQueryService.fetchApp(appId, user);
    }

    @PutMapping(value = "/apps/{appId}/activation")
    public void activateApp(@PathVariable("appId") @NotBlank @AppId String appId,
                            @AuthenticationPrincipal User user) {
        integrationAppCommandService.activateApp(appId, user);
    }

    @PutMapping(value = "/apps/{appId}/deactivation")
    public void deactivateApp(@PathVariable("appId") @NotBlank @AppId String appId,
                              @AuthenticationPrincipal User user) {
        integrationAppCommandService.deactivateApp(appId, user);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/qrs/{qrId}/submissions")
    public ReturnId newSubmission(@PathVariable("qrId") @NotBlank @QrId String qrId,
                                  @RequestBody @Valid IntegrationNewSubmissionCommand command,
                                  @AuthenticationPrincipal User user) {
        String submissionId = integrationSubmissionCommandService.newSubmission(qrId, command, user);
        return returnId(submissionId);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/apps/{appId}/qrs/custom/{qrCustomId}/submissions")
    public ReturnId newSubmissionByQrCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                              @PathVariable("qrCustomId") @NotBlank @CustomId String qrCustomId,
                                              @RequestBody @Valid IntegrationNewSubmissionCommand command,
                                              @AuthenticationPrincipal User user) {
        String submissionId = integrationSubmissionCommandService.newSubmissionByQrCustomId(appId, qrCustomId, command, user);
        return returnId(submissionId);
    }

    @PutMapping(value = "/submissions/{submissionId}")
    public void updateSubmission(@PathVariable("submissionId") @SubmissionId @NotBlank String submissionId,
                                 @RequestBody @Valid IntegrationUpdateSubmissionCommand command,
                                 @AuthenticationPrincipal User user) {
        integrationSubmissionCommandService.updateSubmission(submissionId, command, user);
    }

    @DeleteMapping(value = "/submissions/{submissionId}")
    public void deleteSubmission(@PathVariable("submissionId") @SubmissionId @NotBlank String submissionId,
                                 @AuthenticationPrincipal User user) {
        integrationSubmissionCommandService.deleteSubmission(submissionId, user);
    }

    @GetMapping(value = "/submissions/{submissionId}")
    public QIntegrationSubmission fetchSubmission(@PathVariable("submissionId") @NotBlank @SubmissionId String submissionId,
                                                  @AuthenticationPrincipal User user) {
        return integrationSubmissionQueryService.fetchSubmission(submissionId, user);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/qrs/simple-creation")
    public IntegrationCreateQrResponse createQrSimple(@RequestBody @Valid IntegrationCreateQrSimpleCommand command,
                                                      @AuthenticationPrincipal User user) {
        return integrationQrCommandService.createQrSimple(command, user);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/qrs/advanced-creation")
    public IntegrationCreateQrResponse createQrAdvanced(@RequestBody @Valid IntegrationCreateQrAdvancedCommand command,
                                                        @AuthenticationPrincipal User user) {
        return integrationQrCommandService.createQrAdvanced(command, user);
    }

    @DeleteMapping(value = "/qrs/{qrId}")
    public void deleteQr(@PathVariable("qrId") @NotBlank @QrId String qrId,
                         @AuthenticationPrincipal User user) {
        integrationQrCommandService.deleteQr(qrId, user);
    }

    @DeleteMapping(value = "/apps/{appId}/qrs/custom/{customId}")
    public void deleteQrByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                   @PathVariable("customId") @NotBlank @CustomId String customId,
                                   @AuthenticationPrincipal User user) {
        integrationQrCommandService.deleteQrByCustomId(appId, customId, user);
    }

    @PutMapping(value = "/qrs/{qrId}/activation")
    public void activateQr(@PathVariable("qrId") @NotBlank @QrId String qrId,
                           @AuthenticationPrincipal User user) {
        integrationQrCommandService.activateQr(qrId, user);
    }

    @PutMapping(value = "/apps/{appId}/qrs/custom/{customId}/activation")
    public void activateQrByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                     @PathVariable("customId") @NotBlank @CustomId String customId,
                                     @AuthenticationPrincipal User user) {
        integrationQrCommandService.activateQrByCustomId(appId, customId, user);
    }

    @PutMapping(value = "/qrs/{qrId}/deactivation")
    public void deactivateQr(@PathVariable("qrId") @NotBlank @QrId String qrId,
                             @AuthenticationPrincipal User user) {
        integrationQrCommandService.deactivateQr(qrId, user);
    }

    @PutMapping(value = "/apps/{appId}/qrs/custom/{customId}/deactivation")
    public void deactivateQrByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                       @PathVariable("customId") @NotBlank @CustomId String customId,
                                       @AuthenticationPrincipal User user) {
        integrationQrCommandService.deactivateQrByCustomId(appId, customId, user);
    }

    @PutMapping(value = "/qrs/{qrId}/name")
    public void renameQr(@PathVariable("qrId") @NotBlank @QrId String qrId,
                         @RequestBody @Valid IntegrationRenameQrCommand command,
                         @AuthenticationPrincipal User user) {
        integrationQrCommandService.renameQr(qrId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/qrs/custom/{customId}/name")
    public void renameQrByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                   @PathVariable("customId") @NotBlank @CustomId String customId,
                                   @RequestBody @Valid IntegrationRenameQrCommand command,
                                   @AuthenticationPrincipal User user) {
        integrationQrCommandService.renameQrByCustomId(appId, customId, command, user);
    }

    @PutMapping(value = "/qrs/{qrId}/base-setting")
    public void updateQrBaseSetting(@PathVariable("qrId") @NotBlank @QrId String qrId,
                                    @RequestBody @Valid IntegrationUpdateQrBaseSettingCommand command,
                                    @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrBaseSetting(qrId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/qrs/custom/{customId}/base-setting")
    public void updateQrBaseSettingByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                              @PathVariable("customId") @NotBlank @CustomId String customId,
                                              @RequestBody @Valid IntegrationUpdateQrBaseSettingCommand command,
                                              @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrBaseSettingByCustomId(appId, customId, command, user);
    }

    @PutMapping(value = "/qrs/{qrId}/description")
    public void updateQrDescription(@PathVariable("qrId") @NotBlank @QrId String qrId,
                                    @RequestBody @Valid IntegrationUpdateQrDescriptionCommand command,
                                    @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrDescription(qrId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/qrs/custom/{customId}/description")
    public void updateQrDescriptionByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                              @PathVariable("customId") @NotBlank @CustomId String customId,
                                              @RequestBody @Valid IntegrationUpdateQrDescriptionCommand command,
                                              @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrDescriptionByCustomId(appId, customId, command, user);
    }

    @PutMapping(value = "/qrs/{qrId}/header-image")
    public void updateQrHeaderImage(@PathVariable("qrId") @NotBlank @QrId String qrId,
                                    @RequestBody @Valid IntegrationUpdateQrHeaderImageCommand command,
                                    @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrHeaderImage(qrId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/qrs/custom/{customId}/header-image")
    public void updateQrHeaderImageByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                              @PathVariable("customId") @NotBlank @CustomId String customId,
                                              @RequestBody @Valid IntegrationUpdateQrHeaderImageCommand command,
                                              @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrHeaderImageByCustomId(appId, customId, command, user);
    }

    @PutMapping(value = "/qrs/{qrId}/direct-attribute-values")
    public void updateQrDirectAttributes(@PathVariable("qrId") @NotBlank @QrId String qrId,
                                         @RequestBody @Valid IntegrationUpdateQrDirectAttributesCommand command,
                                         @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrDirectAttributes(qrId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/qrs/custom/{customId}/direct-attribute-values")
    public void updateQrDirectAttributesByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                                   @PathVariable("customId") @NotBlank @CustomId String customId,
                                                   @RequestBody @Valid IntegrationUpdateQrDirectAttributesCommand command,
                                                   @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrDirectAttributesByCustomId(appId, customId, command, user);
    }

    @PutMapping(value = "/qrs/{qrId}/geolocation")
    public void updateQrGeolocation(@PathVariable("qrId") @NotBlank @QrId String qrId,
                                    @RequestBody @Valid IntegrationUpdateQrGeolocationCommand command,
                                    @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrGeolocation(qrId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/qrs/custom/{customId}/geolocation")
    public void updateQrGeolocationByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                              @PathVariable("customId") @NotBlank @CustomId String customId,
                                              @RequestBody @Valid IntegrationUpdateQrGeolocationCommand command,
                                              @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrGeolocationByCustomId(appId, customId, command, user);
    }

    @PutMapping(value = "/qrs/{qrId}/custom-id")
    public void updateQrCustomId(@PathVariable("qrId") @NotBlank @QrId String qrId,
                                 @RequestBody @Valid IntegrationUpdateQrCustomIdCommand command,
                                 @AuthenticationPrincipal User user) {
        integrationQrCommandService.updateQrCustomId(qrId, command, user);
    }

    @GetMapping(value = "/qrs/{qrId}")
    public QIntegrationQr fetchQr(@PathVariable("qrId") @NotBlank @QrId String qrId,
                                  @AuthenticationPrincipal User user) {
        return integrationQrQueryService.fetchQr(qrId, user);
    }

    @GetMapping(value = "/apps/{appId}/qrs/custom/{customId}")
    public QIntegrationQr fetchQrByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                            @PathVariable("customId") @NotBlank @CustomId String customId,
                                            @AuthenticationPrincipal User user) {
        return integrationQrQueryService.fetchQrByCustomId(appId, customId, user);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/groups")
    public ReturnId createGroup(@RequestBody @Valid IntegrationCreateGroupCommand command,
                                @AuthenticationPrincipal User user) {
        String groupId = integrationGroupCommandService.createGroup(command, user);
        return returnId(groupId);
    }

    @PutMapping(value = "/groups/{groupId}/custom-id")
    public void updateGroupCustomId(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                    @RequestBody @Valid IntegrationUpdateGroupCustomIdCommand command,
                                    @AuthenticationPrincipal User user) {
        integrationGroupCommandService.updateGroupCustomId(groupId, command, user);
    }

    @DeleteMapping(value = "/groups/{groupId}")
    public void deleteGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                            @AuthenticationPrincipal User user) {
        integrationGroupCommandService.deleteGroup(groupId, user);
    }

    @DeleteMapping(value = "/apps/{appId}/groups/custom/{customId}")
    public void deleteGroupByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                      @PathVariable("customId") @NotBlank @CustomId String customId,
                                      @AuthenticationPrincipal User user) {
        integrationGroupCommandService.deleteGroupByCustomId(appId, customId, user);
    }

    @PutMapping(value = "/groups/{groupId}/name")
    public void renameGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                            @RequestBody @Valid IntegrationRenameGroupCommand command,
                            @AuthenticationPrincipal User user) {
        integrationGroupCommandService.renameGroup(groupId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/groups/custom/{customId}/name")
    public void renameGroupByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                      @PathVariable("customId") @NotBlank @CustomId String customId,
                                      @RequestBody @Valid IntegrationRenameGroupCommand command,
                                      @AuthenticationPrincipal User user) {
        integrationGroupCommandService.renameGroupByCustomId(appId, customId, command, user);
    }

    @PutMapping(value = "/groups/{groupId}/archive")
    public void archiveGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                             @AuthenticationPrincipal User user) {
        integrationGroupCommandService.archiveGroup(groupId, user);
    }

    @PutMapping(value = "/apps/{appId}/groups/custom/{customId}/archive")
    public void archiveGroupByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                       @PathVariable("customId") @NotBlank @CustomId String customId,
                                       @AuthenticationPrincipal User user) {
        integrationGroupCommandService.archiveGroupByCustomId(appId, customId, user);
    }

    @PutMapping(value = "/groups/{groupId}/unarchive")
    public void unArchiveGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                               @AuthenticationPrincipal User user) {
        integrationGroupCommandService.unArchiveGroup(groupId, user);
    }

    @PutMapping(value = "/apps/{appId}/groups/custom/{customId}/unarchive")
    public void unArchiveGroupByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                         @PathVariable("customId") @NotBlank @CustomId String customId,
                                         @AuthenticationPrincipal User user) {
        integrationGroupCommandService.unArchiveGroupByCustomId(appId, customId, user);
    }

    @PutMapping(value = "/groups/{groupId}/members/additions")
    public void addGroupMembers(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                @RequestBody @Valid IntegrationAddGroupMembersCommand command,
                                @AuthenticationPrincipal User user) {
        integrationGroupCommandService.addGroupMembers(groupId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/groups/custom/{customId}/members/additions")
    public void addGroupMembersByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                          @PathVariable("customId") @NotBlank @CustomId String customId,
                                          @RequestBody @Valid IntegrationCustomAddGroupMembersCommand command,
                                          @AuthenticationPrincipal User user) {
        integrationGroupCommandService.addGroupMembersByCustomId(appId, customId, command, user);
    }

    @PutMapping(value = "/groups/{groupId}/members/deletions")
    public void removeGroupMembers(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                   @RequestBody @Valid IntegrationRemoveGroupMembersCommand command,
                                   @AuthenticationPrincipal User user) {
        integrationGroupCommandService.removeGroupMembers(groupId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/groups/custom/{groupCustomId}/members/deletions")
    public void removeGroupMembersByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                             @PathVariable("groupCustomId") @NotBlank @CustomId String groupCustomId,
                                             @RequestBody @Valid IntegrationCustomRemoveGroupMembersCommand command,
                                             @AuthenticationPrincipal User user) {
        integrationGroupCommandService.removeGroupMembersByCustomId(appId, groupCustomId, command, user);
    }

    @PutMapping(value = "/groups/{groupId}/managers/additions")
    public void addGroupManagers(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                 @RequestBody @Valid IntegrationAddGroupManagersCommand command,
                                 @AuthenticationPrincipal User user) {
        integrationGroupCommandService.addGroupManagers(groupId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/groups/custom/{groupCustomId}/managers/additions")
    public void addGroupManagersByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                           @PathVariable("groupCustomId") @NotBlank @CustomId String groupCustomId,
                                           @RequestBody @Valid IntegrationCustomAddGroupManagersCommand command,
                                           @AuthenticationPrincipal User user) {
        integrationGroupCommandService.addGroupManagersByCustomId(appId, groupCustomId, command, user);
    }

    @PutMapping(value = "/groups/{groupId}/managers/deletions")
    public void removeGroupManagers(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                    @RequestBody @Valid IntegrationRemoveGroupManagersCommand command,
                                    @AuthenticationPrincipal User user) {
        integrationGroupCommandService.removeGroupManagers(groupId, command, user);
    }

    @PutMapping(value = "/apps/{appId}/groups/custom/{groupCustomId}/managers/deletions")
    public void removeGroupManagersByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                              @PathVariable("groupCustomId") @NotBlank @CustomId String groupCustomId,
                                              @RequestBody @Valid IntegrationCustomRemoveGroupManagersCommand command,
                                              @AuthenticationPrincipal User user) {
        integrationGroupCommandService.removeGroupManagersByCustomId(appId, groupCustomId, command, user);
    }

    @PutMapping(value = "/groups/{groupId}/activation")
    public void activateGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                              @AuthenticationPrincipal User user) {
        integrationGroupCommandService.activateGroup(groupId, user);
    }

    @PutMapping(value = "/apps/{appId}/groups/custom/{customId}/activation")
    public void activateGroupByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                        @PathVariable("customId") @NotBlank @CustomId String customId,
                                        @AuthenticationPrincipal User user) {
        integrationGroupCommandService.activateGroupByCustomId(appId, customId, user);
    }

    @PutMapping(value = "/groups/{groupId}/deactivation")
    public void deactivateGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                @AuthenticationPrincipal User user) {
        integrationGroupCommandService.deactivateGroup(groupId, user);
    }

    @PutMapping(value = "/apps/{appId}/groups/custom/{customId}/deactivation")
    public void deactivateGroupByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                          @PathVariable("customId") @NotBlank @CustomId String customId,
                                          @AuthenticationPrincipal User user) {
        integrationGroupCommandService.deactivateGroupByCustomId(appId, customId, user);
    }

    @GetMapping(value = "/groups/{groupId}")
    public QIntegrationGroup fetchGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                        @AuthenticationPrincipal User user) {
        return integrationGroupQueryService.fetchGroup(groupId, user);
    }

    @GetMapping(value = "/apps/{appId}/groups/custom/{customId}")
    public QIntegrationGroup fetchGroupByCustomId(@PathVariable("appId") @NotBlank @AppId String appId,
                                                  @PathVariable("customId") @NotBlank @CustomId String customId,
                                                  @AuthenticationPrincipal User user) {
        return integrationGroupQueryService.fetchGroupByCustomId(appId, customId, user);
    }

    @GetMapping(value = "/apps/{appId}/groups")
    public List<QIntegrationListGroup> listGroups(@PathVariable("appId") @NotBlank @AppId String appId,
                                                  @AuthenticationPrincipal User user) {
        return integrationGroupQueryService.listGroups(appId, user);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/departments")
    public ReturnId createDepartment(@RequestBody @Valid IntegrationCreateDepartmentCommand command,
                                     @AuthenticationPrincipal User user) {
        String departmentId = integrationDepartmentCommandService.createDepartment(command, user);
        return returnId(departmentId);
    }

    @PutMapping(value = "/departments/{departmentId}/custom-id")
    public void updateDepartmentCustomId(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                         @RequestBody @Valid IntegrationUpdateDepartmentCustomIdCommand command,
                                         @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.updateDepartmentCustomId(departmentId, command, user);
    }

    @PutMapping(value = "/departments/{departmentId}/members/{memberId}")
    public void addDepartmentMember(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                    @PathVariable("memberId") @NotBlank @MemberId String memberId,
                                    @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.addDepartmentMember(departmentId, memberId, user);
    }

    @PutMapping(value = "/departments/custom/{departmentCustomId}/members/{memberCustomId}")
    public void addDepartmentMemberByCustomId(@PathVariable("departmentCustomId") @NotBlank @CustomId String departmentCustomId,
                                              @PathVariable("memberCustomId") @NotBlank @CustomId String memberCustomId,
                                              @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.addDepartmentMemberByCustomId(departmentCustomId, memberCustomId, user);
    }

    @DeleteMapping(value = "/departments/{departmentId}/members/{memberId}")
    public void removeDepartmentMember(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                       @PathVariable("memberId") @NotBlank @MemberId String memberId,
                                       @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.removeDepartmentMember(departmentId, memberId, user);
    }

    @DeleteMapping(value = "/departments/custom/{departmentCustomId}/members/{memberCustomId}")
    public void removeDepartmentMemberByCustomId(@PathVariable("departmentCustomId") @NotBlank @CustomId String departmentCustomId,
                                                 @PathVariable("memberCustomId") @NotBlank @CustomId String memberCustomId,
                                                 @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.removeDepartmentMemberByCustomId(departmentCustomId, memberCustomId, user);
    }

    @PutMapping(value = "/departments/{departmentId}/managers/{memberId}")
    public void addDepartmentManager(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                     @PathVariable("memberId") @NotBlank @MemberId String memberId,
                                     @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.addDepartmentManager(departmentId, memberId, user);
    }

    @PutMapping(value = "/departments/custom/{departmentCustomId}/managers/{memberCustomId}")
    public void addDepartmentManagerByCustomId(@PathVariable("departmentCustomId") @NotBlank @CustomId String departmentCustomId,
                                               @PathVariable("memberCustomId") @NotBlank @CustomId String memberCustomId,
                                               @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.addDepartmentManagerByCustomId(departmentCustomId, memberCustomId, user);
    }

    @DeleteMapping(value = "/departments/{departmentId}/managers/{memberId}")
    public void removeDepartmentManager(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                        @PathVariable("memberId") @NotBlank @MemberId String memberId,
                                        @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.removeDepartmentManager(departmentId, memberId, user);
    }

    @DeleteMapping(value = "/departments/custom/{departmentCustomId}/managers/{memberCustomId}")
    public void removeDepartmentManagerByCustomId(@PathVariable("departmentCustomId") @NotBlank @CustomId String departmentCustomId,
                                                  @PathVariable("memberCustomId") @NotBlank @CustomId String memberCustomId,
                                                  @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.removeDepartmentManagerByCustomId(departmentCustomId, memberCustomId, user);
    }

    @DeleteMapping(value = "/departments/{departmentId}")
    public void deleteDepartment(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                 @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.deleteDepartment(departmentId, user);
    }

    @DeleteMapping(value = "/departments/custom/{departmentCustomId}")
    public void deleteDepartmentByCustomId(@PathVariable("departmentCustomId") @NotBlank @CustomId String departmentCustomId,
                                           @AuthenticationPrincipal User user) {
        integrationDepartmentCommandService.deleteDepartmentByCustomId(departmentCustomId, user);
    }

    @GetMapping(value = "/departments")
    public List<QIntegrationListDepartment> listDepartments(@AuthenticationPrincipal User user) {
        return integrationDepartmentQueryService.listDepartments(user);
    }

    @GetMapping(value = "/departments/{departmentId}")
    public QIntegrationDepartment fetchDepartment(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                                  @AuthenticationPrincipal User user) {
        return integrationDepartmentQueryService.fetchDepartment(departmentId, user);
    }

    @GetMapping(value = "/departments/custom/{departmentCustomId}")
    public QIntegrationDepartment fetchDepartmentByCustomId(@PathVariable("departmentCustomId") @NotBlank @CustomId String departmentCustomId,
                                                            @AuthenticationPrincipal User user) {
        return integrationDepartmentQueryService.fetchDepartmentByCustomId(departmentCustomId, user);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/members")
    public ReturnId createMember(@RequestBody @Valid IntegrationCreateMemberCommand command,
                                 @AuthenticationPrincipal User user) {
        String memberId = integrationMemberCommandService.createMember(command, user);
        return returnId(memberId);
    }

    @PutMapping(value = "/members/{memberId}/custom-id")
    public void updateMemberCustomId(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                                     @RequestBody @Valid IntegrationUpdateMemberCustomIdCommand command,
                                     @AuthenticationPrincipal User user) {
        integrationMemberCommandService.updateMemberCustomId(memberId, command, user);
    }

    @DeleteMapping(value = "/members/{memberId}")
    public void deleteMember(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                             @AuthenticationPrincipal User user) {
        integrationMemberCommandService.deleteMember(memberId, user);
    }

    @DeleteMapping(value = "/members/custom/{customId}")
    public void deleteMemberByCustomId(@PathVariable("customId") @NotBlank @CustomId String customId,
                                       @AuthenticationPrincipal User user) {
        integrationMemberCommandService.deleteMemberByCustomId(customId, user);
    }

    @PutMapping(value = "/members/{memberId}")
    public void updateMemberInfo(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                                 @RequestBody @Valid IntegrationUpdateMemberInfoCommand command,
                                 @AuthenticationPrincipal User user) {
        integrationMemberCommandService.updateMemberInfo(memberId, command, user);
    }

    @PutMapping(value = "/members/custom/{customId}")
    public void updateMemberInfoByCustomId(@PathVariable("customId") @NotBlank @CustomId String customId,
                                           @RequestBody @Valid IntegrationUpdateMemberInfoCommand command,
                                           @AuthenticationPrincipal User user) {
        integrationMemberCommandService.updateMemberInfoByCustomId(customId, command, user);
    }

    @PutMapping(value = "/members/{memberId}/activation")
    public void activateMember(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                               @AuthenticationPrincipal User user) {
        integrationMemberCommandService.activateMember(memberId, user);
    }

    @PutMapping(value = "/members/{memberId}/deactivation")
    public void deactivateMember(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                                 @AuthenticationPrincipal User user) {
        integrationMemberCommandService.deactivateMember(memberId, user);
    }

    @PutMapping(value = "/members/custom/{customId}/activation")
    public void activateMemberByCustomId(@PathVariable("customId") @NotBlank @CustomId String customId,
                                         @AuthenticationPrincipal User user) {
        integrationMemberCommandService.activateMemberByCustomId(customId, user);
    }

    @PutMapping(value = "/members/custom/{customId}/deactivation")
    public void deactivateMemberByCustomId(@PathVariable("customId") @NotBlank @CustomId String customId,
                                           @AuthenticationPrincipal User user) {
        integrationMemberCommandService.deactivateMemberByCustomId(customId, user);
    }

    @GetMapping(value = "/members/{memberId}")
    public QIntegrationMember fetchMember(@PathVariable("memberId") @NotBlank @MemberId String memberId,
                                          @AuthenticationPrincipal User user) {
        return integrationMemberQueryService.fetchMember(memberId, user);
    }

    @GetMapping(value = "/members/custom/{customId}")
    public QIntegrationMember fetchMemberByCustomId(@PathVariable("customId") @NotBlank @CustomId String customId,
                                                    @AuthenticationPrincipal User user) {
        return integrationMemberQueryService.fetchMemberByCustomId(customId, user);
    }

    @GetMapping(value = "/members")
    public List<QIntegrationListMember> listMembers(@AuthenticationPrincipal User user) {
        return integrationMemberQueryService.listMembers(user);
    }
}
