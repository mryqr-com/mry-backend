package com.mryqr.management.apptemplate;

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
import com.mryqr.core.app.domain.page.control.AutoCalculateAliasContext;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.app.domain.page.control.FIdentifierControl;
import com.mryqr.core.app.domain.page.control.FItemStatusControl;
import com.mryqr.core.app.domain.page.control.FMultiLineTextControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.control.IdentifierFormatType;
import com.mryqr.core.app.domain.page.menu.Menu;
import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.app.domain.ui.pagelink.PageLink;
import com.mryqr.core.common.domain.TextOption;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.mryqr.core.app.domain.AppTopBar.defaultAppTopBar;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.attribute.AttributeType.DIRECT_INPUT;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CUSTOM_ID;
import static com.mryqr.core.app.domain.config.AppLandingPageType.DEFAULT;
import static com.mryqr.core.app.domain.page.control.AnswerUniqueType.NONE;
import static com.mryqr.core.app.domain.page.control.Control.newControlId;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSetting;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSettingBuilder;
import static com.mryqr.core.app.domain.page.control.ControlNameSetting.defaultControlNameSetting;
import static com.mryqr.core.app.domain.page.control.ControlStyleSetting.defaultControlStyleSetting;
import static com.mryqr.core.app.domain.page.control.ControlType.DROPDOWN;
import static com.mryqr.core.app.domain.page.control.ControlType.IDENTIFIER;
import static com.mryqr.core.app.domain.page.control.ControlType.ITEM_STATUS;
import static com.mryqr.core.app.domain.page.control.ControlType.MULTI_LINE_TEXT;
import static com.mryqr.core.app.domain.page.control.ControlType.NUMBER_INPUT;
import static com.mryqr.core.app.domain.page.control.ControlType.SINGLE_LINE_TEXT;
import static com.mryqr.core.app.domain.page.control.FNumberInputControl.MAX_NUMBER;
import static com.mryqr.core.app.domain.page.header.PageHeader.defaultPageHeaderBuilder;
import static com.mryqr.core.app.domain.page.header.PageHeaderType.INHERITED;
import static com.mryqr.core.app.domain.page.setting.PageSetting.defaultPageSettingBuilder;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.submitbutton.SubmitButton.defaultSubmitButton;
import static com.mryqr.core.app.domain.page.title.PageTitle.defaultPageTitleBuilder;
import static com.mryqr.core.app.domain.ui.BoxedTextStyle.defaultControlDescriptionStyle;
import static com.mryqr.core.app.domain.ui.ImageCropType.FOUR_TO_THREE;
import static com.mryqr.core.app.domain.ui.MinMaxSetting.minMaxOf;
import static com.mryqr.core.app.domain.ui.pagelink.PageLinkType.PAGE;
import static com.mryqr.core.common.domain.ValueType.DOUBLE_VALUE;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.management.apptemplate.MryAppTemplateTenant.APP_TEMPLATE_ROBOT_USER;
import static com.mryqr.management.common.PlanTypeControl.createPlanTypeControl;

@Slf4j
@Component
@RequiredArgsConstructor
public class MryAppTemplateManageApp {
    public static final String MRY_APP_TEMPLATE_MANAGE_APP_ID = "APP00000000000000010";
    public static final String MRY_APP_TEMPLATE_MANAGE_APP_GROUP_ID = "GRP00000000000000010";

    public static final String TEMPLATE_HOME_PAGE_ID = "p_F4O1z6lTRtet_M2GUhC7rw";//首页ID
    public static final String APP_LINK_CONTROL_ID = "c_kH0Lz3EISGews0gp64pvtA";//应用链接录入

    public static final String REFED_APP_ATTRIBUTE_ID = "a_nM2Tmn63RCC_CaqH28QT8Q";//所引用模板应用

    public static final String INTRODUCTION_CONTROL_ID = "c_WSSwc5X3SZmlym4trbkIFg";//模板详情录入
    public static final String INTRODUCTION_ATTRIBUTE_ID = "a_taXF_RCtRdKmsZA8OW3Jgw";//模板详情属性

