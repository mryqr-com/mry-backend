package com.mryqr.core.app;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.TextOption;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedFieldRegistry;
import com.mryqr.common.utils.PagedList;
import com.mryqr.core.app.command.*;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.WebhookSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import com.mryqr.core.app.domain.attribute.AttributeType;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.circulation.StatusAfterSubmission;
import com.mryqr.core.app.domain.circulation.StatusPermission;
import com.mryqr.core.app.domain.config.AppConfig;
import com.mryqr.core.app.domain.event.AppCreatedEvent;
import com.mryqr.core.app.domain.event.AppDeletedEvent;
import com.mryqr.core.app.domain.event.AppGroupSyncEnabledEvent;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.FRichTextInputControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.control.PTimeSegmentControl;
import com.mryqr.core.app.query.*;
import com.mryqr.core.department.DepartmentApi;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.platebatch.PlateBatchApi;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.support.PollingAssertion;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static com.alibaba.excel.support.ExcelTypeEnum.XLSX;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.mryqr.common.domain.permission.Permission.*;
import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.common.event.DomainEventType.*;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.QrWebhookType.ON_CREATE;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.*;
import static com.mryqr.core.app.domain.attribute.AttributeType.*;
import static com.mryqr.core.app.domain.config.AppLandingPageType.DEFAULT;
import static com.mryqr.core.app.domain.page.control.ControlType.TIME_SEGMENT;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.member.MemberApi.createMemberAndLogin;
import static com.mryqr.core.member.domain.Member.newMemberId;
import static com.mryqr.utils.RandomTestFixture.*;
import static com.mryqr.utils.TestUtils.allControlTypesExcept;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.util.CollectionUtils.isEmpty;

class AppControllerApiTest extends BaseApiTest {

    @Test
    public void tenant_admin_can_create_app() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();

        String appName = rAppName();
        CreateAppResponse appResponse = AppApi.createApp(jwt, appName);

        String appId = appResponse.getAppId();
        App app = appRepository.byId(appId);
        assertEquals(appName, app.getName());
        assertTrue(app.isActive());
        assertFalse(app.isLocked());
        assertEquals(app.getSetting().homePage().requiredPermission(), app.requiredPermission());
        assertEquals(AS_TENANT_MEMBER, app.getOperationPermission());
        assertEquals(1, app.getSetting().getPages().size());
        assertTrue(app.getSetting().getPages().get(0).getControls().size() > 0);
        assertNotNull(app.getVersion());
        assertFalse(app.isHasWeeklyResetAttributes());
        assertFalse(app.isHasMonthlyResetAttributes());
        assertFalse(app.isHasSeasonlyResetAttributes());
        assertFalse(app.isHasYearlyResetAttributes());

