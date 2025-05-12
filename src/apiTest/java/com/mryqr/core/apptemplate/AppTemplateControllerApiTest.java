package com.mryqr.core.apptemplate;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.utils.PagedList;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.CreateAppResponse;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.event.AppCreatedFromTemplateEvent;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.appmanual.AppManualApi;
import com.mryqr.core.appmanual.command.UpdateAppManualCommand;
import com.mryqr.core.appmanual.query.QAppManual;
import com.mryqr.core.apptemplate.query.ListAppTemplateQuery;
import com.mryqr.core.apptemplate.query.QDetailedAppTemplate;
import com.mryqr.core.apptemplate.query.QListAppTemplate;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.DoubleAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import com.mryqr.core.submission.domain.answer.multilinetext.MultiLineTextAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.mryqr.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.common.event.DomainEventType.APP_CREATED_FROM_TEMPLATE;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.core.app.domain.page.control.ControlType.*;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.plan.domain.Plan.FREE_PLAN;
import static com.mryqr.core.plan.domain.PlanType.BASIC;
import static com.mryqr.management.apptemplate.MryAppTemplateManageApp.*;
import static com.mryqr.management.apptemplate.MryAppTemplateTenant.ADMIN_INIT_MOBILE;
import static com.mryqr.management.apptemplate.MryAppTemplateTenant.ADMIN_INIT_PASSWORD;
import static com.mryqr.management.common.PlanTypeControl.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class AppTemplateControllerApiTest extends BaseApiTest {

    @Test
    public void should_list_app_templates() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);

        UploadedFile firstHeaderImage = rImageFile();
        List<String> firstCategories = List.of(CATEGORY_AGRICULTURE_ID, CATEGORY_MEDICAL_ID);
        QR firstQr = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, BASIC_PLAN_OPTION_ID, firstCategories,
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));
        QR qr = qrRepository.byId(firstQr.getId());
        qr.updateHeaderImage(firstHeaderImage, NOUSER);
        qrRepository.save(qr);

        List<QR> qrs = IntStream.range(1, 12).mapToObj(
                value -> createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_ORGANIZATION_ID),
                        List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID))).toList();

        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse member = MemberApi.createMemberAndLogin(response.getJwt());
        PagedList<QListAppTemplate> firstPageTemplates = AppTemplateApi.listPublishedAppTemplates(
                ListAppTemplateQuery.builder().pageIndex(1).pageSize(10).build());

        assertEquals(10, firstPageTemplates.getData().size());
        assertEquals(1, firstPageTemplates.getPageIndex());
        assertEquals(10, firstPageTemplates.getPageSize());
        assertTrue(firstPageTemplates.getTotalNumber() >= 12);

        Set<String> qrIds = qrs.stream().map(QR::getId).collect(toSet());
        qrIds.add(firstQr.getId());
        Set<String> listedQrIds = firstPageTemplates.getData().stream().map(QListAppTemplate::getId).collect(toSet());
        assertTrue(qrIds.containsAll(listedQrIds));

        PagedList<QListAppTemplate> secondPageTemplates = AppTemplateApi.listPublishedAppTemplates(
                ListAppTemplateQuery.builder().pageIndex(2).pageSize(10).build());
        QListAppTemplate firstTemplate = secondPageTemplates.getData().stream()
                .filter(qListAppTemplate -> qListAppTemplate.getId().equals(firstQr.getId())).findFirst().get();
        assertEquals(firstQr.getName(), firstTemplate.getName());
        assertNotNull(firstTemplate.getCardDescription());
        assertEquals(BASIC, firstTemplate.getPlanType());
        assertTrue(firstTemplate.getFeatures().contains(FEATURE_NAMES_MAP.get(FEATURE_GEOLOCATION_ID)));
        assertEquals(firstHeaderImage, firstTemplate.getPoster());
    }

    @Test
    public void list_templates_should_only_contain_published_templates() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        QR template = createAppTemplate(jwt, rQrName(), TOBE_PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_ORGANIZATION_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));
        PagedList<QListAppTemplate> templates = AppTemplateApi.listPublishedAppTemplates(
                ListAppTemplateQuery.builder().pageIndex(1).pageSize(10).build());
        Set<String> listedQrIds = templates.getData().stream().map(QListAppTemplate::getId).collect(toSet());
        assertFalse(listedQrIds.contains(template.getId()));
    }

    @Test
    public void should_filter_by_categories_when_list_templates() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        QR shownQr = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_ORGANIZATION_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));
        QR notShowQr = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_AGRICULTURE_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));
        PagedList<QListAppTemplate> templates = AppTemplateApi.listPublishedAppTemplates(
                ListAppTemplateQuery.builder().category(CATEGORY_ORGANIZATION_ID).pageIndex(1).pageSize(10).build());
        Set<String> listedQrIds = templates.getData().stream().map(QListAppTemplate::getId).collect(toSet());
        assertTrue(listedQrIds.contains(shownQr.getId()));
        assertFalse(listedQrIds.contains(notShowQr.getId()));
    }

    @Test
    public void should_filter_by_scenarios_when_list_templates() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        QR shownQr = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_ORGANIZATION_ID),
                List.of(SCENARIO_REGISTRATION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));
        QR notShowQr = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_AGRICULTURE_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));
        PagedList<QListAppTemplate> templates = AppTemplateApi.listPublishedAppTemplates(
                ListAppTemplateQuery.builder().scenario(SCENARIO_REGISTRATION_OPTION_ID).pageIndex(1).pageSize(10).build());
        Set<String> listedQrIds = templates.getData().stream().map(QListAppTemplate::getId).collect(toSet());
        assertTrue(listedQrIds.contains(shownQr.getId()));
        assertFalse(listedQrIds.contains(notShowQr.getId()));
    }

    @Test
    public void should_search_for_templates() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        QR shownQr = createAppTemplate(jwt, "项目管理" + randomAlphanumeric(10), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID,
                List.of(CATEGORY_ORGANIZATION_ID), List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));
        QR notShowQr = createAppTemplate(jwt, "apptemplate" + randomAlphanumeric(10), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID,
                List.of(CATEGORY_AGRICULTURE_ID), List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));
        PagedList<QListAppTemplate> templates = AppTemplateApi.listPublishedAppTemplates(
                ListAppTemplateQuery.builder().search("项目").pageIndex(1).pageSize(10).build());
        Set<String> listedQrIds = templates.getData().stream().map(QListAppTemplate::getId).collect(toSet());
        assertTrue(listedQrIds.contains(shownQr.getId()));
        assertFalse(listedQrIds.contains(notShowQr.getId()));
    }

    @Test
    public void should_create_app_from_template() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);

        QR template = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_ORGANIZATION_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));
        String refedAppId = template.getCustomId();
        assertNull(template.attributeValueOf(APPLIED_COUNT_ATTRIBUTE_ID));

        LoginResponse loginResponse = setupApi.registerWithLogin();
        CreateAppResponse appResponse = AppApi.createAppFromTemplate(loginResponse.getJwt(), template.getId());
        App newApp = appRepository.byId(appResponse.getAppId());
        App sourceApp = appRepository.byId(refedAppId);
        assertEquals(template.getId(), newApp.getAppTemplateId());
        assertEquals(sourceApp.getSetting().getPages().stream().map(Page::getId).collect(toSet()),
                newApp.getSetting().getPages().stream().map(Page::getId).collect(toSet()));

        AppCreatedFromTemplateEvent event = latestEventFor(newApp.getId(), APP_CREATED_FROM_TEMPLATE, AppCreatedFromTemplateEvent.class);
        assertEquals(template.getId(), event.getAppTemplateId());
        assertEquals(refedAppId, event.getSourceAppId());

        QR updatedTemplate = qrRepository.byId(template.getId());
        assertEquals(1, ((DoubleAttributeValue) updatedTemplate.attributeValueOf(APPLIED_COUNT_ATTRIBUTE_ID)).getNumber());
    }

    @Test
    public void should_create_app_from_template_with_modifiers() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);

        QR template = createAppTemplate(jwt,
                rQrName(), PUBLISHED_STATUS_OPTION_ID,
                FREE_PLAN_OPTION_ID,
                List.of(CATEGORY_ORGANIZATION_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID)
        );

        LoginResponse loginResponse = setupApi.registerWithLogin();
        CreateAppResponse appResponse = AppApi.createAppFromTemplate(loginResponse.getJwt(), template.getId());
        App newApp = appRepository.byId(appResponse.getAppId());
        assertEquals(CAN_MANAGE_GROUP, newApp.getSetting().homePage().getSetting().getPermission());
        assertEquals(ONCE_PER_INSTANCE, newApp.getSetting().homePage().getSetting().getSubmitType());
    }

    @Test
    public void create_app_from_template_should_also_clone_app_manual() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        QR template = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_ORGANIZATION_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));
        String refedAppId = template.getCustomId();
        String content = rSentence(10);
        AppManualApi.updateAppManual(jwt, refedAppId, UpdateAppManualCommand.builder().content(content).build());

        LoginResponse loginResponse = setupApi.registerWithLogin();
        CreateAppResponse appResponse = AppApi.createAppFromTemplate(loginResponse.getJwt(), template.getId());

        QAppManual qAppManual = AppManualApi.fetchAppManual(loginResponse.getJwt(), appResponse.getAppId());
        assertEquals(content, qAppManual.getContent());
    }

    @Test
    public void should_fail_create_app_from_template_if_package_too_low() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        QR template = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, FLAGSHIP_PLAN_OPTION_ID, List.of(CATEGORY_ORGANIZATION_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));

        LoginResponse loginResponse = setupApi.registerWithLogin();
        Tenant theTenant = tenantRepository.byId(loginResponse.getTenantId());
        setupApi.updateTenantPlan(theTenant, FREE_PLAN);
        assertError(() -> AppApi.createAppFromTemplateRaw(loginResponse.getJwt(), template.getId()), LOW_PLAN_FOR_APP_TEMPLATE);
    }

    @Test
    public void should_fail_create_app_from_template_if_name_already_exists() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        QR template = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_ORGANIZATION_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));

        LoginResponse loginResponse = setupApi.registerWithLogin();
        AppApi.createAppFromTemplate(loginResponse.getJwt(), template.getId());
        assertError(() -> AppApi.createAppFromTemplateRaw(loginResponse.getJwt(), template.getId()), APP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_create_app_from_template_if_max_app_count_reached() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        QR template = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_ORGANIZATION_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));

        LoginResponse loginResponse = setupApi.registerWithLogin();

        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        tenant.setAppCount(tenant.currentPlan().getMaxAppCount(), NOUSER);
        tenantRepository.save(tenant);

        assertError(() -> AppApi.createAppFromTemplateRaw(loginResponse.getJwt(), template.getId()), APP_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_fail_create_app_from_template_if_template_not_published() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        QR template = createAppTemplate(jwt, rQrName(), TOBE_PUBLISHED_STATUS_OPTION_ID, FREE_PLAN_OPTION_ID, List.of(CATEGORY_ORGANIZATION_ID),
                List.of(SCENARIO_INSPECTION_OPTION_ID), List.of(FEATURE_GEOLOCATION_ID));

        LoginResponse loginResponse = setupApi.registerWithLogin();
        assertError(() -> AppApi.createAppFromTemplateRaw(loginResponse.getJwt(), template.getId()), APP_TEMPLATE_NOT_PUBLISHED);
    }

    @Test
    public void should_fetch_app_template_detail() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        UploadedFile headerImage = rImageFile();
        List<String> categories = List.of(CATEGORY_AGRICULTURE_ID, CATEGORY_MEDICAL_ID);
        List<String> scenarios = List.of(SCENARIO_INSPECTION_OPTION_ID);

        QR firstQr = createAppTemplate(jwt, rQrName(), PUBLISHED_STATUS_OPTION_ID, BASIC_PLAN_OPTION_ID, categories, scenarios,
                List.of(FEATURE_GEOLOCATION_ID));
        QR qr = qrRepository.byId(firstQr.getId());
        qr.updateHeaderImage(headerImage, NOUSER);
        qrRepository.save(qr);

        QDetailedAppTemplate template = AppTemplateApi.fetchAppTemplateDetail(qr.getId());
        assertEquals(qr.getId(), template.getId());
        assertEquals(qr.getName(), template.getName());
        assertEquals(BASIC, template.getPlanType());
        assertNotNull(template.getCardDescription());
        assertNotNull(template.getIntroduction());
        assertNotNull(template.getPlateSetting());
        assertNotNull(template.getDemoQr());
        assertEquals(3, template.getControlCount());
        assertFalse(template.isGeolocationEnabled());
        assertFalse(template.isPlateBatchEnabled());
        assertFalse(template.isAssignmentEnabled());
        assertTrue(template.getNumberReports().isEmpty());
        assertTrue(template.getChartReports().isEmpty());
        assertTrue(template.getKanbans().isEmpty());
        assertTrue(template.getCirculationStatuses().isEmpty());
        assertEquals(1, template.getPages().size());
        assertEquals(1, template.getFillablePages().size());
        assertTrue(template.getApprovalPages().isEmpty());
        assertTrue(template.getNotificationPages().isEmpty());
        assertEquals(2, template.getAttributes().size());
        assertEquals(3, template.getOperationMenus().size());
    }

    private QR createAppTemplate(
            String jwt,
            String name,
            String status,
            String planType,
            List<String> categories,
            List<String> scenarios,
            List<String> features) {
        CreateAppResponse refedApp = AppApi.createApp(jwt);
        CreateQrResponse demoQr = QrApi.createQr(jwt, refedApp.getDefaultGroupId());

        CreateQrResponse response = QrApi.createQr(jwt, name, MRY_APP_TEMPLATE_MANAGE_APP_GROUP_ID);
        QR appTemplate = qrRepository.byId(response.getQrId());
        appTemplate.updateHeaderImage(rImageFile(), NOUSER);
        appTemplate.updateCustomId(refedApp.getAppId(), NOUSER);
        qrRepository.save(appTemplate);
        SubmissionApi.newSubmission(jwt, appTemplate.getId(), TEMPLATE_HOME_PAGE_ID,
                SingleLineTextAnswer.builder()
                        .controlId(CARD_DESCRIPTION_CONTROL_ID)
                        .controlType(SINGLE_LINE_TEXT)
                        .content(rSentence(20))
                        .build(),

                MultiLineTextAnswer.builder()
                        .controlId(INTRODUCTION_CONTROL_ID)
                        .controlType(MULTI_LINE_TEXT)
                        .content(rSentence(20))
                        .build());

        SubmissionApi.newSubmission(jwt, appTemplate.getId(), CONFIG_PAGE_ID,

                ItemStatusAnswer.builder()
                        .controlId(STATUS_CONTROL_ID)
                        .controlType(ITEM_STATUS)
                        .optionId(status)
                        .build(),

                DropdownAnswer.builder()
                        .controlId(TEMPLATE_PLAN_TYPE_CONTROL_ID)
                        .controlType(DROPDOWN)
                        .optionIds(List.of(planType))
                        .build(),

                DropdownAnswer.builder()
                        .controlId(FEATURE_CONTROL_ID)
                        .controlType(DROPDOWN)
                        .optionIds(features)
                        .build(),

                DropdownAnswer.builder()
                        .controlId(CATEGORY_CONTROL_ID)
                        .controlType(DROPDOWN)
                        .optionIds(categories)
                        .build(),

                DropdownAnswer.builder()
                        .controlId(SCENARIO_CONTROL_ID)
                        .controlType(DROPDOWN)
                        .optionIds(scenarios)
                        .build(),

                IdentifierAnswer.builder()
                        .controlId(DEMO_QR_CONTROL_ID)
                        .controlType(IDENTIFIER)
                        .content(demoQr.getPlateId())
                        .build(),

                MultiLineTextAnswer.builder()
                        .controlId(PAGE_MODIFIER_CONTROL_ID)
                        .controlType(MULTI_LINE_TEXT)
                        .content(refedApp.getHomePageId() + ":ONCE_PER_INSTANCE:CAN_MANAGE_GROUP\nwhaterer")
                        .build()
        );
        return appTemplate;
    }
}