    public static final String PAGE_MODIFIER_CONTROL_ID = "c_SyPvH0fTQueK5qaAVTbAnw";//页面配置
    public static final String PAGE_MODIFIER_ATTRIBUTE_ID = "a_YgmFPdsNRB2mufV9z_wCMQ";//页面配置属性

    public static final String CARD_DESCRIPTION_CONTROL_ID = "c_Kz6-K7rTR6CBSyOv0AsFVw";//卡片简介录入
    public static final String CARD_DESCRIPTION_ATTRIBUTE_ID = "a_jKYSfm-MQgu8PWpt8zthHA";//卡片简介属性

    public static final String CONFIG_PAGE_ID = "p_3fR1ve1ZTLy-dIZZQO5x2g";//模板基本信息录入页面
    public static final String STATUS_CONTROL_ID = "c_98HZa0e_SaOonZqm4ZecJg";//发布状态控件
    public static final String STATUS_ATTRIBUTE_ID = "a_GvFgt6wNRx6juexcO-J4ow";//发布状态属性
    public static final String PUBLISHED_STATUS_OPTION_ID = "u2jGD4RlSqmXaThMyl4Ecw";//已发布
    public static final String TOBE_PUBLISHED_STATUS_OPTION_ID = "oMcD6NPITKio0P8O6-dinw";//未发布

    public static final String TEMPLATE_PLAN_TYPE_CONTROL_ID = "c_2f7g5Ql9Sz6o1INI7NXGkw";//套餐控件
    public static final String TEMPLATE_PLAN_TYPE_ATTRIBUTE_ID = "a_L84hKbdtQP6kZmw14N1sNg";//套餐属性

    public static final String APPLIED_COUNT_ATTRIBUTE_ID = "a_0fQwMtxcTI-STIfNBpubPg";//使用次数属性

    public static final String CATEGORY_CONTROL_ID = "c_LefEfg4iRLKTCT0nOeDgnA";//适用行业控件
    public static final String CATEGORIES_ATTRIBUTE_ID = "a_Y1H_FGcKTJWqXtrEfT8l7Q";//使用行业属性
    public static final String CATEGORY_ORGANIZATION_ID = "GcHgD8QtT3-yjTS6FvSE1A";
    public static final String CATEGORY_AGRICULTURE_ID = "X72i7oG0RPScgHMS0hWgyA";
    public static final String CATEGORY_MEDICAL_ID = "xbMCyqhmQkepig1EMpjUHA";
    public static final String CATEGORY_ARCHITECTURE_ID = "OsOInNp5SgennYYc3i8FdQ";
    public static final String CATEGORY_MINERALS_ID = "VO42VUhAQD23AvCh-8dX5Q";
    public static final String CATEGORY_IRRIGATION_ID = "wzpAo82ERIOUihEtzszc-g";
    public static final String CATEGORY_CHEMISTRY_ID = "_ODJvL1FT5Ws9TIL0vXmkQ";
    public static final String CATEGORY_TRANSPORTATION_ID = "IuepzShUSN-eqKogURV0MQ";
    public static final String CATEGORY_INFORMATION_ID = "yyCpJMCrSs-aQ-dVO1Sj6Q";
    public static final String CATEGORY_MECHANICS_ID = "aq7gd6uSR4SWmiOvgYI1Kg";
    public static final String CATEGORY_FOOD_ID = "ehiAFgvFTT2IK-lEoWsJWg";
    public static final String CATEGORY_TEXTILE_ID = "5-VCaBt0R6KKUByyeGLcQw";
    public static final String CATEGORY_SERVICE_ID = "VtiyFWRtRPqjwXOcFEaSRw";
    public static final String CATEGORY_SECURITY_ID = "QyASA9GPRyidczTUgKXm_w";
    public static final String CATEGORY_ENVIRONMENT_ID = "mrtVhm1CSviLcVN04MleUQ";
    public static final String CATEGORY_TRAVEL_ID = "qZVleuUkQtW0hDb7-Xgfww";
    public static final String CATEGORY_OFFICE_ID = "glAJsoGaT_SFGYTVzcCc6A";
    public static final String CATEGORY_ELECTRONICS_ID = "HzHGw-gMQJ-CnSIos3JvYA";
    public static final String CATEGORY_TOY_ID = "YkY3sfaOQmaXNJHOQkw-uQ";
    public static final String CATEGORY_HOME_ID = "EEZ9nZfTQFqPZnJn4_6fJA";
    public static final String CATEGORY_PE_ID = "YVB7H9AiQX2nFSlRGRsiFg";