        List<AppCachedGroup> groups = groupRepository.cachedAppAllGroups(app.getId());
        assertEquals(1, groups.size());
        assertEquals("默认分组", groups.get(0).getName());
        assertEquals(appResponse.getDefaultGroupId(), groups.get(0).getId());

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(appId);
        assertEquals(appId, groupHierarchy.getAppId());
        assertEquals(1, groupHierarchy.groupCount());
        assertEquals(appResponse.getDefaultGroupId(), groupHierarchy.getHierarchy().schemaOf(appResponse.getDefaultGroupId()));
    }

    @Test
    public void create_app_should_raise_event() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());

        AppCreatedEvent event = latestEventFor(response.appId(), APP_CREATED, AppCreatedEvent.class);

        assertEquals(response.appId(), event.getAppId());
        assertEquals(1, tenantRepository.byId(response.tenantId()).getResourceUsage().getAppCount());
    }

    @Test
    public void should_copy_app() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String appName = rAppName();
        CreateAppResponse createAppResponse = AppApi.copyApp(response.jwt(),
                CopyAppCommand.builder().name(appName).sourceAppId(response.appId()).build());

        App copiedApp = appRepository.byId(createAppResponse.getAppId());
        Group group = groupRepository.byId(createAppResponse.getDefaultGroupId());
        assertEquals(appName, copiedApp.getName());
        assertEquals(copiedApp.getId(), group.getAppId());

        App sourceApp = appRepository.byId(response.appId());
        assertEquals(sourceApp.getSetting(), copiedApp.getSetting());

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(copiedApp.getId());
        assertTrue(groupHierarchy.allGroupIds().contains(createAppResponse.getDefaultGroupId()));

        AppCreatedEvent event = latestEventFor(copiedApp.getId(), APP_CREATED, AppCreatedEvent.class);
        assertEquals(copiedApp.getId(), event.getAppId());
    }

    @Test
    public void non_tenant_admin_should_not_copy_app() {
        PreparedAppResponse response = setupApi.registerWithApp();

        CreateMemberResponse memberResponse = createMemberAndLogin(response.jwt());
        AppApi.setAppManagers(response.jwt(), response.appId(), memberResponse.getMemberId());
        assertError(() -> AppApi.copyAppRaw(memberResponse.getJwt(),
                CopyAppCommand.builder().name(rAppName()).sourceAppId(response.appId()).build()), ACCESS_DENIED);
    }

    @Test
    public void should_fail_copy_app_if_name_already_exists() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        String name = rAppName();
        CreateAppResponse appResponse = AppApi.createApp(loginResponse.jwt(), name);
        assertError(
                () -> AppApi.copyAppRaw(loginResponse.jwt(), CopyAppCommand.builder().name(name).sourceAppId(appResponse.getAppId()).build()),
                APP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_copy_app_if_package_not_enough_for_control() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PTimeSegmentControl control = defaultTimeSegmentControlBuilder().build();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Tenant theTenant = tenantRepository.byId(response.tenantId());
        setupApi.updateTenantPlan(theTenant, theTenant.currentPlan().withSupportedControlTypes(allControlTypesExcept(TIME_SEGMENT)));

        assertError(
                () -> AppApi.copyAppRaw(response.jwt(), CopyAppCommand.builder().name(rAppName()).sourceAppId(response.appId()).build()),
                COPY_APP_NOT_ALLOWED);
    }

    @Test
    public void should_fail_copy_app_if_max_app_count_reached() {
        PreparedAppResponse response = setupApi.registerWithApp();

        Tenant tenant = tenantRepository.byId(response.tenantId());
        tenant.setAppCount(tenant.currentPlan().getMaxAppCount(), NO_USER);
        tenantRepository.save(tenant);

        assertError(
                () -> AppApi.copyAppRaw(response.jwt(), CopyAppCommand.builder().name(rAppName()).sourceAppId(response.appId()).build()),
                APP_COUNT_LIMIT_REACHED);
    }

    @Test
    public void app_permission_should_be_same_with_minimum_page_permission() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        Page homePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page childPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).build()).build();

        AppApi.updateAppPages(response.jwt(), response.appId(), homePage, childPage);

        App app = appRepository.byId(response.appId());
        assertEquals(PUBLIC, app.requiredPermission());
    }

    @Test
    public void app_operation_permission_should_be_max_of_app_permission_and_operation_permission() {
        LoginResponse response = setupApi.registerWithLogin();
        CreateAppResponse appResponse = AppApi.createApp(response.jwt(), AS_GROUP_MEMBER, AS_TENANT_MEMBER);

        App app = appRepository.byId(appResponse.getAppId());
        assertEquals(AS_GROUP_MEMBER, app.getOperationPermission());
    }

    @Test
    public void create_app_should_populate_control_indexed_value_registry() {
        PreparedAppResponse response = setupApi.registerWithApp(rEmail(), rPassword());

        FSingleLineTextControl toBeStayControl = defaultSingleLineTextControl();
        FSingleLineTextControl toBeDeleteControl = defaultSingleLineTextControl();
        FSingleLineTextControl toBeAddControl = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), toBeStayControl, toBeDeleteControl);

        App app = appRepository.byId(response.appId());
        assertTrue(app.hasControlIndexKey(response.homePageId(), toBeStayControl.getId()));
        assertTrue(app.hasControlIndexKey(response.homePageId(), toBeDeleteControl.getId()));
        IndexedField stayField = app.indexedFieldForControlOptional(response.homePageId(), toBeStayControl.getId()).get();

        AppApi.updateAppControls(response.jwt(), response.appId(), toBeStayControl, toBeAddControl);
        App updatedApp = appRepository.byId(response.appId());

        assertTrue(updatedApp.hasControlIndexKey(response.homePageId(), toBeStayControl.getId()));
        assertFalse(updatedApp.hasControlIndexKey(response.homePageId(), toBeDeleteControl.getId()));
        assertTrue(updatedApp.hasControlIndexKey(response.homePageId(), toBeAddControl.getId()));

        IndexedField updatedStayField = updatedApp.indexedFieldForControlOptional(response.homePageId(), toBeStayControl.getId()).get();
        assertEquals(stayField, updatedStayField);
    }

    @Test
    public void create_app_should_populate_attribute_index_value_registry() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.appId();

        String tobeStayAttributeId = newAttributeId();
        Attribute tobeStayAttribute = Attribute.builder()
                .id(tobeStayAttributeId)
                .name(rAttributeName())
                .type(INSTANCE_CREATE_TIME)
                .build();

        String tobeDeleteAttributeId = newAttributeId();
        Attribute tobeDeleteAttribute = Attribute.builder()
                .id(tobeDeleteAttributeId)
                .name(rAttributeName())
                .range(AttributeStatisticRange.NO_LIMIT)
                .type(INSTANCE_SUBMIT_COUNT)
                .fixedValue("whatever")
                .build();

        String tobeAddedAttributeId = newAttributeId();
        Attribute tobeAddedAttribute = Attribute.builder()
                .id(tobeAddedAttributeId)
                .name(rAttributeName())
                .type(AttributeType.PAGE_SUBMIT_COUNT)
                .pageId(response.homePageId())
                .range(AttributeStatisticRange.NO_LIMIT)
                .build();

        //insert
        AppApi.updateAppAttributes(response.jwt(), response.appId(), tobeStayAttribute, tobeDeleteAttribute);
        App appAfterInsert = appRepository.byId(appId);
        IndexedFieldRegistry updatedRegistry = appAfterInsert.getAttributeIndexedValueRegistry();
        assertTrue(updatedRegistry.hasKey(tobeStayAttributeId));
        assertTrue(updatedRegistry.hasKey(tobeDeleteAttributeId));
        assertEquals(2, updatedRegistry.size());
        IndexedField tobeStayFieldAfterInsert = updatedRegistry.fieldByKeyOptional(tobeStayAttributeId).get();
        IndexedField tobeDeleteFieldAfterInsert = updatedRegistry.fieldByKeyOptional(tobeDeleteAttributeId).get();

        //add
        AppApi.updateAppAttributes(response.jwt(), response.appId(), tobeStayAttribute, tobeDeleteAttribute, tobeAddedAttribute);
        App appAfterAdd = appRepository.byId(appId);
        IndexedFieldRegistry registryAfterAdd = appAfterAdd.getAttributeIndexedValueRegistry();
        assertTrue(registryAfterAdd.hasKey(tobeStayAttributeId));
        assertTrue(registryAfterAdd.hasKey(tobeDeleteAttributeId));
        assertTrue(registryAfterAdd.hasKey(tobeAddedAttributeId));
        assertEquals(3, registryAfterAdd.size());
        IndexedField tobeStayFieldAfterAdd = registryAfterAdd.fieldByKeyOptional(tobeStayAttributeId).get();
        IndexedField tobeDeleteFieldAfterAdd = registryAfterAdd.fieldByKeyOptional(tobeDeleteAttributeId).get();
        IndexedField tobeAddFieldAfterAdd = registryAfterAdd.fieldByKeyOptional(tobeAddedAttributeId).get();
        assertEquals(tobeStayFieldAfterInsert, tobeStayFieldAfterAdd);
        assertEquals(tobeDeleteFieldAfterInsert, tobeDeleteFieldAfterAdd);

        //delete
        AppApi.updateAppAttributes(response.jwt(), response.appId(), tobeStayAttribute, tobeAddedAttribute);
        App appAfterDelete = appRepository.byId(appId);
        IndexedFieldRegistry registryAfterDelete = appAfterDelete.getAttributeIndexedValueRegistry();
        assertTrue(registryAfterDelete.hasKey(tobeStayAttributeId));
        assertTrue(registryAfterDelete.hasKey(tobeAddedAttributeId));
        assertFalse(registryAfterDelete.hasKey(tobeDeleteAttributeId));
        assertEquals(2, registryAfterDelete.size());
        assertTrue(registryAfterDelete.fieldByKeyOptional(tobeDeleteAttributeId).isEmpty());
        IndexedField tobeStayFieldAfterDelete = registryAfterDelete.fieldByKeyOptional(tobeStayAttributeId).get();
        IndexedField tobeAddFieldAfterDelete = registryAfterDelete.fieldByKeyOptional(tobeAddedAttributeId).get();
        assertEquals(tobeStayFieldAfterInsert, tobeStayFieldAfterDelete);
        assertEquals(tobeAddFieldAfterAdd, tobeAddFieldAfterDelete);
    }

    @Test
    public void create_app_should_populate_ranged_attribute_flag() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());

        Attribute weeklyResetAttribute = Attribute.builder()
                .id(newAttributeId())
                .name(rAttributeName())
                .type(AttributeType.PAGE_SUBMIT_COUNT)
                .pageId(response.homePageId())
                .range(THIS_WEEK)
                .build();

        Attribute monthlyResetAttribute = Attribute.builder()
                .id(newAttributeId())
                .name(rAttributeName())
                .type(AttributeType.PAGE_SUBMIT_COUNT)
                .pageId(response.homePageId())
                .range(THIS_MONTH)
                .build();

        Attribute seasonlyResetAttribute = Attribute.builder()
                .id(newAttributeId())
                .name(rAttributeName())
                .type(AttributeType.PAGE_SUBMIT_COUNT)
                .pageId(response.homePageId())
                .range(THIS_SEASON)
                .build();

        Attribute yearlyResetAttribute = Attribute.builder()
                .id(newAttributeId())
                .name(rAttributeName())
                .type(AttributeType.PAGE_SUBMIT_COUNT)
                .pageId(response.homePageId())
                .range(THIS_YEAR)
                .build();

        AppApi.updateAppAttributes(response.jwt(), response.appId(), weeklyResetAttribute, monthlyResetAttribute, seasonlyResetAttribute,
                yearlyResetAttribute);

        App app = appRepository.byId(response.appId());
        assertTrue(app.isHasWeeklyResetAttributes());
        assertTrue(app.isHasMonthlyResetAttributes());
        assertTrue(app.isHasSeasonlyResetAttributes());
        assertTrue(app.isHasYearlyResetAttributes());
    }

    @Test
    public void non_tenant_admin_should_fail_to_create_app() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());

        CreateMemberResponse memberResponse = createMemberAndLogin(loginResponse.jwt());
        CreateAppCommand command = CreateAppCommand.builder().name(rAppName()).build();

        assertError(() -> AppApi.createAppRaw(memberResponse.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void should_fail_create_app_if_name_already_exists() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();

        String appName = rAppName();
        AppApi.createApp(jwt, appName);
        CreateAppCommand command = CreateAppCommand.builder().name(appName).build();
        assertError(() -> AppApi.createAppRaw(jwt, command), APP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_create_app_if_exceed_packages_limit() {
        LoginResponse admin = setupApi.registerWithLogin(rMobile(), rPassword());
        Tenant tenant = tenantRepository.byId(admin.tenantId());
        tenant.setAppCount(tenant.currentPlan().getMaxAppCount(), NO_USER);
        tenantRepository.save(tenant);

        CreateAppCommand command = CreateAppCommand.builder().name(rAppName()).build();
        assertError(() -> AppApi.createAppRaw(admin.jwt(), command), APP_COUNT_LIMIT_REACHED);
    }

    @Test
    public void tenant_admin_should_rename_app() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();

        String newAppName = rAppName();
        AppApi.renameApp(jwt, appId, RenameAppCommand.builder().name(newAppName).build());

        App app = appRepository.byId(appId);
        assertEquals(newAppName, app.getName());
    }

    @Test
    public void app_manager_should_be_able_to_rename_app() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        String appId = AppApi.createApp(loginResponse.jwt(), rAppName()).getAppId();
        CreateMemberResponse newMember = createMemberAndLogin(loginResponse.jwt(), rMemberName(), rMobile(), rPassword());
        SetAppManagersCommand command = SetAppManagersCommand.builder().managers(newArrayList(newMember.getMemberId())).build();
        AppApi.setAppManagers(loginResponse.jwt(), appId, command);

        String newAppName = rAppName();
        AppApi.renameApp(newMember.getJwt(), appId, RenameAppCommand.builder().name(newAppName).build());

        App app = appRepository.byId(appId);
        assertEquals(newAppName, app.getName());
    }

    @Test
    public void should_fail_rename_app_if_name_already_exists() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        String appName = rAppName();
        AppApi.createApp(jwt, appName);
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();
        RenameAppCommand command = RenameAppCommand.builder().name(appName).build();

        assertError(() -> AppApi.renameAppRaw(jwt, appId, command), APP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void non_app_manager_should_not_rename_app() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        String appId = AppApi.createApp(loginResponse.jwt(), rAppName()).getAppId();
        CreateMemberResponse newMember = createMemberAndLogin(loginResponse.jwt(), rMemberName(), rMobile(), rPassword());

        RenameAppCommand command = RenameAppCommand.builder().name(rAppName()).build();
        assertError(() -> AppApi.renameAppRaw(newMember.getJwt(), appId, command), ACCESS_DENIED);
    }

    @Test
    public void should_deactivate_app() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();

        AppApi.deactivateApp(jwt, appId);
        assertFalse(appRepository.byId(appId).isActive());
    }

    @Test
    public void should_activate_app() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();
        AppApi.deactivateApp(jwt, appId);
        assertFalse(appRepository.byId(appId).isActive());

        AppApi.activateApp(jwt, appId);
        assertTrue(appRepository.byId(appId).isActive());
    }

    @Test
    public void should_lock_and_unlock_app() {
        PreparedAppResponse response = setupApi.registerWithApp(rEmail(), rPassword());
        String appId = response.appId();
        AppApi.lockApp(response.jwt(), appId);
        App app = appRepository.byId(appId);
        assertTrue(app.isLocked());

        AppApi.unlockApp(response.jwt(), appId);
        App unlockedApp = appRepository.byId(appId);
        assertFalse(unlockedApp.isLocked());
    }

    @Test
    public void should_set_app_managers() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        String jwt = loginResponse.jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();

        SetAppManagersCommand command = SetAppManagersCommand.builder().managers(newArrayList(loginResponse.memberId())).build();
        AppApi.setAppManagers(jwt, appId, command);

        App app = appRepository.byId(appId);
        assertTrue(app.getManagers().contains(loginResponse.memberId()));
    }

    @Test
    public void should_fail_set_app_managers_if_has_non_exists_member() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        String jwt = loginResponse.jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();

        String newMemberId = newMemberId();
        SetAppManagersCommand command = SetAppManagersCommand.builder().managers(newArrayList(newMemberId)).build();
        assertError(() -> AppApi.setAppManagersRaw(jwt, appId, command), NOT_ALL_MEMBERS_EXIST);
    }

    @Test
    public void tenant_admin_should_update_app_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppPermission(response.jwt(), response.appId(), AS_GROUP_MEMBER);
        App app = appRepository.byId(response.appId());
        assertEquals(AS_GROUP_MEMBER, app.requiredPermission());
    }

    @Test
    public void should_update_app_setting_if_no_change() {
        PreparedAppResponse response = setupApi.registerWithApp();
        App app = appRepository.byId(response.appId());

        String version = AppApi.updateAppSetting(response.jwt(), response.appId(), app.getVersion(), app.getSetting());

        App updatedApp = appRepository.byId(response.appId());
        assertEquals(version, updatedApp.getVersion());
        assertEquals(version, app.getVersion());
        assertEquals(app.getSetting(), updatedApp.getSetting());
    }

    @Test
    public void should_update_app_config() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.appId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        AppConfig config = AppConfig.builder()
                .homePageId(setting.homePageId())
                .operationPermission(CAN_MANAGE_GROUP)
                .landingPageType(DEFAULT)
                .qrWebhookTypes(newArrayList(ON_CREATE))
                .geolocationEnabled(true)
                .plateBatchEnabled(true)
                .icon(rImageFile())
                .instanceAlias("设备")
                .groupAlias("车间")
                .allowDuplicateInstanceName(true)
                .build();

        ReflectionTestUtils.setField(setting, "config", config);

        AppApi.updateAppSetting(response.jwt(), appId, setting);
        App updatedApp = appRepository.byId(appId);
        assertEquals(config, updatedApp.getSetting().getConfig());
    }

    @Test
    public void app_manager_should_update_app_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt());
        AppApi.setAppManager(response.jwt(), response.appId(), createMemberResponse.getMemberId());

        AppApi.updateAppPermission(createMemberResponse.getJwt(), response.appId(), AS_GROUP_MEMBER);
        App app = appRepository.byId(response.appId());
        assertEquals(AS_GROUP_MEMBER, app.requiredPermission());
    }

    @Test
    public void should_fail_update_app_setting_if_home_page_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.appId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        String nonExistsHomePageId = Page.newPageId();

        ReflectionTestUtils.setField(setting.getConfig(), "homePageId", nonExistsHomePageId);

        assertError(() -> AppApi.updateAppSettingRaw(response.jwt(), appId, app.getVersion(), setting), NO_APP_HOME_PAGE);
    }

    @Test
    public void should_fail_update_setting_if_app_is_locked() {
        PreparedAppResponse response = setupApi.registerWithApp(rEmail(), rPassword());
        AppApi.lockApp(response.jwt(), response.appId());

        App app = appRepository.byId(response.appId());
        AppSetting setting = app.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "instanceAlias", "Alias");

        assertError(() -> AppApi.updateAppSettingRaw(response.jwt(), response.appId(), app.getVersion(), setting), APP_ALREADY_LOCKED);
    }

    @Test
    public void should_fail_update_setting_if_version_mismatch() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "instanceAlias", "Alias");
        assertError(() -> AppApi.updateAppSettingRaw(jwt, appId, "10000", setting), APP_ALREADY_UPDATED);
    }

    @Test
    public void should_fail_update_if_instance_alias_too_short() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "instanceAlias", "A");
        assertError(() -> AppApi.updateAppSettingRaw(jwt, appId, app.getVersion(), setting), INSTANCE_ALIAS_TOO_SHORT);
    }

    @Test
    public void should_fail_update_if_group_alias_too_short() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "groupAlias", "A");
        assertError(() -> AppApi.updateAppSettingRaw(jwt, appId, app.getVersion(), setting), GROUP_ALIAS_TOO_SHORT);
    }

    @Test
    public void should_fail_update_if_custom_id_alias_too_short() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "customIdAlias", "A");
        assertError(() -> AppApi.updateAppSettingRaw(jwt, appId, app.getVersion(), setting), CUSTOM_ID_ALIAS_TOO_SHORT);
    }

    @Test
    public void should_fail_update_if_instance_alias_not_allowed() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "instanceAlias", "应用");
        assertError(() -> AppApi.updateAppSettingRaw(jwt, appId, app.getVersion(), setting), INSTANCE_ALIAS_NOT_ALLOWED);
    }

    @Test
    public void should_fail_update_if_group_alias_not_allowed() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        String appId = AppApi.createApp(jwt, rAppName()).getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "groupAlias", "账户");
        assertError(() -> AppApi.updateAppSettingRaw(jwt, appId, app.getVersion(), setting), GROUP_ALIAS_NOT_ALLOWED);
    }

    @Test
    public void should_delete_app() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        String jwt = loginResponse.jwt();
        CreateAppResponse appResponse = AppApi.createApp(jwt, rAppName());
        String appId = appResponse.getAppId();

        AppApi.deleteApp(jwt, appId);

        Optional<App> app = appRepository.byIdOptional(appId);
        assertFalse(app.isPresent());
    }

    @Test
    public void delete_app_should_raise_event() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        String tenantId = response.tenantId();
        String appId = response.appId();

        FSingleLineTextControl control = defaultSingleLineTextControl();
        SingleLineTextAnswer answer = rAnswer(control);

        AppApi.updateAppControls(response.jwt(), appId, control);
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId(), answer);
        PlateBatchApi.createPlateBatch(response.jwt(), appId, 100);

        assertEquals(1, groupRepository.count(tenantId));
        assertEquals(1, qrRepository.count(tenantId));
        assertEquals(101, plateRepository.count(tenantId));
        assertEquals(1, plateBatchRepository.count(tenantId));
        assertEquals(1, submissionRepository.count(tenantId));
        assertEquals(1, groupHierarchyRepository.count(tenantId));

        Tenant tenant = tenantRepository.byId(tenantId);
        assertEquals(1, tenant.getResourceUsage().getSubmissionCountForApp(appId));
        assertEquals(1, tenant.getResourceUsage().getGroupCountForApp(appId));
        assertEquals(1, tenant.getResourceUsage().getQrCountForApp(appId));
        assertEquals(1, tenant.getResourceUsage().getAppCount());
        assertEquals(1, tenant.getResourceUsage().getQrCountForApp(appId));

        AppApi.deleteApp(response.jwt(), appId);
        AppDeletedEvent event = latestEventFor(appId, APP_DELETED, AppDeletedEvent.class);
        assertEquals(appId, event.getAppId());

        assertEquals(0, groupRepository.count(tenantId));
        assertEquals(0, qrRepository.count(tenantId));
        assertEquals(0, plateRepository.count(tenantId));
        assertEquals(0, plateBatchRepository.count(tenantId));
        assertEquals(0, submissionRepository.count(tenantId));
        assertEquals(0, groupHierarchyRepository.count(tenantId));

        Tenant updatedTenant = tenantRepository.byId(tenantId);
        assertEquals(0, updatedTenant.getResourceUsage().getSubmissionCountForApp(appId));
        assertEquals(0, updatedTenant.getResourceUsage().getGroupCountForApp(appId));
        assertEquals(0, updatedTenant.getResourceUsage().getQrCountForApp(appId));
        assertEquals(0, updatedTenant.getResourceUsage().getAppCount());
        assertEquals(0, updatedTenant.getResourceUsage().getQrCountForApp(appId));
    }

    @Test
    public void root_should_fetch_all_managed_app_list() {
        LoginResponse response = setupApi.registerWithLogin();

        IntStream.rangeClosed(1, 30).forEach(value -> AppApi.createApp(response.jwt(), rAppName()));

        PagedList<QManagedListApp> firstPage = AppApi.listMyManagedApps(response.jwt(),
                ListMyManagedAppsQuery.builder().pageIndex(1).pageSize(20).build());
        assertEquals(20, firstPage.getData().size());
        assertEquals(30, firstPage.getTotalNumber());
        assertEquals(1, firstPage.getPageIndex());
        assertEquals(20, firstPage.getPageSize());
        QManagedListApp aAppListItem = firstPage.getData().get(0);
        assertNotNull(aAppListItem.getId());
        assertNotNull(aAppListItem.getName());
        assertNotNull(aAppListItem.getCreatedAt());
        assertNotNull(aAppListItem.getCreatedBy());
        QManagedListApp firstApp = firstPage.getData().get(0);
        App app = appRepository.byId(firstApp.getId());
        assertEquals(app.getName(), firstApp.getName());
        assertEquals(app.getCreatedBy(), firstApp.getCreatedBy());
        assertEquals(app.getCreatedAt(), firstApp.getCreatedAt());
        assertEquals(app.isActive(), firstApp.isActive());
        assertEquals(app.isLocked(), firstApp.isLocked());

        PagedList<QManagedListApp> secondPage = AppApi.listMyManagedApps(response.jwt(),
                ListMyManagedAppsQuery.builder().pageIndex(2).pageSize(20).build());
        assertEquals(10, secondPage.getData().size());
        assertEquals(30, secondPage.getTotalNumber());
        assertEquals(2, secondPage.getPageIndex());
        assertEquals(20, secondPage.getPageSize());
    }

    @Test
    public void app_manager_should_only_fetch_own_managed_app_list() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        String appName = rAppName();
        String appId = AppApi.createApp(loginResponse.jwt(), appName).getAppId();
        CreateMemberResponse createMemberResponse = createMemberAndLogin(loginResponse.jwt(), rMemberName(), rMobile(), rPassword());
        AppApi.setAppManagers(loginResponse.jwt(), appId, createMemberResponse.getMemberId());
        AppApi.createApp(loginResponse.jwt(), rAppName());

        PagedList<QManagedListApp> list = AppApi.listMyManagedApps(createMemberResponse.getJwt(),
                ListMyManagedAppsQuery.builder().pageIndex(1).pageSize(10).build());

        assertEquals(1, list.getData().size());
        QManagedListApp app = list.getData().get(0);
        assertEquals(appName, app.getName());
        assertEquals(appId, app.getId());
    }

    @Test
    public void should_search_managed_app_list() {
        String mobile = rMobile();
        String password = rPassword();
        String jwt = setupApi.registerWithLogin(mobile, password).jwt();
        AppApi.createApp(jwt, rAppName());
        String name = rAppName();
        CreateAppResponse app = AppApi.createApp(jwt, name);

        PagedList<QManagedListApp> list = AppApi.listMyManagedApps(jwt,
                ListMyManagedAppsQuery.builder().search(name.substring(1)).pageIndex(1).pageSize(10).build());
        assertEquals(1, list.getData().size());
        QManagedListApp listedApp = list.getData().get(0);
        assertEquals(app.getAppId(), listedApp.getId());
        assertEquals(name, listedApp.getName());
    }

    @Test
    public void should_search_app_id_for_app_list() {
        String mobile = rMobile();
        String password = rPassword();
        String jwt = setupApi.registerWithLogin(mobile, password).jwt();
        AppApi.createApp(jwt, rAppName());
        String name = rAppName();
        CreateAppResponse app = AppApi.createApp(jwt, name);

        PagedList<QManagedListApp> list = AppApi.listMyManagedApps(jwt,
                ListMyManagedAppsQuery.builder().search(app.getAppId()).pageIndex(1).pageSize(10).build());
        assertEquals(1, list.getData().size());
        QManagedListApp listedApp = list.getData().get(0);
        assertEquals(app.getAppId(), listedApp.getId());
        assertEquals(name, listedApp.getName());
    }

    @Test
    public void tenant_admin_should_fetch_own_viewable_apps() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        String loginResponseJwt = loginResponse.jwt();

        CreateAppResponse appResponse1 = AppApi.createApp(loginResponseJwt, AS_TENANT_MEMBER, AS_TENANT_MEMBER);
        CreateAppResponse appResponse2 = AppApi.createApp(loginResponseJwt, AS_TENANT_MEMBER, AS_GROUP_MEMBER);
        CreateAppResponse appResponse3 = AppApi.createApp(loginResponseJwt, AS_TENANT_MEMBER, CAN_MANAGE_GROUP);
        CreateAppResponse appResponse4 = AppApi.createApp(loginResponseJwt, AS_TENANT_MEMBER, CAN_MANAGE_APP);

        List<QViewableListApp> appSummaries = AppApi.myViewableApps(loginResponseJwt);

        assertEquals(4, appSummaries.size());
        assertTrue(appSummaries.stream().anyMatch(s -> s.getId().equals(appResponse1.getAppId())));
        assertTrue(appSummaries.stream().anyMatch(s -> s.getId().equals(appResponse2.getAppId())));
        assertTrue(appSummaries.stream().anyMatch(s -> s.getId().equals(appResponse3.getAppId())));
        assertTrue(appSummaries.stream().anyMatch(s -> s.getId().equals(appResponse4.getAppId())));

        QViewableListApp aAppSummary = appSummaries.get(0);
        assertNotNull(aAppSummary.getId());
        assertNotNull(aAppSummary.getName());
        assertFalse(aAppSummary.isLocked());
    }

    @Test
    public void app_manager_should_fetch_own_viewable_apps() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        CreateMemberResponse memberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());

        CreateAppResponse appResponse1 = AppApi.createApp(response.jwt(), AS_TENANT_MEMBER, CAN_MANAGE_APP);
        CreateAppResponse appResponse2 = AppApi.createApp(response.jwt(), AS_TENANT_MEMBER, CAN_MANAGE_APP);

        AppApi.setAppManagers(response.jwt(), appResponse1.getAppId(),
                SetAppManagersCommand.builder().managers(newArrayList(memberResponse.getMemberId())).build());

        List<QViewableListApp> appSummaries = AppApi.myViewableApps(memberResponse.getJwt());
        assertEquals(1, appSummaries.size());
        assertTrue(appSummaries.stream().anyMatch(s -> s.getId().equals(appResponse1.getAppId())));
    }

    @Test
    public void group_member_should_fetch_own_viewable_apps() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        CreateMemberResponse memberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());

        CreateAppResponse appResponse1 = AppApi.createApp(response.jwt(), AS_TENANT_MEMBER, AS_GROUP_MEMBER);
        GroupApi.addGroupMembers(response.jwt(), appResponse1.getDefaultGroupId(), memberResponse.getMemberId());
        CreateAppResponse appResponse2 = AppApi.createApp(response.jwt(), AS_TENANT_MEMBER, CAN_MANAGE_GROUP);

        List<QViewableListApp> appSummaries = AppApi.myViewableApps(memberResponse.getJwt());
        assertEquals(1, appSummaries.size());
        assertTrue(appSummaries.stream().anyMatch(s -> s.getId().equals(appResponse1.getAppId())));
    }

    @Test
    public void tenant_common_member_should_fetch_own_viewable_apps() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        CreateMemberResponse memberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());

        CreateAppResponse appResponse1 = AppApi.createApp(response.jwt(), AS_TENANT_MEMBER, AS_TENANT_MEMBER);
        CreateAppResponse appResponse2 = AppApi.createApp(response.jwt(), AS_TENANT_MEMBER, CAN_MANAGE_APP);

        List<QViewableListApp> appSummaries = AppApi.myViewableApps(memberResponse.getJwt());
        assertEquals(1, appSummaries.size());
        assertTrue(appSummaries.stream().anyMatch(s -> s.getId().equals(appResponse1.getAppId())));
    }

    @Test
    public void non_app_manager_should_only_view_active_apps() {
        PreparedAppResponse response = setupApi.registerWithApp(rEmail(), rPassword());
        AppApi.deactivateApp(response.jwt(), response.appId());
        CreateMemberResponse memberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());

        List<QViewableListApp> appSummaries = AppApi.myViewableApps(memberResponse.getJwt());
        assertTrue(appSummaries.isEmpty());
    }

    @Test
    public void app_manager_should_view_inactive_apps() {
        PreparedAppResponse response = setupApi.registerWithApp(rEmail(), rPassword());
        AppApi.deactivateApp(response.jwt(), response.appId());

        List<QViewableListApp> appSummaries = AppApi.myViewableApps(response.jwt());
        assertEquals(1, appSummaries.size());
    }

    @Test
    public void should_view_apps_based_on_operation_permission() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), CAN_MANAGE_APP);

        CreateMemberResponse memberResponse = createMemberAndLogin(response.jwt());
        assertTrue(isEmpty(AppApi.myViewableApps(memberResponse.getJwt())));

        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), AS_TENANT_MEMBER);
        assertTrue(isNotEmpty(AppApi.myViewableApps(memberResponse.getJwt())));
    }

    @Test
    public void tenant_admin_should_fetch_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Page nonFillablePage = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(PUBLIC).build())
                .build();
        Page publicFillablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page nonPublicFillablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_GROUP_MEMBER).build()).build();
        Page approvablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_GROUP_MEMBER)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).build()).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), nonFillablePage, publicFillablePage, nonPublicFillablePage,
                approvablePage);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        String subGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), groupId);

        QOperationalApp qOperationalApp = AppApi.fetchOperationalApp(response.jwt(), response.appId());

        assertEquals(response.appId(), qOperationalApp.getId());
        assertNotNull(qOperationalApp.getSetting());
        assertNotNull(qOperationalApp.getReportSetting());
        assertTrue(qOperationalApp.isCanManageApp());
        Set<String> allGroupIds = newHashSet(response.defaultGroupId(), groupId, subGroupId);
        Map<String, String> groupFullNames = qOperationalApp.getGroupFullNames();
        assertTrue(groupFullNames.keySet().containsAll(allGroupIds));
        Group defaultGroup = groupRepository.byId(response.defaultGroupId());
        Group group = groupRepository.byId(groupId);
        Group subGroup = groupRepository.byId(subGroupId);
        assertEquals(defaultGroup.getName(), groupFullNames.get(defaultGroup.getId()));
        assertEquals(group.getName(), groupFullNames.get(group.getId()));
        assertEquals(group.getName() + "/" + subGroup.getName(), groupFullNames.get(subGroup.getId()));

        assertTrue(qOperationalApp.getViewableGroupIds().containsAll(allGroupIds));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(publicFillablePage.getId()));
        assertTrue(qOperationalApp.getViewablePageIds().contains(nonPublicFillablePage.getId()));
        assertTrue(qOperationalApp.getViewablePageIds().contains(approvablePage.getId()));
        assertTrue(qOperationalApp.getManagableGroupIds().containsAll(allGroupIds));
        assertFalse(qOperationalApp.getManagablePageIds().contains(nonFillablePage.getId()));
        assertTrue(qOperationalApp.getManagablePageIds().contains(publicFillablePage.getId()));
        assertTrue(qOperationalApp.getManagablePageIds().contains(nonPublicFillablePage.getId()));
        assertTrue(qOperationalApp.getManagablePageIds().contains(approvablePage.getId()));
        assertTrue(qOperationalApp.getApprovableGroupIds().containsAll(allGroupIds));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(publicFillablePage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(nonPublicFillablePage.getId()));
        assertTrue(qOperationalApp.getApprovablePageIds().contains(approvablePage.getId()));
    }

    @Test
    public void app_manager_should_fetch_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Page nonFillablePage = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(PUBLIC).build())
                .build();
        Page publicFillablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page nonPublicFillablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();
        Page approvablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_GROUP_MEMBER)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).build()).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), nonFillablePage, publicFillablePage, nonPublicFillablePage,
                approvablePage);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());
        AppApi.setAppManagers(response.jwt(), response.appId(), createMemberResponse.getMemberId());

        String subGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), groupId);

        QOperationalApp qOperationalApp = AppApi.fetchOperationalApp(createMemberResponse.getJwt(), response.appId());

        assertEquals(response.appId(), qOperationalApp.getId());
        assertNotNull(qOperationalApp.getSetting());
        assertTrue(qOperationalApp.isCanManageApp());
        Set<String> allGroupIds = newHashSet(response.defaultGroupId(), groupId, subGroupId);
        assertTrue(qOperationalApp.getGroupFullNames().keySet().containsAll(allGroupIds));
        assertTrue(qOperationalApp.getViewableGroupIds().containsAll(allGroupIds));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(publicFillablePage.getId()));
        assertTrue(qOperationalApp.getViewablePageIds().contains(nonPublicFillablePage.getId()));
        assertTrue(qOperationalApp.getViewablePageIds().contains(approvablePage.getId()));
        assertTrue(qOperationalApp.getManagableGroupIds().containsAll(allGroupIds));
        assertFalse(qOperationalApp.getManagablePageIds().contains(nonFillablePage.getId()));
        assertTrue(qOperationalApp.getManagablePageIds().contains(publicFillablePage.getId()));
        assertTrue(qOperationalApp.getManagablePageIds().contains(nonPublicFillablePage.getId()));
        assertTrue(qOperationalApp.getManagablePageIds().contains(approvablePage.getId()));
        assertTrue(qOperationalApp.getApprovableGroupIds().containsAll(allGroupIds));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(publicFillablePage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(nonPublicFillablePage.getId()));
        assertTrue(qOperationalApp.getApprovablePageIds().contains(approvablePage.getId()));
    }

    @Test
    public void group_manager_should_fetch_group_manager_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), CAN_MANAGE_GROUP);
        Page nonFillablePage = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(PUBLIC).build())
                .build();
        Page publicPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page permissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        Page nonPermissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();
        Page canApprovePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_GROUP).build()).build()).build();
        Page cannotApprovePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), permissionedPage,
                nonFillablePage,
                publicPage,
                nonPermissionedPage,
                canApprovePage,
                cannotApprovePage);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        String subGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), groupId);
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());
        GroupApi.addGroupManagers(response.jwt(), groupId, createMemberResponse.getMemberId());

        QOperationalApp qOperationalApp = AppApi.fetchOperationalApp(createMemberResponse.getJwt(), response.appId());

        assertEquals(response.appId(), qOperationalApp.getId());
        assertNotNull(qOperationalApp.getSetting());
        assertFalse(qOperationalApp.isCanManageApp());
        Set<String> resultGroupIds = Set.of(groupId, subGroupId);
        Set<String> operationalAppGroupIds = qOperationalApp.getGroupFullNames().keySet();
        assertTrue(operationalAppGroupIds.containsAll(resultGroupIds));
        assertFalse(operationalAppGroupIds.contains(response.defaultGroupId()));

        assertTrue(qOperationalApp.getViewableGroupIds().containsAll(resultGroupIds));
        assertFalse(qOperationalApp.getViewableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonPermissionedPage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(publicPage.getId()));
        assertTrue(qOperationalApp.getViewablePageIds().containsAll(newHashSet(
                permissionedPage.getId(),
                canApprovePage.getId(),
                cannotApprovePage.getId()
        )));

        assertTrue(qOperationalApp.getManagableGroupIds().containsAll(resultGroupIds));
        assertFalse(qOperationalApp.getManagableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getManagablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getManagablePageIds().contains(nonPermissionedPage.getId()));
        assertTrue(qOperationalApp.getManagablePageIds().containsAll(newHashSet(
                publicPage.getId(),
                permissionedPage.getId(),
                canApprovePage.getId(),
                cannotApprovePage.getId()
        )));

        assertTrue(qOperationalApp.getApprovableGroupIds().containsAll(resultGroupIds));
        assertFalse(qOperationalApp.getApprovableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(nonPermissionedPage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(publicPage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(permissionedPage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(cannotApprovePage.getId()));
        assertTrue(qOperationalApp.getApprovablePageIds().contains(canApprovePage.getId()));
    }

    @Test
    public void group_manager_should_fetch_group_member_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), AS_GROUP_MEMBER);
        Page nonFillablePage = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(PUBLIC).build())
                .build();
        Page publicPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page permissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_GROUP_MEMBER).build()).build();
        Page nonPermissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();
        Page canApprovePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_GROUP).build()).build()).build();
        Page cannotApprovePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), permissionedPage,
                nonFillablePage,
                publicPage,
                nonPermissionedPage,
                canApprovePage,
                cannotApprovePage);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        String subGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), groupId);

        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());
        GroupApi.addGroupManagers(response.jwt(), groupId, createMemberResponse.getMemberId());

        QOperationalApp qOperationalApp = AppApi.fetchOperationalApp(createMemberResponse.getJwt(), response.appId());

        assertEquals(response.appId(), qOperationalApp.getId());
        assertNotNull(qOperationalApp.getSetting());
        assertFalse(qOperationalApp.isCanManageApp());
        Set<String> resultGroupIds = Set.of(groupId, subGroupId);
        Set<String> operationalAppGroupIds = qOperationalApp.getGroupFullNames().keySet();
        assertTrue(operationalAppGroupIds.containsAll(resultGroupIds));
        assertFalse(operationalAppGroupIds.contains(response.defaultGroupId()));

        assertTrue(qOperationalApp.getViewableGroupIds().containsAll(resultGroupIds));
        assertFalse(qOperationalApp.getViewableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonPermissionedPage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(publicPage.getId()));
        assertTrue(qOperationalApp.getViewablePageIds().containsAll(newHashSet(
                permissionedPage.getId(),
                canApprovePage.getId(),
                cannotApprovePage.getId()
        )));

        assertTrue(qOperationalApp.getManagableGroupIds().containsAll(resultGroupIds));
        assertFalse(qOperationalApp.getManagableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getManagablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getManagablePageIds().contains(nonPermissionedPage.getId()));
        assertTrue(qOperationalApp.getManagablePageIds().containsAll(newHashSet(
                publicPage.getId(),
                permissionedPage.getId(),
                canApprovePage.getId(),
                cannotApprovePage.getId()
        )));

        assertTrue(qOperationalApp.getApprovableGroupIds().containsAll(resultGroupIds));
        assertFalse(qOperationalApp.getApprovableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(nonPermissionedPage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(publicPage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(permissionedPage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(cannotApprovePage.getId()));
        assertTrue(qOperationalApp.getApprovablePageIds().contains(canApprovePage.getId()));
    }

    @Test
    public void group_manager_should_fetch_tenant_member_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), AS_TENANT_MEMBER);
        Page nonFillablePage = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(PUBLIC).build())
                .build();
        Page publicPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page permissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).build()).build();
        Page nonPermissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();
        Page canApprovePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_GROUP).build()).build()).build();
        Page cannotApprovePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), permissionedPage,
                nonFillablePage,
                publicPage,
                nonPermissionedPage,
                canApprovePage,
                cannotApprovePage);

        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        String subGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), groupId);

        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());
        GroupApi.addGroupManagers(response.jwt(), groupId, createMemberResponse.getMemberId());

        QOperationalApp qOperationalApp = AppApi.fetchOperationalApp(createMemberResponse.getJwt(), response.appId());

        assertEquals(response.appId(), qOperationalApp.getId());
        assertNotNull(qOperationalApp.getSetting());
        assertFalse(qOperationalApp.isCanManageApp());
        Set<String> allGroupIds = newHashSet(response.defaultGroupId(), groupId, subGroupId);
        assertTrue(qOperationalApp.getGroupFullNames().keySet().containsAll(allGroupIds));

        assertTrue(qOperationalApp.getViewableGroupIds().containsAll(allGroupIds));
        assertTrue(qOperationalApp.getViewableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonPermissionedPage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(publicPage.getId()));
        assertTrue(qOperationalApp.getViewablePageIds().containsAll(newHashSet(
                permissionedPage.getId(),
                canApprovePage.getId(),
                cannotApprovePage.getId()
        )));

        assertTrue(qOperationalApp.getManagableGroupIds().containsAll(Set.of(groupId, subGroupId)));
        assertFalse(qOperationalApp.getManagableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getManagablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getManagablePageIds().contains(nonPermissionedPage.getId()));
        assertTrue(qOperationalApp.getManagablePageIds().containsAll(newHashSet(
                publicPage.getId(),
                permissionedPage.getId(),
                canApprovePage.getId(),
                cannotApprovePage.getId()
        )));

        assertTrue(qOperationalApp.getApprovableGroupIds().containsAll(Set.of(groupId, subGroupId)));
        assertFalse(qOperationalApp.getApprovableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(nonPermissionedPage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(publicPage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(permissionedPage.getId()));
        assertFalse(qOperationalApp.getApprovablePageIds().contains(cannotApprovePage.getId()));
        assertTrue(qOperationalApp.getApprovablePageIds().contains(canApprovePage.getId()));
    }

    @Test
    public void fetch_operational_app_should_exclude_archived_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());

        QOperationalApp operationalApp = AppApi.fetchOperationalApp(response.jwt(), response.appId());
        assertTrue(operationalApp.getManagableGroupIds().contains(response.defaultGroupId()));
        assertTrue(operationalApp.getViewableGroupIds().contains(response.defaultGroupId()));
        assertTrue(operationalApp.getGroupFullNames().keySet().containsAll(List.of(response.defaultGroupId(), anotherGroupId)));
        assertTrue(operationalApp.getManagableGroupIds().contains(anotherGroupId));
        assertTrue(operationalApp.getViewableGroupIds().contains(anotherGroupId));

        GroupApi.archiveGroup(response.jwt(), response.defaultGroupId());
        QOperationalApp updatedOperationalApp = AppApi.fetchOperationalApp(response.jwt(), response.appId());
        assertFalse(updatedOperationalApp.getManagableGroupIds().contains(response.defaultGroupId()));
        assertFalse(updatedOperationalApp.getViewableGroupIds().contains(response.defaultGroupId()));
        assertFalse(updatedOperationalApp.getGroupFullNames().containsKey(response.defaultGroupId()));
        assertTrue(updatedOperationalApp.getManagableGroupIds().contains(anotherGroupId));
        assertTrue(updatedOperationalApp.getViewableGroupIds().contains(anotherGroupId));
        assertTrue(updatedOperationalApp.getGroupFullNames().containsKey(anotherGroupId));
    }

    @Test
    public void fetch_operational_app_should_exclude_inactive_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());

        QOperationalApp operationalApp = AppApi.fetchOperationalApp(response.jwt(), response.appId());
        assertTrue(operationalApp.getManagableGroupIds().contains(response.defaultGroupId()));
        assertTrue(operationalApp.getViewableGroupIds().contains(response.defaultGroupId()));
        assertTrue(operationalApp.getGroupFullNames().keySet().containsAll(List.of(response.defaultGroupId(), anotherGroupId)));
        assertTrue(operationalApp.getManagableGroupIds().contains(anotherGroupId));
        assertTrue(operationalApp.getViewableGroupIds().contains(anotherGroupId));

        GroupApi.deactivateGroup(response.jwt(), response.defaultGroupId());
        QOperationalApp updatedOperationalApp = AppApi.fetchOperationalApp(response.jwt(), response.appId());
        assertFalse(updatedOperationalApp.getManagableGroupIds().contains(response.defaultGroupId()));
        assertFalse(updatedOperationalApp.getViewableGroupIds().contains(response.defaultGroupId()));
        assertFalse(updatedOperationalApp.getGroupFullNames().containsKey(response.defaultGroupId()));
        assertTrue(updatedOperationalApp.getManagableGroupIds().contains(anotherGroupId));
        assertTrue(updatedOperationalApp.getViewableGroupIds().contains(anotherGroupId));
        assertTrue(updatedOperationalApp.getGroupFullNames().containsKey(anotherGroupId));
    }

    @Test
    public void fetch_operational_app_should_exclude_invisible_groups_for_group_manager_permission() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), CAN_MANAGE_GROUP);

        String managedGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), response.defaultGroupId());
        String deactivatedGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), managedGroupId);
        String archivedGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), managedGroupId);

        CreateMemberResponse memberResponse = createMemberAndLogin(response.jwt());
        GroupApi.addGroupManagers(response.jwt(), managedGroupId, memberResponse.getMemberId());
        GroupApi.deactivateGroup(response.jwt(), deactivatedGroupId);
        GroupApi.archiveGroup(response.jwt(), archivedGroupId);

        QOperationalApp operationalApp = AppApi.fetchOperationalApp(memberResponse.getJwt(), response.appId());
        assertTrue(operationalApp.getManagableGroupIds().contains(managedGroupId));
        assertFalse(operationalApp.getManagableGroupIds().contains(deactivatedGroupId));
        assertFalse(operationalApp.getManagableGroupIds().contains(archivedGroupId));
    }

    @Test
    public void group_manager_should_fail_fetch_app_manager_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), CAN_MANAGE_APP);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());
        GroupApi.addGroupManagers(response.jwt(), groupId, createMemberResponse.getMemberId());

        assertError(() -> AppApi.fetchOperationalAppRaw(createMemberResponse.getJwt(), response.appId()), ACCESS_DENIED);
    }

    @Test
    public void group_member_should_fetch_group_member_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), AS_GROUP_MEMBER);
        Page nonFillablePage = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(PUBLIC).build())
                .build();
        Page publicPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page permissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_GROUP_MEMBER).build()).build();
        Page nonPermissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();
        Page approvablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_GROUP_MEMBER)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_GROUP).build()).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), permissionedPage,
                nonFillablePage,
                publicPage,
                nonPermissionedPage,
                approvablePage);

        String parentGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        String groupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), parentGroupId);
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());
        GroupApi.addGroupMembers(response.jwt(), groupId, createMemberResponse.getMemberId());

        QOperationalApp qOperationalApp = AppApi.fetchOperationalApp(createMemberResponse.getJwt(), response.appId());

        assertEquals(response.appId(), qOperationalApp.getId());
        assertNotNull(qOperationalApp.getSetting());
        assertFalse(qOperationalApp.isCanManageApp());
        Set<String> operationalAppGroupIds = qOperationalApp.getGroupFullNames().keySet();
        assertTrue(operationalAppGroupIds.containsAll(List.of(parentGroupId, groupId)));
        assertFalse(operationalAppGroupIds.contains(response.defaultGroupId()));

        assertTrue(qOperationalApp.getViewableGroupIds().containsAll(List.of(parentGroupId, groupId)));
        assertFalse(qOperationalApp.getViewableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonPermissionedPage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(publicPage.getId()));
        assertTrue(qOperationalApp.getViewablePageIds().containsAll(newHashSet(
                permissionedPage.getId(),
                approvablePage.getId()
        )));

        assertTrue(qOperationalApp.getManagableGroupIds().isEmpty());
        assertTrue(qOperationalApp.getManagablePageIds().isEmpty());

        assertTrue(qOperationalApp.getApprovableGroupIds().isEmpty());
        assertTrue(qOperationalApp.getApprovablePageIds().isEmpty());
    }

    @Test
    public void group_member_should_fetch_tenant_member_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), AS_TENANT_MEMBER);
        Page nonFillablePage = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(PUBLIC).build())
                .build();
        Page publicPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page permissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).build()).build();
        Page nonPermissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();
        Page approvablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_GROUP_MEMBER)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_GROUP).build()).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), permissionedPage,
                nonFillablePage,
                publicPage,
                nonPermissionedPage,
                approvablePage);

        String parentGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        String groupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), parentGroupId);
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());
        GroupApi.addGroupMembers(response.jwt(), groupId, createMemberResponse.getMemberId());

        QOperationalApp qOperationalApp = AppApi.fetchOperationalApp(createMemberResponse.getJwt(), response.appId());

        assertEquals(response.appId(), qOperationalApp.getId());
        assertNotNull(qOperationalApp.getSetting());
        assertFalse(qOperationalApp.isCanManageApp());
        Set<String> allGroupIds = newHashSet(response.defaultGroupId(), groupId, parentGroupId);
        assertTrue(qOperationalApp.getGroupFullNames().keySet().containsAll(allGroupIds));

        assertTrue(qOperationalApp.getViewableGroupIds().containsAll(allGroupIds));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonPermissionedPage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(publicPage.getId()));
        assertTrue(qOperationalApp.getViewablePageIds().containsAll(newHashSet(
                permissionedPage.getId(),
                approvablePage.getId()
        )));

        assertTrue(qOperationalApp.getManagableGroupIds().isEmpty());
        assertTrue(qOperationalApp.getManagablePageIds().isEmpty());

        assertTrue(qOperationalApp.getApprovableGroupIds().isEmpty());
        assertTrue(qOperationalApp.getApprovablePageIds().isEmpty());
    }

    @Test
    public void should_fetch_operational_app_without_invisible_groups_for_group_member_permission() {
        PreparedAppResponse response = setupApi.registerWithApp();

        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), AS_GROUP_MEMBER);

        String managedGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        String deactivatedGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), managedGroupId);
        String archivedGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), managedGroupId);

        String memberParentGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        String asMemberGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), memberParentGroupId);

        CreateMemberResponse memberResponse = createMemberAndLogin(response.jwt());
        GroupApi.addGroupManagers(response.jwt(), managedGroupId, memberResponse.getMemberId());
        GroupApi.addGroupMembers(response.jwt(), asMemberGroupId, memberResponse.getMemberId());
        GroupApi.deactivateGroup(response.jwt(), deactivatedGroupId);
        GroupApi.archiveGroup(response.jwt(), archivedGroupId);
        GroupApi.archiveGroup(response.jwt(), asMemberGroupId);

        QOperationalApp operationalApp = AppApi.fetchOperationalApp(memberResponse.getJwt(), response.appId());
        assertEquals(1, operationalApp.getManagableGroupIds().size());
        assertTrue(operationalApp.getManagableGroupIds().contains(managedGroupId));

        assertEquals(2, operationalApp.getViewableGroupIds().size());
        assertTrue(operationalApp.getViewableGroupIds().containsAll(Set.of(managedGroupId, memberParentGroupId)));
    }

    @Test
    public void group_member_should_fail_fetch_app_manager_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), CAN_MANAGE_APP);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());
        GroupApi.addGroupMembers(response.jwt(), groupId, createMemberResponse.getMemberId());

        assertError(() -> AppApi.fetchOperationalAppRaw(createMemberResponse.getJwt(), response.appId()), ACCESS_DENIED);
    }

    @Test
    public void group_member_should_fail_fetch_group_manager_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), CAN_MANAGE_GROUP);
        Page nonPermissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), nonPermissionedPage);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());
        GroupApi.addGroupMembers(response.jwt(), groupId, createMemberResponse.getMemberId());

        assertError(() -> AppApi.fetchOperationalAppRaw(createMemberResponse.getJwt(), response.appId()), ACCESS_DENIED);
    }

    @Test
    public void tenant_member_should_fetch_tenant_member_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), AS_TENANT_MEMBER);
        Page nonFillablePage = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(PUBLIC).build())
                .build();
        Page publicPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page permissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).build()).build();
        Page nonPermissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();
        Page approvablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER)
                .approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_GROUP).build()).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), permissionedPage,
                nonFillablePage,
                publicPage,
                nonPermissionedPage,
                approvablePage);

        String parentGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        String groupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), parentGroupId);
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());

        QOperationalApp qOperationalApp = AppApi.fetchOperationalApp(createMemberResponse.getJwt(), response.appId());

        assertEquals(response.appId(), qOperationalApp.getId());
        assertNotNull(qOperationalApp.getSetting());
        assertFalse(qOperationalApp.isCanManageApp());
        Set<String> allGroupIds = newHashSet(response.defaultGroupId(), groupId, parentGroupId);
        assertTrue(qOperationalApp.getGroupFullNames().keySet().containsAll(allGroupIds));

        assertTrue(qOperationalApp.getViewableGroupIds().containsAll(allGroupIds));
        assertTrue(qOperationalApp.getViewableGroupIds().contains(response.defaultGroupId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonFillablePage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(nonPermissionedPage.getId()));
        assertFalse(qOperationalApp.getViewablePageIds().contains(publicPage.getId()));
        assertTrue(qOperationalApp.getViewablePageIds().containsAll(newHashSet(
                permissionedPage.getId(),
                approvablePage.getId()
        )));

        assertTrue(qOperationalApp.getManagableGroupIds().isEmpty());
        assertTrue(qOperationalApp.getManagablePageIds().isEmpty());

        assertTrue(qOperationalApp.getApprovableGroupIds().isEmpty());
        assertTrue(qOperationalApp.getApprovablePageIds().isEmpty());
    }

    @Test
    public void should_fetch_operational_app_without_invisible_groups_for_tenant_member_permission() {
        PreparedAppResponse response = setupApi.registerWithApp();

        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), AS_TENANT_MEMBER);

        String managedGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        String deactivatedGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), managedGroupId);
        String archivedGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), managedGroupId);

        String memberParentGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        String asMemberGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), memberParentGroupId);

        CreateMemberResponse memberResponse = createMemberAndLogin(response.jwt());
        GroupApi.addGroupManagers(response.jwt(), managedGroupId, memberResponse.getMemberId());
        GroupApi.addGroupMembers(response.jwt(), asMemberGroupId, memberResponse.getMemberId());
        GroupApi.deactivateGroup(response.jwt(), deactivatedGroupId);
        GroupApi.archiveGroup(response.jwt(), archivedGroupId);
        GroupApi.archiveGroup(response.jwt(), asMemberGroupId);

        QOperationalApp operationalApp = AppApi.fetchOperationalApp(memberResponse.getJwt(), response.appId());
        assertEquals(1, operationalApp.getManagableGroupIds().size());
        assertTrue(operationalApp.getManagableGroupIds().contains(managedGroupId));

        assertEquals(3, operationalApp.getViewableGroupIds().size());
        assertTrue(operationalApp.getViewableGroupIds().containsAll(Set.of(managedGroupId, memberParentGroupId, response.defaultGroupId())));
    }

    @Test
    public void tenant_member_should_fail_fetch_app_manager_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), CAN_MANAGE_APP);
        Page nonPermissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), nonPermissionedPage);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());

        assertError(() -> AppApi.fetchOperationalAppRaw(createMemberResponse.getJwt(), response.appId()), ACCESS_DENIED);
    }

    @Test
    public void tenant_member_should_fail_fetch_group_manager_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), CAN_MANAGE_GROUP);
        Page nonPermissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), nonPermissionedPage);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());

        assertError(() -> AppApi.fetchOperationalAppRaw(createMemberResponse.getJwt(), response.appId()), ACCESS_DENIED);
    }

    @Test
    public void tenant_member_should_fail_fetch_group_member_required_operational_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.jwt(), response.appId(), AS_GROUP_MEMBER);
        Page nonPermissionedPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_GROUP_MEMBER).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), nonPermissionedPage);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId());
        CreateMemberResponse createMemberResponse = createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());

        assertError(() -> AppApi.fetchOperationalAppRaw(createMemberResponse.getJwt(), response.appId()), ACCESS_DENIED);
    }

    @Test
    public void should_fetch_updatable_app() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        String appName = rAppName();
        String appId = AppApi.createApp(loginResponse.jwt(), appName).getAppId();
        QUpdatableApp appDetail = AppApi.fetchUpdatableApp(loginResponse.jwt(), appId);
        assertEquals(appId, appDetail.getId());
        assertEquals(appName, appDetail.getName());
        assertEquals(loginResponse.tenantId(), appDetail.getTenantId());
        assertNotNull(appDetail.getSetting());
    }

    @Test
    public void should_fail_fetch_updatable_app_if_app_is_locked() {
        PreparedAppResponse response = setupApi.registerWithApp(rEmail(), rPassword());
        AppApi.lockApp(response.jwt(), response.appId());
        assertError(() -> AppApi.fetchUpdatableAppRaw(response.jwt(), response.appId()), APP_ALREADY_LOCKED);
    }

    @Test
    public void should_fetch_app_managers() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());

        String appId = AppApi.createApp(loginResponse.jwt(), rAppName()).getAppId();

        CreateMemberResponse createMemberResponse = createMemberAndLogin(loginResponse.jwt(), rMemberName(), rMobile(), rPassword());
        SetAppManagersCommand command = SetAppManagersCommand.builder().managers(newArrayList(createMemberResponse.getMemberId())).build();
        AppApi.setAppManagers(loginResponse.jwt(), appId, command);

        List<String> managers = AppApi.fetchAppManagers(loginResponse.jwt(), appId);
        assertTrue(managers.contains(createMemberResponse.getMemberId()));
    }

    @Test
    public void should_fetch_app_resource_usages() {
        PreparedQrResponse response = setupApi.registerWithQr();
        SubmissionApi.newSubmission(response.jwt(), response.qrId(), response.homePageId());

        QAppResourceUsages usages = AppApi.fetchAppResourceUsages(response.jwt(), response.appId());
        assertEquals(1, usages.getUsedQrCount());
        assertEquals(1, usages.getUsedGroupCount());
        assertEquals(1, usages.getUsedSubmissionCount());
    }

    @Test
    public void should_fetch_qr_upload_template() {
        PreparedAppResponse response = setupApi.registerWithApp();
        FSingleLineTextControl eligibleControl = defaultSingleLineTextControl();
        FRichTextInputControl notEligibleControl = defaultRichTextInputControl();
        FSingleLineTextControl notEligiblePageControl = defaultSingleLineTextControl();
        Page homePage = defaultPageBuilder().controls(newArrayList(eligibleControl, notEligibleControl))
                .setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).submitType(ONCE_PER_INSTANCE).build()).build();
        Page notEligiblePage = defaultPageBuilder().controls(newArrayList(notEligiblePageControl))
                .setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPages(response.jwt(), response.appId(), homePage, notEligiblePage);

        Attribute direstAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        Attribute lastAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(homePage.getId())
                .controlId(eligibleControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute firstAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(homePage.getId())
                .controlId(eligibleControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute notEligibleAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(homePage.getId()).controlId(notEligibleControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();
        Attribute notEligiblePageAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(notEligiblePage.getId()).controlId(notEligiblePageControl.getId()).range(AttributeStatisticRange.NO_LIMIT).build();

        AppApi.updateAppAttributes(response.jwt(), response.appId(), direstAttribute, lastAttribute, firstAttribute, notEligibleAttribute,
                notEligiblePageAttribute);

        byte[] templateBytes = AppApi.fetchQrImportTemplate(response.jwt(), response.appId());

        Set<String> result = newHashSet();
        EasyExcel.read(new ByteArrayInputStream(templateBytes), new AnalysisEventListener<Map<Integer, String>>() {

            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {

            }

            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                result.addAll(headMap.values());
            }
        }).excelType(XLSX).sheet().doRead();

        assertEquals(5, result.size());
        assertTrue(result.contains("名称（必填）"));
        assertTrue(result.contains("自定义编号（必填）"));
        assertTrue(result.contains(direstAttribute.getName()));
        assertTrue(result.contains(lastAttribute.getName()));
        assertTrue(result.contains(firstAttribute.getName()));
    }

    @Test
    public void should_update_webhook_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant theTenant = tenantRepository.byId(response.tenantId());
        setupApi.updateTenantPlan(theTenant, theTenant.currentPlan().withDeveloperAllowed(true));

        WebhookSetting setting = WebhookSetting.builder()
                .enabled(true)
                .notAccessible(true)
                .url(rUrl())
                .username(randomAlphanumeric(10))
                .password(randomAlphanumeric(10))
                .build();

        UpdateAppWebhookSettingCommand command = UpdateAppWebhookSettingCommand.builder().webhookSetting(setting).build();
        AppApi.updateWebhookSetting(response.jwt(), response.appId(), command);
        App app = appRepository.byId(response.appId());
        WebhookSetting webhookSetting = app.getWebhookSetting();
        assertFalse(webhookSetting.isNotAccessible());
        assertTrue(webhookSetting.isEnabled());
        assertEquals(setting.getUrl(), webhookSetting.getUrl());
        assertEquals(setting.getUsername(), webhookSetting.getUsername());
        assertEquals(setting.getPassword(), webhookSetting.getPassword());
    }

    @Test
    public void should_not_update_webhook_setting_if_plan_not_allowed() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant theTenant = tenantRepository.byId(response.tenantId());
        setupApi.updateTenantPlan(theTenant, theTenant.currentPlan().withDeveloperAllowed(false));

        UpdateAppWebhookSettingCommand command = UpdateAppWebhookSettingCommand.builder().webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .notAccessible(true)
                        .url(rUrl())
                        .username(randomAlphanumeric(10))
                        .password(randomAlphanumeric(10))
                        .build())
                .build();
        assertError(() -> AppApi.updateWebhookSettingRaw(response.jwt(), response.appId(), command), UPDATE_WEBHOOK_SETTING_NOT_ALLOWED);
    }

    @Test
    public void should_fetch_app_webhook_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant theTenant = tenantRepository.byId(response.tenantId());
        setupApi.updateTenantPlan(theTenant, theTenant.currentPlan().withDeveloperAllowed(true));

        WebhookSetting setting = WebhookSetting.builder()
                .enabled(true)
                .notAccessible(true)
                .url(rUrl())
                .username(randomAlphanumeric(10))
                .password(randomAlphanumeric(10))
                .build();

        UpdateAppWebhookSettingCommand command = UpdateAppWebhookSettingCommand.builder().webhookSetting(setting).build();
        AppApi.updateWebhookSetting(response.jwt(), response.appId(), command);

        QAppWebhookSetting qAppWebhookSetting = AppApi.fetchWebhookSetting(response.jwt(), response.appId());
        App app = appRepository.byId(response.appId());
        assertEquals(app.getWebhookSetting(), qAppWebhookSetting.getWebhookSetting());
    }

    @Test
    public void should_enable_group_sync() {
        PreparedAppResponse response = setupApi.registerWithApp();
        assertFalse(appRepository.byId(response.appId()).isGroupSynced());
        AppApi.enableGroupSync(response.jwt(), response.appId());
        assertTrue(appRepository.byId(response.appId()).isGroupSynced());
        AppGroupSyncEnabledEvent event = latestEventFor(response.appId(), APP_GROUP_SYNC_ENABLED, AppGroupSyncEnabledEvent.class);
        assertEquals(response.appId(), event.getAppId());
    }

    @Test
    public void enable_group_sync_should_do_sync() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(response.jwt(), departmentId, rDepartmentName());
        AppApi.enableGroupSync(response.jwt(), response.appId());

        Group group = groupRepository.byDepartmentIdOptional(departmentId, response.appId()).get();
        assertEquals(departmentId, group.getDepartmentId());
        Group subGroup = groupRepository.byDepartmentIdOptional(subDepartmentId, response.appId()).get();
        assertEquals(subDepartmentId, subGroup.getDepartmentId());
        assertEquals(3, groupRepository.cachedAppAllGroups(response.appId()).size());

        GroupHierarchy hierarchy = groupHierarchyRepository.byAppId(response.appId());
        Set<String> strings = hierarchy.allGroupIds();
        assertTrue(strings.containsAll(Set.of(response.defaultGroupId(), group.getId(), subGroup.getId())));
        assertEquals(group.getId() + "/" + subGroup.getId(), hierarchy.getHierarchy().schemaOf(subGroup.getId()));
    }

    @Test
    public void should_update_circulation_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(option1.getId())
                .statusAfterSubmissions(
                        List.of(StatusAfterSubmission.builder().id(newShortUuid()).optionId(option1.getId()).pageId(response.homePageId()).build()))
                .statusPermissions(List.of(
                        StatusPermission.builder().id(newShortUuid()).optionId(option2.getId()).notAllowedPageIds(List.of(response.homePageId()))
                                .build()))
                .build();
        AppApi.updateCirculationStatusSetting(response.jwt(), response.appId(), setting);
        App app = appRepository.byId(response.appId());
        CirculationStatusSetting updatedSetting = app.getSetting().getCirculationStatusSetting();
        assertEquals(setting, updatedSetting);
    }

    @Test
    public void update_circulation_setting_should_exclude_empty_settings() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(option1.getId())
                .statusAfterSubmissions(List.of(
                        StatusAfterSubmission.builder().id(newShortUuid()).optionId(null).pageId(response.homePageId()).build(),
                        StatusAfterSubmission.builder().id(newShortUuid()).optionId(option1.getId()).pageId(null).build()
                ))
                .statusPermissions(List.of(
                        StatusPermission.builder().id(newShortUuid()).optionId(null).notAllowedPageIds(List.of(response.homePageId())).build(),
                        StatusPermission.builder().id(newShortUuid()).optionId(option2.getId()).notAllowedPageIds(List.of()).build()
                ))
                .build();

        AppApi.updateCirculationStatusSetting(response.jwt(), response.appId(), setting);
        App app = appRepository.byId(response.appId());
        CirculationStatusSetting updatedSetting = app.getSetting().getCirculationStatusSetting();
        assertTrue(updatedSetting.getStatusAfterSubmissions().isEmpty());
        assertTrue(updatedSetting.getStatusPermissions().isEmpty());
    }

    @Test
    public void should_fail_update_circulation_setting_if_option_id_duplicates() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String id = newShortUuid();
        TextOption option1 = TextOption.builder().id(id).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(id).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .statusAfterSubmissions(List.of())
                .statusPermissions(List.of())
                .build();
        assertError(() -> AppApi.updateCirculationStatusSettingRaw(response.jwt(), response.appId(), setting), TEXT_OPTION_ID_DUPLICATED);
    }

    @Test
    public void should_fail_update_circulation_setting_if_submission_setting_id_duplicates() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        String id = newShortUuid();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(option1.getId())
                .statusAfterSubmissions(List.of(
                        StatusAfterSubmission.builder().id(id).optionId(option1.getId()).pageId(response.homePageId()).build(),
                        StatusAfterSubmission.builder().id(id).optionId(option2.getId()).pageId(response.homePageId()).build()
                ))
                .statusPermissions(List.of(
                        StatusPermission.builder().id(newShortUuid()).optionId(option2.getId()).notAllowedPageIds(List.of(response.homePageId()))
                                .build()))
                .build();
        assertError(() -> AppApi.updateCirculationStatusSettingRaw(response.jwt(), response.appId(), setting),
                CIRCULATION_AFTER_SUBMISSION_ID_DUPLICATED);
    }

    @Test
    public void should_fail_update_circulation_setting_if_permission_setting_id_duplicates() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        String id = newShortUuid();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(option1.getId())
                .statusAfterSubmissions(
                        List.of(StatusAfterSubmission.builder().id(newShortUuid()).optionId(option1.getId()).pageId(response.homePageId()).build()))
                .statusPermissions(List.of(
                        StatusPermission.builder().id(id).optionId(option1.getId()).notAllowedPageIds(List.of(response.homePageId())).build(),
                        StatusPermission.builder().id(id).optionId(option2.getId()).notAllowedPageIds(List.of(response.homePageId())).build()
                ))
                .build();
        assertError(() -> AppApi.updateCirculationStatusSettingRaw(response.jwt(), response.appId(), setting),
                CIRCULATION_PERMISSION_ID_DUPLICATED);
    }

    @Test
    public void should_fail_update_circulation_setting_if_init_option_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(newShortUuid())
                .statusAfterSubmissions(
                        List.of(StatusAfterSubmission.builder().id(newShortUuid()).optionId(option1.getId()).pageId(response.homePageId()).build()))
                .statusPermissions(List.of(
                        StatusPermission.builder().id(newShortUuid()).optionId(option2.getId()).notAllowedPageIds(List.of(response.homePageId()))
                                .build()))
                .build();
        assertError(() -> AppApi.updateCirculationStatusSettingRaw(response.jwt(), response.appId(), setting),
                CIRCULATION_OPTION_NOT_EXISTS);
    }

    @Test
    public void should_fail_update_circulation_setting_if_page_not_exist_for_submission_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(null)
                .statusAfterSubmissions(
                        List.of(StatusAfterSubmission.builder().id(newShortUuid()).optionId(option1.getId()).pageId(Page.newPageId()).build()))
                .statusPermissions(List.of(
                        StatusPermission.builder().id(newShortUuid()).optionId(option2.getId()).notAllowedPageIds(List.of(response.homePageId()))
                                .build()))
                .build();
        assertError(() -> AppApi.updateCirculationStatusSettingRaw(response.jwt(), response.appId(), setting), VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_update_circulation_setting_if_option_not_exist_for_submission_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(null)
                .statusAfterSubmissions(
                        List.of(StatusAfterSubmission.builder().id(newShortUuid()).optionId(newShortUuid()).pageId(response.homePageId()).build()))
                .statusPermissions(List.of(
                        StatusPermission.builder().id(newShortUuid()).optionId(option2.getId()).notAllowedPageIds(List.of(response.homePageId()))
                                .build()))
                .build();
        assertError(() -> AppApi.updateCirculationStatusSettingRaw(response.jwt(), response.appId(), setting),
                CIRCULATION_OPTION_NOT_EXISTS);
    }

    @Test
    public void should_fail_update_circulation_setting_if_option_not_exist_for_permission_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(null)
                .statusAfterSubmissions(
                        List.of(StatusAfterSubmission.builder().id(newShortUuid()).optionId(option1.getId()).pageId(response.homePageId()).build()))
                .statusPermissions(List.of(
                        StatusPermission.builder().id(newShortUuid()).optionId(newShortUuid()).notAllowedPageIds(List.of(response.homePageId()))
                                .build()))
                .build();
        assertError(() -> AppApi.updateCirculationStatusSettingRaw(response.jwt(), response.appId(), setting),
                CIRCULATION_OPTION_NOT_EXISTS);
    }

    @Test
    public void should_fail_update_circulation_setting_if_page_not_exist_for_permission_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(null)
                .statusAfterSubmissions(
                        List.of(StatusAfterSubmission.builder().id(newShortUuid()).optionId(option1.getId()).pageId(response.homePageId()).build()))
                .statusPermissions(List.of(
                        StatusPermission.builder().id(newShortUuid()).optionId(option2.getId()).notAllowedPageIds(List.of(Page.newPageId())).build()))
                .build();
        assertError(() -> AppApi.updateCirculationStatusSettingRaw(response.jwt(), response.appId(), setting), VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_cache_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String key = "Cache:APP::" + response.appId();
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(key)));
        appRepository.cachedById(response.appId());
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(key)));

        App app = appRepository.byId(response.appId());
        appRepository.save(app);

        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(key)));
    }

    @Test
    public void should_cache_tenant_apps() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String key = "Cache:TENANT_APPS::" + response.tenantId();
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(key)));

        appRepository.cachedTenantAllApps(response.tenantId());
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(key)));

        App app = appRepository.byId(response.appId());
        appRepository.save(app);
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(key)));
    }

    @Test
    public void save_app_should_evict_cache() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateAppResponse anotherApp = AppApi.createApp(response.jwt());
        String appKey = "Cache:APP::" + response.appId();
        String anotherAppKey = "Cache:APP::" + anotherApp.getAppId();
        String appsKey = "Cache:TENANT_APPS::" + response.tenantId();

        appRepository.cachedById(response.appId());
        appRepository.cachedById(anotherApp.getAppId());
        appRepository.cachedTenantAllApps(response.tenantId());
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(appKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(anotherAppKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(appsKey)));

        App app = appRepository.byId(response.appId());
        appRepository.save(app);
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(appKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(anotherAppKey)));
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(appsKey)));
    }

    @Test
    public void delete_app_should_evict_cache() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateAppResponse newApp = AppApi.createApp(response.jwt());
        String appKey = "Cache:APP::" + response.appId();
        String newAppKey = "Cache:APP::" + newApp.getAppId();
        String appsKey = "Cache:TENANT_APPS::" + response.tenantId();

        appRepository.cachedById(response.appId());
        appRepository.cachedById(newApp.getAppId());
        appRepository.cachedTenantAllApps(response.tenantId());
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(appKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(newAppKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(appsKey)));

        App app = appRepository.byId(response.appId());
        app.onDelete(NO_USER);
        appRepository.delete(app);
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(appKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(newAppKey)));
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(appsKey)));
    }

    @Test
    public void should_fetch_first_qr_for_app() {
        PreparedAppResponse response = setupApi.registerWithApp();

        CreateQrResponse qrResponse = QrApi.createQr(response.jwt(), response.defaultGroupId());
        assertEquals(qrResponse.getPlateId(), AppApi.fetchFirstQrPlateId(response.jwt(), response.appId()).getPlateId());

        QrApi.createQr(response.jwt(), response.defaultGroupId());
        assertEquals(qrResponse.getPlateId(), AppApi.fetchFirstQrPlateId(response.jwt(), response.appId()).getPlateId());
    }
}