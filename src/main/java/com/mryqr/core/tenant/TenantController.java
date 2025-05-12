package com.mryqr.core.tenant;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.order.domain.delivery.Consignee;
import com.mryqr.core.tenant.command.*;
import com.mryqr.core.tenant.query.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/tenants")
public class TenantController {
    private final TenantQueryService tenantQueryService;
    private final TenantCommandService tenantCommandService;

    @PutMapping(value = "/current/base-setting")
    public void updateTenantBaseSetting(@RequestBody @Valid UpdateTenantBaseSettingCommand command,
                                        @AuthenticationPrincipal User user) {
        tenantCommandService.updateTenantBaseSetting(command, user);
    }

    @PutMapping(value = "/current/logo")
    public void updateTenantLogo(@RequestBody @Valid UpdateTenantLogoCommand command,
                                 @AuthenticationPrincipal User user) {
        tenantCommandService.updateTenantLogo(command, user);
    }

    @PutMapping(value = "/current/api-secret")
    public Map<String, String> refreshTenantApiSecret(@AuthenticationPrincipal User user) {
        return Map.of("secret", tenantCommandService.refreshTenantApiSecret(user));
    }

    @PutMapping(value = "/current/invoice-title")
    public void updateTenantInvoiceTitle(@RequestBody @Valid UpdateTenantInvoiceTitleCommand command,
                                         @AuthenticationPrincipal User user) {
        tenantCommandService.updateTenantInvoiceTitle(command, user);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/current/consignees")
    public void addConsignee(@RequestBody @Valid AddConsigneeCommand command,
                             @AuthenticationPrincipal User user) {
        tenantCommandService.addConsignee(command, user);
    }

    @PutMapping(value = "/current/consignees")
    public void updateConsignee(@RequestBody @Valid UpdateConsigneeCommand command,
                                @AuthenticationPrincipal User user) {
        tenantCommandService.updateConsignee(command, user);
    }

    @DeleteMapping(value = "/current/consignees/{consigneeId}")
    public void deleteConsignee(@PathVariable("consigneeId") @NotBlank @ShortUuid String consigneeId,
                                @AuthenticationPrincipal User user) {
        tenantCommandService.deleteConsignee(consigneeId, user);
    }

    @GetMapping(value = "/current/info")
    public QTenantInfo fetchTenantInfo(@AuthenticationPrincipal User user) {
        return tenantQueryService.fetchTenantInfo(user);
    }

    @GetMapping(value = "/current/base-setting")
    public QTenantBaseSetting fetchTenantBaseSetting(@AuthenticationPrincipal User user) {
        return tenantQueryService.fetchTenantBaseSetting(user);
    }

    @GetMapping(value = "/current/logo")
    public QTenantLogo fetchTenantLogo(@AuthenticationPrincipal User user) {
        return tenantQueryService.fetchTenantLogo(user);
    }

    @GetMapping(value = "/current/api-setting")
    public QTenantApiSetting fetchTenantApiSetting(@AuthenticationPrincipal User user) {
        return tenantQueryService.fetchTenantApiSetting(user);
    }

    @GetMapping(value = "/current/invoice-title")
    public QTenantInvoiceTitle fetchTenantInvoiceTitle(@AuthenticationPrincipal User user) {
        return tenantQueryService.fetchTenantInvoiceTitle(user);
    }

    @GetMapping(value = "/current/consignees")
    public List<Consignee> listTenantConsignees(@AuthenticationPrincipal User user) {
        return tenantQueryService.listConsignees(user);
    }

}