    public static final String FEATURE_CONTROL_ID = "c_oWm8RkXuQPu2gs80gucmrw";//功能特性控件
    public static final String FEATURE_ATTRIBUTE_ID = "a_6MNlQbF-TMOYAhhJm4savQ";//功能特性属性
    public static final String FEATURE_REPORT_ID = "nzwRi9MESi-dPdzjI6XIMA";
    public static final String FEATURE_NOTIFY_ID = "ohcSjVE4R1SuzLyLQoBe8Q";
    public static final String FEATURE_STATUS_ID = "Pcc_DN-dSfqqgFIILHJcHg";
    public static final String FEATURE_TASK_ID = "p-K5BYxFTqOhZkbXV8NArQ";
    public static final String FEATURE_APPROVAL_ID = "Y67l9z9vRbay-1zv6Gte3A";
    public static final String FEATURE_BATCH_ID = "3uODAae2S4ikGlGgNJdJ3g";
    public static final String FEATURE_GEOLOCATION_ID = "6IrGINpxQkyYaubvwUrzqA";
    public static final String FEATURE_CIRCULATION_ID = "bUYmntVhu3cfzNe4jHFUtd";

    public static final String DISPLAY_ORDER_CONTROL_ID = "c_vjvTTeBBby1zhkW9P1ZFvM";//排序控件
    public static final String DISPLAY_ORDER_ATTRIBUTE_ID = "a_2YHjiQ7gvtEpVkwZzWKAkq";//排序属性，数值越大越靠前

    public static final Map<String, String> FEATURE_NAMES_MAP = ImmutableMap.of(
            FEATURE_REPORT_ID, "统计报表",
            FEATURE_NOTIFY_ID, "提交提醒",
            FEATURE_STATUS_ID, "状态看板",
            FEATURE_TASK_ID, "定期任务",
            FEATURE_APPROVAL_ID, "表单审批",
            FEATURE_BATCH_ID, "批量生码",
            FEATURE_GEOLOCATION_ID, "实例定位",
            FEATURE_CIRCULATION_ID, "状态流转"
    );

    public static final String SCENARIO_CONTROL_ID = "c_jMB8fbAXR5mC8Xe4LIALLg";//适用场景控件
    public static final String SCENARIO_ATTRIBUTE_ID = "a_Gjfo4-lmT3ahVCczN_3BAg";//适用场景属性
    public static final String SCENARIO_MANAGE_OPTION_ID = "8i4HpAFkW7MHjBnZSvV4DM";//设备管理
    public static final String SCENARIO_INSPECTION_OPTION_ID = "5Gr6JiLhQMq-Owy1g15Cvg";//巡检维保
    public static final String SCENARIO_ASSET_OPTION_ID = "mHgmPDIMTaKQRlWvSlv-kQ";//固定资产
    public static final String SCENARIO_LABEL_OPTION_ID = "3lCRiPZTTuyvfkBXw30IQw";//物品标签
    public static final String SCENARIO_INTRODUCTION_OPTION_ID = "Mem4pGyDQXCjWIBi4Xfe_g";//信息介绍
    public static final String SCENARIO_QUESTIONNAIRE_OPTION_ID = "yC16ZFaoSWSHK0NCa849zA";//问卷调查
    public static final String SCENARIO_REGISTRATION_OPTION_ID = "MCJPteBbSzqPLFKIu7rQtw";//登记申请
    public static final String SCENARIO_SUGGESTION_OPTION_ID = "xufrCcjVQ7OJ6bz2DGvunA";//意见反馈
    public static final String SCENARIO_MARKETING_OPTION_ID = "410M7wy5TZufBKC6WgBdnA";//营销获客
    public static final String SCENARIO_TEACH_OPTION_ID = "lVrObWP5RZ200SmuGntzcA";//教学培训
    public static final String SCENARIO_EXAM_OPTION_ID = "6BNvi2pcRcSx8YwxGCwvPg";//考试评分

