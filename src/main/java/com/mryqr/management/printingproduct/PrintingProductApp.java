package com.mryqr.management.printingproduct;

import com.google.common.collect.ImmutableMap;
import com.mryqr.core.app.domain.AppFactory;
import com.mryqr.core.app.domain.AppHeaderImageProvider;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.CreateAppResult;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.config.AppConfig;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.app.domain.page.control.FMultiLineTextControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.menu.Menu;
import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.common.domain.TextOption;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.printing.domain.MaterialType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.mryqr.core.app.domain.AppTopBar.defaultAppTopBar;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.config.AppLandingPageType.DEFAULT;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSettingBuilder;
import static com.mryqr.core.app.domain.page.control.ControlNameSetting.defaultControlNameSetting;
import static com.mryqr.core.app.domain.page.control.ControlStyleSetting.defaultControlStyleSetting;
import static com.mryqr.core.app.domain.page.control.ControlType.DROPDOWN;
import static com.mryqr.core.app.domain.page.control.ControlType.MULTI_LINE_TEXT;
import static com.mryqr.core.app.domain.page.control.ControlType.SINGLE_LINE_TEXT;
import static com.mryqr.core.app.domain.page.header.PageHeader.defaultPageHeaderBuilder;
import static com.mryqr.core.app.domain.page.setting.PageSetting.defaultPageSettingBuilder;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.submitbutton.SubmitButton.defaultSubmitButton;
import static com.mryqr.core.app.domain.page.title.PageTitle.defaultPageTitleBuilder;
import static com.mryqr.core.app.domain.ui.BoxedTextStyle.defaultControlDescriptionStyle;
import static com.mryqr.core.app.domain.ui.ImageCropType.FOUR_TO_THREE;
import static com.mryqr.core.app.domain.ui.MinMaxSetting.minMaxOf;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.printing.domain.MaterialType.ARGENTOUS_ADHESIVE;
import static com.mryqr.core.printing.domain.MaterialType.PORCELAIN_ACRYLIC;
import static com.mryqr.core.printing.domain.MaterialType.PVC_CARD;
import static com.mryqr.core.printing.domain.MaterialType.SYNTHETIC_ADHESIVE;
import static com.mryqr.core.printing.domain.MaterialType.TRANSPARENT_ACRYLIC;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_ROBOT_USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrintingProductApp {
    public static final String PP_APP_ID = "APP00000000000000003";
    public static final String PP_GROUP_ID = "GRP00000000000000003";

    public static final String PP_HOME_PAGE_ID = "p_9TAqAU2jTCuHlWXzr2nfgg";

    public static final String PP_MATERIAL_TYPE_CONTROL_ID = "c_riE5v4tRuYxC2xBvMHxzTp";
    public static final String PP_TRANSPARENT_ACRYLIC_OPTION_ID = "2MqZtqSmSN4tdwisyj7LAB";
    public static final String PP_PORCELAIN_ACRYLIC_OPTION_ID = "HI_Z6iKvQtO-RLGRf8MTdg";
    public static final String PP_PVC_CARD_OPTION_ID = "sVd6c8pu73ehTG6hx7XbWN";
    public static final String PP_SYNTHETIC_ADHESIVE_OPTION_ID = "p3CXyvF739D9zZmRYZxTSi";
    public static final String PP_ARGENTOUS_ADHESIVE_OPTION_ID = "mYuAtd8VQhjiHqCmtvAoAK";
    public static final Map<String, MaterialType> MATERIAL_TYPE_MAP = ImmutableMap.of(
            PP_TRANSPARENT_ACRYLIC_OPTION_ID, TRANSPARENT_ACRYLIC,
            PP_PORCELAIN_ACRYLIC_OPTION_ID, PORCELAIN_ACRYLIC,
            PP_PVC_CARD_OPTION_ID, PVC_CARD,
            PP_SYNTHETIC_ADHESIVE_OPTION_ID, SYNTHETIC_ADHESIVE,
            PP_ARGENTOUS_ADHESIVE_OPTION_ID, ARGENTOUS_ADHESIVE
    );

    public static final String PP_DESCRIPTION_CONTROL_ID = "c_rJNUD9ceY6qczVhpXhtWn7";
    public static final String PP_INTRODUCTION_CONTROL_ID = "c_aVKk6fx3qYmWm4jasGwm14";

    public static final String PP_MATERIAL_TYPE_ATTRIBUTE_ID = "a_rQykxspnDiTo46pUsSpTkp";
    public static final String PP_DESCRIPTION_ATTRIBUTE_ID = "a_sD8JF8pCCy4bLApmEgwvGM";
    public static final String PP_INTRODUCTION_ATTRIBUTE_ID = "a_d9AWDLqS2vtg6Wq6tDXr8A";

    private final AppRepository appRepository;
    private final AppFactory appFactory;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final AppHeaderImageProvider appHeaderImageProvider;

    @Transactional
    public void init() {
        if (appRepository.exists(PP_APP_ID)) {
            return;
        }

        FDropdownControl materialTypeControl = FDropdownControl.builder()
                .type(DROPDOWN)
                .id(PP_MATERIAL_TYPE_CONTROL_ID)
                .name("码牌材质")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder()
                        .mandatory(true)
                        .submissionSummaryEligible(true)
                        .build())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .multiple(false)
                .minMaxSetting(minMaxOf(0, 10))
                .options(List.of(
                        TextOption.builder().id(PP_TRANSPARENT_ACRYLIC_OPTION_ID).name("透明亚克力").build(),
                        TextOption.builder().id(PP_PORCELAIN_ACRYLIC_OPTION_ID).name("瓷白亚克力").build(),
                        TextOption.builder().id(PP_PVC_CARD_OPTION_ID).name("PVC卡").build(),
                        TextOption.builder().id(PP_SYNTHETIC_ADHESIVE_OPTION_ID).name("PVC不干胶").build(),
                        TextOption.builder().id(PP_ARGENTOUS_ADHESIVE_OPTION_ID).name("亚银不干胶").build()
                ))
                .build();

        FSingleLineTextControl descriptionControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(PP_DESCRIPTION_CONTROL_ID)
                .name("简介")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder()
                        .mandatory(true)
                        .submissionSummaryEligible(true)
                        .build())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 30))
                .build();

        FMultiLineTextControl introductionControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(PP_INTRODUCTION_CONTROL_ID)
                .name("详细介绍")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder()
                        .mandatory(true)
                        .submissionSummaryEligible(false)
                        .build())
                .permissionEnabled(true)
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10000))
                .rows(5)
                .build();

        //首页
        Page homePage = Page.builder()
                .id(PP_HOME_PAGE_ID)
                .header(defaultPageHeaderBuilder()
                        .image(appHeaderImageProvider.defaultAppHeaderImage())
                        .imageCropType(FOUR_TO_THREE)
                        .build())
                .title(defaultPageTitleBuilder().text("产品信息").build())
                .controls(List.of(materialTypeControl, descriptionControl, introductionControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(ONCE_PER_INSTANCE)
                        .permission(CAN_MANAGE_APP)
                        .pageName("产品信息")
                        .build())
                .build();

        Attribute materialTypeAttribute = Attribute.builder()
                .id(PP_MATERIAL_TYPE_ATTRIBUTE_ID)
                .name("材质")
                .type(CONTROL_LAST)
                .pageId(homePage.getId())
                .controlId(materialTypeControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute descriptionAttribute = Attribute.builder()
                .id(PP_DESCRIPTION_ATTRIBUTE_ID)
                .name("简介")
                .type(CONTROL_LAST)
                .pageId(homePage.getId())
                .controlId(descriptionControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute introductionAttribute = Attribute.builder()
                .id(PP_INTRODUCTION_ATTRIBUTE_ID)
                .name("详细介绍")
                .type(CONTROL_LAST)
                .pageId(homePage.getId())
                .controlId(introductionControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(false)
                .build();

        Menu menu = Menu.builder().links(List.of()).build();

        AppSetting setting = AppSetting.builder()
                .config(AppConfig.builder()
                        .operationPermission(CAN_MANAGE_APP)
                        .landingPageType(DEFAULT)
                        .qrWebhookTypes(List.of())
                        .instanceAlias("印刷产品")
                        .appManualEnabled(true)
                        .homePageId(homePage.getId())
                        .build())
                .appTopBar(defaultAppTopBar())
                .pages(List.of(homePage))
                .menu(menu)
                .attributes(List.of(materialTypeAttribute,
                        descriptionAttribute,
                        introductionAttribute))
                .operationMenuItems(List.of())
                .plateSetting(PlateSetting.create())
                .circulationStatusSetting(CirculationStatusSetting.create())
                .build();

        CreateAppResult result = appFactory.create(PP_APP_ID,
                "印刷产品",
                setting, PP_GROUP_ID,
                MRY_MANAGE_ROBOT_USER);

        appRepository.save(result.getApp());
        groupRepository.save(result.getDefaultGroup());
        groupHierarchyRepository.save(result.getGroupHierarchy());

        log.info("Created printing service product list app.");

    }
}
