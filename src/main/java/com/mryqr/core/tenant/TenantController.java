package com.mryqr.core.tenant;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.common.validation.nospace.NoSpace;
import com.mryqr.core.order.domain.delivery.Consignee;
import com.mryqr.core.tenant.command.AddConsigneeCommand;
import com.mryqr.core.tenant.command.TenantCommandService;
import com.mryqr.core.tenant.command.UpdateConsigneeCommand;
import com.mryqr.core.tenant.command.UpdateTenantBaseSettingCommand;
import com.mryqr.core.tenant.command.UpdateTenantInvoiceTitleCommand;
import com.mryqr.core.tenant.command.UpdateTenantLogoCommand;
import com.mryqr.core.tenant.command.UpdateTenantSubdomainCommand;
import com.mryqr.core.tenant.query.QTenantApiSetting;
import com.mryqr.core.tenant.query.QTenantBaseSetting;
import com.mryqr.core.tenant.query.QTenantInfo;
import com.mryqr.core.tenant.query.QTenantInvoiceTitle;
import com.mryqr.core.tenant.query.QTenantLogo;
import com.mryqr.core.tenant.query.QTenantPublicProfile;
import com.mryqr.core.tenant.query.QTenantSubdomain;
import com.mryqr.core.tenant.query.TenantQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @PutMapping(value = "/current/subdomain")
    public void updateTenantSubdomain(@RequestBody @Valid UpdateTenantSubdomainCommand command,
                                      @AuthenticationPrincipal User user) {
        tenantCommandService.updateTenantSubdomain(command, user);
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

    @GetMapping(value = "/current/subdomain")
    public QTenantSubdomain fetchTenantSubdomain(@AuthenticationPrincipal User user) {
        return tenantQueryService.fetchTenantSubdomain(user);
    }

    @GetMapping(value = "/current/api-setting")
    public QTenantApiSetting fetchTenantApiSetting(@AuthenticationPrincipal User user) {
        return tenantQueryService.fetchTenantApiSetting(user);
    }

    @GetMapping(value = "/public-profile/{subdomainPrefix}")
    public QTenantPublicProfile fetchTenantPublicProfile(@PathVariable("subdomainPrefix")
                                                         @Size(max = 20)
                                                         @NotBlank
                                                         @NoSpace String subdomainPrefix) {
        return tenantQueryService.fetchTenantPublicProfile(subdomainPrefix);
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