    public static final String DEMO_QR_CONTROL_ID = "c_2Qnwbr0ESX25s1Lac_geLQ";//演示QR控件
    public static final String DEMO_QR_ATTRIBUTE_ID = "a_mQAmPkzzTWuG4eI81oqcxg";//演示QR控件属性

    private final AppRepository appRepository;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final AppFactory appFactory;
    private final AppHeaderImageProvider appHeaderImageProvider;

    @Transactional
    public void init() {
        if (appRepository.exists(MRY_APP_TEMPLATE_MANAGE_APP_ID)) {
            return;
        }

        FMultiLineTextControl appLinkControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(APP_LINK_CONTROL_ID)
                .name("应用链接")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 500))
                .rows(3)
                .build();

        FSingleLineTextControl cardDescriptionControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(CARD_DESCRIPTION_CONTROL_ID)
                .name("卡片简介")
                .description("在模板列表页面，针对每个模板的简介，显示在模板图片下方")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 50))
                .build();

        FMultiLineTextControl introductionControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(INTRODUCTION_CONTROL_ID)
                .name("模板详情")
                .description("设置模板详情介绍页面的内容")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 50000))
                .rows(10)
                .build();

        Page homePage = Page.builder()
                .id(MryAppTemplateManageApp.TEMPLATE_HOME_PAGE_ID)
                .header(defaultPageHeaderBuilder()
                        .image(appHeaderImageProvider.defaultAppHeaderImage())
                        .imageCropType(FOUR_TO_THREE)
                        .build())
                .title(defaultPageTitleBuilder().text("模板首页").build())
                .controls(List.of(appLinkControl, cardDescriptionControl, introductionControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(ONCE_PER_INSTANCE)
                        .pageName("模板首页")
                        .showAsterisk(true)
                        .build())
                .build();

        FItemStatusControl statusControl = FItemStatusControl.builder()
                .type(ITEM_STATUS)
                .id(STATUS_CONTROL_ID)
                .name("状态设置")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(PUBLISHED_STATUS_OPTION_ID).name("已发布").color("#10b01b").build(),
                        TextOption.builder().id(TOBE_PUBLISHED_STATUS_OPTION_ID).name("未发布").color("#FF8C00").build()
                ))
                .build();

        FDropdownControl planTypeControl = createPlanTypeControl("套餐等级", TEMPLATE_PLAN_TYPE_CONTROL_ID, true);

        FDropdownControl featureControl = FDropdownControl.builder()
                .type(DROPDOWN)
                .id(FEATURE_CONTROL_ID)
                .name("功能特性")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(false).build())
                .permission(CAN_MANAGE_APP)
                .multiple(true)
                .minMaxSetting(minMaxOf(0, 3))
                .options(List.of(
                        TextOption.builder().id(FEATURE_REPORT_ID).name(FEATURE_NAMES_MAP.get(FEATURE_REPORT_ID)).build(),
                        TextOption.builder().id(FEATURE_NOTIFY_ID).name(FEATURE_NAMES_MAP.get(FEATURE_NOTIFY_ID)).build(),
                        TextOption.builder().id(FEATURE_STATUS_ID).name(FEATURE_NAMES_MAP.get(FEATURE_STATUS_ID)).build(),
                        TextOption.builder().id(FEATURE_APPROVAL_ID).name(FEATURE_NAMES_MAP.get(FEATURE_APPROVAL_ID)).build(),
                        TextOption.builder().id(FEATURE_TASK_ID).name(FEATURE_NAMES_MAP.get(FEATURE_TASK_ID)).build(),
                        TextOption.builder().id(FEATURE_BATCH_ID).name(FEATURE_NAMES_MAP.get(FEATURE_BATCH_ID)).build(),
                        TextOption.builder().id(FEATURE_GEOLOCATION_ID).name(FEATURE_NAMES_MAP.get(FEATURE_GEOLOCATION_ID)).build(),
                        TextOption.builder().id(FEATURE_CIRCULATION_ID).name(FEATURE_NAMES_MAP.get(FEATURE_CIRCULATION_ID)).build()
                ))
                .build();

        FDropdownControl categoriesControl = FDropdownControl.builder()
                .type(DROPDOWN)
                .id(CATEGORY_CONTROL_ID)
                .name("适用行业")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(false).build())
                .permission(CAN_MANAGE_APP)
                .multiple(true)
                .minMaxSetting(minMaxOf(0, 10))
                .options(List.of(
                        TextOption.builder().id(CATEGORY_ORGANIZATION_ID).name("组织机构").build(),
                        TextOption.builder().id(CATEGORY_AGRICULTURE_ID).name("农林牧渔").build(),
                        TextOption.builder().id(CATEGORY_MEDICAL_ID).name("医疗卫生").build(),
                        TextOption.builder().id(CATEGORY_ARCHITECTURE_ID).name("建筑建材").build(),
                        TextOption.builder().id(CATEGORY_MINERALS_ID).name("冶金矿产").build(),
                        TextOption.builder().id(CATEGORY_IRRIGATION_ID).name("水利水电").build(),
                        TextOption.builder().id(CATEGORY_CHEMISTRY_ID).name("石油化工").build(),
                        TextOption.builder().id(CATEGORY_TRANSPORTATION_ID).name("交通运输").build(),
                        TextOption.builder().id(CATEGORY_INFORMATION_ID).name("信息产业").build(),
                        TextOption.builder().id(CATEGORY_MECHANICS_ID).name("机械机电").build(),
                        TextOption.builder().id(CATEGORY_FOOD_ID).name("轻工食品").build(),
                        TextOption.builder().id(CATEGORY_TEXTILE_ID).name("服装纺织").build(),
                        TextOption.builder().id(CATEGORY_SERVICE_ID).name("专业服务").build(),
                        TextOption.builder().id(CATEGORY_SECURITY_ID).name("安全防护").build(),
                        TextOption.builder().id(CATEGORY_ENVIRONMENT_ID).name("环保绿化").build(),
                        TextOption.builder().id(CATEGORY_TRAVEL_ID).name("旅游休闲").build(),
                        TextOption.builder().id(CATEGORY_OFFICE_ID).name("办公文教").build(),
                        TextOption.builder().id(CATEGORY_ELECTRONICS_ID).name("电子电工").build(),
                        TextOption.builder().id(CATEGORY_TOY_ID).name("玩具礼品").build(),
                        TextOption.builder().id(CATEGORY_HOME_ID).name("家居用品").build(),
                        TextOption.builder().id(CATEGORY_PE_ID).name("体育产业").build()
                ))
                .build();

        FDropdownControl scenariosControl = FDropdownControl.builder()
                .type(DROPDOWN)
                .id(SCENARIO_CONTROL_ID)
                .name("适用场景")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(false).build())
                .permission(CAN_MANAGE_APP)
                .multiple(true)
                .minMaxSetting(minMaxOf(0, 10))
                .options(List.of(
                        TextOption.builder().id(SCENARIO_MANAGE_OPTION_ID).name("设备管理").build(),
                        TextOption.builder().id(SCENARIO_INSPECTION_OPTION_ID).name("巡检维保").build(),
                        TextOption.builder().id(SCENARIO_ASSET_OPTION_ID).name("固定资产").build(),
                        TextOption.builder().id(SCENARIO_LABEL_OPTION_ID).name("物品标签").build(),
                        TextOption.builder().id(SCENARIO_INTRODUCTION_OPTION_ID).name("信息介绍").build(),
                        TextOption.builder().id(SCENARIO_QUESTIONNAIRE_OPTION_ID).name("问卷调查").build(),
                        TextOption.builder().id(SCENARIO_REGISTRATION_OPTION_ID).name("登记申请").build(),
                        TextOption.builder().id(SCENARIO_SUGGESTION_OPTION_ID).name("意见反馈").build(),
                        TextOption.builder().id(SCENARIO_MARKETING_OPTION_ID).name("营销获客").build(),
                        TextOption.builder().id(SCENARIO_TEACH_OPTION_ID).name("教学培训").build(),
                        TextOption.builder().id(SCENARIO_EXAM_OPTION_ID).name("考试评分").build()
                ))
                .build();

        FIdentifierControl demoQrControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(DEMO_QR_CONTROL_ID)
                .name("演示实例码牌ID")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FIdentifierControl tagControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(newControlId())
                .name("标签(以空格或逗号分割)")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(false).build())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FNumberInputControl displayOrderControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(DISPLAY_ORDER_CONTROL_ID)
                .name("显示顺序")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(0, MAX_NUMBER))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .build();

        FMultiLineTextControl pageModifierControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(PAGE_MODIFIER_CONTROL_ID)
                .name("页面类型和权限设置")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().build())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 1000))
                .rows(10)
                .placeholder("格式为：页面ID:提交方式:页面权限（用于修正应用模板的页面公开权限）")
                .build();

        Page configPage = Page.builder()
                .id(CONFIG_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("模板设置").build())
                .controls(List.of(statusControl,
                        planTypeControl,
                        featureControl,
                        categoriesControl,
                        scenariosControl,
                        demoQrControl,
                        tagControl,
                        displayOrderControl,
                        pageModifierControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(ONCE_PER_INSTANCE)
                        .pageName("模板设置")
                        .showAsterisk(true)
                        .build())
                .build();

        Menu menu = Menu.builder().links(List.of(
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("模板设置")
                                .type(PAGE)
                                .pageId(configPage.getId())
                                .build()))
                .showBasedOnPermission(false).build();

        Attribute statusAttribute = Attribute.builder()
                .id(STATUS_ATTRIBUTE_ID)
                .name("发布状态")
                .type(CONTROL_LAST)
                .pageId(configPage.getId())
                .controlId(statusControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute appliedCountAttribute = Attribute.builder()
                .id(APPLIED_COUNT_ATTRIBUTE_ID)
                .name("使用次数")
                .type(DIRECT_INPUT)
                .range(NO_LIMIT)
                .manualInput(false)
                .valueType(DOUBLE_VALUE)
                .precision(0)
                .pcListEligible(true)
                .build();

        Attribute planTypeAttribute = Attribute.builder()
                .id(TEMPLATE_PLAN_TYPE_ATTRIBUTE_ID)
                .name("套餐等级")
                .type(CONTROL_LAST)
                .pageId(configPage.getId())
                .controlId(planTypeControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute featureAttribute = Attribute.builder()
                .id(FEATURE_ATTRIBUTE_ID)
                .name("功能特性")
                .type(CONTROL_LAST)
                .pageId(configPage.getId())
                .controlId(featureControl.getId())
                .range(NO_LIMIT)
                .build();

        Attribute categoriesAttribute = Attribute.builder()
                .id(CATEGORIES_ATTRIBUTE_ID)
                .name("适用行业")
                .type(CONTROL_LAST)
                .pageId(configPage.getId())
                .controlId(categoriesControl.getId())
                .range(NO_LIMIT)
                .build();

        Attribute scenarioAttribute = Attribute.builder()
                .id(SCENARIO_ATTRIBUTE_ID)
                .name("适用场景")
                .type(CONTROL_LAST)
                .pageId(configPage.getId())
                .controlId(scenariosControl.getId())
                .range(NO_LIMIT)
                .build();

        Attribute demoQrAttribute = Attribute.builder()
                .id(DEMO_QR_ATTRIBUTE_ID)
                .name("演示实例")
                .type(CONTROL_LAST)
                .pageId(configPage.getId())
                .controlId(demoQrControl.getId())
                .range(NO_LIMIT)
                .build();

        Attribute tagAttribute = Attribute.builder()
                .id(newAttributeId())
                .name("标签")
                .type(CONTROL_LAST)
                .pageId(configPage.getId())
                .controlId(tagControl.getId())
                .range(NO_LIMIT)
                .build();

        Attribute displayOrderAttribute = Attribute.builder()
                .id(DISPLAY_ORDER_ATTRIBUTE_ID)
                .name("显示顺序")
                .type(CONTROL_LAST)
                .pageId(configPage.getId())
                .controlId(displayOrderControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute refAppAttribute = Attribute.builder()
                .id(REFED_APP_ATTRIBUTE_ID)
                .name("模板应用")
                .type(INSTANCE_CUSTOM_ID)
                .range(NO_LIMIT)
                .build();

        Attribute cardDescriptionAttribute = Attribute.builder()
                .id(CARD_DESCRIPTION_ATTRIBUTE_ID)
                .name("卡片简介")
                .type(CONTROL_LAST)
                .pageId(homePage.getId())
                .controlId(cardDescriptionControl.getId())
                .range(NO_LIMIT)
                .build();

        Attribute introductionAttribute = Attribute.builder()
                .id(INTRODUCTION_ATTRIBUTE_ID)
                .name("模板详情")
                .type(CONTROL_LAST)
                .pageId(homePage.getId())
                .controlId(introductionControl.getId())
                .range(NO_LIMIT)
                .build();

        Attribute pageModifierAttribute = Attribute.builder()
                .id(PAGE_MODIFIER_ATTRIBUTE_ID)
                .name("页面类型和权限")
                .type(CONTROL_LAST)
                .pageId(configPage.getId())
                .controlId(pageModifierControl.getId())
                .range(NO_LIMIT)
                .build();

        AppSetting setting = AppSetting.builder()
                .config(AppConfig.builder()
                        .operationPermission(CAN_MANAGE_GROUP)
                        .landingPageType(DEFAULT)
                        .qrWebhookTypes(List.of())
                        .instanceAlias("应用模板")
                        .customIdAlias("应用ID")
                        .homePageId(homePage.getId())
                        .build())
                .appTopBar(defaultAppTopBar())
                .pages(List.of(homePage, configPage))
                .menu(menu)
                .attributes(List.of(statusAttribute,
                        appliedCountAttribute,
                        planTypeAttribute,
                        featureAttribute,
                        categoriesAttribute,
                        scenarioAttribute,
                        demoQrAttribute,
                        refAppAttribute,
                        cardDescriptionAttribute,
                        introductionAttribute,
                        pageModifierAttribute,
                        tagAttribute,
                        displayOrderAttribute))
                .operationMenuItems(List.of())
                .plateSetting(PlateSetting.create())
                .circulationStatusSetting(CirculationStatusSetting.create())
                .build();

        CreateAppResult result = appFactory.create(MRY_APP_TEMPLATE_MANAGE_APP_ID,
                "应用模板管理",
                setting, MRY_APP_TEMPLATE_MANAGE_APP_GROUP_ID,
                APP_TEMPLATE_ROBOT_USER);

        appRepository.save(result.getApp());
        groupRepository.save(result.getDefaultGroup());
        groupHierarchyRepository.save(result.getGroupHierarchy());
        log.info("Created app template manage app.");
    }

    private FNumberInputControl.AutoCalculateSetting emptyAutoCalculateSetting() {
        return FNumberInputControl.AutoCalculateSetting.builder()
                .aliasContext(AutoCalculateAliasContext.builder().controlAliases(List.of()).build())
                .expression(null)
                .build();
    }
}
