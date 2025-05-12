package com.mryqr.management.crm;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppFactory;
import com.mryqr.core.app.domain.AppHeaderImageProvider;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.CreateAppResult;
import com.mryqr.core.app.domain.WebhookSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.config.AppConfig;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.AutoCalculateAliasContext;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FDateControl;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.app.domain.page.control.FIdentifierControl;
import com.mryqr.core.app.domain.page.control.FItemStatusControl;
import com.mryqr.core.app.domain.page.control.FMobileNumberControl;
import com.mryqr.core.app.domain.page.control.FMultiLineTextControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FPersonNameControl;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.control.FTimeControl;
import com.mryqr.core.app.domain.page.control.IdentifierFormatType;
import com.mryqr.core.app.domain.page.control.PSubmissionReferenceControl;
import com.mryqr.core.app.domain.page.control.PSubmitHistoryControl;
import com.mryqr.core.app.domain.page.menu.Menu;
import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.app.domain.ui.AppearanceStyle;
import com.mryqr.core.app.domain.ui.pagelink.PageLink;
import com.mryqr.core.app.domain.ui.shadow.Shadow;
import com.mryqr.core.common.domain.TextOption;
import com.mryqr.core.common.properties.CommonProperties;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.core.app.domain.AppTopBar.defaultAppTopBar;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CUSTOM_ID;
import static com.mryqr.core.app.domain.config.AppLandingPageType.DEFAULT;
import static com.mryqr.core.app.domain.page.control.AnswerUniqueType.NONE;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSetting;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSettingBuilder;
import static com.mryqr.core.app.domain.page.control.ControlNameSetting.defaultControlNameSetting;
import static com.mryqr.core.app.domain.page.control.ControlStyleSetting.defaultControlStyleSetting;
import static com.mryqr.core.app.domain.page.control.ControlType.CHECKBOX;
import static com.mryqr.core.app.domain.page.control.ControlType.DATE;
import static com.mryqr.core.app.domain.page.control.ControlType.IDENTIFIER;
import static com.mryqr.core.app.domain.page.control.ControlType.ITEM_STATUS;
import static com.mryqr.core.app.domain.page.control.ControlType.MOBILE;
import static com.mryqr.core.app.domain.page.control.ControlType.MULTI_LINE_TEXT;
import static com.mryqr.core.app.domain.page.control.ControlType.NUMBER_INPUT;
import static com.mryqr.core.app.domain.page.control.ControlType.PERSON_NAME;
import static com.mryqr.core.app.domain.page.control.ControlType.RADIO;
import static com.mryqr.core.app.domain.page.control.ControlType.SINGLE_LINE_TEXT;
import static com.mryqr.core.app.domain.page.control.ControlType.SUBMISSION_REFERENCE;
import static com.mryqr.core.app.domain.page.control.ControlType.SUBMIT_HISTORY;
import static com.mryqr.core.app.domain.page.control.ControlType.TIME;
import static com.mryqr.core.app.domain.page.control.FNumberInputControl.MAX_NUMBER;
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
import static com.mryqr.core.app.domain.ui.pagelink.PageLinkType.PAGE;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_ROBOT_USER;
import static com.mryqr.management.common.PlanTypeControl.createPlanTypeControl;

@Slf4j
@Component
@RequiredArgsConstructor
public class MryTenantManageApp {
    public static final String MRY_TENANT_MANAGE_APP_ID = "APP00000000000000001";
    public static final String MRY_TENANT_MANAGE_GROUP_ID = "GRP00000000000000001";

    public static final String MRY_TENANT_HOME_PAGE_ID = "p_BuYoerwXR1uAJTkllkNsog";
    public static final String TENANT_DETAIL_CONTROL_ID = "c_brYnfLxDy6NA851gTwL1mk";
    public static final String COMMUNICATION_HISTORY_CONTROL_ID = "c_oY77ipswtN7aKw3NHp1PXZ";

    public static final String TENANT_SYNC_PAGE_ID = "p_3nbM6aYj9y4FuaFTYrLADG";

    public static final String CURRENT_PACKAGE_CONTROL_ID = "c_4woyD1FqR4v2ZQzjHUvg2E";
    public static final String EXPIRE_DATE_CONTROL_ID = "c_xne8R6QgmSpe8wYsFQRm1p";

