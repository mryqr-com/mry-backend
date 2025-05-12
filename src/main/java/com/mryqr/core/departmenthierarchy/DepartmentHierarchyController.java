package com.mryqr.core.departmenthierarchy;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.departmenthierarchy.command.DepartmentHierarchyCommandService;
import com.mryqr.core.departmenthierarchy.command.UpdateDepartmentHierarchyCommand;
import com.mryqr.core.departmenthierarchy.query.DepartmentHierarchyQueryService;
import com.mryqr.core.departmenthierarchy.query.QDepartmentHierarchy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/department-hierarchy")
public class DepartmentHierarchyController {
    private final DepartmentHierarchyCommandService departmentHierarchyCommandService;
    private final DepartmentHierarchyQueryService departmentHierarchyQueryService;

    @PutMapping
    public void updateDepartmentHierarchy(@RequestBody @Valid UpdateDepartmentHierarchyCommand command, @AuthenticationPrincipal User user) {
        departmentHierarchyCommandService.updateDepartmentHierarchy(command, user);
    }

    @GetMapping
    public QDepartmentHierarchy fetchDepartmentHierarchy(@AuthenticationPrincipal User user) {
        return departmentHierarchyQueryService.fetchDepartmentHierarchy(user);
    }

}
