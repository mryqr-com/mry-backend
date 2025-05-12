package com.mryqr.core.department;


import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.common.validation.id.department.DepartmentId;
import com.mryqr.core.common.validation.id.member.MemberId;
import com.mryqr.core.department.command.CreateDepartmentCommand;
import com.mryqr.core.department.command.DepartmentCommandService;
import com.mryqr.core.department.command.RenameDepartmentCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping(value = "/departments")
public class DepartmentController {
    private final DepartmentCommandService departmentCommandService;

    @PostMapping
    @ResponseStatus(CREATED)
    public ReturnId createDepartment(@RequestBody @Valid CreateDepartmentCommand command,
                                     @AuthenticationPrincipal User user) {
        String departmentId = departmentCommandService.createDepartment(command, user);
        return returnId(departmentId);
    }

    @PutMapping(value = "/{departmentId}/name")
    public void renameDepartment(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                 @RequestBody @Valid RenameDepartmentCommand command,
                                 @AuthenticationPrincipal User user) {
        departmentCommandService.renameDepartment(departmentId, command, user);
    }

    @PutMapping(value = "/{departmentId}/managers/{memberId}")
    public void addDepartmentManager(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                     @PathVariable("memberId") @NotBlank @MemberId String memberId,
                                     @AuthenticationPrincipal User user) {
        departmentCommandService.addDepartmentManager(departmentId, memberId, user);
    }

    @DeleteMapping(value = "/{departmentId}/managers/{memberId}")
    public void removeDepartmentManager(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                        @PathVariable("memberId") @NotBlank @MemberId String memberId,
                                        @AuthenticationPrincipal User user) {
        departmentCommandService.removeDepartmentManager(departmentId, memberId, user);
    }

    @DeleteMapping(value = "/{departmentId}")
    public void deleteDepartment(@PathVariable("departmentId") @NotBlank @DepartmentId String departmentId,
                                 @AuthenticationPrincipal User user) {
        departmentCommandService.deleteDepartment(departmentId, user);
    }
}