    public static final String PACKAGES_STATUS_CONTROL_ID = "c_p9ghfZHoSqeQiJN_R-MuUQ";
    public static final String PACKAGES_STATUS_NORMAL_OPTION_ID = "iGZz0vpuRG2AgaGcNcKsYQ";
    public static final String PACKAGES_STATUS_EXPIRING_OPTION_ID = "2HR__veHQh-gMQmmURCA6A";
    public static final String PACKAGES_STATUS_EXPIRED_OPTION_ID = "FXugHqsSSFyZV1-PBK1BFQ";

    public static final String REGISTER_DATE_CONTROL_ID = "c_xyVojsU9KzyvKvwCP8icaD";
    public static final String RECENT_ACTIVE_DATE_CONTROL_ID = "c_mufkwZZwFuoEtPc7eyNkhi";
    public static final String APP_USAGE_CONTROL_ID = "c_oJn4WWVzXb2Xk5N33SNbay";
    public static final String QR_USAGE_CONTROL_ID = "c_kscXVgWJQTGZnoQhRMjpmw";
    public static final String SUBMISSION_USAGE_CONTROL_ID = "c_g8Xj0r5yRWaLeIs987tJug";
    public static final String MEMBER_USAGE_CONTROL_ID = "c_kp7c1iTrkMMnQQfARkGczc";
    public static final String STORAGE_USAGE_CONTROL_ID = "c_1FuTxf9P281P1cyqPxyHfh";
    public static final String SMS_USAGE_CONTROL_ID = "c_oer4g09LS-uS9RGL8MR-zQ";

    public static final String ACTIVE_STATUS_CONTROL_ID = "c_aZACcWc3nfWJRzfWc7LWpV";
    public static final String ACTIVE_STATUS_YES_OPTION_ID = "688LJbCDZKFTUs1vhxTLst";
    public static final String ACTIVE_STATUS_NO_OPTION_ID = "7HHR3V2w8RCTQ6LWPvj6TQ";

    public static final String SUBDOMAIN_CONTROL_ID = "c_32rice89VFjLCAxR44JUKp";
    public static final String INVOICE_TITLE_CONTROL_ID = "c_ghdgATdxAXm8cpDaArcRoa";
    public static final String PACKAGE_DESCRIPTION_CONTROL_ID = "c_hron9GsLDfyd7Pyo7HThra";
    public static final String ADMINS_CONTROL_ID = "c_owD0lE2zQbuYRev-WDp25g";
    public static final String OPS_LOG_CONTROL_ID = "c_nTSc7n1bKBncWWGKnp7xJz";

    public static final String ADD_COMMUNICATION_PAGE_ID = "p_tHTz3zmK8C1qEfwSQ1ntBh";
    public static final String COMMUNICATION_CONTACT_CONTROL_ID = "c_jjfNSZPU2epiXjGgEb7oFa";
    public static final String COMMUNICATION_CONTACT_MOBILE_CONTROL_ID = "c_pUDBi5NuD26TpsB4ufY2d7";
    public static final String COMMUNICATION_DETAIL_CONTROL_ID = "c_1uzJZvT29eR3jquhPfHnd3";

    public static final String STATUS_SETTING_PAGE_ID = "p_wrK7rbHmC8WwaiYgJKtFXG";
    public static final String STATUS_SETTING_CONTROL_ID = "c_3arH2FftsKYYbD17Q87djG";
    public static final String STATUS_SETTING_ACTIVE_OPTION_ID = "hcXMVBKMAzXgwEHDvjbDrd";
    public static final String STATUS_SETTING_INACTIVE_OPTION_ID = "pfx2Q6mqr7ZBhCEvTLXDjy";
    public static final String STATUS_SETTING_NOTE_CONTROL_ID = "c_cChdBGE2F8C3DUNUjaNmKW";

    public static final String PACKAGE_SETTING_PAGE_ID = "p_hDj6oMo3noxGJ9WvujSFYJ";
    public static final String PACKAGE_SETTING_CONTROL_ID = "c_hG1qHuVrTgwBSn1vEXwGcf";
    public static final String PACKAGE_SETTING_EXPIRE_DATE_CONTROL_ID = "c_1hkQA8H85m4jb9B4myJzrB";
    public static final String PACKAGE_SETTING_NOTE_CONTROL_ID = "c_SaFM2HRFR3iGB8N0tDpPyA";

