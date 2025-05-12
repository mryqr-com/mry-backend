package com.mryqr.management.offencereport;

import com.mryqr.common.domain.TextOption;
import com.mryqr.common.properties.CommonProperties;
import com.mryqr.core.app.domain.*;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.config.AppConfig;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.app.domain.page.menu.Menu;
import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.app.domain.ui.AppearanceStyle;
import com.mryqr.core.app.domain.ui.ButtonStyle;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.border.BorderSide;
import com.mryqr.core.app.domain.ui.pagelink.PageLink;
import com.mryqr.core.app.domain.ui.shadow.Shadow;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.qr.domain.PlatedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrFactory;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.common.domain.permission.Permission.PUBLIC;
import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.AppTopBar.defaultAppTopBar;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CREATE_TIME;
import static com.mryqr.core.app.domain.config.AppLandingPageType.DEFAULT;
import static com.mryqr.core.app.domain.page.Page.newPageId;
import static com.mryqr.core.app.domain.page.control.AnswerUniqueType.NONE;
import static com.mryqr.core.app.domain.page.control.Control.newControlId;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSetting;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSettingBuilder;
import static com.mryqr.core.app.domain.page.control.ControlNameSetting.defaultControlNameSetting;
import static com.mryqr.core.app.domain.page.control.ControlStyleSetting.defaultControlStyleSetting;
import static com.mryqr.core.app.domain.page.control.ControlType.*;
import static com.mryqr.core.app.domain.page.control.FileCompressType.MEDIUM;
import static com.mryqr.core.app.domain.page.control.PSubmissionReferenceControl.StyleType.HORIZONTAL_TABLE;
import static com.mryqr.core.app.domain.page.header.PageHeader.defaultPageHeaderBuilder;
import static com.mryqr.core.app.domain.page.header.PageHeaderType.INHERITED;
import static com.mryqr.core.app.domain.page.setting.PageSetting.defaultPageSettingBuilder;
import static com.mryqr.core.app.domain.page.setting.SubmissionWebhookType.ON_CREATE;
import static com.mryqr.core.app.domain.page.setting.SubmitType.NEW;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.submitbutton.SubmitButton.defaultSubmitButton;
import static com.mryqr.core.app.domain.page.title.PageTitle.defaultPageTitleBuilder;
import static com.mryqr.core.app.domain.ui.AppearanceStyle.defaultAppearanceStyle;
import static com.mryqr.core.app.domain.ui.BoxedTextStyle.defaultBoxedTextStyle;
import static com.mryqr.core.app.domain.ui.BoxedTextStyle.defaultControlDescriptionStyle;
import static com.mryqr.core.app.domain.ui.FontStyle.defaultFontStyle;
import static com.mryqr.core.app.domain.ui.ImageCropType.FOUR_TO_THREE;
import static com.mryqr.core.app.domain.ui.MinMaxSetting.minMaxOf;
import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static com.mryqr.core.app.domain.ui.border.BorderType.SOLID;
import static com.mryqr.core.app.domain.ui.pagelink.PageLinkType.PAGE;
import static com.mryqr.core.app.domain.ui.shadow.Shadow.noShadow;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_ROBOT_USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class MryOffenceReportApp {
    public static final String MRY_OFFENCE_APP_ID = "APP00000000000000005";
    public static final String MRY_OFFENCE_GROUP_ID = "GRP00000000000000005";

    public static final String OFFENCE_TEMPLATE_PLATE_ID = "MRY337143110947570688";
    public static final String OFFENCE_FORM_PAGE_ID = "p_VvDFqQ78Q5m5K15vK_UkTg";
    public static final String OFFENCE_REASON_CONTROL_ID = "c_r0KjTUKXTmud9sxkEVr2Sg";
    public static final String OFFENCE_DETAIL_CONTROL_ID = "c_He9O0tQfQI2zIaMyfKZWEQ";
    public static final String OFFENCE_FILES_CONTROL_ID = "c_oaGn_WxdSgiEQllOMYSNjA";
    public static final String OFFENCE_MOBILE_CONTROL_ID = "c_o5HLKU7XQhWGnT9_yyHbkg";

    public static final String OFFENCE_SYNC_PAGE_ID = "p_m7rlz2TnTDSPVShsBW6oOg";
    public static final String OFFENCE_SYNC_TENANT_NAME_CONTROL_ID = "c_zLdWa7BvRx2btZXZghwwKg";
    public static final String OFFENCE_SYNC_APP_NAME_CONTROL_ID = "c_P-ZptrPeRPCUrplwVE3_kQ";
    public static final String OFFENCE_SYNC_QR_NAME_CONTROL_ID = "c_AAPcrGd-Q4WQKsuICfkRrQ";
    public static final String OFFENCE_SYNC_TENANT_ID_CONTROL_ID = "c_a5piOxuRTtKCMky03752zA";
    public static final String OFFENCE_SYNC_APP_ID_CONTROL_ID = "c_cg_qJbReSTCAh51ibKOjnw";
    public static final String OFFENCE_SYNC_QR_ID_CONTROL_ID = "c_kTkqdL0nRtuNIyTVXmZU2Q";
    public static final String OFFENCE_SYNC_URL_CONTROL_ID = "c_WX9PQ0X7RBypneadXeXnig";

    private final AppRepository appRepository;
    private final AppFactory appFactory;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final AppHeaderImageProvider appHeaderImageProvider;
    private final CommonProperties commonProperties;
    private final ServerProperties serverProperties;
    private final QrFactory qrFactory;
    private final QrRepository qrRepository;
    private final PlateRepository plateRepository;

    @Transactional
    public void init() {
        if (appRepository.exists(MRY_OFFENCE_APP_ID)) {
            return;
        }

        FRadioControl offenceReasonControl = FRadioControl.builder()
                .type(RADIO)
                .id(OFFENCE_REASON_CONTROL_ID)
                .name("举报理由")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(newShortUuid()).name("传播黄赌毒等非法内容").build(),
                        TextOption.builder().id(newShortUuid()).name("非法集资或传销").build(),
                        TextOption.builder().id(newShortUuid()).name("假冒官网发布信息").build(),
                        TextOption.builder().id(newShortUuid()).name("侵犯他人知识产权").build(),
                        TextOption.builder().id(newShortUuid()).name("发布虚假信息").build(),
                        TextOption.builder().id(newShortUuid()).name("其他").build()
                ))
                .build();

        FMultiLineTextControl offenceDetailControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(OFFENCE_DETAIL_CONTROL_ID)
                .name("详细描述")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10000))
                .rows(5)
                .build();

        FImageUploadControl offenceImageControl = FImageUploadControl.builder()
                .type(IMAGE_UPLOAD)
                .id(OFFENCE_FILES_CONTROL_ID)
                .name("证明文件")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .compressType(MEDIUM)
                .max(5)
                .buttonText("上传图片")
                .buttonStyle(ButtonStyle.builder()
                        .fontStyle(FontStyle.builder()
                                .fontFamily("默认")
                                .fontSize(14)
                                .bold(false)
                                .italic(false)
                                .color("rgba(48, 49, 51, 1)")
                                .build())
                        .backgroundColor("rgba(237, 241, 248, 1)")
                        .border(Border.builder().type(SOLID).width(1).sides(Set.of(BorderSide.values())).color("rgba(220, 223, 230, 1)").build())
                        .shadow(noShadow())
                        .vPadding(10)
                        .borderRadius(0)
                        .build())
                .build();

        FMobileNumberControl offenceMobileControl = FMobileNumberControl.builder()
                .type(MOBILE)
                .id(OFFENCE_MOBILE_CONTROL_ID)
                .name("手机号")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .build();

        Page offenceFormPage = Page.builder()
                .id(OFFENCE_FORM_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("举报").build())
                .controls(List.of(offenceReasonControl, offenceDetailControl, offenceImageControl, offenceMobileControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .showAsterisk(true)
                        .submitType(ONCE_PER_INSTANCE)
                        .pageName("举报表单")
                        .permission(PUBLIC)
                        .submissionWebhookTypes(List.of(ON_CREATE))
                        .build())
                .build();

        FMultiLineTextControl processDetailControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(newControlId())
                .name("处理详情")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10000))
                .rows(3)
                .build();

        FItemStatusControl statusControl = FItemStatusControl.builder()
                .type(ITEM_STATUS)
                .id(newControlId())
                .name("处理状态")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(newShortUuid()).name("进行中").color("rgba(254, 190, 16, 1)").build(),
                        TextOption.builder().id(newShortUuid()).name("处理完毕").color("rgba(0, 195, 0, 1)").build(),
                        TextOption.builder().id(newShortUuid()).name("误报").color("rgba(169, 169, 169, 1)").build()
                ))
                .build();

        Page processPage = Page.builder()
                .id(newPageId())
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("添加处理记录").build())
                .controls(List.of(processDetailControl, statusControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("处理记录")
                        .permission(CAN_MANAGE_APP)
                        .build())
                .build();

        FSingleLineTextControl syncTenantNameControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(OFFENCE_SYNC_TENANT_NAME_CONTROL_ID)
                .name("租户名称")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        FSingleLineTextControl syncAppNameControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(OFFENCE_SYNC_APP_NAME_CONTROL_ID)
                .name("应用名称")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        FSingleLineTextControl syncQrNameControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(OFFENCE_SYNC_QR_NAME_CONTROL_ID)
                .name("实例名称")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        FIdentifierControl syncTenantIdControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(OFFENCE_SYNC_TENANT_ID_CONTROL_ID)
                .name("租户编号")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FIdentifierControl syncAppIdControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(OFFENCE_SYNC_APP_ID_CONTROL_ID)
                .name("应用编号")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FIdentifierControl syncQrIdControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(OFFENCE_SYNC_QR_ID_CONTROL_ID)
                .name("实例编号")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FMultiLineTextControl syncUrlControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(OFFENCE_SYNC_URL_CONTROL_ID)
                .name("页面地址")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10000))
                .rows(3)
                .build();

        Page syncPage = Page.builder()
                .id(OFFENCE_SYNC_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("后台数据同步").build())
                .controls(List.of(syncTenantNameControl,
                        syncAppNameControl,
                        syncQrNameControl,
                        syncTenantIdControl,
                        syncAppIdControl,
                        syncQrIdControl,
                        syncUrlControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(ONCE_PER_INSTANCE)
                        .pageName("后台数据同步")
                        .permission(CAN_MANAGE_APP)
                        .build())
                .build();

        PSubmissionReferenceControl offenceFormRefControl = PSubmissionReferenceControl.builder()
                .type(SUBMISSION_REFERENCE)
                .id(newControlId())
                .name("用户举报信息")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .permission(CAN_MANAGE_APP)
                .pageId(offenceFormPage.getId())
                .styleType(HORIZONTAL_TABLE)
                .keyFontStyle(defaultFontStyle())
                .valueFontStyle(defaultFontStyle())
                .headerFontStyle(defaultFontStyle())
                .verticalKeyStyle(defaultBoxedTextStyle())
                .verticalValueStyle(defaultBoxedTextStyle())
                .appearanceStyle(defaultAppearanceStyle())
                .stripped(true)
                .build();

        PSubmissionReferenceControl offenceSyncRefControl = PSubmissionReferenceControl.builder()
                .type(SUBMISSION_REFERENCE)
                .id(newControlId())
                .name("举报对象")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .permission(CAN_MANAGE_APP)
                .pageId(syncPage.getId())
                .styleType(HORIZONTAL_TABLE)
                .keyFontStyle(defaultFontStyle())
                .valueFontStyle(defaultFontStyle())
                .headerFontStyle(defaultFontStyle())
                .verticalKeyStyle(defaultBoxedTextStyle())
                .verticalValueStyle(defaultBoxedTextStyle())
                .appearanceStyle(defaultAppearanceStyle())
                .stripped(true)
                .build();

        PSubmitHistoryControl processHistoryControl = PSubmitHistoryControl.builder()
                .type(SUBMIT_HISTORY)
                .id(newControlId())
                .name("处理记录")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(null)
                .permission(CAN_MANAGE_APP)
                .pageIds(newArrayList(processPage.getId()))
                .appearanceStyle(AppearanceStyle.builder()
                        .backgroundColor("rgba(255, 255, 255, 1)")
                        .borderRadius(0)
                        .shadow(Shadow.builder()
                                .width(6)
                                .color("rgba(0, 0, 0, 0.15)")
                                .build())
                        .border(noBorder())
                        .vPadding(10)
                        .hPadding(15)
                        .build())
                .max(100)
                .showSubmitter(true)
                .showPageName(false)
                .build();

        Page homePage = Page.builder()
                .id(Page.newPageId())
                .header(defaultPageHeaderBuilder()
                        .image(appHeaderImageProvider.defaultAppHeaderImage())
                        .imageCropType(FOUR_TO_THREE)
                        .build())
                .title(defaultPageTitleBuilder().text("举报详情").build())
                .controls(List.of(offenceFormRefControl, offenceSyncRefControl, processHistoryControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("举报详情")
                        .build())
                .build();

        Attribute createdAtAttribute = Attribute.builder()
                .id(newAttributeId())
                .name("创建时间")
                .type(INSTANCE_CREATE_TIME)
                .pcListEligible(true)
                .build();

        Attribute statusAttribute = Attribute.builder()
                .id(newAttributeId())
                .name("当前状态")
                .type(CONTROL_LAST)
                .pageId(processPage.getId())
                .controlId(statusControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute tenantNameAttribute = Attribute.builder()
                .id(newAttributeId())
                .name("租户名称")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(syncTenantNameControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute reasonAttribute = Attribute.builder()
                .id(newAttributeId())
                .name("举报理由")
                .type(CONTROL_LAST)
                .pageId(offenceFormPage.getId())
                .controlId(offenceReasonControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Menu menu = Menu.builder().links(List.of(PageLink.builder()
                        .id(newShortUuid())
                        .name("添加处理记录")
                        .type(PAGE)
                        .pageId(processPage.getId())
                        .build()))
                .build();

        AppSetting setting = AppSetting.builder()
                .config(AppConfig.builder()
                        .operationPermission(CAN_MANAGE_APP)
                        .landingPageType(DEFAULT)
                        .qrWebhookTypes(List.of())
                        .homePageId(homePage.getId())
                        .appManualEnabled(true)
                        .instanceAlias("举报")
                        .build())
                .appTopBar(defaultAppTopBar())
                .pages(List.of(homePage, offenceFormPage, processPage, syncPage))
                .menu(menu)
                .attributes(List.of(createdAtAttribute, statusAttribute, reasonAttribute, tenantNameAttribute))
                .operationMenuItems(List.of())
                .plateSetting(PlateSetting.create())
                .circulationStatusSetting(CirculationStatusSetting.create())
                .build();

        CreateAppResult result = appFactory.create(MRY_OFFENCE_APP_ID,
                "举报管理",
                setting, MRY_OFFENCE_GROUP_ID,
                MRY_MANAGE_ROBOT_USER);

        App app = result.getApp();
        Group defaultGroup = result.getDefaultGroup();
        WebhookSetting webhookSetting = WebhookSetting.builder()
                .enabled(true)
                .notAccessible(false)
                .url("http://localhost:" + serverProperties.getPort() + "/webhook")
                .username(commonProperties.getWebhookUserName())
                .password(commonProperties.getWebhookPassword())
                .build();

        app.updateWebhookSetting(webhookSetting, NOUSER);

        appRepository.save(app);
        groupRepository.save(defaultGroup);
        groupHierarchyRepository.save(result.getGroupHierarchy());

        log.info("Created offence reporting manage app.");

        PlatedQr platedQr = qrFactory.createPlatedQr("举报", OFFENCE_TEMPLATE_PLATE_ID, defaultGroup, app, MRY_MANAGE_ROBOT_USER);
        QR qr = platedQr.getQr();
        qr.markAsTemplate(MRY_MANAGE_ROBOT_USER);
        Plate plate = platedQr.getPlate();
        qrRepository.save(qr);
        plateRepository.save(plate);
    }
}
