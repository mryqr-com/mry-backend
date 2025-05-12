package com.mryqr.core.mangement;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.UpdateAppWebhookSettingCommand;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.WebhookSetting;
import com.mryqr.core.app.domain.page.control.FDateControl;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.common.properties.CommonProperties;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.DropdownAttributeValue;
import com.mryqr.core.qr.domain.attribute.IdentifierAttributeValue;
import com.mryqr.core.qr.domain.attribute.ItemStatusAttributeValue;
import com.mryqr.core.qr.domain.attribute.LocalDateAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.TenantApi;
import com.mryqr.core.tenant.command.UpdateTenantSubdomainCommand;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static com.mryqr.core.plan.domain.PlanType.BASIC;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.management.MryManageTenant.ADMIN_INIT_MOBILE;
import static com.mryqr.management.MryManageTenant.ADMIN_INIT_PASSWORD;
import static com.mryqr.management.common.PlanTypeControl.BASIC_PLAN_OPTION_ID;
import static com.mryqr.management.common.PlanTypeControl.FREE_PLAN_OPTION_ID;
import static com.mryqr.management.crm.MryTenantManageApp.CLEAR_SUBDOMAIN_NOTE_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.CLEAR_SUBDOMAIN_PAGE_ID;
import static com.mryqr.management.crm.MryTenantManageApp.CURRENT_PACKAGE_ATTR_ID;
import static com.mryqr.management.crm.MryTenantManageApp.MRY_TENANT_MANAGE_APP_ID;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGES_STATUS_NORMAL_OPTION_ID;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGE_EXPIRE_ATTR_ID;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGE_SETTING_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGE_SETTING_EXPIRE_DATE_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGE_SETTING_NOTE_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGE_SETTING_PAGE_ID;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGE_STATUS_ATTR_ID;
import static com.mryqr.management.crm.MryTenantManageApp.STATUS_SETTING_ACTIVE_OPTION_ID;
import static com.mryqr.management.crm.MryTenantManageApp.STATUS_SETTING_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.STATUS_SETTING_INACTIVE_OPTION_ID;
import static com.mryqr.management.crm.MryTenantManageApp.STATUS_SETTING_NOTE_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.STATUS_SETTING_PAGE_ID;
import static com.mryqr.management.crm.MryTenantManageApp.SUBDOMAIN_READY_OPTION_ID;
import static com.mryqr.management.crm.MryTenantManageApp.TENANT_ID_ATTR_ID;
import static com.mryqr.management.crm.MryTenantManageApp.UPDATE_SUBDOMAIN_READY_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.UPDATE_SUBDOMAIN_READY_PAGE_ID;
import static com.mryqr.utils.RandomTestFixture.rSubdomainPrefix;
import static java.time.ZoneId.systemDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class TenantManagementApiTest extends BaseApiTest {
    @Autowired
    private CommonProperties commonProperties;

    @Test
    public void register_tenant_should_sync_managed_tenant_qr() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        QR qr = qrRepository.byCustomId(MRY_TENANT_MANAGE_APP_ID, loginResponse.getTenantId());

        assertEquals(tenant.getName(), qr.getName());
        DropdownAttributeValue packageAttrValue = (DropdownAttributeValue) qr.attributeValueOf(CURRENT_PACKAGE_ATTR_ID);
        assertEquals(FREE_PLAN_OPTION_ID, packageAttrValue.getOptionIds().get(0));

        LocalDateAttributeValue expireDateAttrValue = (LocalDateAttributeValue) qr.attributeValueOf(PACKAGE_EXPIRE_ATTR_ID);
        LocalDate localDate = LocalDate.ofInstant(tenant.packagesExpiredAt(), systemDefault());
        assertEquals(localDate.toString(), expireDateAttrValue.getDate());

        ItemStatusAttributeValue packageStatusAttrValue = (ItemStatusAttributeValue) qr.attributeValueOf(PACKAGE_STATUS_ATTR_ID);
        assertEquals(PACKAGES_STATUS_NORMAL_OPTION_ID, packageStatusAttrValue.getOptionId());

        IdentifierAttributeValue tenantIdAttrValue = (IdentifierAttributeValue) qr.attributeValueOf(TENANT_ID_ATTR_ID);
        assertEquals(loginResponse.getTenantId(), tenantIdAttrValue.getContent());
    }

    @Test
    public void should_set_packages_for_tenant() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        QR qr = qrRepository.byCustomId(MRY_TENANT_MANAGE_APP_ID, loginResponse.getTenantId());
        assertNotNull(qr);

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, MRY_TENANT_MANAGE_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());

        App app = appRepository.byId(MRY_TENANT_MANAGE_APP_ID);
        FDropdownControl packagesControl = (FDropdownControl) app.controlById(PACKAGE_SETTING_CONTROL_ID);
        DropdownAnswer packageAnswer = DropdownAnswer.answerBuilder(packagesControl).optionIds(List.of(BASIC_PLAN_OPTION_ID)).build();

        FDateControl expireDateControl = (FDateControl) app.controlById(PACKAGE_SETTING_EXPIRE_DATE_CONTROL_ID);
        DateAnswer expireDateAnswer = DateAnswer.answerBuilder(expireDateControl).date(LocalDate.now().plusDays(30).toString()).build();

        FSingleLineTextControl noteControl = (FSingleLineTextControl) app.controlById(PACKAGE_SETTING_NOTE_CONTROL_ID);
        SingleLineTextAnswer noteAnswer = SingleLineTextAnswer.answerBuilder(noteControl).content("some note").build();

        SubmissionApi.newSubmission(jwt, qr.getId(), PACKAGE_SETTING_PAGE_ID, packageAnswer, expireDateAnswer, noteAnswer);
    }

    @Test
    public void should_set_packages_for_tenant_2() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        QR qr = qrRepository.byCustomId(MRY_TENANT_MANAGE_APP_ID, loginResponse.getTenantId());
        assertNotNull(qr);

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, MRY_TENANT_MANAGE_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());

        App app = appRepository.byId(MRY_TENANT_MANAGE_APP_ID);
        FDropdownControl packagesControl = (FDropdownControl) app.controlById(PACKAGE_SETTING_CONTROL_ID);
        DropdownAnswer packageAnswer = DropdownAnswer.answerBuilder(packagesControl).optionIds(List.of(BASIC_PLAN_OPTION_ID)).build();

        FDateControl expireDateControl = (FDateControl) app.controlById(PACKAGE_SETTING_EXPIRE_DATE_CONTROL_ID);
        DateAnswer expireDateAnswer = DateAnswer.answerBuilder(expireDateControl).date(LocalDate.now().plusDays(30).toString()).build();

        FSingleLineTextControl noteControl = (FSingleLineTextControl) app.controlById(PACKAGE_SETTING_NOTE_CONTROL_ID);
        SingleLineTextAnswer noteAnswer = SingleLineTextAnswer.answerBuilder(noteControl).content("some note").build();

        SubmissionApi.newSubmission(jwt, qr.getId(), PACKAGE_SETTING_PAGE_ID, packageAnswer, expireDateAnswer, noteAnswer);

        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        assertEquals(BASIC, tenant.currentPlanType());
        assertEquals(expireDateAnswer.getDate(), LocalDate.ofInstant(tenant.packagesExpiredAt(), systemDefault()).toString());
    }

    @Test
    public void should_set_active_status_for_tenant() {
        PreparedQrResponse response = setupApi.registerWithQr();
        QR qr = qrRepository.byCustomId(MRY_TENANT_MANAGE_APP_ID, response.getTenantId());
        assertNotNull(qr);

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, MRY_TENANT_MANAGE_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());

        App app = appRepository.byId(MRY_TENANT_MANAGE_APP_ID);
        FRadioControl statusControl = (FRadioControl) app.controlById(STATUS_SETTING_CONTROL_ID);
        RadioAnswer inActiveStatusAnswer = RadioAnswer.answerBuilder(statusControl).optionId(STATUS_SETTING_INACTIVE_OPTION_ID).build();
        RadioAnswer activeStatusAnswer = RadioAnswer.answerBuilder(statusControl).optionId(STATUS_SETTING_ACTIVE_OPTION_ID).build();

        FSingleLineTextControl noteControl = (FSingleLineTextControl) app.controlById(STATUS_SETTING_NOTE_CONTROL_ID);
        SingleLineTextAnswer noteAnswer = SingleLineTextAnswer.answerBuilder(noteControl).content("some note").build();

        SubmissionApi.newSubmission(jwt, qr.getId(), STATUS_SETTING_PAGE_ID, inActiveStatusAnswer, noteAnswer);
        assertFalse(tenantRepository.byId(response.getTenantId()).isActive());
        assertFalse(memberRepository.byId(response.getMemberId()).isTenantActive());

        SubmissionApi.newSubmission(jwt, qr.getId(), STATUS_SETTING_PAGE_ID, activeStatusAnswer, noteAnswer);
        assertTrue(tenantRepository.byId(response.getTenantId()).isActive());
        assertTrue(memberRepository.byId(response.getMemberId()).isTenantActive());
    }

    @Test
    public void should_clear_subdomain_for_tenant() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), PROFESSIONAL);

        QR qr = qrRepository.byCustomId(MRY_TENANT_MANAGE_APP_ID, loginResponse.getTenantId());
        assertNotNull(qr);

        TenantApi.updateSubdomain(loginResponse.getJwt(), UpdateTenantSubdomainCommand.builder()
                .subdomainPrefix(rSubdomainPrefix())
                .build());
        assertNotNull(tenantRepository.byId(loginResponse.getTenantId()).getSubdomainPrefix());

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, MRY_TENANT_MANAGE_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());

        App app = appRepository.byId(MRY_TENANT_MANAGE_APP_ID);
        FSingleLineTextControl noteControl = (FSingleLineTextControl) app.controlById(CLEAR_SUBDOMAIN_NOTE_CONTROL_ID);
        SingleLineTextAnswer noteAnswer = SingleLineTextAnswer.answerBuilder(noteControl).content("some note").build();
        SubmissionApi.newSubmission(jwt, qr.getId(), CLEAR_SUBDOMAIN_PAGE_ID, noteAnswer);

        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        assertNull(tenant.getSubdomainPrefix());
        assertFalse(tenant.isSubdomainReady());
        assertNull(tenant.getSubdomainRecordId());
        assertNull(tenant.getSubdomainUpdatedAt());
    }


    @Test
    public void should_udpate_subdomain_ready_status_for_tenant() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), PROFESSIONAL);

        QR qr = qrRepository.byCustomId(MRY_TENANT_MANAGE_APP_ID, loginResponse.getTenantId());
        assertNotNull(qr);

        TenantApi.updateSubdomain(loginResponse.getJwt(), UpdateTenantSubdomainCommand.builder()
                .subdomainPrefix(rSubdomainPrefix())
                .build());
        assertNotNull(tenantRepository.byId(loginResponse.getTenantId()).getSubdomainPrefix());

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, MRY_TENANT_MANAGE_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());

        assertFalse(tenantRepository.byId(loginResponse.getTenantId()).isSubdomainReady());

        App app = appRepository.byId(MRY_TENANT_MANAGE_APP_ID);
        FRadioControl statusControl = (FRadioControl) app.controlById(UPDATE_SUBDOMAIN_READY_CONTROL_ID);
        RadioAnswer statusAnswer = RadioAnswer.answerBuilder(statusControl).optionId(SUBDOMAIN_READY_OPTION_ID).build();
        SubmissionApi.newSubmission(jwt, qr.getId(), UPDATE_SUBDOMAIN_READY_PAGE_ID, statusAnswer);

        assertTrue(tenantRepository.byId(loginResponse.getTenantId()).isSubdomainReady());
    }
}