    public static final String LIMIT_SETTING_PAGE_ID = "p_qficmmMxEtojgY63YfQEDi";
    public static final String LIMIT_SETTING_APP_COUNT_CONTROL_ID = "c_sxvakgU6FDwtXaARvRCd9h";
    public static final String LIMIT_SETTING_QR_COUNT_CONTROL_ID = "c_aUNDaVSR4QhJbsxCS6WVkc";
    public static final String LIMIT_SETTING_SUBMISSION_COUNT_CONTROL_ID = "c_3tFHzzxcELfaEZRFQy5faV";
    public static final String LIMIT_SETTING_MEMBER_COUNT_CONTROL_ID = "c_4sk1gihGWCeTBDCtXjWS9y";
    public static final String LIMIT_SETTING_STORAGE_COUNT_CONTROL_ID = "c_nuvyvVMt2tZeD2MvXkL6sT";
    public static final String LIMIT_SETTING_SMS_COUNT_CONTROL_ID = "c_qkLkpzvDFUbQMwudDvMzy2";
    public static final String LIMIT_SETTING_GROUP_PER_APP_COUNT_CONTROL_ID = "c_juLPVybyAh6FLdHDrg5Ga6";
    public static final String LIMIT_SETTING_DEPARTMENT_COUNT_CONTROL_ID = "c_pJP21zyknit8EMwCWb7cve";
    public static final String LIMIT_SETTING_DEVELOPER_CONTROL_ID = "c_oGbUQXb2oMptUvYsfTQe8f";
    public static final String LIMIT_SETTING_DEVELOPER_ACTIVATED_ID = "gwYVvtXcaR1qe2av6D7sdL";
    public static final String LIMIT_SETTING_DEVELOPER_DEACTIVATED_ID = "1YKmm2h7yPxLDoMTSsCgdh";

    public static final String TRIGGER_SYNC_PAGE_ID = "p_qugvuvgCyYbLboY94eJhey";
    public static final String TRIGGER_SYNC_NOTE_CONTROL_ID = "c_hu6G8Lk3Ei1M8FmbQHF8Xy";

    public static final String CLEAR_SUBDOMAIN_PAGE_ID = "p_bozcRiFr3xyJvpWwvPyJy4";
    public static final String CLEAR_SUBDOMAIN_NOTE_CONTROL_ID = "c_bWpJJhpC2idx2zoBsicsCQ";

    public static final String UPDATE_SUBDOMAIN_READY_PAGE_ID = "p_kXia5HMr9Zmh9BTE2K1f4H";
    public static final String UPDATE_SUBDOMAIN_READY_CONTROL_ID = "c_1VAJvXF4jzZ8YACij9BXfF";
    public static final String SUBDOMAIN_READY_OPTION_ID = "mheN1dcGBpYmKMqexgB38v";
    public static final String SUBDOMAIN_NOT_READY_OPTION_ID = "xmcA247vTyuxiuec7UgYdP";

    public static final String CLEAR_CACHE_PAGE_ID = "p_y_LVE2_vR0OhRHXVUPZFmQ";
    public static final String CLEAR_CACHE_CONTROL_ID = "c_682lx0TPSNO6VYscrQM8LQ";
    public static final String CLEAR_APP_CACHE_OPTION_ID = "pA9OZD55SZOyeCFA_DncOQ";
    public static final String CLEAR_GROUP_CACHE_OPTION_ID = "Bu8gCG3iS6ez23wCkEnHBA";
    public static final String CLEAR_MEMBER_CACHE_OPTION_ID = "UOaMB0eqQsmTeRyzjshhqg";
    public static final String CLEAR_DEPARTMENT_CACHE_OPTION_ID = "lvTGQa3ZRHKw-D5Dss81dA";
    public static final String CLEAR_TENANT_CACHE_OPTION_ID = "y-j6653FTOmCpXHQZyZy7Q";

    public static final String SEND_EVENT_PAGE_ID = "p_dfzkeOpOSSeD1RvVG16G6g";
    public static final String SEND_EVENT_START_DATE_CONTROL_ID = "c_tSmh-DlsTVqZI9Gpc9jxTQ";
    public static final String SEND_EVENT_START_TIME_CONTROL_ID = "c_ik3THWIVRUmVya_7MPpVCQ";
    public static final String SEND_EVENT_END_DATE_CONTROL_ID = "c_lbEUXKpJQ3GpiuKw9v4LVA";
    public static final String SEND_EVENT_END_TIME_CONTROL_ID = "c_tPFv4fJuT6uw2zN6kJ3D1A";
    public static final String SEND_EVENT_ALL_TENANT_CONTROL_ID = "c_eOT3WZzmS1ShFiDJC7Nb5w";
    public static final String SEND_EVENT_ALL_TENANT_YES_OPTION_ID = "43HBJE9PQDyEVcijNxqdtw";
    public static final String SEND_EVENT_ALL_TENANT_NO_OPTION_ID = "xxNPYbaRTIatJO9gp-qmig";
    public static final String SEND_EVENT_APP_CONTROL_ID = "c_zYiWeo_jQhusdcRo5MTKXw";

