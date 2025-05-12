package com.mryqr.core.app;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.utils.PagedList;
import com.mryqr.core.app.command.*;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.config.AppConfig;
import com.mryqr.core.app.domain.operationmenu.OperationMenuItem;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.menu.Menu;
import com.mryqr.core.app.domain.page.setting.PageSetting;
import com.mryqr.core.app.query.*;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.utils.RandomTestFixture.rAppName;

public class AppApi {
    public static Response createAppRaw(String jwt, CreateAppCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/apps");
    }

    public static CreateAppResponse createApp(String jwt, CreateAppCommand command) {
        return createAppRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(CreateAppResponse.class);
    }

    public static CreateAppResponse createApp(String jwt) {
        CreateAppCommand createAppCommand = CreateAppCommand.builder().name(rAppName()).build();
        return createApp(jwt, createAppCommand);
    }

    public static CreateAppResponse createApp(String jwt, String name) {
        CreateAppCommand createAppCommand = CreateAppCommand.builder().name(name).build();
        return createApp(jwt, createAppCommand);
    }

    public static CreateAppResponse createApp(String jwt, Permission permission) {
        CreateAppResponse createAppResponse = createApp(jwt, rAppName());
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, createAppResponse.getAppId());

        AppSetting setting = qUpdatableApp.getSetting();
        String version = qUpdatableApp.getVersion();
        Page page = setting.homePage();
        PageSetting pageSetting = page.getSetting();
        ReflectionTestUtils.setField(pageSetting, "permission", permission);

