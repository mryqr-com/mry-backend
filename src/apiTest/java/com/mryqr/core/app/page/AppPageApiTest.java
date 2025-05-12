package com.mryqr.core.app.page;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.UpdateAppReportSettingCommand;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import com.mryqr.core.app.domain.config.AppConfig;
import com.mryqr.core.app.domain.event.AppControlOptionsDeletedEvent;
import com.mryqr.core.app.domain.event.AppControlsDeletedEvent;
import com.mryqr.core.app.domain.event.AppPagesDeletedEvent;
import com.mryqr.core.app.domain.event.PageChangedToSubmitPerInstanceEvent;
import com.mryqr.core.app.domain.event.PageChangedToSubmitPerMemberEvent;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.PageInfo;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.app.domain.page.setting.ApprovalSetting;
import com.mryqr.core.app.domain.page.setting.PageSetting;
import com.mryqr.core.app.domain.report.ReportSetting;
import com.mryqr.core.app.domain.report.chart.ChartReportConfiguration;
import com.mryqr.core.app.domain.report.chart.ChartReportSetting;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.app.domain.report.number.NumberReportConfiguration;
import com.mryqr.core.app.domain.report.number.NumberReportSetting;
import com.mryqr.core.app.domain.report.number.page.PageNumberReport;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.RadioAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import com.mryqr.utils.RandomTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.page.setting.SubmitType.NEW;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_MEMBER;
import static com.mryqr.core.app.domain.page.setting.SubmitterUpdateRange.IN_1_DAY;
import static com.mryqr.core.app.domain.page.setting.SubmitterUpdateRange.IN_1_HOUR;
import static com.mryqr.core.app.domain.page.setting.SubmitterUpdateRange.NO_RESTRICTION;
import static com.mryqr.core.app.domain.report.number.NumberReportType.PAGE_NUMBER_REPORT;
import static com.mryqr.core.app.domain.report.number.page.PageNumberReportType.PAGE_SUBMIT_COUNT;
import static com.mryqr.core.common.domain.event.DomainEventType.CONTROLS_DELETED;
import static com.mryqr.core.common.domain.event.DomainEventType.CONTROL_OPTIONS_DELETED;
import static com.mryqr.core.common.domain.event.DomainEventType.PAGES_DELETED;
import static com.mryqr.core.common.domain.event.DomainEventType.PAGE_CHANGED_TO_SUBMIT_PER_INSTANCE;
import static com.mryqr.core.common.domain.event.DomainEventType.PAGE_CHANGED_TO_SUBMIT_PER_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.AS_GROUP_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.AS_TENANT_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.core.common.domain.permission.Permission.PUBLIC;
import static com.mryqr.core.common.domain.report.ReportRange.NO_LIMIT;
import static com.mryqr.core.common.exception.ErrorCode.APPROVAL_PERMISSION_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.MODIFY_PERMISSION_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.OPERATION_PERMISSION_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.PAGE_ID_DUPLICATED;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.utils.RandomTestFixture.defaultPage;
import static com.mryqr.utils.RandomTestFixture.defaultPageApproveSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultPageBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultPageSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControl;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static com.mryqr.utils.RandomTestFixture.rEmail;
import static com.mryqr.utils.RandomTestFixture.rFormName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPageActionName;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static com.mryqr.utils.RandomTestFixture.rReportName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppPageApiTest extends BaseApiTest {

    @Test
    public void update_app_setting_should_reset_page_setting_for_non_fillable_page() {
        PreparedAppResponse response = setupApi.registerWithApp(rEmail(), rPassword());

        PageSetting pageSetting = defaultPageSettingBuilder()
                .submitType(ONCE_PER_INSTANCE)
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitterUpdatable(true)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(AS_GROUP_MEMBER).build())
                .pageName(rFormName())
                .actionName(rPageActionName())
                .showAsterisk(true)
                .build();
        String appId = response.getAppId();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), appId, pageSetting, newArrayList());

        App app = appRepository.byId(appId);
        PageSetting setting = app.getSetting().homePage().getSetting();
        assertEquals(NEW, setting.getSubmitType());
        assertEquals(AS_TENANT_MEMBER, setting.getPermission());
        assertEquals(CAN_MANAGE_APP, setting.getModifyPermission());
        assertFalse(setting.isSubmitterUpdatable());
        assertEquals(IN_1_DAY, setting.getSubmitterUpdateRange());
        assertFalse(setting.getApprovalSetting().isApprovalEnabled());
        assertEquals(CAN_MANAGE_APP, setting.getApprovalSetting().getPermission());
        assertNotNull(setting.getPageName());
        assertNull(setting.getActionName());
        assertTrue(setting.isShowAsterisk());
    }

    @Test
    public void update_app_setting_should_reset_page_setting_for_public_and_once_per_member() {
        PreparedAppResponse response = setupApi.registerWithApp(rEmail(), rPassword());

        PageSetting pageSetting = defaultPageSettingBuilder()
                .submitType(ONCE_PER_MEMBER)
                .permission(PUBLIC)
                .build();
        String appId = response.getAppId();
        AppApi.updateAppHomePageSetting(response.getJwt(), appId, pageSetting);

        App app = appRepository.byId(appId);
        PageSetting setting = app.getSetting().homePage().getSetting();
        assertEquals(ONCE_PER_MEMBER, setting.getSubmitType());
        assertEquals(AS_TENANT_MEMBER, setting.getPermission());
    }

    @Test
    public void update_app_setting_should_reset_page_setting_for_public() {
        PreparedAppResponse response = setupApi.registerWithApp(rEmail(), rPassword());

        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(PUBLIC)
                .submitterUpdatable(true)
                .submitterUpdateRange(NO_RESTRICTION)
                .build();
        String appId = response.getAppId();
        AppApi.updateAppHomePageSetting(response.getJwt(), appId, pageSetting);

        App app = appRepository.byId(appId);
        PageSetting setting = app.getSetting().homePage().getSetting();
        assertFalse(setting.isSubmitterUpdatable());
        assertEquals(IN_1_DAY, setting.getSubmitterUpdateRange());
    }

    @Test
    public void delete_page_should_raise_event() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();

        FRadioControl control = defaultRadioControl();
        Page newPage = defaultPage(control);
        setting.getPages().add(newPage);
        AppApi.updateAppSetting(response.getJwt(), appId, setting);

        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), newPage.getId(), rAnswer(control));
        assertEquals(1, submissionRepository.count(response.getTenantId()));
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(1, tenant.getResourceUsage().getSubmissionCountForApp(appId));

        setting.getPages().remove(newPage);
        AppApi.updateAppSetting(response.getJwt(), appId, setting);

        AppPagesDeletedEvent appPagesDeletedEvent = domainEventDao.latestEventFor(appId, PAGES_DELETED, AppPagesDeletedEvent.class);
        assertEquals(1, appPagesDeletedEvent.getPages().size());
        PageInfo pageInfo = appPagesDeletedEvent.getPages().stream().findFirst().get();
        assertEquals(newPage.toPageInfo(), pageInfo);

        //虽然页面中包含了控件，删除页面不会发出删除控件事件
        AppControlsDeletedEvent controlDeletedEvent = domainEventDao.latestEventFor(app.getId(), CONTROLS_DELETED, AppControlsDeletedEvent.class);
        assertNull(controlDeletedEvent);

        //虽然页面中包含了控件，删除页面不会发出删除控件选项事件
        AppControlOptionsDeletedEvent optionsDeletedEvent = domainEventDao.latestEventFor(appId, CONTROL_OPTIONS_DELETED, AppControlOptionsDeletedEvent.class);
        assertNull(optionsDeletedEvent);

        assertEquals(0, submissionRepository.count(response.getTenantId()));
        Tenant updatedTenant = tenantRepository.byId(response.getTenantId());
        assertEquals(0, updatedTenant.getResourceUsage().getSubmissionCountForApp(appId));
    }

    @Test
    public void delete_page_should_also_delete_page_aware_number_reports() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        Page page = defaultPage();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.getPages().add(page);
        AppApi.updateAppSetting(response.getJwt(), response.getAppId(), setting);

        PageNumberReport pageNumberReport = PageNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(PAGE_NUMBER_REPORT)
                .range(NO_LIMIT)
                .pageNumberReportType(PAGE_SUBMIT_COUNT)
                .pageId(page.getId())
                .build();

        List<NumberReport> reports = newArrayList(pageNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();
        AppApi.updateAppReportSetting(response.getJwt(), response.getAppId(), command);
        assertEquals(1, appRepository.byId(response.getAppId())
                .getReportSetting().getNumberReportSetting().getReports().size());

        setting.getPages().remove(page);
        AppApi.updateAppSetting(response.getJwt(), response.getAppId(), setting);

        assertEquals(0, appRepository.byId(response.getAppId())
                .getReportSetting().getNumberReportSetting().getReports().size());
    }


    @Test
    public void page_change_to_per_instance_should_raise_event() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String appId = response.getAppId();

        FRadioControl control = defaultRadioControl();
        Page newPage = defaultPage(control);
        AppApi.updateAppPage(response.getJwt(), appId, newPage);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), newPage.getId(), rAnswer(control));
        assertEquals(1, submissionRepository.count(response.getTenantId()));
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(1, tenant.getResourceUsage().getSubmissionCountForApp(appId));

        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        PageSetting pageSetting = setting.homePage().getSetting();
        ReflectionTestUtils.setField(pageSetting, "submitType", ONCE_PER_INSTANCE);

        AppApi.updateAppSetting(response.getJwt(), appId, setting);

        PageChangedToSubmitPerInstanceEvent event = domainEventDao.latestEventFor(appId, PAGE_CHANGED_TO_SUBMIT_PER_INSTANCE, PageChangedToSubmitPerInstanceEvent.class);
        assertEquals(appId, event.getAppId());
        assertEquals(1, event.getPageIds().size());
        assertTrue(event.getPageIds().contains(newPage.getId()));

        assertEquals(0, submissionRepository.count(response.getTenantId()));
        Tenant updatedTenant = tenantRepository.byId(response.getTenantId());
        assertEquals(0, updatedTenant.getResourceUsage().getSubmissionCountForApp(appId));
    }

    @Test
    public void page_changed_to_per_instance_should_delete_page_related_attribute_values() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FRadioControl control = defaultRadioControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        final RadioAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        RadioAttributeValue attributeValue = (RadioAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getOptionId(), attributeValue.getOptionId());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.contains(answer.getOptionId()));

        AppSetting setting = app.getSetting();
        PageSetting pageSetting = setting.homePage().getSetting();
        ReflectionTestUtils.setField(pageSetting, "submitType", ONCE_PER_INSTANCE);

        AppApi.updateAppSetting(response.getJwt(), response.getAppId(), setting);
        QR updatedQr = qrRepository.byId(response.getQrId());
        assertNull(updatedQr.getAttributeValues().get(attribute.getId()));
        assertNull(updatedQr.getIndexedValues().valueOf(indexedField));
    }

    @Test
    public void page_change_to_per_member_should_raise_event() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String appId = response.getAppId();

        FRadioControl control = defaultRadioControl();
        Page newPage = defaultPage(control);
        AppApi.updateAppPage(response.getJwt(), appId, newPage);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), newPage.getId(), rAnswer(control));
        assertEquals(1, submissionRepository.count(response.getTenantId()));
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(1, tenant.getResourceUsage().getSubmissionCountForApp(appId));

        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        PageSetting pageSetting = setting.homePage().getSetting();
        ReflectionTestUtils.setField(pageSetting, "submitType", ONCE_PER_MEMBER);

        AppApi.updateAppSetting(response.getJwt(), appId, setting);

        PageChangedToSubmitPerMemberEvent event = domainEventDao.latestEventFor(appId, PAGE_CHANGED_TO_SUBMIT_PER_MEMBER, PageChangedToSubmitPerMemberEvent.class);
        assertEquals(appId, event.getAppId());
        assertEquals(1, event.getPageIds().size());
        assertTrue(event.getPageIds().contains(newPage.getId()));

        assertEquals(0, submissionRepository.count(response.getTenantId()));
        Tenant updatedTenant = tenantRepository.byId(response.getTenantId());
        assertEquals(0, updatedTenant.getResourceUsage().getSubmissionCountForApp(appId));
    }


    @Test
    public void page_changed_to_per_member_should_delete_page_related_attribute_values() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FRadioControl control = defaultRadioControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        final RadioAnswer answer = rAnswer(control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        RadioAttributeValue attributeValue = (RadioAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(answer.getOptionId(), attributeValue.getOptionId());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.contains(answer.getOptionId()));

        AppSetting setting = app.getSetting();
        PageSetting pageSetting = setting.homePage().getSetting();
        ReflectionTestUtils.setField(pageSetting, "submitType", ONCE_PER_MEMBER);

        AppApi.updateAppSetting(response.getJwt(), response.getAppId(), setting);
        QR updatedQr = qrRepository.byId(response.getQrId());
        assertNull(updatedQr.getAttributeValues().get(attribute.getId()));
        assertNull(updatedQr.getIndexedValues().valueOf(indexedField));
    }


    @Test
    public void should_fail_update_app_setting_if_page_id_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);

        AppSetting setting = app.getSetting();
        List<Page> pages = setting.getPages();
        Page newPage = RandomTestFixture.defaultPage();
        pages.add(newPage);
        pages.add(newPage);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), PAGE_ID_DUPLICATED);
    }

    @Test
    public void should_fail_update_app_if_page_modify_permission_not_allowed() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.getAppId();

        Page page = defaultPageBuilder().setting(defaultPageSettingBuilder().modifyPermission(AS_TENANT_MEMBER).build()).build();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        List<Page> pages = setting.getPages();
        pages.add(page);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), MODIFY_PERMISSION_NOT_ALLOWED);
    }

    @Test
    public void should_fail_update_app_if_page_approve_permission_not_allowed() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.getAppId();

        Page page = defaultPageBuilder()
                .setting(defaultPageSettingBuilder()
                        .approvalSetting(defaultPageApproveSettingBuilder().permission(AS_TENANT_MEMBER).build()).build()).build();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        List<Page> pages = setting.getPages();
        pages.add(page);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), APPROVAL_PERMISSION_NOT_ALLOWED);
    }

    @Test
    public void should_fail_update_app_if_app_operation_permission_not_allowed() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "operationPermission", PUBLIC);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), OPERATION_PERMISSION_NOT_ALLOWED);
    }

}
