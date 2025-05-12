package com.mryqr.management.operation;

import com.mryqr.core.app.domain.*;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.config.AppConfig;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.AutoCalculateAliasContext;
import com.mryqr.core.app.domain.page.control.FDateControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.PSubmissionReferenceControl;
import com.mryqr.core.app.domain.page.menu.Menu;
import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mryqr.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.app.domain.AppTopBar.defaultAppTopBar;
import static com.mryqr.core.app.domain.config.AppLandingPageType.DEFAULT;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSetting;
import static com.mryqr.core.app.domain.page.control.ControlNameSetting.defaultControlNameSetting;
import static com.mryqr.core.app.domain.page.control.ControlStyleSetting.defaultControlStyleSetting;
import static com.mryqr.core.app.domain.page.control.ControlType.*;
import static com.mryqr.core.app.domain.page.control.FNumberInputControl.MAX_NUMBER;
import static com.mryqr.core.app.domain.page.control.PSubmissionReferenceControl.StyleType.HORIZONTAL_TABLE;
import static com.mryqr.core.app.domain.page.header.PageHeader.defaultPageHeaderBuilder;
import static com.mryqr.core.app.domain.page.header.PageHeaderType.INHERITED;
import static com.mryqr.core.app.domain.page.setting.PageSetting.defaultPageSettingBuilder;
import static com.mryqr.core.app.domain.page.setting.SubmitType.NEW;
import static com.mryqr.core.app.domain.page.submitbutton.SubmitButton.defaultSubmitButton;
import static com.mryqr.core.app.domain.page.title.PageTitle.defaultPageTitleBuilder;
import static com.mryqr.core.app.domain.ui.AppearanceStyle.defaultAppearanceStyle;
import static com.mryqr.core.app.domain.ui.BoxedTextStyle.defaultBoxedTextStyle;
import static com.mryqr.core.app.domain.ui.BoxedTextStyle.defaultControlDescriptionStyle;
import static com.mryqr.core.app.domain.ui.FontStyle.defaultFontStyle;
import static com.mryqr.core.app.domain.ui.ImageCropType.FOUR_TO_THREE;
import static com.mryqr.core.app.domain.ui.MinMaxSetting.minMaxOf;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_ROBOT_USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class MryOperationApp {
    public static final String MRY_OPERATION_APP_ID = "APP00000000000000004";
    public static final String MRY_OPERATION_GROUP_ID = "GRP00000000000000004";

    public static final String OPERATION_QR_CUSTOM_ID = "BvuVczz3Tlaxfy6jytgBZQ";

    public static final String OPERATION_HOME_PAGE_ID = "p_latuDckuQ7GSSxPV6TWcXQ";
    public static final String OPERATION_TOTAL_CONTROL_ID = "c_UEibLBlnSDqNpvAWOU24NQ";

    public static final String SYNC_TOTAL_PAGE_ID = "p_aFcQswbUSGi4IojZuw29YQ";
    public static final String TOTAL_TENANT_CONTROL_ID = "c_Or-m1R3lQBGZhkduCQiRFA";
    public static final String TOTAL_APP_CONTROL_ID = "c_Q2edUR82ScS7N3bBd5EGPQ";
    public static final String TOTAL_QR_CONTROL_ID = "c_ONw0n7MXRoymSTnO9-WcIA";
    public static final String TOTAL_SUBMISSION_CONTROL_ID = "c_u1MHtIm_Q0WPWOJAxiHE0Q";
    public static final String TOTAL_MEMBER_CONTROL_ID = "c_Jv2GvxZtTU-F2aCofIp0FA";
    public static final String MOBILE_ACCESS_RATIO_CONTROL_ID = "c_vmMwzro34G3QuG9W4GLziA";
    public static final String TOTAL_DATE_CONTROL_ID = "c_ba9pMtAq7k49AAHF8fmS8h";
    public static final String SYNC_DELTA_PAGE_ID = "p_xlOcxTIBQMqki1a6D5sskw";
    public static final String DELTA_TENANT_CONTROL_ID = "c_058wJ8RHSlS7EPBsu3XvVA";
    public static final String DELTA_APP_CONTROL_ID = "c_hp9BRCIOTjet6_zdyM3nCg";
    public static final String DELTA_QR_CONTROL_ID = "c_5-d810dIT-auY5tN8ue4qg";
    public static final String DELTA_SUBMISSION_CONTROL_ID = "c_Kvx1A11PTemnb4xcbVcuGw";
    public static final String DELTA_MEMBER_CONTROL_ID = "c_51KpOiDEQ8yZIpRBfhFKgg";
    public static final String ACTIVE_TENANT_CONTROL_ID = "c_2gMcR3YQyBFryZ6KPnHkKe";
    public static final String DELTA_DATE_CONTROL_ID = "c_nviYFAP-QYaPFuEZ2DIYdg";
    private final AppRepository appRepository;
    private final AppFactory appFactory;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final AppHeaderImageProvider appHeaderImageProvider;

    @Transactional
    public void init() {
        if (appRepository.exists(MRY_OPERATION_APP_ID)) {
            return;
        }

        FNumberInputControl totalTenantControl = createNumberInputControl("总租户数", TOTAL_TENANT_CONTROL_ID);
        FNumberInputControl totalAppControl = createNumberInputControl("总应用数", TOTAL_APP_CONTROL_ID);
        FNumberInputControl totalQrControl = createNumberInputControl("总实例数", TOTAL_QR_CONTROL_ID);
        FNumberInputControl totalSubmissionControl = createNumberInputControl("总提交数", TOTAL_SUBMISSION_CONTROL_ID);
        FNumberInputControl totalMemberControl = createNumberInputControl("总用户数", TOTAL_MEMBER_CONTROL_ID);
        FNumberInputControl mobileAccessRatioControl = createNumberInputControl("移动端占比", MOBILE_ACCESS_RATIO_CONTROL_ID);
        FDateControl totalDateControl = FDateControl.builder()
                .type(DATE)
                .id(TOTAL_DATE_CONTROL_ID)
                .name("当日日期")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .build();

        Page syncTotalPage = Page.builder()
                .id(SYNC_TOTAL_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("每日总量统计").build())
                .controls(List.of(totalTenantControl,
                        totalAppControl,
                        totalQrControl,
                        totalSubmissionControl,
                        totalMemberControl,
                        mobileAccessRatioControl,
                        totalDateControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("每日总量统计")
                        .permission(CAN_MANAGE_APP)
                        .build())
                .build();

        FNumberInputControl deltaTenantControl = createNumberInputControl("当日新增租户数", DELTA_TENANT_CONTROL_ID);
        FNumberInputControl deltaAppControl = createNumberInputControl("当日新增应用数", DELTA_APP_CONTROL_ID);
        FNumberInputControl deltaQrControl = createNumberInputControl("当日新增实例数", DELTA_QR_CONTROL_ID);
        FNumberInputControl deltaSubmissionControl = createNumberInputControl("当日新增提交数", DELTA_SUBMISSION_CONTROL_ID);
        FNumberInputControl deltaMemberControl = createNumberInputControl("当日新增用户数", DELTA_MEMBER_CONTROL_ID);
        FNumberInputControl activeTenantControl = createNumberInputControl("当日活跃租户数", ACTIVE_TENANT_CONTROL_ID);

        FDateControl deltaDateControl = FDateControl.builder()
                .type(DATE)
                .id(DELTA_DATE_CONTROL_ID)
                .name("当日日期")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .build();

        Page syncDeltaPage = Page.builder()
                .id(SYNC_DELTA_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("每日增量统计").build())
                .controls(List.of(deltaTenantControl, deltaAppControl, deltaQrControl,
                        deltaSubmissionControl, deltaMemberControl, activeTenantControl, deltaDateControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("每日增量统计")
                        .permission(CAN_MANAGE_APP)
                        .build())
                .build();

        PSubmissionReferenceControl operationTotalControl = PSubmissionReferenceControl.builder()
                .type(SUBMISSION_REFERENCE)
                .id(OPERATION_TOTAL_CONTROL_ID)
                .name("用量统计")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .permission(CAN_MANAGE_APP)
                .pageId(syncTotalPage.getId())
                .styleType(HORIZONTAL_TABLE)
                .keyFontStyle(defaultFontStyle())
                .valueFontStyle(defaultFontStyle())
                .headerFontStyle(defaultFontStyle())
                .verticalKeyStyle(defaultBoxedTextStyle())
                .verticalValueStyle(defaultBoxedTextStyle())
                .appearanceStyle(defaultAppearanceStyle())
                .stripped(true)
                .build();

        Page homePage = Page.builder()
                .id(OPERATION_HOME_PAGE_ID)
                .header(defaultPageHeaderBuilder()
                        .image(appHeaderImageProvider.defaultAppHeaderImage())
                        .imageCropType(FOUR_TO_THREE)
                        .build())
                .title(defaultPageTitleBuilder().text("码如云运营数据统计").build())
                .controls(List.of(operationTotalControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("码如云运营数据统计")
                        .permission(CAN_MANAGE_APP)
                        .build())
                .build();

        Menu menu = Menu.builder().links(List.of()).build();

        AppSetting setting = AppSetting.builder()
                .config(AppConfig.builder()
                        .operationPermission(CAN_MANAGE_APP)
                        .landingPageType(DEFAULT)
                        .qrWebhookTypes(List.of())
                        .homePageId(homePage.getId())
                        .appManualEnabled(true)
                        .build())
                .appTopBar(defaultAppTopBar())
                .pages(List.of(homePage, syncTotalPage, syncDeltaPage))
                .menu(menu)
                .attributes(List.of())
                .operationMenuItems(List.of())
                .plateSetting(PlateSetting.create())
                .circulationStatusSetting(CirculationStatusSetting.create())
                .build();

        CreateAppResult result = appFactory.create(MRY_OPERATION_APP_ID,
                "运营数据",
                setting, MRY_OPERATION_GROUP_ID,
                MRY_MANAGE_ROBOT_USER);

        App app = result.getApp();
        Group defaultGroup = result.getDefaultGroup();
        appRepository.save(app);
        groupRepository.save(defaultGroup);
        groupHierarchyRepository.save(result.getGroupHierarchy());

        log.info("Created operation manage app.");
    }

    private FNumberInputControl createNumberInputControl(String name, String controlId) {
        return FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(controlId)
                .name(name)
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(0, MAX_NUMBER))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .build();
    }

    private FNumberInputControl.AutoCalculateSetting emptyAutoCalculateSetting() {
        return FNumberInputControl.AutoCalculateSetting.builder()
                .aliasContext(AutoCalculateAliasContext.builder().controlAliases(List.of()).build())
                .expression(null)
                .build();
    }
}