        UpdateAppSettingCommand command = UpdateAppSettingCommand.builder().version(version).setting(setting).build();
        updateAppSetting(jwt, createAppResponse.getAppId(), command);
        return createAppResponse;
    }

    public static CreateAppResponse createApp(String jwt, Permission permission, Permission operationPermission) {
        CreateAppResponse createAppResponse = createApp(jwt, rAppName());
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, createAppResponse.getAppId());

        AppSetting setting = qUpdatableApp.getSetting();
        String version = qUpdatableApp.getVersion();
        Page page = setting.homePage();
        PageSetting pageSetting = page.getSetting();
        ReflectionTestUtils.setField(pageSetting, "permission", permission);
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "operationPermission", operationPermission);

        UpdateAppSettingCommand command = UpdateAppSettingCommand.builder().version(version).setting(setting).build();
        updateAppSetting(jwt, createAppResponse.getAppId(), command);
        return createAppResponse;
    }

    public static Response copyAppRaw(String jwt, CopyAppCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/apps/copies");
    }

    public static CreateAppResponse copyApp(String jwt, CopyAppCommand command) {
        return copyAppRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(CreateAppResponse.class);
    }

    public static Response createAppFromTemplateRaw(String jwt, String appTemplateId) {
        return BaseApiTest.given(jwt)
                .when()
                .post("/apps/templates/{appTemplateId}/adoptions", appTemplateId);
    }

    public static CreateAppResponse createAppFromTemplate(String jwt, String appTemplateId) {
        return createAppFromTemplateRaw(jwt, appTemplateId)
                .then()
                .statusCode(201)
                .extract()
                .as(CreateAppResponse.class);
    }

    public static Response renameAppRaw(String jwt, String appId, RenameAppCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/apps/{appId}/name", appId);

    }

    public static void renameApp(String jwt, String appId, RenameAppCommand command) {
        renameAppRaw(jwt, appId, command)
                .then()
                .statusCode(200);
    }

    public static void activateApp(String jwt, String appId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/apps/{appId}/activation", appId)
                .then()
                .statusCode(200);
    }

    public static void deactivateApp(String jwt, String appId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/apps/{appId}/deactivation", appId)
                .then()
                .statusCode(200);
    }

    public static void lockApp(String jwt, String appId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/apps/{appId}/lock", appId)
                .then()
                .statusCode(200);
    }

    public static void unlockApp(String jwt, String appId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/apps/{appId}/unlock", appId)
                .then()
                .statusCode(200);
    }

    public static Response setAppManagersRaw(String jwt, String appId, SetAppManagersCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/apps/{appId}/managers", appId);
    }

    public static void setAppManagers(String jwt, String appId, SetAppManagersCommand command) {
        setAppManagersRaw(jwt, appId, command)
                .then()
                .statusCode(200);
    }

    public static void setAppManagers(String jwt, String appId, String... memberIds) {
        SetAppManagersCommand command = SetAppManagersCommand.builder().managers(newArrayList(memberIds)).build();
        setAppManagers(jwt, appId, command);
    }

    public static void setAppManager(String jwt, String appId, String memberId) {
        SetAppManagersCommand command = SetAppManagersCommand.builder().managers(newArrayList(memberId)).build();
        setAppManagers(jwt, appId, command);
    }

    public static Response updateAppSettingRaw(String jwt, String appId, UpdateAppSettingCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/apps/{appId}/setting", appId);
    }

    public static Response updateAppSettingRaw(String jwt, String appId, AppSetting setting) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        UpdateAppSettingCommand command = UpdateAppSettingCommand.builder().setting(setting).version(qUpdatableApp.getVersion()).build();
        return updateAppSettingRaw(jwt, appId, command);
    }

    public static Response updateAppSettingRaw(String jwt, String appId, String version, AppSetting setting) {
        UpdateAppSettingCommand command = UpdateAppSettingCommand.builder().setting(setting).version(version).build();
        return updateAppSettingRaw(jwt, appId, command);
    }

    public static String updateAppSetting(String jwt, String appId, UpdateAppSettingCommand command) {
        return updateAppSettingRaw(jwt, appId, command)
                .then()
                .statusCode(200)
                .extract()
                .as(Map.class)
                .get("updatedVersion")
                .toString();
    }

    public static String updateAppSetting(String jwt, String appId, AppSetting setting) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        UpdateAppSettingCommand command = UpdateAppSettingCommand.builder().setting(setting).version(qUpdatableApp.getVersion()).build();
        return updateAppSetting(jwt, appId, command);
    }

    public static Response updateAppReportSettingRaw(String jwt, String appId, UpdateAppReportSettingCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/apps/{appId}/report-setting", appId);
    }

    public static void updateAppReportSetting(String jwt, String appId, UpdateAppReportSettingCommand command) {
        updateAppReportSettingRaw(jwt, appId, command)
                .then()
                .statusCode(200);
    }

    public static String enableAppPosition(String jwt, String appId) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting setting = qUpdatableApp.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "geolocationEnabled", true);
        UpdateAppSettingCommand command = UpdateAppSettingCommand.builder().setting(setting).version(qUpdatableApp.getVersion()).build();
        return updateAppSetting(jwt, appId, command);
    }

    public static String setAppAssignmentEnabled(String jwt, String appId, boolean enabled) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting setting = qUpdatableApp.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "assignmentEnabled", enabled);
        UpdateAppSettingCommand command = UpdateAppSettingCommand.builder().setting(setting).version(qUpdatableApp.getVersion()).build();
        return updateAppSetting(jwt, appId, command);
    }

    public static String updateAppSetting(String jwt, String appId, String version, AppSetting setting) {
        UpdateAppSettingCommand command = UpdateAppSettingCommand.builder().setting(setting).version(version).build();
        return updateAppSetting(jwt, appId, command);
    }

    public static void updateAppPermission(String jwt, String appId, Permission permission) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting setting = qUpdatableApp.getSetting();
        Page page = setting.homePage();
        PageSetting pageSetting = page.getSetting();
        ReflectionTestUtils.setField(pageSetting, "permission", permission);
        updateAppSetting(jwt, appId, qUpdatableApp.getVersion(), setting);
    }

    public static void updateAppOperationPermission(String jwt, String appId, Permission permission) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting setting = qUpdatableApp.getSetting();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "operationPermission", permission);
        updateAppSetting(jwt, appId, qUpdatableApp.getVersion(), setting);
    }

    public static void updateAppControls(String jwt, String appId, Control... controls) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting setting = qUpdatableApp.getSetting();
        Page page = setting.homePage();
        List<Control> allControls = page.getControls();
        allControls.clear();
        allControls.addAll(Arrays.asList(controls));
        updateAppSetting(jwt, appId, qUpdatableApp.getVersion(), setting);
    }

    public static void updateAppMenu(String jwt, String appId, Menu menu) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting setting = qUpdatableApp.getSetting();
        ReflectionTestUtils.setField(setting, "menu", menu);
        updateAppSetting(jwt, appId, qUpdatableApp.getVersion(), setting);
    }

    public static void updateAppAttributes(String jwt, String appId, Attribute... attributes) {
        updateAppAttributes(jwt, appId, Arrays.asList(attributes));
    }

    public static void updateAppAttributes(String jwt, String appId, List<Attribute> attributes) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting setting = qUpdatableApp.getSetting();
        List<Attribute> allAttributes = setting.getAttributes();
        allAttributes.clear();
        allAttributes.addAll(attributes);
        updateAppSetting(jwt, appId, qUpdatableApp.getVersion(), setting);
    }

    public static void updateAppOperationMenuItems(String jwt, String appId, OperationMenuItem... operationMenuItems) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting setting = qUpdatableApp.getSetting();
        List<OperationMenuItem> allOperationMenuItems = setting.getOperationMenuItems();
        allOperationMenuItems.clear();
        allOperationMenuItems.addAll(Arrays.asList(operationMenuItems));
        updateAppSetting(jwt, appId, qUpdatableApp.getVersion(), setting);
    }

    public static void updateAppOperationMenuItems(String jwt, String appId, List<OperationMenuItem> operationMenuItems) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting setting = qUpdatableApp.getSetting();
        List<OperationMenuItem> allOperationMenuItems = setting.getOperationMenuItems();
        allOperationMenuItems.clear();
        allOperationMenuItems.addAll(operationMenuItems);
        updateAppSetting(jwt, appId, qUpdatableApp.getVersion(), setting);
    }

    public static void updateAppPermissionAndControls(String jwt, String appId, Permission permission, Control... controls) {
        updateAppPermissionAndControls(jwt, appId, permission, Arrays.asList(controls));
    }

    public static void updateAppPermissionAndControls(String jwt, String appId, Permission permission, List<Control> controls) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);

        AppSetting setting = qUpdatableApp.getSetting();
        String version = qUpdatableApp.getVersion();
        Page page = setting.homePage();
        PageSetting pageSetting = page.getSetting();
        ReflectionTestUtils.setField(pageSetting, "permission", permission);

        List<Control> allControls = page.getControls();
        allControls.clear();
        allControls.addAll(controls);

        updateAppSetting(jwt, appId, version, setting);
    }

    public static void updateAppHomePageSetting(String jwt, String appId, PageSetting pageSetting) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);

        AppSetting setting = qUpdatableApp.getSetting();
        String version = qUpdatableApp.getVersion();
        Page page = setting.homePage();
        ReflectionTestUtils.setField(page, "setting", pageSetting);

        updateAppSetting(jwt, appId, version, setting);
    }

    public static void updateAppHomePageSettingAndControls(String jwt, String appId, PageSetting pageSetting, List<Control> controls) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);

        AppSetting setting = qUpdatableApp.getSetting();
        String version = qUpdatableApp.getVersion();
        Page page = setting.homePage();
        ReflectionTestUtils.setField(page, "setting", pageSetting);

        List<Control> allControls = page.getControls();
        allControls.clear();
        allControls.addAll(controls);

        updateAppSetting(jwt, appId, version, setting);
    }

    public static void updateAppHomePageSettingAndControls(String jwt, String appId, PageSetting pageSetting, Control... controls) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);

        AppSetting setting = qUpdatableApp.getSetting();
        String version = qUpdatableApp.getVersion();
        Page page = setting.homePage();
        ReflectionTestUtils.setField(page, "setting", pageSetting);

        List<Control> allControls = page.getControls();
        allControls.clear();
        allControls.addAll(Arrays.asList(controls));

        updateAppSetting(jwt, appId, version, setting);
    }

    public static void updateAppPage(String jwt, String appId, Page page) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting setting = qUpdatableApp.getSetting();
        List<Page> allPages = setting.getPages();

        allPages.clear();
        allPages.add(page);
        String version = qUpdatableApp.getVersion();
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "homePageId", page.getId());
        updateAppSetting(jwt, appId, version, setting);
    }

    public static void updateAppPages(String jwt, String appId, Page homePage, Page... childPages) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);

        AppSetting setting = qUpdatableApp.getSetting();
        List<Page> allPages = setting.getPages();
        allPages.clear();
        allPages.add(homePage);
        allPages.addAll(Arrays.asList(childPages));
        AppConfig config = setting.getConfig();
        ReflectionTestUtils.setField(config, "homePageId", homePage.getId());
        String version = qUpdatableApp.getVersion();
        updateAppSetting(jwt, appId, version, setting);
    }

    public static Response updateCirculationStatusSettingRaw(String jwt, String appId, CirculationStatusSetting setting) {
        QUpdatableApp qUpdatableApp = fetchUpdatableApp(jwt, appId);
        AppSetting appSetting = qUpdatableApp.getSetting();
        ReflectionTestUtils.setField(appSetting, "circulationStatusSetting", setting);
        String version = qUpdatableApp.getVersion();
        UpdateAppSettingCommand command = UpdateAppSettingCommand.builder().setting(appSetting).version(version).build();
        return updateAppSettingRaw(jwt, appId, command);
    }

    public static String updateCirculationStatusSetting(String jwt, String appId, CirculationStatusSetting setting) {
        return updateCirculationStatusSettingRaw(jwt, appId, setting).then()
                .statusCode(200)
                .extract()
                .as(Map.class)
                .get("updatedVersion")
                .toString();
    }

    public static void enableGroupSync(String jwt, String appId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/apps/{appId}/group-sync", appId)
                .then()
                .statusCode(200);
    }

    public static Response deleteAppRaw(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/apps/{appId}", appId);
    }

    public static void deleteApp(String jwt, String appId) {
        deleteAppRaw(jwt, appId)
                .then()
                .statusCode(200);
    }

    public static Response listMyManagedListRaw(String jwt, ListMyManagedAppsQuery query) {
        return BaseApiTest.given(jwt)
                .body(query)
                .when()
                .post("/apps/my-managed-apps");
    }

    public static PagedList<QManagedListApp> listMyManagedApps(String jwt, ListMyManagedAppsQuery query) {
        return listMyManagedListRaw(jwt, query)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static List<QViewableListApp> myViewableApps(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/apps/my-viewable-apps")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<QViewableListApp>>() {
                });
    }

    public static Response fetchOperationalAppRaw(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/apps/operation/{appId}", appId);
    }

    public static QOperationalApp fetchOperationalApp(String jwt, String appId) {
        return fetchOperationalAppRaw(jwt, appId)
                .then()
                .statusCode(200)
                .extract()
                .as(QOperationalApp.class);
    }

    public static Response fetchUpdatableAppRaw(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/apps/updatable/{appId}", appId);
    }

    public static QUpdatableApp fetchUpdatableApp(String jwt, String appId) {
        return fetchUpdatableAppRaw(jwt, appId)
                .then()
                .statusCode(200)
                .extract()
                .as(QUpdatableApp.class);
    }

    public static List<String> fetchAppManagers(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/apps/{appId}/managers", appId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static QAppResourceUsages fetchAppResourceUsages(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/apps/{appId}/resource-usages", appId)
                .then()
                .statusCode(200)
                .extract()
                .as(QAppResourceUsages.class);
    }

    public static byte[] fetchQrImportTemplate(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/apps/{appId}/qr-import-template", appId)
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();
    }

    public static Response updateWebhookSettingRaw(String jwt, String appId, UpdateAppWebhookSettingCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/apps/{appId}/webhook-setting", appId);
    }

    public static void updateWebhookSetting(String jwt, String appId, UpdateAppWebhookSettingCommand command) {
        updateWebhookSettingRaw(jwt, appId, command)
                .then()
                .statusCode(200);
    }


    public static QAppWebhookSetting fetchWebhookSetting(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/apps/{appId}/webhook-setting", appId)
                .then()
                .statusCode(200)
                .extract()
                .as(QAppWebhookSetting.class);
    }

    public static QAppFirstQr fetchFirstQrPlateId(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/apps/{appId}/first-qr", appId)
                .then()
                .statusCode(200)
                .extract()
                .as(QAppFirstQr.class);
    }

}
