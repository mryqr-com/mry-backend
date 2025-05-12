package com.mryqr.core.appmanual;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.validation.id.app.AppId;
import com.mryqr.core.appmanual.command.AppManualCommandService;
import com.mryqr.core.appmanual.command.UpdateAppManualCommand;
import com.mryqr.core.appmanual.query.AppManualQueryService;
import com.mryqr.core.appmanual.query.QAppManual;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/app-manuals")
public class AppManualController {
    private final AppManualCommandService appManualCommandService;
    private final AppManualQueryService appManualQueryService;

    @PutMapping(value = "/{appId}")
    public void updateAppManual(@PathVariable("appId") @NotBlank @AppId String appId,
                                @RequestBody @Valid UpdateAppManualCommand command,
                                @AuthenticationPrincipal User user) {
        appManualCommandService.updateAppManual(appId, command, user);
    }

    @GetMapping(value = "/{appId}")
    public QAppManual fetchAppManual(@PathVariable("appId") @NotBlank @AppId String appId,
                                     @AuthenticationPrincipal User user) {
        return appManualQueryService.fetchAppManual(appId, user);
    }
}
