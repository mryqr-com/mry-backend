package com.mryqr.core.app;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.utils.EasyExcelResult;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.validation.id.app.AppId;
import com.mryqr.common.validation.id.qr.QrId;
import com.mryqr.core.app.command.*;
import com.mryqr.core.app.query.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/apps")
public class AppController {
    private final AppCommandService appCommandService;
    private final AppQueryService appQueryService;

    @PostMapping
    @ResponseStatus(CREATED)
    public CreateAppResponse createApp(
            @RequestBody @Valid CreateAppCommand command,
            @AuthenticationPrincipal User user) {
        return appCommandService.createApp(command, user);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/copies")
    public CreateAppResponse copyApp(
            @RequestBody @Valid CopyAppCommand command,
            @AuthenticationPrincipal User user) {
        return appCommandService.copyApp(command, user);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/templates/{appTemplateId}/adoptions")
    public CreateAppResponse createAppFromTemplate(
            @PathVariable("appTemplateId") @NotBlank @QrId String appTemplateId,
            @AuthenticationPrincipal User user) {
        return appCommandService.createAppFromTemplate(appTemplateId, user);
    }

    @PutMapping(value = "/{appId}/name")
    public void renameApp(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @RequestBody @Valid RenameAppCommand command,
            @AuthenticationPrincipal User user) {
        appCommandService.renameApp(appId, command, user);
    }

    @PutMapping(value = "/{appId}/activation")
    public void activateApp(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        appCommandService.activateApp(appId, user);
    }

    @PutMapping(value = "/{appId}/deactivation")
    public void deactivateApp(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        appCommandService.deactivateApp(appId, user);
    }

    @PutMapping(value = "/{appId}/lock")
    public void lockApp(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        appCommandService.lockApp(appId, user);
    }

    @PutMapping(value = "/{appId}/unlock")
    public void unlockApp(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        appCommandService.unlockApp(appId, user);
    }

    @PutMapping(value = "/{appId}/managers")
    public void setAppManagers(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @RequestBody @Valid SetAppManagersCommand command,
            @AuthenticationPrincipal User user) {
        appCommandService.setAppManagers(appId, command, user);
    }

    @PutMapping(value = "/{appId}/setting")
    public UpdateAppResponse updateAppSetting(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @RequestBody @Valid UpdateAppSettingCommand command,
            @AuthenticationPrincipal User user) {
        String version = appCommandService.updateAppSetting(appId, command, user);
        return UpdateAppResponse.builder().updatedVersion(version).build();
    }

    @PutMapping(value = "/{appId}/report-setting")
    public void updateAppReportSetting(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @RequestBody @Valid UpdateAppReportSettingCommand command,
            @AuthenticationPrincipal User user) {
        appCommandService.updateAppReportSetting(appId, command, user);
    }

    @PutMapping(value = "/{appId}/webhook-setting")
    public void updateAppWebhookSetting(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @RequestBody @Valid UpdateAppWebhookSettingCommand command,
            @AuthenticationPrincipal User user) {
        appCommandService.updateAppWebhookSetting(appId, command, user);
    }

    @PutMapping(value = "/{appId}/group-sync")
    public void enableGroupSync(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        appCommandService.enableGroupSync(appId, user);
    }

    @DeleteMapping(value = "/{appId}")
    public void deleteApp(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        appCommandService.deleteApp(appId, user);
    }

    @PostMapping(value = "/my-managed-apps")
    public PagedList<QManagedListApp> listMyManagedApps(
            @RequestBody @Valid ListMyManagedAppsQuery queryCommand,
            @AuthenticationPrincipal User user) {
        return appQueryService.listMyManagedApps(queryCommand, user);
    }

    @GetMapping(value = "/my-viewable-apps")
    public List<QViewableListApp> listMyViewableApps(@AuthenticationPrincipal User user) {
        return appQueryService.listMyViewableApps(user);
    }

    @GetMapping(value = "/operation/{appId}")
    public QOperationalApp fetchOperationalApp(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        return appQueryService.fetchOperationalApp(appId, user);
    }

    @GetMapping(value = "/updatable/{appId}")
    public QUpdatableApp fetchUpdatableApp(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        return appQueryService.fetchUpdatableApp(appId, user);
    }

    @GetMapping(value = "/{appId}/managers")
    public List<String> listAppManagers(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        return appQueryService.listAppManagers(appId, user);
    }

    @GetMapping(value = "/{appId}/resource-usages")
    public QAppResourceUsages fetchAppResourceUsages(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        return appQueryService.fetchAppResourceUsages(appId, user);
    }

    @GetMapping(value = "/{appId}/qr-import-template")
    public void fetchQrImportTemplateExcel(
            @PathVariable("appId") @NotBlank @AppId String appId,
            HttpServletResponse response,
            @AuthenticationPrincipal User user) throws IOException {
        EasyExcelResult result = appQueryService.fetchQrImportTemplateForApp(appId, user);

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + encode(result.getFileName(), UTF_8));
        EasyExcel.write(response.getOutputStream())
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .head(result.getHeaders())
                .sheet(1)
                .doWrite(result.getRecords());
    }

    @GetMapping(value = "/{appId}/webhook-setting")
    public QAppWebhookSetting fetchAppWebhookSetting(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        return appQueryService.fetchAppWebhookSetting(appId, user);
    }

    @GetMapping(value = "/{appId}/first-qr")
    public QAppFirstQr fetchAppFirstQr(
            @PathVariable("appId") @NotBlank @AppId String appId,
            @AuthenticationPrincipal User user) {
        return appQueryService.fetchAppFirstQr(appId, user);
    }
}
