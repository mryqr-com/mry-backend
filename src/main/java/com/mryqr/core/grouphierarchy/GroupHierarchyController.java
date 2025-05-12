package com.mryqr.core.grouphierarchy;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.validation.id.app.AppId;
import com.mryqr.core.grouphierarchy.command.GroupHierarchyCommandService;
import com.mryqr.core.grouphierarchy.command.UpdateGroupHierarchyCommand;
import com.mryqr.core.grouphierarchy.query.GroupHierarchyQueryService;
import com.mryqr.core.grouphierarchy.query.QGroupHierarchy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/group-hierarchies")
public class GroupHierarchyController {
    private final GroupHierarchyQueryService groupHierarchyQueryService;
    private final GroupHierarchyCommandService groupHierarchyCommandService;

    @PutMapping(value = "/apps/{appId}")
    public void updateGroupHierarchy(@PathVariable("appId") @NotBlank @AppId String appId,
                                     @RequestBody @Valid UpdateGroupHierarchyCommand command,
                                     @AuthenticationPrincipal User user) {
        groupHierarchyCommandService.updateGroupHierarchy(appId, command, user);
    }

    @GetMapping(value = "/apps/{appId}")
    public QGroupHierarchy fetchGroupHierarchy(@PathVariable("appId") @NotBlank @AppId String appId,
                                               @AuthenticationPrincipal User user) {
        return groupHierarchyQueryService.fetchGroupHierarchy(appId, user);
    }

}
