package com.mryqr.core.assignment;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.validation.id.assignment.AssignmentId;
import com.mryqr.common.validation.id.qr.QrId;
import com.mryqr.core.assignment.command.AssignmentCommandService;
import com.mryqr.core.assignment.command.SetAssignmentOperatorsCommand;
import com.mryqr.core.assignment.query.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping(value = "/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentQueryService assignmentQueryService;
    private final AssignmentCommandService assignmentCommandService;

    @DeleteMapping(value = "/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") @NotBlank @AssignmentId String assignmentId,
                                 @AuthenticationPrincipal User user) {
        assignmentCommandService.deleteAssignment(assignmentId, user);
    }

    @PutMapping(value = "/{assignmentId}/operators")
    public void setAssignmentOperators(@PathVariable("assignmentId") @NotBlank @AssignmentId String assignmentId,
                                       @RequestBody @Valid SetAssignmentOperatorsCommand command,
                                       @AuthenticationPrincipal User user) {
        assignmentCommandService.setAssignmentOperators(assignmentId, command, user);
    }

    @PostMapping(value = "/my-managed-assignments")
    public PagedList<QListAssignment> listMyManagedAssignments(@RequestBody @Valid ListMyManagedAssignmentsQuery queryCommand,
                                                               @AuthenticationPrincipal User user) {
        return assignmentQueryService.listMyManagedAssignments(queryCommand, user);
    }

    @PostMapping(value = "/my-assignments")
    public PagedList<QListAssignment> listMyAssignments(@RequestBody @Valid ListMyAssignmentsQuery queryCommand,
                                                        @AuthenticationPrincipal User user) {
        return assignmentQueryService.listMyAssignments(queryCommand, user);
    }

    @PostMapping(value = "/{assignmentId}/qrs")
    public PagedList<QAssignmentListQr> listAssignmentQrs(@PathVariable("assignmentId") @NotBlank @AssignmentId String assignmentId,
                                                          @RequestBody @Valid ListAssignmentQrsQuery query,
                                                          @AuthenticationPrincipal User user) {
        return assignmentQueryService.listAssignmentQrs(assignmentId, query, user);
    }

    @GetMapping(value = "/{assignmentId}/detail")
    public QAssignmentDetail fetchAssignmentDetail(@PathVariable("assignmentId") @NotBlank @AssignmentId String assignmentId,
                                                   @AuthenticationPrincipal User user) {
        return assignmentQueryService.fetchAssignmentDetail(assignmentId, user);
    }

    @GetMapping(value = "/{assignmentId}/qrs/{qrId}/detail")
    public QAssignmentQrDetail fetchAssignmentQrDetail(@PathVariable("assignmentId") @NotBlank @AssignmentId String assignmentId,
                                                       @PathVariable("qrId") @NotBlank @QrId String qrId,
                                                       @AuthenticationPrincipal User user) {
        return assignmentQueryService.fetchAssignmentQrDetail(assignmentId, qrId, user);
    }

}
