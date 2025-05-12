package com.mryqr.core.tenant;

import com.mryqr.BaseApiTest;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.domain.invoice.InvoiceTitle;
import com.mryqr.core.order.domain.delivery.Consignee;
import com.mryqr.core.tenant.command.AddConsigneeCommand;
import com.mryqr.core.tenant.command.UpdateConsigneeCommand;
import com.mryqr.core.tenant.command.UpdateTenantBaseSettingCommand;
import com.mryqr.core.tenant.command.UpdateTenantInvoiceTitleCommand;
import com.mryqr.core.tenant.command.UpdateTenantLogoCommand;
import com.mryqr.core.tenant.command.UpdateTenantSubdomainCommand;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.event.TenantSubdomainUpdatedEvent;
import com.mryqr.core.tenant.query.QTenantApiSetting;
import com.mryqr.core.tenant.query.QTenantBaseSetting;
import com.mryqr.core.tenant.query.QTenantInfo;
import com.mryqr.core.tenant.query.QTenantInvoiceTitle;
import com.mryqr.core.tenant.query.QTenantLogo;
import com.mryqr.core.tenant.query.QTenantPublicProfile;
import com.mryqr.core.tenant.query.QTenantSubdomain;
import com.mryqr.management.apptemplate.MryAppTemplateTenant;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static com.mryqr.core.common.domain.event.DomainEventType.TENANT_SUBDOMAIN_UPDATED;
import static com.mryqr.core.common.exception.ErrorCode.CONSIGNEE_ID_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.FORBIDDEN_SUBDOMAIN_PREFIX;
import static com.mryqr.core.common.exception.ErrorCode.MAX_CONSIGNEE_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.REFRESH_API_SECRET_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.SUBDOMAIN_UPDATED_TOO_OFTEN;
import static com.mryqr.core.common.exception.ErrorCode.TENANT_WITH_SUBDOMAIN_PREFIX_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.UPDATE_LOGO_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.UPDATE_SUBDOMAIN_NOT_ALLOWED;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.plan.domain.PlanType.BASIC;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.management.MryManageTenant.ADMIN_MEMBER_ID;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static com.mryqr.management.apptemplate.MryAppTemplateManageApp.MRY_APP_TEMPLATE_MANAGE_APP_GROUP_ID;
import static com.mryqr.management.apptemplate.MryAppTemplateManageApp.MRY_APP_TEMPLATE_MANAGE_APP_ID;
import static com.mryqr.management.apptemplate.MryAppTemplateTenant.MRY_APP_TEMPLATE_TENANT_ID;
import static com.mryqr.management.crm.MryTenantManageApp.MRY_TENANT_MANAGE_APP_ID;
import static com.mryqr.management.crm.MryTenantManageApp.MRY_TENANT_MANAGE_GROUP_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.MRY_OFFENCE_APP_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.MRY_OFFENCE_GROUP_ID;
import static com.mryqr.management.operation.MryOperationApp.MRY_OPERATION_APP_ID;
import static com.mryqr.management.operation.MryOperationApp.MRY_OPERATION_GROUP_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_APP_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_GROUP_ID;
import static com.mryqr.management.printingproduct.PrintingProductApp.PP_APP_ID;
import static com.mryqr.management.printingproduct.PrintingProductApp.PP_GROUP_ID;
import static com.mryqr.utils.RandomTestFixture.rAddress;
import static com.mryqr.utils.RandomTestFixture.rImageFile;
import static com.mryqr.utils.RandomTestFixture.rMemberName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static com.mryqr.utils.RandomTestFixture.rSubdomainPrefix;
import static com.mryqr.utils.RandomTestFixture.rTenantName;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantControllerApiTest extends BaseApiTest {

    @Test
    public void should_pre_create_mry_manage_tenant() {
        assertEquals(FLAGSHIP, tenantRepository.byId(MRY_MANAGE_TENANT_ID).currentPlanType());
        assertEquals(MRY_MANAGE_TENANT_ID, memberRepository.byId(ADMIN_MEMBER_ID).getTenantId());

        assertEquals(MRY_MANAGE_TENANT_ID, appRepository.byId(ORDER_APP_ID).getTenantId());
        assertEquals(ORDER_APP_ID, groupRepository.byId(ORDER_GROUP_ID).getAppId());

        assertEquals(MRY_MANAGE_TENANT_ID, appRepository.byId(PP_APP_ID).getTenantId());
        assertEquals(PP_APP_ID, groupRepository.byId(PP_GROUP_ID).getAppId());

        assertEquals(MRY_MANAGE_TENANT_ID, appRepository.byId(MRY_TENANT_MANAGE_APP_ID).getTenantId());
        assertEquals(MRY_TENANT_MANAGE_APP_ID, groupRepository.byId(MRY_TENANT_MANAGE_GROUP_ID).getAppId());

        assertEquals(MRY_MANAGE_TENANT_ID, appRepository.byId(MRY_OFFENCE_APP_ID).getTenantId());
        assertEquals(MRY_OFFENCE_APP_ID, groupRepository.byId(MRY_OFFENCE_GROUP_ID).getAppId());

        assertEquals(MRY_MANAGE_TENANT_ID, appRepository.byId(MRY_OPERATION_APP_ID).getTenantId());
        assertEquals(MRY_OPERATION_APP_ID, groupRepository.byId(MRY_OPERATION_GROUP_ID).getAppId());

        assertEquals(FLAGSHIP, tenantRepository.byId(MRY_APP_TEMPLATE_TENANT_ID).currentPlanType());
        assertEquals(MRY_APP_TEMPLATE_TENANT_ID, memberRepository.byId(MryAppTemplateTenant.ADMIN_MEMBER_ID).getTenantId());

        assertEquals(MRY_APP_TEMPLATE_TENANT_ID, appRepository.byId(MRY_APP_TEMPLATE_MANAGE_APP_ID).getTenantId());
        assertEquals(MRY_APP_TEMPLATE_MANAGE_APP_ID, groupRepository.byId(MRY_APP_TEMPLATE_MANAGE_APP_GROUP_ID).getAppId());
    }

    @Test
    public void should_update_general_setting() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        String tenantName = rTenantName();
        UploadedFile loginBackground = rImageFile();
        UpdateTenantBaseSettingCommand command = UpdateTenantBaseSettingCommand.builder().name(tenantName).loginBackground(loginBackground).build();
        TenantApi.updateBaseSetting(response.getJwt(), command);
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(tenantName, tenant.getName());
        assertEquals(loginBackground, tenant.getLoginBackground());
    }

    @Test
    public void should_update_logo() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), BASIC);
        UploadedFile logo = rImageFile();
        TenantApi.updateLogo(response.getJwt(), UpdateTenantLogoCommand.builder().logo(logo).build());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(logo, tenant.getLogo());
    }

    @Test
    public void should_not_update_logo_if_packages_not_allowed() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        UpdateTenantLogoCommand command = UpdateTenantLogoCommand.builder().logo(rImageFile()).build();
        assertError(() -> TenantApi.updateLogoRaw(response.getJwt(), command), UPDATE_LOGO_NOT_ALLOWED);
    }

    @Test
    public void should_update_subdomain() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        String subdomainPrefix = rSubdomainPrefix();
        TenantApi.updateSubdomain(response.getJwt(), UpdateTenantSubdomainCommand.builder().subdomainPrefix(subdomainPrefix).build());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(subdomainPrefix, tenant.getSubdomainPrefix());
    }

    @Test
    public void should_fail_update_subdomain_is_plan_not_allowed() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        UpdateTenantSubdomainCommand command = UpdateTenantSubdomainCommand.builder().subdomainPrefix(rSubdomainPrefix()).build();
        assertError(() -> TenantApi.updateSubdomainRaw(response.getJwt(), command), UPDATE_SUBDOMAIN_NOT_ALLOWED);
    }

    @Test
    public void should_fail_update_subdomain_if_already_exists() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        String subdomainPrefix = rSubdomainPrefix();
        TenantApi.updateSubdomain(response.getJwt(), UpdateTenantSubdomainCommand.builder().subdomainPrefix(subdomainPrefix).build());

        LoginResponse anotherTenant = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(anotherTenant.getTenantId(), PROFESSIONAL);
        UpdateTenantSubdomainCommand command = UpdateTenantSubdomainCommand.builder().subdomainPrefix(subdomainPrefix).build();
        assertError(() -> TenantApi.updateSubdomainRaw(anotherTenant.getJwt(), command), TENANT_WITH_SUBDOMAIN_PREFIX_ALREADY_EXISTS);
    }

    @Test
    public void should_raise_event_when_subdomain_name_changed() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        String subdomainPrefix = rSubdomainPrefix();
        TenantApi.updateSubdomain(response.getJwt(), UpdateTenantSubdomainCommand.builder().subdomainPrefix(subdomainPrefix).build());

        TenantSubdomainUpdatedEvent event = domainEventDao.latestEventFor(response.getTenantId(), TENANT_SUBDOMAIN_UPDATED, TenantSubdomainUpdatedEvent.class);
        assertNull(event.getOldSubdomainPrefix());
        assertEquals(subdomainPrefix, event.getNewSubdomainPrefix());
        assertEquals(response.getTenantId(), event.getArTenantId());
    }

    @Test
    public void should_fail_update_domain_name_if_changed_too_often() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        TenantApi.updateSubdomain(response.getJwt(), UpdateTenantSubdomainCommand.builder().subdomainPrefix(rSubdomainPrefix()).build());
        UpdateTenantSubdomainCommand command = UpdateTenantSubdomainCommand.builder().subdomainPrefix(rSubdomainPrefix()).build();
        assertError(() -> TenantApi.updateSubdomainRaw(response.getJwt(), command), SUBDOMAIN_UPDATED_TOO_OFTEN);
    }

    @Test
    public void should_fail_update_subdomain_if_domain_name_is_forbidden() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        String notAllowedDomainName = "www";
        UpdateTenantSubdomainCommand command = UpdateTenantSubdomainCommand.builder().subdomainPrefix(notAllowedDomainName).build();

        assertError(() -> TenantApi.updateSubdomainRaw(response.getJwt(), command), FORBIDDEN_SUBDOMAIN_PREFIX);
    }

    @Test
    public void should_refresh_api_secret() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        String newApiSecret = TenantApi.refreshApiSecret(response.getJwt());
        Tenant updatedTenant = tenantRepository.byId(response.getTenantId());

        assertNotEquals(tenant.getApiSetting().getApiSecret(), newApiSecret);
        assertEquals(updatedTenant.getApiSetting().getApiSecret(), newApiSecret);
    }

    @Test
    public void should_not_refresh_api_secret_if_plan_not_allowed() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        assertError(() -> TenantApi.refreshApiSecretRaw(response.getJwt()), REFRESH_API_SECRET_NOT_ALLOWED);
    }

    @Test
    public void should_fetch_tenant_info() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        Tenant tenant = tenantRepository.byId(response.getTenantId());

        QTenantInfo baseInfo = TenantApi.fetchTenantInfo(response.getJwt());
        assertEquals(response.getTenantId(), baseInfo.getTenantId());
        assertEquals(tenant.getCreatedAt(), baseInfo.getCreatedAt());
        assertEquals(tenant.getCreatedBy(), baseInfo.getCreatedBy());
        assertEquals(tenant.getName(), baseInfo.getName());
        assertEquals(tenant.getPackages().currentPlanType(), baseInfo.getPlanType());
        assertEquals(tenant.getPackages().currentPlanName(), baseInfo.getPackagesName());
        assertEquals(tenant.getPackages().expireAt(), baseInfo.getPackagesExpireAt());
        assertEquals(tenant.getPackages().isExpired(), baseInfo.isPackagesExpired());
        assertEquals(1, baseInfo.getUsedAppCount());
        assertEquals(1, baseInfo.getUsedMemberCount());
    }

    @Test
    public void should_fetch_base_setting() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        QTenantBaseSetting baseSetting = TenantApi.fetchBaseSetting(response.getJwt());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(tenant.getId(), baseSetting.getId());
        assertEquals(tenant.getName(), baseSetting.getName());
    }

    @Test
    public void should_fetch_logo() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), BASIC);
        UploadedFile logo = rImageFile();
        TenantApi.updateLogo(response.getJwt(), UpdateTenantLogoCommand.builder().logo(logo).build());
        QTenantLogo qLogo = TenantApi.fetchLogo(response.getJwt());
        assertEquals(logo, qLogo.getLogo());
    }

    @Test
    public void should_fetch_subdomain() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        assertTrue(TenantApi.fetchSubdomain(response.getJwt()).isUpdatable());

        String subdomainPrefix = rSubdomainPrefix();
        TenantApi.updateSubdomain(response.getJwt(), UpdateTenantSubdomainCommand.builder().subdomainPrefix(subdomainPrefix).build());
        QTenantSubdomain qSubdomain = TenantApi.fetchSubdomain(response.getJwt());
        assertEquals(subdomainPrefix, qSubdomain.getSubdomainPrefix());
        assertFalse(qSubdomain.isUpdatable());
    }

    @Test
    public void should_fetch_tenant_api_setting() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        QTenantApiSetting qTenantApiSetting = TenantApi.fetchApiSetting(response.getJwt());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(qTenantApiSetting.getApiSetting(), tenant.getApiSetting());
    }

    @Test
    public void should_fetch_tenant_public_profile() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        UploadedFile logo = rImageFile();
        TenantApi.updateLogo(response.getJwt(), UpdateTenantLogoCommand.builder().logo(logo).build());

        String subdomainPrefix = rSubdomainPrefix();
        UploadedFile loginBackground = rImageFile();
        TenantApi.updateSubdomain(response.getJwt(), UpdateTenantSubdomainCommand.builder().subdomainPrefix(subdomainPrefix).build());
        TenantApi.updateBaseSetting(response.getJwt(), UpdateTenantBaseSettingCommand.builder().name("aTenantName").loginBackground(loginBackground).build());

        QTenantPublicProfile publicProfile = TenantApi.fetchTenantPublicProfile(subdomainPrefix);
        assertEquals(response.getTenantId(), publicProfile.getTenantId());
        assertEquals(logo, publicProfile.getLogo());
        assertEquals(loginBackground, publicProfile.getLoginBackground());
        assertEquals("aTenantName", publicProfile.getName());
    }

    @Test
    public void should_update_and_fetchtenant_invoice_title() {
        LoginResponse response = setupApi.registerWithLogin();

        final UpdateTenantInvoiceTitleCommand command = UpdateTenantInvoiceTitleCommand.builder()
                .title(InvoiceTitle.builder()
                        .title("成都码如云信息技术有限公司")
                        .unifiedCode("124403987955856482")
                        .bankName("成都天府新区招商银行")
                        .bankAccount("1234567890")
                        .address("成都市高新区天府软件园")
                        .phone("028-12342345")
                        .build())
                .build();
        TenantApi.updateInvoiceTitle(response.getJwt(), command);

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(tenant.getInvoiceTitle(), command.getTitle());

        QTenantInvoiceTitle qInvoiceTitle = TenantApi.fetchInvoiceTitle(response.getJwt());
        assertEquals(tenant.getInvoiceTitle(), qInvoiceTitle.getTitle());
    }

    @Test
    public void should_add_consignee() {
        LoginResponse response = setupApi.registerWithLogin();

        Consignee consignee = Consignee.builder()
                .id(newShortUuid())
                .name(rMemberName())
                .mobile(rMobile())
                .address(rAddress())
                .build();

        TenantApi.addConsignee(response.getJwt(), AddConsigneeCommand.builder()
                .consignee(consignee)
                .build());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(1, tenant.getConsignees().size());
        assertEquals(consignee, tenant.getConsignees().get(0));
    }

    @Test
    public void should_fail_add_consignee_if_max_size_reached() {
        LoginResponse response = setupApi.registerWithLogin();

        IntStream.range(0, 5).forEach(value -> TenantApi.addConsignee(response.getJwt(), AddConsigneeCommand.builder()
                .consignee(Consignee.builder()
                        .id(newShortUuid())
                        .name(rMemberName())
                        .mobile(rMobile())
                        .address(rAddress())
                        .build())
                .build()));

        assertError(() -> TenantApi.addConsigneeRaw(response.getJwt(), AddConsigneeCommand.builder()
                .consignee(Consignee.builder()
                        .id(newShortUuid())
                        .name(rMemberName())
                        .mobile(rMobile())
                        .address(rAddress())
                        .build())
                .build()), MAX_CONSIGNEE_REACHED);
    }

    @Test
    public void should_fail_add_consignee_if_id_duplicated() {
        LoginResponse response = setupApi.registerWithLogin();

        Consignee consignee = Consignee.builder()
                .id(newShortUuid())
                .name(rMemberName())
                .mobile(rMobile())
                .address(rAddress())
                .build();

        TenantApi.addConsignee(response.getJwt(), AddConsigneeCommand.builder()
                .consignee(consignee)
                .build());

        assertError(() -> TenantApi.addConsigneeRaw(response.getJwt(), AddConsigneeCommand.builder()
                .consignee(consignee)
                .build()), CONSIGNEE_ID_DUPLICATED);
    }

    @Test
    public void should_update_consignee() {
        LoginResponse response = setupApi.registerWithLogin();

        Consignee consignee = Consignee.builder()
                .id(newShortUuid())
                .name(rMemberName())
                .mobile(rMobile())
                .address(rAddress())
                .build();

        TenantApi.addConsignee(response.getJwt(), AddConsigneeCommand.builder()
                .consignee(consignee)
                .build());

        Consignee updateConsignee = Consignee.builder()
                .id(consignee.getId())
                .name(rMemberName())
                .mobile(rMobile())
                .address(rAddress())
                .build();

        TenantApi.updateConsignee(response.getJwt(), UpdateConsigneeCommand.builder()
                .consignee(updateConsignee)
                .build());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(1, tenant.getConsignees().size());
        assertEquals(updateConsignee, tenant.getConsignees().get(0));
    }

    @Test
    public void should_delete_consignee() {
        LoginResponse response = setupApi.registerWithLogin();

        Consignee consignee = Consignee.builder()
                .id(newShortUuid())
                .name(rMemberName())
                .mobile(rMobile())
                .address(rAddress())
                .build();

        TenantApi.addConsignee(response.getJwt(), AddConsigneeCommand.builder()
                .consignee(consignee)
                .build());
        assertEquals(1, tenantRepository.byId(response.getTenantId()).getConsignees().size());

        TenantApi.deleteConsignee(response.getJwt(), consignee.getId());
        assertEquals(0, tenantRepository.byId(response.getTenantId()).getConsignees().size());
    }

    @Test
    public void should_list_consignees() {
        LoginResponse response = setupApi.registerWithLogin();

        IntStream.range(0, 5).forEach(value -> TenantApi.addConsignee(response.getJwt(), AddConsigneeCommand.builder()
                .consignee(Consignee.builder()
                        .id(newShortUuid())
                        .name(rMemberName())
                        .mobile(rMobile())
                        .address(rAddress())
                        .build())
                .build()));

        List<Consignee> consignees = TenantApi.listConsignees(response.getJwt());
        assertEquals(5, consignees.size());
    }

    @Test
    public void should_cache_tenant() {
        LoginResponse response = setupApi.registerWithLogin();
        String key = "Cache:TENANT::" + response.getTenantId();
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));
        Tenant tenant = tenantRepository.cachedById(response.getTenantId());
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));

        tenantRepository.save(tenant);
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));
    }

    @Test
    public void should_cache_api_tenant() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.cachedById(response.getTenantId());
        String apiKey = tenant.getApiSetting().getApiKey();

        String key = "Cache:API_TENANT::" + apiKey;
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));

        tenantRepository.cachedByApiKey(apiKey);
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));

        tenantRepository.save(tenant);
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));
    }

}