    public static final String TENANT_CREATE_TIME_ATTR_ID = "a_imD9er3lSjG_vsTG1kijyw";
    public static final String RECENT_ACTIVE_DATE_ATTR_ID = "a_vh9xXK53XrxruGxQ89ZgR1";
    public static final String CURRENT_PACKAGE_ATTR_ID = "a_cYHZAsQPftgesEDRLw4kCH";
    public static final String PACKAGE_EXPIRE_ATTR_ID = "a_1bWo9KTJ44JjyxRuyfHDcY";
    public static final String PACKAGE_STATUS_ATTR_ID = "a_5ajvsHrTTGWBECJjnh-ILg";
    public static final String APP_USAGE_ATTR_ID = "a_dsuza4f45urEq2j7cYnmcJ";
    public static final String QR_USAGE_ATTR_ID = "a_oy6jdS_vSqKsW77k0iKSag";
    public static final String SUBMISSION_USAGE_ATTR_ID = "a_boHqm-XeS8KoOSc3jdmQWA";
    public static final String MEMBER_USAGE_ATTR_ID = "a_ceTLi9FU55CTzKUWxHGNdp";
    public static final String STORAGE_USAGE_ATTR_ID = "a_cdfqisbJpz1XU2dsSxDdF3";
    public static final String SMS_USAGE_ATTR_ID = "a_j67O39xVRrei0CzA7R1y-g";
    public static final String TENANT_ID_ATTR_ID = "a_rcxVDc8fYGJh1av4f6fSJe";

    private final AppRepository appRepository;
    private final AppFactory appFactory;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final AppHeaderImageProvider appHeaderImageProvider;
    private final CommonProperties commonProperties;
    private final ServerProperties serverProperties;

