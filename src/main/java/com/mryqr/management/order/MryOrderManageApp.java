package com.mryqr.management.order;

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
import com.mryqr.core.app.domain.ui.pagelink.PageLink;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mryqr.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.AppTopBar.defaultAppTopBar;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CREATE_TIME;
import static com.mryqr.core.app.domain.config.AppLandingPageType.DEFAULT;
import static com.mryqr.core.app.domain.page.control.AnswerUniqueType.NONE;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSetting;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSettingBuilder;
import static com.mryqr.core.app.domain.page.control.ControlNameSetting.defaultControlNameSetting;
import static com.mryqr.core.app.domain.page.control.ControlStyleSetting.defaultControlStyleSetting;
import static com.mryqr.core.app.domain.page.control.ControlType.*;
import static com.mryqr.core.app.domain.page.control.FileCategory.DOCUMENT;
import static com.mryqr.core.app.domain.page.control.FileCategory.IMAGE;
import static com.mryqr.core.app.domain.page.control.PSubmissionReferenceControl.StyleType.HORIZONTAL_TABLE;
import static com.mryqr.core.app.domain.page.header.PageHeader.defaultPageHeaderBuilder;
import static com.mryqr.core.app.domain.page.header.PageHeaderType.INHERITED;
import static com.mryqr.core.app.domain.page.setting.PageSetting.defaultPageSettingBuilder;
import static com.mryqr.core.app.domain.page.setting.SubmissionWebhookType.ON_CREATE;
import static com.mryqr.core.app.domain.page.setting.SubmissionWebhookType.ON_UPDATE;
import static com.mryqr.core.app.domain.page.setting.SubmitType.NEW;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.submitbutton.SubmitButton.defaultSubmitButton;
import static com.mryqr.core.app.domain.page.title.PageTitle.defaultPageTitleBuilder;
import static com.mryqr.core.app.domain.ui.AppearanceStyle.defaultAppearanceStyle;
import static com.mryqr.core.app.domain.ui.BoxedTextStyle.defaultBoxedTextStyle;
import static com.mryqr.core.app.domain.ui.BoxedTextStyle.defaultControlDescriptionStyle;
import static com.mryqr.core.app.domain.ui.ButtonStyle.defaultButtonStyle;
import static com.mryqr.core.app.domain.ui.FontStyle.defaultFontStyle;
import static com.mryqr.core.app.domain.ui.ImageCropType.FOUR_TO_THREE;
import static com.mryqr.core.app.domain.ui.MinMaxSetting.minMaxOf;
import static com.mryqr.core.app.domain.ui.pagelink.PageLinkType.PAGE;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_ROBOT_USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class MryOrderManageApp {
    public static final String ORDER_APP_ID = "APP00000000000000002";
    public static final String ORDER_GROUP_ID = "GRP00000000000000002";
    public static final String ORDER_HOME_PAGE_ID = "p_qe-9WuI6RaG3l808aE1rXA";

    public static final String ORDER_DETAIL_REF_CONTROL_ID = "c_npMv78xZ9QGQPdmAHzugrD";

    public static final String ORDER_SYNC_PAGE_ID = "p_hJDGhYTynEbYg3f28rsBfH";

    public static final String ORDER_ID_CONTROL_ID = "c_rg9w2_DGRhGsD0WaKLKwew";

    public static final String ORDER_TYPE_CONTROL_ID = "c_idzHet2zCKYw9Mfiap68wk";
    public static final String ORDER_TYPE_PACKAGE_OPTION_ID = "nqZNmX2PnAyMfHoaAiEPmT";
    public static final String ORDER_TYPE_EXTRA_MEMBER_OPTION_ID = "uDPx6SvZTSJDrLjd8dkJZr";
    public static final String ORDER_TYPE_EXTRA_SMS_OPTION_ID = "yiIoHPx2ShiLBZyQvLEONw";
    public static final String ORDER_TYPE_EXTRA_STORAGE_OPTION_ID = "vxbuNJwuujusHaD9qub2Ym";
    public static final String ORDER_TYPE_EXTRA_VIDEO_OPTION_ID = "H0JSlkc3ROedHAP0BOBZUQ";
    public static final String ORDER_TYPE_PLATE_OPTION_ID = "aqxeX1vLVBhTUWHu2iQKdp";

    public static final String ORDER_STATUS_CONTROL_ID = "c_tB3oJEVvfZdV24Si9HVhaK";
    public static final String ORDER_STATUS_CREATED_OPTION_ID = "6gm7GmxgUaZUNG6fGDgq6f";
    public static final String ORDER_STATUS_PAID_OPTION_ID = "sDAcZobFReX2w8KF5UCbDk";
    public static final String ORDER_STATUS_REFUND_OPTION_ID = "vSLFJgmVkdqFTehyvggSS1";

    public static final String ORDER_DESCRIPTION_CONTROL_ID = "c_omC3bTuqpXYqxQQ37wvj64";
    public static final String ORDER_PRICE_CONTROL_ID = "c_aQeTYM8vA7NS3UnRi2j27S";
    public static final String ORDER_SUBMITTER_CONTROL_ID = "c_uP2hYreUHeeEgi8u7rigma";
    public static final String ORDER_SUBMITTER_MOBILE_CONTROL_ID = "c_a3Saz9xUWZ4w1sLXihphHa";
    public static final String ORDER_SUBMITTER_EMAIL_CONTROL_ID = "c_3wCEZYNWLLqnu4yMwnJnKk";

    public static final String ORDER_CHANNEL_CONTROL_ID = "c_fUbpXTDdF1CZ9jpjJ88UV6";
    public static final String ORDER_CHANNEL_WX_PAY_OPTION_ID = "pSMLTv62dohQcM9HEkdK9v";
    public static final String ORDER_CHANNEL_WX_TRANSFER_OPTION_ID = "_cRE5-DUReSosUfCAh75WA";
    public static final String ORDER_CHANNEL_BANK_TRANSFER_OPTION_ID = "69TUExvHSuY4Nwk2gAZ8BK";

    public static final String ORDER_WX_TXN_CONTROL_ID = "c_21sgRAj1u5er4hN9dvmH67";
    public static final String ORDER_BANK_ACCOUNT_CONTROL_ID = "c_9kQ9P6LFt4w1z69psk6Dqf";

    public static final String ORDER_DELIVERY_STATUS_CONTROL_ID = "c_b5JndGV6bPuAvGmDhCSxEQ";
    public static final String ORDER_DELIVERY_NONE_OPTION_ID = "cd3oiNYwCxpjvSjVSg2uT4";
    public static final String ORDER_DELIVERY_NOT_YET_OPTION_ID = "x5J1pespxfWBs38WdUZ3Ct";
    public static final String ORDER_DELIVERY_ALREADY_OPTION_ID = "wepZvEdTJABd8KbCzSmeu8";

    public static final String ORDER_DELIVER_CONTROL_ID = "c_q7egonceseesZ9d6faMCmu";
    public static final String ORDER_DELIVER_EMS_OPTION_ID = "4XB85MKPwbUh8ZGS1Cjv2B";
    public static final String ORDER_DELIVER_SHUNFENG_OPTION_ID = "2A4Rt526BkXDXTMr32xsyb";
    public static final String ORDER_DELIVER_YUANTONG_OPTION_ID = "1fFexdpyEuWNHBT9iQM8gz";
    public static final String ORDER_DELIVER_ZHONGTONG_OPTION_ID = "oD4dy4fG9kXfW7ecBxEoAT";
    public static final String ORDER_DELIVER_ZHONGTONG_EXPRESS_OPTION_ID = "bxZYNmL59Re85GUXrUi8kY";
    public static final String ORDER_DELIVER_SHENTONG_OPTION_ID = "oEGzEvwujXzeNrhqWFunWg";
    public static final String ORDER_DELIVER_YUNDA_OPTION_ID = "9RFhEW7Zz5boDK3KTs15Vz";
    public static final String ORDER_DELIVER_YUNDA_EXPRESS_OPTION_ID = "2RjAKAPT6zVcD3nHJUQgsQ";

    public static final String ORDER_DELIVERY_ID_CONTROL_ID = "c_tSHCGdgNfefcJGU1N9EEWK";

    public static final String ORDER_INVOICE_STATUS_CONTROL_ID = "c_sLRL5hJhNDeyCjR5NAy2sp";
    public static final String ORDER_INVOICE_STATUS_NONE_OPTION_ID = "4MAy8U82JZozUuiBbASrk6";
    public static final String ORDER_INVOICE_STATUS_WAITING_OPTION_ID = "k4YB9ugc5onJx38ACBKn51";
    public static final String ORDER_INVOICE_STATUS_DONE_OPTION_ID = "hZERqMVf9wRkmAcYpQwNPQ";

    public static final String ORDER_INVOICE_INFO_CONTROL_ID = "c_9gvJRxGVmoKaj7U8bEGNmy";
    public static final String ORDER_INVOICE_FILE_CONTROL_ID = "c_rkTwUTCtqMzX6HRiQ96sey";
    public static final String ORDER_SCREENSHOTS_CONTROL_ID = "c_AhDVQUx7QsSi0Dn8uqtsiA";
    public static final String ORDER_TENANT_CONTROL_ID = "c_eveCPdFWKCs3GEtEPtnWdV";

    public static final String ORDER_ID_ATTRIBUTE_ID = "a_gzv6-0muRI28jsOXgAmfsw";
    public static final String ORDER_TYPE_ATTRIBUTE_ID = "a_iuZY2U8HPsqq7h76aGq94F";
    public static final String ORDER_STATUS_ATTRIBUTE_ID = "a_gPwKVVZDK7bB7Cz7Jo1QhG";
    public static final String ORDER_PRICE_ATTRIBUTE_ID = "a_vPMgr7VAd9B3k39qgphADL";
    public static final String ORDER_DESCRIPTION_ATTRIBUTE_ID = "a_wzK1__PxS0eVhIZfECrunw";
    public static final String ORDER_DELIVERY_STATUS_ATTRIBUTE_ID = "a_khptwhvhvyKXR2r5RMe7xQ";
    public static final String ORDER_INVOICE_STATUS_ATTRIBUTE_ID = "a_ow9zJAiVJ7THvgQcDcbpnv";
    public static final String ORDER_PAY_CHANNEL_ATTRIBUTE_ID = "a_r2knarDTXUCJ4hQW3hnxUZ";
    public static final String ORDER_CREATED_AT_ATTRIBUTE_ID = "a_6hrSs3aVTC-HCMdVALmfBA";
    public static final String ORDER_TENANT_ATTRIBUTE_ID = "a_SZzBXyt5QOi2lZFRPpDOig";

    public static final String ORDER_REGISTER_DELIVERY_PAGE_ID = "p_1oCgcsDHJ6VxKMsofwqGk2";
    public static final String ORDER_REGISTER_DELIVERY_DELIVER_CONTROL_ID = "c_1N54unoXxaSiuoc1vCU8YE";
    public static final String ORDER_REGISTER_DELIVERY_DELIVERY_ID_CONTROL_ID = "c_1QtLgZiY9fdUuXggERH5Gz";

    public static final String ORDER_REGISTER_WX_PAGE_ID = "p_scTJcyTH7nv6CPKnV6Dsd9";
    public static final String ORDER_REGISTER_WX_TXN_CONTROL_ID = "c_rkPCa5k5Zt7MMsW9ptkTia";
    public static final String ORDER_REGISTER_WX_DATE_CONTROL_ID = "c_258qHEYBxkWVVobcXBbYR1";
    public static final String ORDER_REGISTER_WX_TIME_CONTROL_ID = "c_1QmvU4gcUn8s7XpDRAmgD8";

    public static final String ORDER_REGISTER_BANK_PAGE_ID = "p_cN5cG5MRxbeHxk236THyfo";
    public static final String ORDER_REGISTER_BANK_NAME_CONTROL_ID = "c_vUiYMacVURu51aupitewyU";
    public static final String ORDER_REGISTER_BANK_ACCOUNT_CONTROL_ID = "c_rsRTKB54guEKpxBXNbB2Vx";
    public static final String ORDER_REGISTER_BANK_DATE_CONTROL_ID = "c_a33a7PzD24g9Vhc7tCk6ad";
    public static final String ORDER_REGISTER_BANK_TIME_CONTROL_ID = "c_6JaYZhStygFwyYpp82psvU";

    public static final String ORDER_REGISTER_WX_TRANSFER_PAGE_ID = "p_Uq4bMV4_Thq6lER9xSDZoQ";
    public static final String ORDER_REGISTER_WX_TRANSFER_SCREENSHOTS_CONTROL_ID = "c_Dfp7Nr7nQN2IelbewbJR3A";
    public static final String ORDER_REGISTER_WX_TRANSFER_DATE_CONTROL_ID = "c_H4AtBAv6Tr67y6V_wA-tpA";
    public static final String ORDER_REGISTER_WX_TRANSFER_TIME_CONTROL_ID = "c_mzDlNGRFQOa0OTMwvvKdJg";

    public static final String ORDER_REGISTER_INVOICE_PAGE_ID = "p_rCiQcq2HF8fAxB9arUbJf5";
    public static final String ORDER_REGISTER_INVOICE_FILE_CONTROL_ID = "c_hZLRZEwdhzhP7qW6GL5Ydt";

    public static final String ORDER_REFUND_PAGE_ID = "p_j7FfqhdrRGaRW4vk7hBx82";
    public static final String ORDER_REFUND_REASON_CONTROL_ID = "c_8oyMk1T9p8sPpSGdFMeoMP";

    public static final String ORDER_TRIGGER_SYNC_PAGE_ID = "p_bq9QyJTYYSYKxN89GmM3g1";
    public static final String ORDER_TRIGGER_SYNC_NOTE_CONTROL_ID = "c_pmRvMBgncFC6572WVwDTF3";

    public static final String ORDER_DELETE_PAGE_ID = "p_soWWI_DbQAG7wmNWkYVIRQ";
    public static final String ORDER_DELETE_NOTE_CONTROL_ID = "c_D7dL_QGARp24-TxmSseZuA";

    private final AppRepository appRepository;
    private final AppFactory appFactory;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final AppHeaderImageProvider appHeaderImageProvider;
    private final CommonProperties commonProperties;
    private final ServerProperties serverProperties;

    @Transactional
    public void init() {
        if (appRepository.exists(ORDER_APP_ID)) {
            return;
        }

        FIdentifierControl orderIdControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(ORDER_ID_CONTROL_ID)
                .name("订单编号")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FDropdownControl orderTypeControl = FDropdownControl.builder()
                .type(DROPDOWN)
                .id(ORDER_TYPE_CONTROL_ID)
                .name("订单类型")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10))
                .options(List.of(
                        TextOption.builder().id(ORDER_TYPE_PACKAGE_OPTION_ID).name("购买套餐").build(),
                        TextOption.builder().id(ORDER_TYPE_EXTRA_MEMBER_OPTION_ID).name("增购成员").build(),
                        TextOption.builder().id(ORDER_TYPE_EXTRA_SMS_OPTION_ID).name("增购短信量").build(),
                        TextOption.builder().id(ORDER_TYPE_EXTRA_STORAGE_OPTION_ID).name("增购存储空间").build(),
                        TextOption.builder().id(ORDER_TYPE_EXTRA_VIDEO_OPTION_ID).name("增购视频流量").build(),
                        TextOption.builder().id(ORDER_TYPE_PLATE_OPTION_ID).name("印刷码牌").build()
                ))
                .build();

        FItemStatusControl orderStatusControl = FItemStatusControl.builder()
                .type(ITEM_STATUS)
                .id(ORDER_STATUS_CONTROL_ID)
                .name("订单状态")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(ORDER_STATUS_CREATED_OPTION_ID).name("未支付").color("rgba(254, 165, 0, 1)").build(),
                        TextOption.builder().id(ORDER_STATUS_PAID_OPTION_ID).name("已支付").color("rgba(0, 195, 0, 1)").build(),
                        TextOption.builder().id(ORDER_STATUS_REFUND_OPTION_ID).name("已退款").color("rgba(210, 18, 46, 1)").build()
                ))
                .build();

        FSingleLineTextControl orderDescriptionControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(ORDER_DESCRIPTION_CONTROL_ID)
                .name("订单详情")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        FNumberInputControl orderPriceControl = FNumberInputControl.builder()
                .type(NUMBER_INPUT)
                .id(ORDER_PRICE_CONTROL_ID)
                .name("实付金额")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .precision(2)
                .minMaxSetting(minMaxOf(0, 1000000))
                .autoCalculateSetting(FNumberInputControl.AutoCalculateSetting.builder()
                        .aliasContext(AutoCalculateAliasContext.builder().controlAliases(List.of()).build())
                        .expression(null)
                        .build())
                .build();

        FPersonNameControl orderSubmitterControl = FPersonNameControl.builder()
                .type(PERSON_NAME)
                .id(ORDER_SUBMITTER_CONTROL_ID)
                .name("下单人")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .build();

        FMobileNumberControl orderSubmitterMobileControl = FMobileNumberControl.builder()
                .type(MOBILE)
                .id(ORDER_SUBMITTER_MOBILE_CONTROL_ID)
                .name("下单人手机")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .build();

        FEmailControl orderSubmitterEmailControl = FEmailControl.builder()
                .type(EMAIL)
                .id(ORDER_SUBMITTER_EMAIL_CONTROL_ID)
                .name("下单人邮箱")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .build();

        FDropdownControl orderChannelControl = FDropdownControl.builder()
                .type(DROPDOWN)
                .id(ORDER_CHANNEL_CONTROL_ID)
                .name("支付渠道")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10))
                .options(List.of(
                        TextOption.builder().id(ORDER_CHANNEL_WX_PAY_OPTION_ID).name("在线微信支付").build(),
                        TextOption.builder().id(ORDER_CHANNEL_WX_TRANSFER_OPTION_ID).name("线下微信转账").build(),
                        TextOption.builder().id(ORDER_CHANNEL_BANK_TRANSFER_OPTION_ID).name("银行对公转账").build()
                ))
                .build();

        FIdentifierControl orderWxTxnIdControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(ORDER_WX_TXN_CONTROL_ID)
                .name("微信订单号")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FSingleLineTextControl orderBankAccountControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(ORDER_BANK_ACCOUNT_CONTROL_ID)
                .name("转账银行")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        FItemStatusControl orderDeliveryStatusControl = FItemStatusControl.builder()
                .type(ITEM_STATUS)
                .id(ORDER_DELIVERY_STATUS_CONTROL_ID)
                .name("物流状态")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(ORDER_DELIVERY_NONE_OPTION_ID).name("不适用").build(),
                        TextOption.builder().id(ORDER_DELIVERY_NOT_YET_OPTION_ID).name("待发货").color("rgba(254, 165, 0, 1)").build(),
                        TextOption.builder().id(ORDER_DELIVERY_ALREADY_OPTION_ID).name("已发货").color("rgba(12, 175, 255, 1)").build()
                ))
                .build();

        FDropdownControl orderDeliverControl = FDropdownControl.builder()
                .type(DROPDOWN)
                .id(ORDER_DELIVER_CONTROL_ID)
                .name("物流商")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10))
                .options(List.of(
                        TextOption.builder().id(ORDER_DELIVER_EMS_OPTION_ID).name("EMS").build(),
                        TextOption.builder().id(ORDER_DELIVER_SHUNFENG_OPTION_ID).name("顺丰").build(),
                        TextOption.builder().id(ORDER_DELIVER_YUANTONG_OPTION_ID).name("圆通").build(),
                        TextOption.builder().id(ORDER_DELIVER_ZHONGTONG_OPTION_ID).name("中通").build(),
                        TextOption.builder().id(ORDER_DELIVER_ZHONGTONG_EXPRESS_OPTION_ID).name("中通快运").build(),
                        TextOption.builder().id(ORDER_DELIVER_SHENTONG_OPTION_ID).name("申通").build(),
                        TextOption.builder().id(ORDER_DELIVER_YUNDA_OPTION_ID).name("韵达").build(),
                        TextOption.builder().id(ORDER_DELIVER_YUNDA_EXPRESS_OPTION_ID).name("韵达快运").build()
                ))
                .build();

        FIdentifierControl orderDeliveryIdControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(ORDER_DELIVERY_ID_CONTROL_ID)
                .name("物流单号")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FItemStatusControl orderInvoiceStatusControl = FItemStatusControl.builder()
                .type(ITEM_STATUS)
                .id(ORDER_INVOICE_STATUS_CONTROL_ID)
                .name("发票状态")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .options(List.of(
                        TextOption.builder().id(ORDER_INVOICE_STATUS_NONE_OPTION_ID).name("未申请").build(),
                        TextOption.builder().id(ORDER_INVOICE_STATUS_WAITING_OPTION_ID).name("待开票").color("rgba(254, 165, 0, 1)").build(),
                        TextOption.builder().id(ORDER_INVOICE_STATUS_DONE_OPTION_ID).name("已开票").color("rgba(0, 195, 0, 1)").build()
                ))
                .build();

        FMultiLineTextControl invoiceInfoControl = FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)
                .id(ORDER_INVOICE_INFO_CONTROL_ID)
                .name("发票信息")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10000))
                .rows(3)
                .build();

        FFileUploadControl orderInvoiceFileControl = FFileUploadControl.builder()
                .type(FILE_UPLOAD)
                .id(ORDER_INVOICE_FILE_CONTROL_ID)
                .name("发票文件")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .max(3)
                .perMaxSize(20)
                .category(DOCUMENT)
                .buttonText("添加文件")
                .buttonStyle(defaultButtonStyle())
                .build();

        FFileUploadControl orderScreenshotsControl = FFileUploadControl.builder()
                .type(FILE_UPLOAD)
                .id(ORDER_SCREENSHOTS_CONTROL_ID)
                .name("支付截图")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .max(3)
                .perMaxSize(20)
                .category(IMAGE)
                .buttonText("添加文件")
                .buttonStyle(defaultButtonStyle())
                .build();

        FIdentifierControl orderTenantIdControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(ORDER_TENANT_CONTROL_ID)
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

        Page syncPage = Page.builder()
                .id(ORDER_SYNC_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("后台信息同步").build())
                .controls(List.of(
                        orderIdControl, orderTenantIdControl, orderTypeControl, orderStatusControl, orderDescriptionControl, orderPriceControl,
                        orderSubmitterControl, orderSubmitterMobileControl, orderSubmitterEmailControl, orderChannelControl,
                        orderWxTxnIdControl, orderBankAccountControl, orderScreenshotsControl, orderDeliveryStatusControl,
                        orderDeliverControl, orderDeliveryIdControl, orderInvoiceStatusControl, invoiceInfoControl,
                        orderInvoiceFileControl
                ))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .submitType(ONCE_PER_INSTANCE)
                        .pageName("后台信息同步")
                        .permission(CAN_MANAGE_APP)
                        .build())
                .build();

        PSubmissionReferenceControl orderDetailReferenceControl = PSubmissionReferenceControl.builder()
                .type(SUBMISSION_REFERENCE)
                .id(ORDER_DETAIL_REF_CONTROL_ID)
                .name("订单详情")
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

        Page homePage = Page.builder()
                .id(ORDER_HOME_PAGE_ID)
                .header(defaultPageHeaderBuilder()
                        .image(appHeaderImageProvider.defaultAppHeaderImage())
                        .imageCropType(FOUR_TO_THREE)
                        .build())
                .title(defaultPageTitleBuilder().text("订单详情").build())
                .controls(List.of(orderDetailReferenceControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("订单详情")
                        .permission(CAN_MANAGE_APP)
                        .build())
                .build();

        FDropdownControl orderRegisterDeliverControl = FDropdownControl.builder()
                .type(DROPDOWN)
                .id(ORDER_REGISTER_DELIVERY_DELIVER_CONTROL_ID)
                .name("物流承运商")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 10))
                .options(List.of(
                        TextOption.builder().id(ORDER_DELIVER_EMS_OPTION_ID).name("EMS").build(),
                        TextOption.builder().id(ORDER_DELIVER_SHUNFENG_OPTION_ID).name("顺丰").build(),
                        TextOption.builder().id(ORDER_DELIVER_YUANTONG_OPTION_ID).name("圆通").build(),
                        TextOption.builder().id(ORDER_DELIVER_ZHONGTONG_OPTION_ID).name("中通").build(),
                        TextOption.builder().id(ORDER_DELIVER_ZHONGTONG_EXPRESS_OPTION_ID).name("中通快运").build(),
                        TextOption.builder().id(ORDER_DELIVER_SHENTONG_OPTION_ID).name("申通").build(),
                        TextOption.builder().id(ORDER_DELIVER_YUNDA_OPTION_ID).name("韵达").build(),
                        TextOption.builder().id(ORDER_DELIVER_YUNDA_EXPRESS_OPTION_ID).name("韵达快运").build()
                ))
                .build();

        FIdentifierControl orderRegisterDeliveryIdControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(ORDER_REGISTER_DELIVERY_DELIVERY_ID_CONTROL_ID)
                .name("物流单号")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        Page registerDeliveryPage = Page.builder()
                .id(ORDER_REGISTER_DELIVERY_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("发货登记").build())
                .controls(List.of(orderRegisterDeliverControl, orderRegisterDeliveryIdControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("发货登记")
                        .submitType(ONCE_PER_INSTANCE)
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE, ON_UPDATE))
                        .build())
                .build();

        FIdentifierControl orderRegisterWxTxnIdControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(ORDER_REGISTER_WX_TXN_CONTROL_ID)
                .name("微信订单号")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FDateControl orderRegisterWxDateControl = FDateControl.builder()
                .type(DATE)
                .id(ORDER_REGISTER_WX_DATE_CONTROL_ID)
                .name("支付日期")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        FTimeControl orderRegisterWxTimeControl = FTimeControl.builder()
                .type(TIME)
                .id(ORDER_REGISTER_WX_TIME_CONTROL_ID)
                .name("支付时间")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        Page registerWxPage = Page.builder()
                .id(ORDER_REGISTER_WX_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("微信支付登记").build())
                .controls(List.of(orderRegisterWxTxnIdControl, orderRegisterWxDateControl, orderRegisterWxTimeControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("微信支付登记")
                        .submitType(ONCE_PER_INSTANCE)
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE, ON_UPDATE))
                        .build())
                .build();

        FIdentifierControl orderRegisterBankNameControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(ORDER_REGISTER_BANK_NAME_CONTROL_ID)
                .name("转入银行")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FIdentifierControl orderRegisterBankAccountControl = FIdentifierControl.builder()
                .type(IDENTIFIER)
                .id(ORDER_REGISTER_BANK_ACCOUNT_CONTROL_ID)
                .name("银行卡号")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .uniqueType(NONE)
                .minMaxSetting(minMaxOf(0, 50))
                .identifierFormatType(IdentifierFormatType.NONE)
                .build();

        FDateControl orderRegisterBankDateControl = FDateControl.builder()
                .type(DATE)
                .id(ORDER_REGISTER_BANK_DATE_CONTROL_ID)
                .name("转入日期")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        FTimeControl orderRegisterBankTimeControl = FTimeControl.builder()
                .type(TIME)
                .id(ORDER_REGISTER_BANK_TIME_CONTROL_ID)
                .name("转入时间")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        Page registerBankPage = Page.builder()
                .id(ORDER_REGISTER_BANK_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("银行对公转账登记").build())
                .controls(List.of(orderRegisterBankNameControl, orderRegisterBankAccountControl,
                        orderRegisterBankDateControl, orderRegisterBankTimeControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("银行对公转账登记")
                        .submitType(ONCE_PER_INSTANCE)
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE, ON_UPDATE))
                        .build())
                .build();

        FFileUploadControl orderRegisterWxSreenshotsControl = FFileUploadControl.builder()
                .type(FILE_UPLOAD)
                .id(ORDER_REGISTER_WX_TRANSFER_SCREENSHOTS_CONTROL_ID)
                .name("转账截图")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .max(3)
                .perMaxSize(20)
                .category(IMAGE)
                .buttonText("添加文件")
                .buttonStyle(defaultButtonStyle())
                .build();

        FDateControl orderRegisterWxTransferDateControl = FDateControl.builder()
                .type(DATE)
                .id(ORDER_REGISTER_WX_TRANSFER_DATE_CONTROL_ID)
                .name("转账日期")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        FTimeControl orderRegisterWxTransferTimeControl = FTimeControl.builder()
                .type(TIME)
                .id(ORDER_REGISTER_WX_TRANSFER_TIME_CONTROL_ID)
                .name("转账时间")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .build();

        Page registerWxTransferPage = Page.builder()
                .id(ORDER_REGISTER_WX_TRANSFER_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("线下微信转账登记").build())
                .controls(List.of(orderRegisterWxSreenshotsControl,
                        orderRegisterWxTransferDateControl,
                        orderRegisterWxTransferTimeControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("线下微信转账登记")
                        .submitType(ONCE_PER_INSTANCE)
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE, ON_UPDATE))
                        .build())
                .build();

        FFileUploadControl orderRegisterInvoiceFileControl = FFileUploadControl.builder()
                .type(FILE_UPLOAD)
                .id(ORDER_REGISTER_INVOICE_FILE_CONTROL_ID)
                .name("发票")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .max(3)
                .perMaxSize(20)
                .nameEditable(true)
                .category(DOCUMENT)
                .buttonText("上传发票")
                .buttonStyle(defaultButtonStyle())
                .build();

        Page registerInvoicePage = Page.builder()
                .id(ORDER_REGISTER_INVOICE_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("开具发票").build())
                .controls(List.of(orderRegisterInvoiceFileControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("开具发票")
                        .submitType(ONCE_PER_INSTANCE)
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE, ON_UPDATE))
                        .build())
                .build();

        FSingleLineTextControl orderRefundReasonControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(ORDER_REFUND_REASON_CONTROL_ID)
                .name("退款原因")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        Page refundPage = Page.builder()
                .id(ORDER_REFUND_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("退款").build())
                .controls(List.of(orderRefundReasonControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("退款")
                        .submitType(ONCE_PER_INSTANCE)
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE, ON_UPDATE))
                        .build())
                .build();

        FSingleLineTextControl orderTriggerSyncNoteControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(ORDER_TRIGGER_SYNC_NOTE_CONTROL_ID)
                .name("备注")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSetting())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        Page triggerSyncPage = Page.builder()
                .id(ORDER_TRIGGER_SYNC_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("同步信息").build())
                .controls(List.of(orderTriggerSyncNoteControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("同步信息")
                        .submitType(NEW)
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE, ON_UPDATE))
                        .build())
                .build();

        FSingleLineTextControl deleteOrderNoteControl = FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)
                .id(ORDER_DELETE_NOTE_CONTROL_ID)
                .name("备注")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(true).build())
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(minMaxOf(0, 100))
                .build();

        Page deleteOrderPage = Page.builder()
                .id(ORDER_DELETE_PAGE_ID)
                .header(defaultPageHeaderBuilder().type(INHERITED).build())
                .title(defaultPageTitleBuilder().text("删除订单").build())
                .controls(List.of(deleteOrderNoteControl))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder()
                        .pageName("删除订单")
                        .submitType(NEW)
                        .permission(CAN_MANAGE_APP)
                        .submissionWebhookTypes(List.of(ON_CREATE))
                        .build())
                .build();

        Attribute orderCreatedAtAttribute = Attribute.builder()
                .id(ORDER_CREATED_AT_ATTRIBUTE_ID)
                .name("下单时间")
                .type(INSTANCE_CREATE_TIME)
                .pcListEligible(true)
                .build();

        Attribute orderTypeAttribute = Attribute.builder()
                .id(ORDER_TYPE_ATTRIBUTE_ID)
                .name("订单类型")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(orderTypeControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute orderDescriptionAttribute = Attribute.builder()
                .id(ORDER_DESCRIPTION_ATTRIBUTE_ID)
                .name("订单详情")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(orderDescriptionControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute orderStatusAttribute = Attribute.builder()
                .id(ORDER_STATUS_ATTRIBUTE_ID)
                .name("订单状态")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(orderStatusControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .kanbanEligible(true)
                .build();

        Attribute orderPriceAttribute = Attribute.builder()
                .id(ORDER_PRICE_ATTRIBUTE_ID)
                .name("实付金额")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(orderPriceControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute orderDeliveryStatusAttribute = Attribute.builder()
                .id(ORDER_DELIVERY_STATUS_ATTRIBUTE_ID)
                .name("物流状态")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(orderDeliveryStatusControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .kanbanEligible(true)
                .build();

        Attribute orderInvoiceStatusAttribute = Attribute.builder()
                .id(ORDER_INVOICE_STATUS_ATTRIBUTE_ID)
                .name("开票状态")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(orderInvoiceStatusControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .kanbanEligible(true)
                .build();

        Attribute orderChannelAttribute = Attribute.builder()
                .id(ORDER_PAY_CHANNEL_ATTRIBUTE_ID)
                .name("支付渠道")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(orderChannelControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute orderIdAttribute = Attribute.builder()
                .id(ORDER_ID_ATTRIBUTE_ID)
                .name("订单编号")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(orderIdControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Attribute orderTenantAttribute = Attribute.builder()
                .id(ORDER_TENANT_ATTRIBUTE_ID)
                .name("租户编号")
                .type(CONTROL_LAST)
                .pageId(syncPage.getId())
                .controlId(orderTenantIdControl.getId())
                .range(NO_LIMIT)
                .pcListEligible(true)
                .build();

        Menu menu = Menu.builder().links(List.of(
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("发货登记")
                                .type(PAGE)
                                .pageId(registerDeliveryPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("银行对公转账")
                                .type(PAGE)
                                .pageId(registerBankPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("线下微信转账")
                                .type(PAGE)
                                .pageId(registerWxTransferPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("开具发票")
                                .type(PAGE)
                                .pageId(registerInvoicePage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("退款")
                                .type(PAGE)
                                .pageId(refundPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("在线微信支付")
                                .type(PAGE)
                                .pageId(registerWxPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("同步后台数据")
                                .type(PAGE)
                                .pageId(triggerSyncPage.getId())
                                .build(),
                        PageLink.builder()
                                .id(newShortUuid())
                                .name("删除订单")
                                .type(PAGE)
                                .pageId(deleteOrderPage.getId())
                                .build()
                ))
                .showBasedOnPermission(true).build();

        AppSetting setting = AppSetting.builder()
                .config(AppConfig.builder()
                        .operationPermission(CAN_MANAGE_APP)
                        .landingPageType(DEFAULT)
                        .qrWebhookTypes(List.of())
                        .instanceAlias("订单")
                        .customIdAlias("订单编号")
                        .homePageId(homePage.getId())
                        .allowDuplicateInstanceName(true)
                        .appManualEnabled(true)
                        .build())
                .appTopBar(defaultAppTopBar())
                .pages(List.of(homePage, syncPage, registerDeliveryPage, registerWxPage, registerBankPage,
                        registerWxTransferPage, registerInvoicePage, refundPage, triggerSyncPage, deleteOrderPage))
                .menu(menu)
                .attributes(List.of(orderDescriptionAttribute, orderStatusAttribute, orderPriceAttribute,
                        orderDeliveryStatusAttribute, orderInvoiceStatusAttribute, orderCreatedAtAttribute,
                        orderTypeAttribute, orderChannelAttribute, orderTenantAttribute, orderIdAttribute
                ))
                .operationMenuItems(List.of())
                .plateSetting(PlateSetting.create())
                .circulationStatusSetting(CirculationStatusSetting.create())
                .build();

        CreateAppResult result = appFactory.create(ORDER_APP_ID,
                "订单管理",
                setting, ORDER_GROUP_ID,
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

        app.updateWebhookSetting(webhookSetting, NO_USER);
        appRepository.save(app);
        groupRepository.save(defaultGroup);
        groupHierarchyRepository.save(result.getGroupHierarchy());

        log.info("Created order manage app.");
    }

}
