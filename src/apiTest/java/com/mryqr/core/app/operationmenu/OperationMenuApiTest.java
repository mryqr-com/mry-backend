package com.mryqr.core.app.operationmenu;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.operationmenu.OperationMenuItem;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.ALL_SUBMIT_HISTORY;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.SUBMITTER_SUBMISSION;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.TO_BE_APPROVED;
import static com.mryqr.core.common.exception.ErrorCode.OPERATION_MENU_ITEM_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.OPERATION_MENU_ITEM_ID_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.OPERATION_MENU_ITEM_NAME_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_OPERATION_MENU_REF_PAGE_NOT_EXIST;
import static com.mryqr.core.common.utils.MryConstants.ALL;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OperationMenuApiTest extends BaseApiTest {

    @Test
    public void update_app_setting_should_also_update_operation_menus() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();

        List<OperationMenuItem> submissionListMenuItems = newArrayList();
        OperationMenuItem menuItem1 = OperationMenuItem.builder().id(newShortUuid()).type(SUBMITTER_SUBMISSION).name("提交人提交").pageId(response.getHomePageId()).build();
        OperationMenuItem menuItem2 = OperationMenuItem.builder().id(newShortUuid()).type(ALL_SUBMIT_HISTORY).name("提交历史").pageId(ALL).build();
        OperationMenuItem menuItem3 = OperationMenuItem.builder().id(newShortUuid()).type(TO_BE_APPROVED).name("待审批").pageId(ALL).build();
        submissionListMenuItems.addAll(newArrayList(menuItem1, menuItem2, menuItem3));
        AppApi.updateAppOperationMenuItems(response.getJwt(), appId, submissionListMenuItems);

        App updatedApp = appRepository.byId(appId);
        List<OperationMenuItem> updatedMenus = updatedApp.getSetting().getOperationMenuItems();
        assertEquals(submissionListMenuItems, updatedMenus);
    }

    @Test
    public void should_fail_update_app_setting_if_operation_menu_reference_non_exist_page() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();

        List<OperationMenuItem> submissionListMenuItems = setting.getOperationMenuItems();
        submissionListMenuItems.clear();
        OperationMenuItem menuItem = OperationMenuItem.builder().id(newShortUuid()).type(SUBMITTER_SUBMISSION).name("提交人提交").pageId(Page.newPageId()).build();
        submissionListMenuItems.addAll(newArrayList(menuItem));

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), VALIDATION_OPERATION_MENU_REF_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_update_app_setting_if_operation_menu_if_id_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();

        List<OperationMenuItem> submissionListMenuItems = setting.getOperationMenuItems();
        submissionListMenuItems.clear();
        String menuId = newShortUuid();
        OperationMenuItem menuItem1 = OperationMenuItem.builder().id(menuId).type(SUBMITTER_SUBMISSION).name("所有提交").pageId(ALL).build();
        OperationMenuItem menuItem2 = OperationMenuItem.builder().id(menuId).type(TO_BE_APPROVED).name("待审批").pageId(ALL).build();
        submissionListMenuItems.addAll(newArrayList(menuItem1, menuItem2));

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), OPERATION_MENU_ITEM_ID_DUPLICATED);
    }

    @Test
    public void should_fail_update_app_setting_if_operation_menu_if_has_same_schema() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();

        List<OperationMenuItem> submissionListMenuItems = setting.getOperationMenuItems();
        submissionListMenuItems.clear();
        OperationMenuItem menuItem1 = OperationMenuItem.builder().id(newShortUuid()).type(SUBMITTER_SUBMISSION).name("所有提交1").pageId(ALL).build();
        OperationMenuItem menuItem2 = OperationMenuItem.builder().id(newShortUuid()).type(SUBMITTER_SUBMISSION).name("所有提交2").pageId(ALL).build();
        submissionListMenuItems.addAll(newArrayList(menuItem1, menuItem2));

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), OPERATION_MENU_ITEM_DUPLICATED);
    }

    @Test
    public void should_fail_update_app_setting_if_operation_menu_if_has_same_name() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();

        List<OperationMenuItem> submissionListMenuItems = setting.getOperationMenuItems();
        submissionListMenuItems.clear();
        OperationMenuItem menuItem1 = OperationMenuItem.builder().id(newShortUuid()).type(SUBMITTER_SUBMISSION).name("提交").pageId(ALL).build();
        OperationMenuItem menuItem2 = OperationMenuItem.builder().id(newShortUuid()).type(TO_BE_APPROVED).name("提交").pageId(ALL).build();
        submissionListMenuItems.addAll(newArrayList(menuItem1, menuItem2));

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), OPERATION_MENU_ITEM_NAME_DUPLICATED);
    }

}
