package com.mryqr.core.platebatch;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.ReturnId;
import com.mryqr.common.validation.id.platebatch.PlateBatchId;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mryqr.common.utils.ReturnId.returnId;
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