    @Transactional
    public void init() {
        if (appRepository.exists(MRY_TENANT_MANAGE_APP_ID)) {
            return;
        }

        FDropdownControl currentPlanControl = createPlanTypeControl("当前套餐", CURRENT_PACKAGE_CONTROL_ID, false);

        FDateControl expireDateControl = FDateControl.builder()
                .type(DATE)
                .id(EXPIRE_DATE_CONTROL_ID)
                .name("过期时间")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .build();

        FItemStatusControl packagesStatusControl = FItemStatusControl.builder()
                .type(ITEM_STATUS)
                .id(PACKAGES_STATUS_CONTROL_ID)
                .name("套餐状态")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(PACKAGES_STATUS_NORMAL_OPTION_ID).name("正常").color("rgba(0, 195, 0, 1)").build(),
                        TextOption.builder().id(PACKAGES_STATUS_EXPIRING_OPTION_ID).name("即将过期").color("rgba(254, 165, 0, 1)").build(),
                        TextOption.builder().id(PACKAGES_STATUS_EXPIRED_OPTION_ID).name("已过期").color("rgba(195, 0, 0, 1)").build()
                ))
                .build();

        FDateControl registerDateControl = FDateControl.builder()
                .type(DATE)
                .id(REGISTER_DATE_CONTROL_ID)
                .name("注册时间")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .build();

        FDateControl recentActiveDateControl = FDateControl.builder()
                .type(DATE)
                .id(RECENT_ACTIVE_DATE_CONTROL_ID)
                .name("最近活跃时间")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .build();

        FNumberInputControl appUsageControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(APP_USAGE_CONTROL_ID)
                .name("应用用量")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(0, 1000000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .build();

        FNumberInputControl qrUsageControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(QR_USAGE_CONTROL_ID)
                .name("实例用量")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(0, MAX_NUMBER))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .build();

        FNumberInputControl submissionUsageControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(SUBMISSION_USAGE_CONTROL_ID)
                .name("提交用量")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(0, MAX_NUMBER))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .build();

        FNumberInputControl memberUsageControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(MEMBER_USAGE_CONTROL_ID)
                .name("成员用量")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(0, 1000000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .build();

        FNumberInputControl storageUsageControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(STORAGE_USAGE_CONTROL_ID)
                .name("存储用量(GB)")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(2)
                .minMaxSetting(minMaxOf(0, 1000000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .build();

        FNumberInputControl smsUsageControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(SMS_USAGE_CONTROL_ID)
                .name("本月短信使用量")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(0, 1000000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .build();

        FItemStatusControl activeStatusControl = FItemStatusControl.builder()
                .type(ITEM_STATUS)
                .id(ACTIVE_STATUS_CONTROL_ID)
                .name("启用状态")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(ACTIVE_STATUS_YES_OPTION_ID).name("启用").color("rgba(0, 195, 0, 1)").build(),
                        TextOption.builder().id(ACTIVE_STATUS_NO_OPTION_ID).name("禁用").color("rgba(195, 0, 0, 1)").build()
                ))
                .build();

        FIdentifierControl subdomainControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(SUBDOMAIN_CONTROL_ID)
                .name("子域名")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FMultiLineTextControl invoiceTitleControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(INVOICE_TITLE_CONTROL_ID)
                .name("发票抬头")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10000))
                .rows(3)
                .build();

        FMultiLineTextControl packageDescriptionControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(PACKAGE_DESCRIPTION_CONTROL_ID)
                .name("套餐详情")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10000))
                .rows(3)
                .build();

        FMultiLineTextControl opsLogControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(OPS_LOG_CONTROL_ID)
                .name("操作记录")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10000))
                .rows(3)
                .build();

        FMultiLineTextControl adminsControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(ADMINS_CONTROL_ID)
                .name("管理员")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10000))
                .rows(3)
                .build();

        Page syncPage = Page.builder()
                .id(TENANT_SYNC_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("后台数据同步").build())
                .controls(List.of(currentPlanControl, expireDateControl, packagesStatusControl, registerDateControl, appUsageControl,
                        qrUsageControl, submissionUsageControl, memberUsageControl, storageUsageControl, smsUsageControl, activeStatusControl,
                        subdomainControl, invoiceTitleControl, packageDescriptionControl, adminsControl, opsLogControl, recentActiveDateControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(ONCE_PER_INSTANCE)
                        .pageName("后台数据同步")
                        .permission(CAN_MANAGE_APP)
                        .build())
                .build();

        FPersonNameControl communicationContactControl = FPersonNameControl.builder()
                .type(PERSON_NAME)
                .id(COMMUNICATION_CONTACT_CONTROL_ID)
                .name("租户对接人")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().autoFill(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        FMobileNumberControl communicationContactMobileControl = FMobileNumberControl.builder()
                .type(MOBILE)
                .id(COMMUNICATION_CONTACT_MOBILE_CONTROL_ID)
                .name("对接人手机")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().autoFill(true).build())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .build();

        FMultiLineTextControl communicationDetailControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(COMMUNICATION_DETAIL_CONTROL_ID)
                .name("记录详情")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10000))
                .rows(5)
                .build();

        Page communicationPage = Page.builder()
                .id(ADD_COMMUNICATION_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("添加沟通记录").build())
                .controls(List.of(communicationContactControl, communicationContactMobileControl, communicationDetailControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("沟通记录")
                        .permission(CAN_MANAGE_APP)
                        .build())
                .build();

        FRadioControl statusSettingControl = FRadioControl.builder()
                .type(RADIO)
                .id(STATUS_SETTING_CONTROL_ID)
                .name("设置状态")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(STATUS_SETTING_ACTIVE_OPTION_ID).name("启用").build(),
                        TextOption.builder().id(STATUS_SETTING_INACTIVE_OPTION_ID).name("禁用").build()
                ))
                .build();

        FSingleLineTextControl statusSettingNoteControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(STATUS_SETTING_NOTE_CONTROL_ID)
                .name("备注")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        Page statusSettingPage = Page.builder()
                .id(STATUS_SETTING_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("启禁用设置").build())
                .controls(List.of(statusSettingControl, statusSettingNoteControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("启禁用设置")
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE))
                        .build())
                .build();

        FDropdownControl packageSettingControl = createPlanTypeControl("套餐", PACKAGE_SETTING_CONTROL_ID, true);

        FDateControl packageSettingExpireDateControl = FDateControl.builder()
                .type(DATE)
                .id(PACKAGE_SETTING_EXPIRE_DATE_CONTROL_ID)
                .name("过期时间")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        FSingleLineTextControl packageSettingNoteControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(PACKAGE_SETTING_NOTE_CONTROL_ID)
                .name("备注")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        Page packageSettingPage = Page.builder()
                .id(PACKAGE_SETTING_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("设置套餐")
                        .description("设置套餐和设置额度均会影响租户的功能额度，以最后设置的那个为准。")
                        .build())
                .controls(List.of(packageSettingControl, packageSettingExpireDateControl, packageSettingNoteControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("设置套餐")
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE))
                        .build())
                .build();

        FNumberInputControl appCountControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(LIMIT_SETTING_APP_COUNT_CONTROL_ID)
                .name("应用额度")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(1, 1000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .suffix("个")
                .build();

        FNumberInputControl qrCountControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(LIMIT_SETTING_QR_COUNT_CONTROL_ID)
                .name("实例额度")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(1, 10000000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .suffix("个")
                .build();

        FNumberInputControl submissionCountControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(LIMIT_SETTING_SUBMISSION_COUNT_CONTROL_ID)
                .name("提交量额度")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(1, 10000000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .suffix("份")
                .build();

        FNumberInputControl memberCountControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(LIMIT_SETTING_MEMBER_COUNT_CONTROL_ID)
                .name("成员额度")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(1, 10000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .suffix("名")
                .build();

        FNumberInputControl storageCountControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(LIMIT_SETTING_STORAGE_COUNT_CONTROL_ID)
                .name("存储空间额度")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(1, 1000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .suffix("G")
                .build();

        FNumberInputControl smsCountControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(LIMIT_SETTING_SMS_COUNT_CONTROL_ID)
                .name("短信额度")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(1, 1000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .suffix("条/月")
                .build();

        FNumberInputControl groupCountControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(LIMIT_SETTING_GROUP_PER_APP_COUNT_CONTROL_ID)
                .name("分组额度")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(1, 1000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .suffix("个/应用")
                .build();

        FNumberInputControl departmentCountControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(LIMIT_SETTING_DEPARTMENT_COUNT_CONTROL_ID)
                .name("部门额度")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(0)
                .minMaxSetting(minMaxOf(1, 1000))
                .autoCalculateSetting(emptyAutoCalculateSetting())
                .suffix("个")
                .build();

        FRadioControl developerEnabledControl = FRadioControl.builder()
                .type(RADIO)
                .id(LIMIT_SETTING_DEVELOPER_CONTROL_ID)
                .name("开发者功能")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(LIMIT_SETTING_DEVELOPER_ACTIVATED_ID).name("开启").build(),
                        TextOption.builder().id(LIMIT_SETTING_DEVELOPER_DEACTIVATED_ID).name("关闭").build()
                ))
                .build();

        Page limitSettingPage = Page.builder()
                .id(LIMIT_SETTING_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("设置额度")
                        .description("设置额度和设置套餐均会影响租户的功能额度，以最后设置的那个为准。")
                        .build())
                .controls(List.of(appCountControl, qrCountControl, submissionCountControl,
                        memberCountControl, storageCountControl, smsCountControl,
                        groupCountControl, departmentCountControl, developerEnabledControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("设置额度")
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE))
                        .build())
                .build();

        FSingleLineTextControl triggerSyncNoteControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(TRIGGER_SYNC_NOTE_CONTROL_ID)
                .name("备注")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        Page triggerSyncPage = Page.builder()
                .id(TRIGGER_SYNC_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("同步后台数据").build())
                .controls(List.of(triggerSyncNoteControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("同步后台数据")
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE))
                        .build())
                .build();

        FSingleLineTextControl clearSubdomainNoteControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(CLEAR_SUBDOMAIN_NOTE_CONTROL_ID)
                .name("备注")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        Page clearSubdomainPage = Page.builder()
                .id(CLEAR_SUBDOMAIN_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("清空子域名").build())
                .controls(List.of(clearSubdomainNoteControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("清空子域名")
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE))
                        .build())
                .build();

        FRadioControl subdomainReadyControl = FRadioControl.builder()
                .type(RADIO)
                .id(UPDATE_SUBDOMAIN_READY_CONTROL_ID)
                .name("设置就绪状态")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(SUBDOMAIN_READY_OPTION_ID).name("就绪").build(),
                        TextOption.builder().id(SUBDOMAIN_NOT_READY_OPTION_ID).name("未就绪").build()
                ))
                .build();

        Page updateSubdomainReadyPage = Page.builder()
                .id(UPDATE_SUBDOMAIN_READY_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("设置子域名就绪状态").build())
                .controls(List.of(subdomainReadyControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("设置子域名就绪状态")
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE))
                        .build())
                .build();

        FCheckboxControl clearCacheControl = FCheckboxControl.builder()
                .type(CHECKBOX)
                .id(CLEAR_CACHE_CONTROL_ID)
                .name("清空缓存范围")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(CLEAR_APP_CACHE_OPTION_ID).name("应用（包含单个应用和应用列表）").build(),
                        TextOption.builder().id(CLEAR_GROUP_CACHE_OPTION_ID).name("分组数据（包含单个分组、分组列表和分组层级）").build(),
                        TextOption.builder().id(CLEAR_MEMBER_CACHE_OPTION_ID).name("成员数据（包含单个成员和成员列表）").build(),
                        TextOption.builder().id(CLEAR_DEPARTMENT_CACHE_OPTION_ID).name("部门数据（包含部门列表和部门层级）").build(),
                        TextOption.builder().id(CLEAR_TENANT_CACHE_OPTION_ID).name("租户（包含当个租户和API Key）").build()
                ))
                .minMaxSetting(minMaxOf(0, 10))
                .build();

        Page clearCachePage = Page.builder()
                .id(CLEAR_CACHE_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("清空缓存").build())
                .controls(List.of(clearCacheControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("清空缓存")
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE))
                        .build())
                .build();

        FDateControl sendEventStartDateControl = FDateControl.builder()
                .type(DATE)
                .id(SEND_EVENT_START_DATE_CONTROL_ID)
                .name("开始日期")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        FTimeControl sendEventStartTimeControl = FTimeControl.builder()
                .type(TIME)
                .id(SEND_EVENT_START_TIME_CONTROL_ID)
                .name("开始时间")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        FDateControl sendEventEndDateControl = FDateControl.builder()
                .type(DATE)
                .id(SEND_EVENT_END_DATE_CONTROL_ID)
                .name("结束日期")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        FTimeControl sendEventEndTimeControl = FTimeControl.builder()
                .type(TIME)
                .id(SEND_EVENT_END_TIME_CONTROL_ID)
                .name("结束时间")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        FRadioControl sendEventAllTenantControl = FRadioControl.builder()
                .type(RADIO)
                .id(SEND_EVENT_ALL_TENANT_CONTROL_ID)
                .name("所有租户")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().build())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(SEND_EVENT_ALL_TENANT_YES_OPTION_ID).name("是").build(),
                        TextOption.builder().id(SEND_EVENT_ALL_TENANT_NO_OPTION_ID).name("否（不填时默认即为否）").build()
                ))
                .build();

        FSingleLineTextControl sendEventAppControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(SEND_EVENT_APP_CONTROL_ID)
                .name("应用ID")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().build())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 50))
                .build();

        Page sendEventPage = Page.builder()
                .id(SEND_EVENT_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("发送事件").build())
                .controls(List.of(sendEventStartDateControl,
                        sendEventStartTimeControl,
                        sendEventEndDateControl,
                        sendEventEndTimeControl,
                        sendEventAllTenantControl,
                        sendEventAppControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(NEW)
                        .pageName("发送事件")
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE))
                        .build())
                .build();

        PSubmissionReferenceControl tenantDetailControl = PSubmissionReferenceControl.builder()
                .type(SUBMISSION_REFERENCE)
                .id(TENANT_DETAIL_CONTROL_ID)
                .name("租户详情")
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

        PSubmitHistoryControl communicationHistoryControl = PSubmitHistoryControl.builder()
                .type(SUBMIT_HISTORY)
                .id(COMMUNICATION_HISTORY_CONTROL_ID)
                .name("历史沟通记录")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(null)
                .permission(CAN_MANAGE_APP)
                .pageIds(newArrayList(communicationPage.getId()))
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
                .id(MRY_TENANT_HOME_PAGE_ID)
                .header(defaultPageHeaderBuilder()
                        .image(appHeaderImageProvider.defaultAppHeaderImage())
                        .imageCropType(FOUR_TO_THREE)
                        .build())
                .title(defaultPageTitleBuilder().text("租户详情").build())
                .controls(List.of(tenantDetailControl, communicationHistoryControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("租户详情")
                        .permission(CAN_MANAGE_APP)
                        .build())
                .build();

        Attribute currentPackageAttribute = Attribute.builder()
                .id(CURRENT_PACKAGE_ATTR_ID)
                .name("当前套餐")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(currentPlanControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .kanbanEligible(true)
                .build();

        Attribute packageExpireAttribute = Attribute.builder()
                .id(PACKAGE_EXPIRE_ATTR_ID)
                .name("过期时间")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(expireDateControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute packageStatusAttribute = Attribute.builder()
                .id(PACKAGE_STATUS_ATTR_ID)
                .name("套餐状态")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(packagesStatusControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .kanbanEligible(true)
                .build();

        Attribute tenantRegisterTimeAttribute = Attribute.builder()
                .id(TENANT_CREATE_TIME_ATTR_ID)
                .name("注册时间")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(registerDateControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute recentActiveDateAttribute = Attribute.builder()
                .id(RECENT_ACTIVE_DATE_ATTR_ID)
                .name("最近活跃时间")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(recentActiveDateControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute appUsageAttribute = Attribute.builder()
                .id(APP_USAGE_ATTR_ID)
                .name("应用用量")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(appUsageControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute qrUsageAttribute = Attribute.builder()
                .id(QR_USAGE_ATTR_ID)
                .name("实例用量")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(qrUsageControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute submissionUsageAttribute = Attribute.builder()
                .id(SUBMISSION_USAGE_ATTR_ID)
                .name("提交用量")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(submissionUsageControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute memberUsageAttribute = Attribute.builder()
                .id(MEMBER_USAGE_ATTR_ID)
                .name("成员用量")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(memberUsageControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute storageUsageAttribute = Attribute.builder()
                .id(STORAGE_USAGE_ATTR_ID)
                .name("存储用量(GB)")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(storageUsageControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute smsUsageAttribute = Attribute.builder()
                .id(SMS_USAGE_ATTR_ID)
                .name("本月短信发送量")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(smsUsageControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute tenantIdAttribute = Attribute.builder()
                .id(TENANT_ID_ATTR_ID)
                .name("租户编号")
                .type(INSTANCE_CUSTOM_ID)
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Menu menu = Menu.builder().links(List.of(
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("添加沟通记录")
                                .type(PAGE)
                                .pageId(communicationPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("设置套餐")
                                .type(PAGE)
                                .pageId(packageSettingPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("启禁用设置")
                                .type(PAGE)
                                .pageId(statusSettingPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("设置额度")
                                .type(PAGE)
                                .pageId(limitSettingPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("同步后台数据")
                                .type(PAGE)
                                .pageId(triggerSyncPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("清空子域名")
                                .type(PAGE)
                                .pageId(clearSubdomainPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("子域名就绪")
                                .type(PAGE)
                                .pageId(updateSubdomainReadyPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("清空缓存")
                                .type(PAGE)
                                .pageId(clearCachePage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("发送事件")
                                .type(PAGE)
                                .pageId(sendEventPage.getId())
                                .build()
                ))
                .showBasedOnPermission(true).build();

        AppSetting setting = AppSetting.builder()
                .config(AppConfig.builder()
                        .operationPermission(CAN_MANAGE_APP)
                        .landingPageType(DEFAULT)
                        .qrWebhookTypes(List.of())
                        .instanceAlias("租户")
                        .customIdAlias("租户编号")
                        .homePageId(homePage.getId())
                        .allowDuplicateInstanceName(true)
                        .appManualEnabled(true)
                        .build())
                .appTopBar(defaultAppTopBar())
                .pages(List.of(homePage, syncPage, communicationPage, statusSettingPage, packageSettingPage, limitSettingPage,
                        triggerSyncPage, clearSubdomainPage, updateSubdomainReadyPage, clearCachePage, sendEventPage))
                .menu(menu)
                .attributes(List.of(currentPackageAttribute, packageExpireAttribute, packageStatusAttribute, tenantRegisterTimeAttribute,
                        recentActiveDateAttribute, appUsageAttribute, qrUsageAttribute, submissionUsageAttribute, memberUsageAttribute,
                        storageUsageAttribute, smsUsageAttribute, tenantIdAttribute))
                .operationMenuItems(List.of())
                .plateSetting(PlateSetting.create())
                .circulationStatusSetting(CirculationStatusSetting.create())
                .build();

        CreateAppResult result = appFactory.create(MRY_TENANT_MANAGE_APP_ID,
                "租户管理",
                setting, MRY_TENANT_MANAGE_GROUP_ID,
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
        log.info("Created tenant manage app.");
    }

    private FNumberInputControl.AutoCalculateSetting emptyAutoCalculateSetting() {
        return FNumberInputControl.AutoCalculateSetting.builder()
                .aliasContext(AutoCalculateAliasContext.builder().controlAliases(List.of()).build())
                .expression(null)
                .build();
    }
}
