package com.mryqr.core.group;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.common.validation.id.group.GroupId;
import com.mryqr.core.common.validation.id.member.MemberId;
import com.mryqr.core.group.command.AddGroupManagersCommand;
import com.mryqr.core.group.command.AddGroupMembersCommand;
import com.mryqr.core.group.command.CreateGroupCommand;
import com.mryqr.core.group.command.GroupCommandService;
import com.mryqr.core.group.command.RenameGroupCommand;
import com.mryqr.core.group.query.GroupQueryService;
import com.mryqr.core.group.query.ListGroupQrsQuery;
import com.mryqr.core.group.query.QGroupMembers;
import com.mryqr.core.group.query.QGroupQr;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.mryqr.core.common.utils.ReturnId.returnId;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/groups")
public class GroupController {
    private final GroupQueryService groupQueryService;
    private final GroupCommandService groupCommandService;

    @PostMapping
    @ResponseStatus(CREATED)
    public ReturnId createGroup(@RequestBody @Valid CreateGroupCommand command,
                                @AuthenticationPrincipal User user) {
        String id = groupCommandService.createGroup(command, user);
        return returnId(id);
    }

    @PutMapping(value = "/{groupId}/name")
    public void renameGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                            @RequestBody @Valid RenameGroupCommand command,
                            @AuthenticationPrincipal User user) {
        groupCommandService.renameGroup(groupId, command, user);
    }

    @PutMapping(value = "/{groupId}/members")
    public void addGroupMembers(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                @RequestBody @Valid AddGroupMembersCommand command,
                                @AuthenticationPrincipal User user) {
        groupCommandService.addGroupMembers(groupId, command.getMemberIds(), user);
    }

    @PutMapping(value = "/{groupId}/managers")
    public void addGroupManagers(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                 @RequestBody @Valid AddGroupManagersCommand command,
                                 @AuthenticationPrincipal User user) {
        groupCommandService.addGroupManagers(groupId, command.getMemberIds(), user);
    }

    @DeleteMapping(value = "/{groupId}/members/{memberId}")
    public void removeGroupMember(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                  @PathVariable("memberId") @NotBlank @MemberId String memberId,
                                  @AuthenticationPrincipal User user) {
        groupCommandService.removeGroupMember(groupId, memberId, user);
    }

    @PutMapping(value = "/{groupId}/managers/{memberId}")
    public void addGroupManager(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                @PathVariable("memberId") @NotBlank @MemberId String memberId,
                                @AuthenticationPrincipal User user) {
        groupCommandService.addGroupManager(groupId, memberId, user);
    }

    @DeleteMapping(value = "/{groupId}/managers/{memberId}")
    public void removeGroupManager(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                   @PathVariable("memberId") @NotBlank @MemberId String memberId,
                                   @AuthenticationPrincipal User user) {
        groupCommandService.removeGroupManager(groupId, memberId, user);
    }

    @DeleteMapping(value = "/{groupId}")
    public void deleteGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                            @AuthenticationPrincipal User user) {
        groupCommandService.deleteGroup(groupId, user);
    }

    @PutMapping(value = "/{groupId}/archive")
    public void archiveGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                             @AuthenticationPrincipal User user) {
        groupCommandService.archiveGroup(groupId, user);
    }

    @DeleteMapping(value = "/{groupId}/archive")
    public void unArchiveGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                               @AuthenticationPrincipal User user) {
        groupCommandService.unArchiveGroup(groupId, user);
    }

    @PutMapping(value = "/{groupId}/activation")
    public void activateGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                              @AuthenticationPrincipal User user) {
        groupCommandService.activateGroup(groupId, user);
    }

    @PutMapping(value = "/{groupId}/deactivation")
    public void deactivateGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                @AuthenticationPrincipal User user) {
        groupCommandService.deactivateGroup(groupId, user);
    }

    @GetMapping(value = "/{groupId}/members")
    public QGroupMembers listGroupMembers(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                          @AuthenticationPrincipal User user) {
        return groupQueryService.listGroupMembers(groupId, user);
    }

    @PostMapping(value = "/{groupId}/qrs")
    public PagedList<QGroupQr> listGroupQrs(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                            @RequestBody @Valid ListGroupQrsQuery queryCommand,
                                            @AuthenticationPrincipal User user) {
        return groupQueryService.listGroupQrs(groupId, queryCommand, user);
    }

}
