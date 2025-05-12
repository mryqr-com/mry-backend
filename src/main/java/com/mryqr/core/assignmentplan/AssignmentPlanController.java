package com.mryqr.core.assignmentplan;

import com.mryqr.core.assignmentplan.command.AssignmentPlanCommandService;
import com.mryqr.core.assignmentplan.command.CreateAssignmentPlanCommand;
import com.mryqr.core.assignmentplan.command.ExcludeGroupsCommand;
import com.mryqr.core.assignmentplan.command.SetGroupOperatorsCommand;
import com.mryqr.core.assignmentplan.command.UpdateAssignmentPlanSettingCommand;
import com.mryqr.core.assignmentplan.query.AssignmentPlanQueryService;
import com.mryqr.core.assignmentplan.query.QAssignmentPlan;
import com.mryqr.core.assignmentplan.query.QAssignmentPlanSummary;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.common.validation.id.app.AppId;
import com.mryqr.core.common.validation.id.assignmentplan.AssignmentPlanId;
import com.mryqr.core.common.validation.id.group.GroupId;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.mryqr.core.common.utils.ReturnId.returnId;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/assignment-plans")
public class AssignmentPlanController {
    private final AssignmentPlanCommandService assignmentPlanCommandService;
    private final AssignmentPlanQueryService assignmentPlanQueryService;

    @PostMapping
    @ResponseStatus(CREATED)
    public ReturnId createAssignmentPlan(@RequestBody @Valid CreateAssignmentPlanCommand command,
                                         @AuthenticationPrincipal User user) {
        String id = assignmentPlanCommandService.createAssignmentPlan(command, user);
        return returnId(id);
    }

    @PutMapping(value = "/{id}/setting")
    public void updateAssignmentPlanSetting(@PathVariable("id") @NotBlank @AssignmentPlanId String assignmentPlanId,
                                            @RequestBody @Valid UpdateAssignmentPlanSettingCommand command,
                                            @AuthenticationPrincipal User user) {
        assignmentPlanCommandService.updateAssignmentPlanSetting(assignmentPlanId, command, user);
    }

    @PutMapping(value = "/{id}/excluded-groups")
    public void excludeGroups(@PathVariable("id") @NotBlank @AssignmentPlanId String assignmentPlanId,
                              @RequestBody @Valid ExcludeGroupsCommand command,
                              @AuthenticationPrincipal User user) {
        assignmentPlanCommandService.excludeGroups(assignmentPlanId, command, user);
    }

    @PutMapping(value = "/{id}/group-operators")
    public void setGroupOperators(@PathVariable("id") @NotBlank @AssignmentPlanId String assignmentPlanId,
                                  @RequestBody @Valid SetGroupOperatorsCommand command,
                                  @AuthenticationPrincipal User user) {
        assignmentPlanCommandService.setGroupOperators(assignmentPlanId, command, user);
    }

    @PutMapping(value = "/{id}/activation")
    public void activate(@PathVariable("id") @NotBlank @AssignmentPlanId String assignmentPlanId,
                         @AuthenticationPrincipal User user) {
        assignmentPlanCommandService.activate(assignmentPlanId, user);
    }

    @PutMapping(value = "/{id}/deactivation")
    public void deactivate(@PathVariable("id") @NotBlank @AssignmentPlanId String assignmentPlanId,
                           @AuthenticationPrincipal User user) {
        assignmentPlanCommandService.deactivate(assignmentPlanId, user);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteAssignmentPlan(@PathVariable("id") @NotBlank @AssignmentPlanId String assignmentPlanId,
                                     @AuthenticationPrincipal User user) {
        assignmentPlanCommandService.deleteAssignmentPlan(assignmentPlanId, user);
    }

    @GetMapping(value = "/apps/{appId}")
    public List<QAssignmentPlan> listAssignmentPlans(@PathVariable("appId") @NotBlank @AppId String appId,
                                                     @RequestParam(value = "groupId", required = false) @GroupId String groupId,
                                                     @AuthenticationPrincipal User user) {
        return assignmentPlanQueryService.listAssignmentPlans(appId, groupId, user);
    }

    @GetMapping(value = "/apps/{appId}/summaries")
    public List<QAssignmentPlanSummary> listAssignmentPlanSummaries(@PathVariable("appId") @NotBlank @AppId String appId,
                                                                    @AuthenticationPrincipal User user) {
        return assignmentPlanQueryService.listAssignmentPlanSummaries(appId, user);
    }

    @GetMapping(value = "/groups/{groupId}/summaries")
    public List<QAssignmentPlanSummary> listAssignmentPlanSummariesForGroup(@PathVariable("groupId") @NotBlank @GroupId String groupId,
                                                                            @AuthenticationPrincipal User user) {
        return assignmentPlanQueryService.listAssignmentPlanSummariesForGroup(groupId, user);
    }

}
