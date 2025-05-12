package com.mryqr.core.platebatch;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.common.validation.id.platebatch.PlateBatchId;
import com.mryqr.core.plate.query.PlateQueryService;
import com.mryqr.core.platebatch.command.CreatePlateBatchCommand;
import com.mryqr.core.platebatch.command.PlateBatchCommandService;
import com.mryqr.core.platebatch.command.RenamePlateBatchCommand;
import com.mryqr.core.platebatch.query.ListMyManagedPlateBatchesQuery;
import com.mryqr.core.platebatch.query.PlateBatchQueryService;
import com.mryqr.core.platebatch.query.QManagedListPlateBatch;
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

import java.util.List;

import static com.mryqr.core.common.utils.ReturnId.returnId;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/platebatches")
public class PlateBatchController {
    private final PlateBatchCommandService plateBatchCommandService;
    private final PlateBatchQueryService plateBatchQueryService;
    private final PlateQueryService plateQueryService;

    @PostMapping
    @ResponseStatus(CREATED)
    public ReturnId createPlateBatch(@RequestBody @Valid CreatePlateBatchCommand command,
                                     @AuthenticationPrincipal User user) {
        String id = plateBatchCommandService.createPlateBatch(command, user);
        return returnId(id);
    }

    @PutMapping(value = "/{id}/name")
    public void renamePlateBatch(@PathVariable("id") @NotBlank @PlateBatchId String plateBatchId,
                                 @RequestBody @Valid RenamePlateBatchCommand command,
                                 @AuthenticationPrincipal User user) {
        plateBatchCommandService.renamePlateBatch(plateBatchId, command, user);
    }

    @DeleteMapping(value = "/{id}")
    public void deletePlateBatch(@PathVariable("id") @NotBlank @PlateBatchId String plateBatchId,
                                 @AuthenticationPrincipal User user) {
        plateBatchCommandService.deletePlateBatch(plateBatchId, user);
    }

    @PostMapping(value = "/my-managed-platebatches")
    public PagedList<QManagedListPlateBatch> listManagedPlateBatches(@RequestBody @Valid ListMyManagedPlateBatchesQuery queryCommand,
                                                                     @AuthenticationPrincipal User user) {
        return plateBatchQueryService.listManagedPlateBatches(queryCommand, user);
    }

    @GetMapping(value = "/{id}/plate-ids")
    public List<String> allPlateIdsUnderPlateBatch(@PathVariable("id") @NotBlank @PlateBatchId String plateBatchId,
                                                   @AuthenticationPrincipal User user) {
        return plateQueryService.allPlateIdsUnderPlateBatch(plateBatchId, user);
    }

    @GetMapping(value = "/{id}/unused-plate-ids")
    public List<String> unusedPlateIdsUnderPlateBatch(@PathVariable("id") @NotBlank @PlateBatchId String plateBatchId,
                                                      @AuthenticationPrincipal User user) {
        return plateQueryService.unusedPlateIdsUnderPlateBatch(plateBatchId, user);
    }

}
