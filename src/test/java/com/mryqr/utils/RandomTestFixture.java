package com.mryqr.utils;

import com.apifan.common.random.source.AreaSource;
import com.apifan.common.random.source.OtherSource;
import com.apifan.common.random.source.PersonInfoSource;
import com.mryqr.common.domain.*;
import com.mryqr.common.domain.administrative.Administrative;
import com.mryqr.common.domain.administrative.AdministrativeProvider;
import com.mryqr.common.domain.stat.StatRange;
import com.mryqr.common.domain.stat.TimeSegmentInterval;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.app.domain.page.header.PageHeader;
import com.mryqr.core.app.domain.page.setting.AfterSubmitBehaviour;
import com.mryqr.core.app.domain.page.setting.ApprovalSetting;
import com.mryqr.core.app.domain.page.setting.PageSetting;
import com.mryqr.core.app.domain.page.setting.notification.NotificationSetting;
import com.mryqr.core.app.domain.page.title.PageTitle;
import com.mryqr.core.app.domain.ui.*;
import com.mryqr.core.app.domain.ui.align.HorizontalAlignType;
import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.border.BorderSide;
import com.mryqr.core.app.domain.ui.border.BorderType;
import com.mryqr.core.app.domain.ui.shadow.Shadow;
import com.mryqr.core.submission.domain.answer.address.AddressAnswer;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.datetime.DateTimeAnswer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.email.EmailAnswer;
import com.mryqr.core.submission.domain.answer.fileupload.FileUploadAnswer;
import com.mryqr.core.submission.domain.answer.geolocation.GeolocationAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.imageupload.ImageUploadAnswer;
import com.mryqr.core.submission.domain.answer.itemcount.ItemCountAnswer;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import com.mryqr.core.submission.domain.answer.memberselect.MemberSelectAnswer;
import com.mryqr.core.submission.domain.answer.mobilenumber.MobileNumberAnswer;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelection;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelectionAnswer;
import com.mryqr.core.submission.domain.answer.multilinetext.MultiLineTextAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.core.submission.domain.answer.numberranking.NumberRankingAnswer;
import com.mryqr.core.submission.domain.answer.personname.PersonNameAnswer;
import com.mryqr.core.submission.domain.answer.pointcheck.PointCheckAnswer;
import com.mryqr.core.submission.domain.answer.pointcheck.PointCheckValue;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.submission.domain.answer.richtext.RichTextInputAnswer;
import com.mryqr.core.submission.domain.answer.signature.SignatureAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.mryqr.common.domain.permission.Permission.AS_TENANT_MEMBER;
import static com.mryqr.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.common.domain.stat.SubmissionSegmentType.SUBMIT_COUNT_SUM;
import static com.mryqr.common.domain.stat.SubmissionTimeBasedType.CREATED_AT;
import static com.mryqr.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.page.Page.newPageId;
import static com.mryqr.core.app.domain.page.control.Control.newControlId;
import static com.mryqr.core.app.domain.page.control.ControlStyleSetting.defaultControlStyleSetting;
import static com.mryqr.core.app.domain.page.control.ControlType.*;
import static com.mryqr.core.app.domain.page.control.PButtonPageLinkControl.StyleType.CARD_BUTTON;
import static com.mryqr.core.app.domain.page.control.PSeparatorControl.SeparatorType.DASHED;
import static com.mryqr.core.app.domain.page.header.PageHeaderType.CUSTOM;
import static com.mryqr.core.app.domain.page.setting.AfterSubmitNavigationType.DEFAULT;
import static com.mryqr.core.app.domain.page.setting.SubmitType.NEW;
import static com.mryqr.core.app.domain.page.setting.SubmitterUpdateRange.IN_1_DAY;
import static com.mryqr.core.app.domain.page.submitbutton.SubmitButton.defaultSubmitButton;
import static com.mryqr.core.app.domain.ui.FontStyle.defaultFontStyle;
import static com.mryqr.core.app.domain.ui.ImageCropType.NO_CROP;
import static com.mryqr.core.app.domain.ui.MinMaxSetting.minMaxOf;
import static com.mryqr.core.app.domain.ui.align.VerticalAlignType.MIDDLE;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class RandomTestFixture {

    public static String rMobile() {
        return String.valueOf(RandomUtils.secure().randomLong(13000000000L, 19000000000L));
    }

    public static String rEmail() {
        return (PersonInfoSource.getInstance().randomEnglishName().split(" ")[0] + "@" + RandomStringUtils.secure().nextAlphabetic(rInt(3, 8)) + ".com").toLowerCase();
    }

    public static String rMobileOrEmail() {
        return rBool() ? rMobile() : rEmail();
    }

    public static String rPassword() {
        return RandomStringUtils.secure().nextAlphabetic(10);
    }

    public static String rVerificationCode() {
        return RandomStringUtils.secure().nextNumeric(6);
    }

    public static String rMemberName() {
        return rRawMemberName() + RandomStringUtils.secure().nextAlphabetic(10);
    }

    public static String rRawMemberName() {
        return PersonInfoSource.getInstance().randomChineseName();
    }

    public static String rCompanyName() {
        return OtherSource.getInstance().randomCompanyName(AreaSource.getInstance().randomCity(""));
    }

    public static String rTenantName() {
        return rCompanyName();
    }

    public static String rAppName() {
        return OtherSource.getInstance().randomEconomicCategory().getName().replace("、", "") + RandomStringUtils.secure().nextAlphabetic(5) + "应用系统";
    }

    public static String rDepartmentName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "部门";
    }

    public static String rFormName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "表单";
    }

    public static String rPageActionName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "提交";
    }

    public static String rMobileWxOpenId() {
        return RandomStringUtils.secure().nextAlphabetic(28);
    }

    public static String rPcWxOpenId() {
        return RandomStringUtils.secure().nextAlphabetic(28);
    }

    public static String rWxUnionId() {
        return RandomStringUtils.secure().nextAlphabetic(28);
    }

    public static String rPageLinkName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "链接";
    }

    public static String rPageName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "页面";
    }

    public static String rPlateBatchName() {
        return RandomStringUtils.secure().nextAlphabetic(10) + "批次";
    }

    public static String rQrName() {
        return rRawQrName() + RandomStringUtils.secure().nextAlphabetic(10);
    }

    public static String rRawQrName() {
        int i = rInt(1, 10);
        if (i > 5) {
            String sentence = OtherSource.getInstance().randomChineseSentence();
            if (sentence.length() > 10) {
                return sentence.substring(0, rInt(5, 10));
            }

            if (sentence.length() > 5) {
                return sentence.substring(0, 5);
            }

            return sentence;
        }

        if (i > 2) {
            return AreaSource.getInstance().randomCity(",").split(",")[1] + rRawMemberName();
        }

        return AreaSource.getInstance().randomCity("");
    }

    public static String rPlateKeyName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "键";
    }

    public static String rGroupName() {
        return rRawGroupName() + RandomStringUtils.secure().nextAlphabetic(10);
    }

    public static String rRawGroupName() {
        return AreaSource.getInstance().randomCity(",").split(",")[1] + OtherSource.getInstance().randomCompanyDepartment();
    }

    public static String rMobileWxCode() {
        return RandomStringUtils.secure().nextAlphabetic(10);
    }

    public static String rDeliveryId() {
        return RandomStringUtils.secure().nextAlphabetic(10);
    }

    public static String rBankAccountId() {
        return RandomStringUtils.secure().nextNumeric(16);
    }

    public static String rWxPayTxnId() {
        return RandomStringUtils.secure().nextNumeric(10);
    }

    public static String rAttributeName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "属性";
    }

    public static String rReportName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "报告";
    }

    public static String rTrendItemName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "趋势项";
    }

    public static String rControlName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "控件";
    }

    public static String rAddressDetail() {
        return AreaSource.getInstance().randomAddress();
    }

    public static String rAssignmentPlanName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "任务计划";
    }

    //排除掉省市县不全的
    private static final Set<String> excludedProvinces = Set.of("台湾省", "香港", "澳门", "海南省", "新疆维吾尔自治区", "湖北省", "河南省", "广东省", "甘肃省");

    public static Address rAddress() {
        String provinceName;
        String cityName = null;
        String districtName = null;

        List<Administrative> provinces = AdministrativeProvider.CHINA.getChild().stream()
                .filter(administrative -> !excludedProvinces.contains(administrative.getName())).toList();

        Administrative province = provinces.get(RandomUtils.secure().randomInt(0, provinces.size()));
        provinceName = province.getName();
        List<Administrative> cities = province.getChild();
        if (isNotEmpty(cities)) {
            Administrative city = cities.get(RandomUtils.secure().randomInt(0, cities.size()));
            cityName = city.getName();
            List<Administrative> districts = city.getChild();
            if (isNotEmpty(districts)) {
                Administrative district = districts.get(RandomUtils.secure().randomInt(0, districts.size()));
                districtName = district.getName();
            }
        }
        return Address.builder()
                .province(provinceName)
                .city(cityName)
                .district(districtName)
                .address(rAddressDetail())
                .build();
    }

    public static Geolocation rGeolocation() {
        return Geolocation.builder()
                .address(rAddress())
                .point(Geopoint.builder()
                        .longitude(RandomUtils.secure().randomFloat(74, 135))
                        .latitude(RandomUtils.secure().randomFloat(18, 53))
                        .build())
                .build();
    }

    public static String rCustomId() {
        return newShortUuid();
    }

    public static boolean rBool() {
        return RandomUtils.secure().randomBoolean();
    }

    public static <T extends Enum<?>> T rEnumOf(Class<T> clazz) {
        int x = RandomUtils.secure().randomInt(0, clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    public static String rSentence(int maxLength) {
        if (maxLength < 5) {
            return RandomStringUtils.secure().next(maxLength);
        }

        String sentence = OtherSource.getInstance().randomChinese(RandomUtils.secure().randomInt(1, 5000));
        if (sentence.length() > maxLength) {
            return sentence.substring(0, maxLength - 1).trim();
        }

        String trimed = sentence.trim();
        if (isBlank(trimed)) {
            return RandomStringUtils.secure().next(maxLength);
        }

        return trimed;
    }

    public static int rInt(int minInclusive, int maxInclusive) {
        return RandomUtils.secure().randomInt(minInclusive, maxInclusive + 1);
    }

    public static String rUrl() {
        return "https://www." + RandomStringUtils.secure().nextAlphabetic(10) + ".com";
    }

    public static String rColor() {
        return OtherSource.getInstance().randomHexColor();
    }

    public static String rDate() {
        //最近5年
        LocalDate start = LocalDate.of(Year.now().getValue() - 5, Month.JANUARY, 1);
        long days = ChronoUnit.DAYS.between(start, LocalDate.now());
        LocalDate randomDate = start.plusDays(new Random().nextInt((int) days + 1));
        return randomDate.toString();
    }

    public static String rTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static Instant rInstant() {
        //最近5年
        return Instant.now().minusSeconds(rInt(0, 5 * 365 * 24 * 3600));
    }

    public static String rFontFamily() {
        ArrayList<String> fonts = newArrayList("默认", "宋体", "黑体", "楷体", "Helvetica", "Arial", "Verdana");
        return fonts.get(RandomUtils.secure().randomInt(0, fonts.size()));
    }

    public static FontStyle rFontStyle() {
        return FontStyle.builder()
                .fontFamily(rFontFamily())
                .fontSize(14)
                .bold(rBool())
                .italic(rBool())
                .color(rColor())
                .build();
    }

    public static BoxedTextStyle rBoxedTextStyle() {
        return BoxedTextStyle.builder()
                .fontStyle(rFontStyle())
                .alignType(rEnumOf(HorizontalAlignType.class))
                .lineHeight(1.5f)
                .fullWidth(rBool())
                .backgroundColor(rColor())
                .border(rBorder())
                .shadow(rShadow())
                .vPadding(0)
                .hPadding(0)
                .topMargin(0)
                .bottomMargin(0)
                .borderRadius(4)
                .build();
    }

    public static ButtonStyle rButtonStyle() {
        return ButtonStyle.builder()
                .fontStyle(rFontStyle())
                .backgroundColor(rColor())
                .border(rBorder())
                .shadow(rShadow())
                .vPadding(0)
                .build();
    }

    public static Border rBorder() {
        return Border.builder()
                .type(rEnumOf(BorderType.class))
                .width(0)
                .sides(newHashSet(BorderSide.values()))
                .color(rColor())
                .build();
    }

    public static Shadow rShadow() {
        return Shadow.builder().width(0).color(rColor()).build();
    }

    public static AppearanceStyle rAppearanceStyle() {
        return AppearanceStyle.builder()
                .backgroundColor(rColor())
                .shadow(rShadow())
                .border(rBorder())
                .vPadding(0)
                .hPadding(0)
                .build();
    }

    private static MarkdownStyle rMarkdownStyle() {
        return MarkdownStyle.builder()
                .fontStyle(FontStyle.builder()
                        .fontFamily("默认")
                        .fontSize(14)
                        .bold(false)
                        .italic(false)
                        .color("#444")
                        .build())
                .lineHeight(1.6f)
                .build();
    }

    public static UploadedFile rUploadedFile() {
        String ossKey = RandomStringUtils.secure().nextAlphabetic(10) + "/" + RandomStringUtils.secure().nextAlphabetic(10) + "/" + ".pdf";
        String fileUrl = "https://mry-files-local.oss-cn-hagnzhou.aliyuncs.com/" + ossKey;
        return UploadedFile.builder()
                .id(newShortUuid())
                .name(RandomStringUtils.secure().nextAlphabetic(10) + "文件")
                .type("application/pdf")
                .fileUrl(fileUrl)
                .ossKey(ossKey)
                .size(100)
                .build();
    }

    public static UploadedFile rImageFile() {
        String ossKey = RandomStringUtils.secure().nextAlphabetic(10) + "/" + RandomStringUtils.secure().nextAlphabetic(10) + "/" + ".png";
        String fileUrl = "https://mry-files-local.oss-cn-hangzhou.aliyuncs.com/" + ossKey;
        return UploadedFile.builder()
                .id(newShortUuid())
                .name(RandomStringUtils.secure().nextAlphabetic(10) + "图片")
                .type("image/png")
                .fileUrl(fileUrl)
                .ossKey(ossKey)
                .size(100)
                .build();
    }

    public static UploadedFile rVideoFile() {
        String ossKey = RandomStringUtils.secure().nextAlphabetic(10) + "/" + RandomStringUtils.secure().nextAlphabetic(10) + "/" + ".mp4";
        String fileUrl = "https://mry-files-local.oss-cn-hangzhou.aliyuncs.com/" + ossKey;
        return UploadedFile.builder()
                .id(newShortUuid())
                .name(RandomStringUtils.secure().nextAlphabetic(10) + "视频")
                .type("video/mp4")
                .fileUrl(fileUrl)
                .ossKey(ossKey)
                .size(100)
                .build();
    }

    public static String rControlErrorTip() {
        return rBool() ? null : rSentence(100);
    }

    public static String rPlaceholder() {
        return rBool() ? null : rSentence(MAX_PLACEHOLDER_LENGTH);
    }

    public static String rControlFieldName() {
        return rBool() ? null : RandomStringUtils.secure().nextAlphabetic(6) + "字段名";
    }

    public static String rInputNumberControlSuffix() {
        return rBool() ? null : RandomStringUtils.secure().nextAlphabetic(6) + "后缀";
    }

    public static ControlFillableSetting defaultFillableSetting() {
        return defaultFillableSettingBuilder().build();
    }

    public static ControlFillableSetting.ControlFillableSettingBuilder defaultFillableSettingBuilder() {
        return ControlFillableSetting.builder()
                .mandatory(false)
                .errorTips(rControlErrorTip())
                .fieldName(rControlFieldName())
                .submissionSummaryEligible(false);
    }

    public static List<TextOption> rTextOptions(int size) {
        return IntStream.range(0, size).mapToObj(value -> TextOption.builder()
                        .id(newShortUuid())
                        .color(rColor())
                        .name(RandomStringUtils.secure().nextAlphabetic(6) + "选项")
                        .build())
                .collect(toList());
    }

    public static ControlNameSetting rControlNameSetting() {
        return ControlNameSetting.builder()
                .hidden(rBool())
                .position(rEnumOf(VerticalPosition.class))
                .textStyle(rBoxedTextStyle())
                .build();
    }

    public static String rControlDescription() {
        return rSentence(100);
    }

    public static BoxedTextStyle rControlDescriptionStyle() {
        return rBoxedTextStyle();
    }

    public static ControlStyleSetting rControlStyleSetting() {
        return defaultControlStyleSetting();
    }

    public static PSectionTitleViewControl defaultSectionTitleControl() {
        return defaultSectionTitleControlBuilder().build();
    }

    public static PSectionTitleViewControl.PSectionTitleViewControlBuilder<?, ?> defaultSectionTitleControlBuilder() {
        return PSectionTitleViewControl.builder()
                .id(newControlId())
                .type(SECTION_TITLE)
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false);
    }

    public static PSeparatorControl defaultSeparatorControl() {
        return defaultSeparatorControlBuilder().build();
    }

    public static PSeparatorControl.PSeparatorControlBuilder<?, ?> defaultSeparatorControlBuilder() {
        return PSeparatorControl.builder()
                .type(SEPARATOR)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .separatorType(DASHED)
                .text("分隔符")
                .fontStyle(rFontStyle())
                .widthRatio(100)
                .borderWidth(1);
    }

    public static PParagraphControl defaultParagraphControl() {
        return defaultParagraphControlBuilder().build();
    }

    public static PParagraphControl.PParagraphControlBuilder<?, ?> defaultParagraphControlBuilder() {
        return PParagraphControl.builder()
                .type(PARAGRAPH)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .content(rSentence(1000))
                .markdownStyle(rMarkdownStyle());
    }

    public static PRichTextControl defaultRichTextControl() {
        return defaultRichTextControlBuilder().build();
    }

    public static PRichTextControl.PRichTextControlBuilder<?, ?> defaultRichTextControlBuilder() {
        return PRichTextControl.builder()
                .type(RICH_TEXT)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .content(rSentence(1000));
    }

    public static PImageViewControl defaultImageViewControl() {
        return defaultImageViewControlBuilder().build();
    }

    public static PImageViewControl.PImageViewControlBuilder<?, ?> defaultImageViewControlBuilder() {
        return PImageViewControl.builder()
                .id(newControlId())
                .type(IMAGE_VIEW)
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .images(IntStream.range(0, PImageViewControl.MAX_IMAGE_SIZE).mapToObj(value -> rImageFile()).collect(toList()))
                .widthRatio(100)
                .showImageName(rBool())
                .verticalMargin(0)
                .border(rBorder())
                .shadow(rShadow());
    }


    public static PVideoViewControl defaultVideoViewControl() {
        return defaultVideoViewControlBuilder().build();
    }

    public static PVideoViewControl.PVideoViewControlBuilder<?, ?> defaultVideoViewControlBuilder() {
        return PVideoViewControl.builder()
                .id(newControlId())
                .type(VIDEO_VIEW)
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .videos(newArrayList(rVideoFile()))
                .poster(rImageFile());
    }

    public static PAttachmentViewControl defaultAttachmentViewControl() {
        return defaultAttachmentViewControlBuilder().build();
    }

    public static PAttachmentViewControl.PAttachmentViewControlBuilder<?, ?> defaultAttachmentViewControlBuilder() {
        return PAttachmentViewControl.builder()
                .type(ATTACHMENT_VIEW)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .attachments(IntStream.range(0, PAttachmentViewControl.MAX_ATTACHMENT_SIZE).mapToObj(value -> rUploadedFile()).collect(toList()))
                .fileNameStyle(rBoxedTextStyle())
                .appearanceStyle(rAppearanceStyle());
    }

    public static PInstanceListControl defaultInstanceListControl() {
        return defaultInstanceListControlBuilder().build();
    }

    public static PInstanceListControl.PInstanceListControlBuilder<?, ?> defaultInstanceListControlBuilder() {
        return PInstanceListControl.builder()
                .id(newControlId())
                .type(INSTANCE_LIST)
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)
                .rowGutter(20)
                .max(10);
    }

    public static PSubmitHistoryControl.PSubmitHistoryControlBuilder<?, ?> defaultSubmitHistoryControlBuilder() {
        return PSubmitHistoryControl.builder()
                .type(SUBMIT_HISTORY)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .pageIds(newArrayList())
                .appearanceStyle(rAppearanceStyle())
                .max(rInt(PSubmitHistoryControl.MIN_MAX, PSubmitHistoryControl.MAX_MAX))
                .showSubmitter(rBool())
                .showPageName(rBool())
                .orderByAsc(rBool())
                .hideControlIfNoData(rBool());
    }

    public static PImageCardLinkControl defaultImageCardLinkControl() {
        return defaultImageCardLinkControlBuilder().build();
    }

    public static PImageCardLinkControl.PImageCardLinkControlBuilder<?, ?> defaultImageCardLinkControlBuilder() {
        return PImageCardLinkControl.builder()
                .type(IMAGE_CARD_LINK)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .links(newArrayList())
                .imageAspectRatio(100)
                .linkNameTextStyle(rBoxedTextStyle())
                .linkDescriptionTextStyle(rBoxedTextStyle())
                .gutter(0)
                .countPerRow(1)
                .appearanceStyle(rAppearanceStyle())
                .textOverImage(rBool())
                .showBasedOnPermission(rBool());
    }

    public static PButtonPageLinkControl defaultButtonPageLinkControl() {
        return defaultButtonPageLinkControlBuilder().build();
    }

    public static PButtonPageLinkControl.PButtonPageLinkControlBuilder<?, ?> defaultButtonPageLinkControlBuilder() {
        return PButtonPageLinkControl.builder()
                .type(BUTTON_PAGE_LINK)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .styleType(CARD_BUTTON)
                .links(newArrayList())
                .nameTextStyle(rBoxedTextStyle())
                .descriptionTextStyle(rBoxedTextStyle())
                .buttonTextStyle(rButtonStyle())
                .cardButtonTextStyle(rButtonStyle())
                .appearanceStyle(rAppearanceStyle())
                .linkPerLine(1)
                .gutter(0)
                .linkImageSize(30)
                .showBasedOnPermission(rBool());
    }

    public static PIconPageLinkControl defaultIconPageLinkControl() {
        return defaultIconPageLinkControlBuilder().build();
    }

    public static PIconPageLinkControl.PIconPageLinkControlBuilder<?, ?> defaultIconPageLinkControlBuilder() {
        return PIconPageLinkControl.builder()
                .type(ICON_PAGE_LINK)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .links(newArrayList())
                .iconWidth(30)
                .numberPerRow(3)
                .textIconSpace(10)
                .rowGutter(10)
                .appearanceStyle(rAppearanceStyle())
                .fontStyle(defaultFontStyle());
    }


    public static PAnswerReferenceControl.PAnswerReferenceControlBuilder<?, ?> defaultAnswerReferenceControlBuilder() {
        return PAnswerReferenceControl.builder()
                .type(ANSWER_REFERENCE)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .pageId(null)
                .controlId(null)
                .hideControlIfNoData(rBool())
                .textAnswerStyle(PAnswerReferenceControl.TextAnswerStyle.builder().textStyle(rBoxedTextStyle()).build())
                .markdownAnswerStyle(PAnswerReferenceControl.MarkdownAnswerStyle.builder().markdownStyle(rMarkdownStyle()).build())
                .imageAnswerStyle(PAnswerReferenceControl.ImageAnswerStyle.builder()
                        .widthRatio(100)
                        .showImageName(rBool())
                        .verticalMargin(0)
                        .border(rBorder())
                        .shadow(rShadow())
                        .build())
                .fileAnswerStyle(PAnswerReferenceControl.FileAnswerStyle.builder()
                        .fileNameStyle(rBoxedTextStyle())
                        .appearanceStyle(rAppearanceStyle())
                        .build())
                .videoAnswerStyle(PAnswerReferenceControl.VideoAnswerStyle.builder().showFileName(rBool()).build())
                .audioAnswerStyle(PAnswerReferenceControl.AudioAnswerStyle.builder().showFileName(rBool()).build());

    }

    public static PSubmissionReferenceControl.PSubmissionReferenceControlBuilder<?, ?> defaultSubmissionReferenceControlBuilder() {
        return PSubmissionReferenceControl.builder()
                .type(SUBMISSION_REFERENCE)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .pageId(null)
                .stripped(rBool())
                .styleType(PSubmissionReferenceControl.StyleType.HORIZONTAL_TABLE)
                .appearanceStyle(rAppearanceStyle())
                .keyFontStyle(rFontStyle())
                .valueFontStyle(rFontStyle())
                .headerText("未命名表头")
                .headerFontStyle(rFontStyle())
                .verticalKeyStyle(rBoxedTextStyle())
                .verticalValueStyle(rBoxedTextStyle())
                .hideControlIfNoData(rBool());
    }

    public static PAttributeTableControl.PAttributeTableControlBuilder<?, ?> defaultAttributeTableControlBuilder() {
        return PAttributeTableControl.builder()
                .type(ATTRIBUTE_TABLE)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .styleType(PAttributeTableControl.StyleType.HORIZONTAL_TABLE)
                .attributeIds(newArrayList())
                .keyFontStyle(rFontStyle())
                .valueFontStyle(rFontStyle())
                .headerText("未命名表头")
                .headerFontStyle(rFontStyle())
                .stripped(rBool())
                .verticalKeyStyle(rBoxedTextStyle())
                .verticalValueStyle(rBoxedTextStyle())
                .appearanceStyle(rAppearanceStyle())
                .hideControlIfNoData(rBool());
    }

    public static PAttributeDashboardControl.PAttributeDashboardControlBuilder<?, ?> defaultAttributeDashboardControlBuilder() {
        return PAttributeDashboardControl.builder()
                .type(ATTRIBUTE_DASHBOARD)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .attributeIds(newArrayList())
                .itemsPerLine(2)
                .itemTitlePosition(rEnumOf(VerticalPosition.class))
                .appearanceStyle(rAppearanceStyle())
                .titleStyle(rBoxedTextStyle())
                .contentStyle(rBoxedTextStyle())
                .hideControlIfNoData(rBool());
    }


    public static PBarControl.PBarControlBuilder<?, ?> defaultBarControlBuilder() {
        return PBarControl.builder()
                .type(BAR)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(null)
                .basedControlId(null)
                .targetControlIds(List.of())
                .range(StatRange.NO_LIMIT)
                .max(rInt(PBarControl.MIN_MAX, PBarControl.MAX_MAX))
                .horizontal(rBool())
                .hideGrid(rBool())
                .colors(List.of(rColor()))
                .addressPrecisionType(rEnumOf(AddressPrecisionType.class))
                .multiLevelSelectionPrecisionType(rEnumOf(MultiLevelSelectionPrecisionType.class))
                .hideControlIfNoData(rBool())
                .showNumber(rBool())
                .sizeRatio(60);
    }

    public static PPieControl.PPieControlBuilder<?, ?> defaultPieControlBuilder() {
        return PPieControl.builder()
                .type(PIE)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(null)
                .basedControlId(null)
                .targetControlId(null)
                .range(StatRange.NO_LIMIT)
                .max(rInt(PPieControl.MIN_MAX, PPieControl.MAX_MAX))
                .addressPrecisionType(rEnumOf(AddressPrecisionType.class))
                .multiLevelSelectionPrecisionType(rEnumOf(MultiLevelSelectionPrecisionType.class))
                .colors(List.of(rColor()))
                .hideControlIfNoData(rBool())
                .showPercentage(rBool())
                .showValue(rBool())
                .showLabels(rBool())
                .sizeRatio(60);
    }

    public static PDoughnutControl.PDoughnutControlBuilder<?, ?> defaultDoughnutControlBuilder() {
        return PDoughnutControl.builder()
                .type(DOUGHNUT)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(null)
                .basedControlId(null)
                .targetControlId(null)
                .range(StatRange.NO_LIMIT)
                .max(rInt(PDoughnutControl.MIN_MAX, PDoughnutControl.MAX_MAX))
                .addressPrecisionType(rEnumOf(AddressPrecisionType.class))
                .multiLevelSelectionPrecisionType(rEnumOf(MultiLevelSelectionPrecisionType.class))
                .colors(List.of(rColor()))
                .hideControlIfNoData(rBool())
                .showValue(rBool())
                .showPercentage(rBool())
                .showLabels(rBool())
                .showCenterTotal(rBool())
                .sizeRatio(60);
    }

    public static PTrendControl.PTrendControlBuilder<?, ?> defaultTrendControlBuilder() {
        return PTrendControl.builder()
                .type(TREND)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .trendItems(newArrayList())
                .range(StatRange.NO_LIMIT)
                .colors(List.of(rColor()))
                .hideControlIfNoData(rBool())
                .bezier(rBool())
                .showNumber(rBool())
                .maxPoints(rInt(PTrendControl.MIN_POINTS, PTrendControl.MAX_POINTS))
                .sizeRatio(60);
    }

    public static PTimeSegmentControl.PTimeSegmentControlBuilder<?, ?> defaultTimeSegmentControlBuilder() {
        return PTimeSegmentControl.builder()
                .type(TIME_SEGMENT)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .segmentSettings(List.of(PTimeSegmentControl.TimeSegmentSetting.builder()
                        .id(newShortUuid())
                        .name("未命名统计项")
                        .segmentType(SUBMIT_COUNT_SUM)
                        .basedType(CREATED_AT)
                        .build()))
                .horizontal(rBool())
                .colors(List.of(rColor()))
                .interval(rEnumOf(TimeSegmentInterval.class))
                .hideGrid(rBool())
                .max(rInt(PTimeSegmentControl.MIN_MAX, PTimeSegmentControl.MAX_MAX))
                .hideControlIfNoData(rBool())
                .showNumber(rBool())
                .sizeRatio(60);
    }

    public static PNumberRangeSegmentControl.PNumberRangeSegmentControlBuilder<?, ?> defaultValueSegmentControlBuilder() {
        return PNumberRangeSegmentControl.builder()
                .type(NUMBER_RANGE_SEGMENT)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(null)
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .segmentType(SUBMIT_COUNT_SUM)
                .pageId(null)
                .basedControlId(null)
                .targetControlId(null)
                .range(StatRange.NO_LIMIT)
                .hideGrid(rBool())
                .color(rColor())
                .hideControlIfNoData(rBool())
                .showNumber(rBool())
                .numberRangesString("0,10,20,30,40,50,60,70,80,90,100")
                .sizeRatio(60);
    }

    public static FRadioControl defaultRadioControl() {
        return defaultRadioControlBuilder().build();
    }

    public static FRadioControl.FRadioControlBuilder<?, ?> defaultRadioControlBuilder() {
        return FRadioControl.builder()
                .type(RADIO)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .options(rTextOptions(rInt(FRadioControl.MIN_OPTION_SIZE, FRadioControl.MAX_OPTION_SIZE)));
    }

    public static RadioAnswer rAnswer(FRadioControl control) {
        return rAnswerBuilder(control).build();
    }

    public static RadioAnswer.RadioAnswerBuilder<?, ?> rAnswerBuilder(FRadioControl control) {
        TextOption option = control.getOptions().get(RandomUtils.secure().randomInt(0, control.getOptions().size()));
        return RadioAnswer.builder().controlId(control.getId()).controlType(control.getType()).optionId(option.getId());
    }

    public static FCheckboxControl defaultCheckboxControl() {
        return defaultCheckboxControlBuilder().build();
    }

    public static FCheckboxControl.FCheckboxControlBuilder<?, ?> defaultCheckboxControlBuilder() {
        return FCheckboxControl.builder()
                .type(CHECKBOX)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .options(rTextOptions(rInt(FCheckboxControl.MIN_OPTION_SIZE, FCheckboxControl.MAX_OPTION_SIZE)))
                .minMaxSetting(minMaxOf(FCheckboxControl.MIN_SELECTION, FCheckboxControl.MAX_SELECTION));
    }

    public static CheckboxAnswer rAnswer(FCheckboxControl control) {
        return rAnswerBuilder(control).build();
    }

    public static CheckboxAnswer.CheckboxAnswerBuilder<?, ?> rAnswerBuilder(FCheckboxControl control) {
        int max = (int) control.getMinMaxSetting().getMax();
        int optionSize = control.getOptions().size();

        int size = rInt(1, Math.min(max, optionSize));
        List<String> optionIds = control.getOptions().stream().map(TextOption::getId).collect(toList());
        Collections.shuffle(optionIds);
        List<String> result = optionIds.stream().limit(size).collect(toList());
        return CheckboxAnswer.builder().controlId(control.getId()).controlType(control.getType()).optionIds(result);
    }

    public static FSingleLineTextControl defaultSingleLineTextControl() {
        return defaultSingleLineTextControlBuilder().build();
    }

    public static FSingleLineTextControl.FSingleLineTextControlBuilder<?, ?> defaultSingleLineTextControlBuilder() {
        return FSingleLineTextControl.builder()
                .type(SINGLE_LINE_TEXT)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder())
                .minMaxSetting(minMaxOf(FSingleLineTextControl.MIN_ANSWER_LENGTH, FSingleLineTextControl.MAX_ANSWER_LENGTH));
    }

    public static SingleLineTextAnswer rAnswer(FSingleLineTextControl control) {
        return rAnswerBuilder(control).build();
    }

    public static SingleLineTextAnswer.SingleLineTextAnswerBuilder<?, ?> rAnswerBuilder(FSingleLineTextControl control) {
        int size = rInt(1, (int) control.getMinMaxSetting().getMax());

        return SingleLineTextAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .content(rSentence(size));
    }

    public static FMultiLineTextControl defaultMultipleLineTextControl() {
        return defaultMultiLineTextControlBuilder().build();
    }

    public static FMultiLineTextControl.FMultiLineTextControlBuilder<?, ?> defaultMultiLineTextControlBuilder() {
        return FMultiLineTextControl.builder()
                .type(MULTI_LINE_TEXT)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder())
                .minMaxSetting(minMaxOf(0, 500))
                .rows(5);
    }

    public static MultiLineTextAnswer rAnswer(FMultiLineTextControl control) {
        return rAnswerBuilder(control).build();
    }

    public static MultiLineTextAnswer.MultiLineTextAnswerBuilder<?, ?> rAnswerBuilder(FMultiLineTextControl control) {
        int size = rInt(1, (int) control.getMinMaxSetting().getMax());
        return MultiLineTextAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .content(rSentence(size));
    }

    public static FRichTextInputControl defaultRichTextInputControl() {
        return defaultRichTextInputControlBuilder().build();
    }

    public static FRichTextInputControl.FRichTextInputControlBuilder<?, ?> defaultRichTextInputControlBuilder() {
        return FRichTextInputControl.builder()
                .type(RICH_TEXT_INPUT)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder())
                .minMaxSetting(minMaxOf(0, 500));
    }


    public static RichTextInputAnswer rAnswer(FRichTextInputControl control) {
        return rAnswerBuilder(control).build();
    }

    public static RichTextInputAnswer.RichTextInputAnswerBuilder<?, ?> rAnswerBuilder(FRichTextInputControl control) {
        int size = rInt(1, (int) control.getMinMaxSetting().getMax());
        return RichTextInputAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .content(rSentence(size));
    }

    public static FDropdownControl defaultDropdownControl() {
        return defaultDropdownControlBuilder()
                .build();
    }

    public static FDropdownControl.FDropdownControlBuilder<?, ?> defaultDropdownControlBuilder() {
        return FDropdownControl.builder()
                .type(DROPDOWN)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder())
                .multiple(rBool())
                .filterable(rBool())
                .options(rTextOptions(rInt(FDropdownControl.MIN_OPTION_SIZE, FDropdownControl.MAX_OPTION_SIZE)))
                .minMaxSetting(minMaxOf(0, FDropdownControl.MAX_SELECTION));
    }

    public static DropdownAnswer rAnswer(FDropdownControl control) {
        return rAnswerBuilder(control).build();
    }

    public static DropdownAnswer.DropdownAnswerBuilder<?, ?> rAnswerBuilder(FDropdownControl control) {
        List<String> optionIds = newArrayList();
        if (control.isMultiple()) {
            int max = (int) control.getMinMaxSetting().getMax();
            int optionSize = control.getOptions().size();
            int size = rInt(1, Math.min(max, optionSize));
            List<String> allOptionIds = control.getOptions().stream().map(TextOption::getId).collect(toList());
            Collections.shuffle(allOptionIds);
            List<String> result = allOptionIds.stream().limit(size).toList();
            optionIds.addAll(result);
        } else {
            TextOption option = control.getOptions().get(RandomUtils.secure().randomInt(0, control.getOptions().size()));
            optionIds.add(option.getId());
        }

        return DropdownAnswer.builder().controlId(control.getId()).controlType(control.getType()).optionIds(optionIds);
    }

    public static FMemberSelectControl defaultMemberSelectControl() {
        return defaultMemberSelectControlBuilder()
                .build();
    }

    public static FMemberSelectControl.FMemberSelectControlBuilder<?, ?> defaultMemberSelectControlBuilder() {
        return FMemberSelectControl.builder()
                .type(MEMBER_SELECT)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder())
                .multiple(rBool())
                .filterable(rBool())
                .minMaxSetting(minMaxOf(0, FMemberSelectControl.MAX_MEMBER_SELECTION));
    }

    public static MemberSelectAnswer rAnswer(FMemberSelectControl control, String... memberIds) {
        if (control.isMultiple()) {
            return rAnswerBuilder(control)
                    .memberIds(Arrays.stream(memberIds).limit((long) control.getMinMaxSetting().getMax()).collect(toList()))
                    .build();
        } else {
            return rAnswerBuilder(control)
                    .memberIds(Arrays.stream(memberIds).findAny().stream().toList())
                    .build();
        }
    }

    public static MemberSelectAnswer rAnswer(FMemberSelectControl control) {
        return rAnswerBuilder(control)
                .memberIds(newArrayList())
                .build();
    }

    public static MemberSelectAnswer.MemberSelectAnswerBuilder<?, ?> rAnswerBuilder(FMemberSelectControl control) {
        return MemberSelectAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .memberIds(newArrayList());
    }

    public static FFileUploadControl defaultFileUploadControl() {
        return defaultFileUploadControlBuilder()
                .build();
    }

    public static FFileUploadControl.FFileUploadControlBuilder<?, ?> defaultFileUploadControlBuilder() {
        return FFileUploadControl.builder()
                .type(FILE_UPLOAD)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .nameEditable(rBool())
                .sortable(rBool())
                .max(rInt(FFileUploadControl.MIN_MAX_FILE_SIZE, FFileUploadControl.MAX_MAX_FILE_SIZE))
                .perMaxSize(5)
                .category(rEnumOf(FileCategory.class))
                .buttonText("上传文件")
                .buttonStyle(rButtonStyle());
    }

    public static FileUploadAnswer rAnswer(FFileUploadControl control) {
        return rAnswerBuilder(control).build();
    }

    public static FileUploadAnswer.FileUploadAnswerBuilder<?, ?> rAnswerBuilder(FFileUploadControl control) {
        int size = rInt(1, control.getMax());
        List<UploadedFile> files = IntStream.range(0, size).mapToObj(value -> rUploadedFile()).collect(toList());
        return FileUploadAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .files(files);
    }

    public static FImageUploadControl defaultImageUploadControl() {
        return defaultImageUploadControlBuilder().build();
    }

    public static FImageUploadControl.FImageUploadControlBuilder<?, ?> defaultImageUploadControlBuilder() {
        return FImageUploadControl.builder()
                .type(IMAGE_UPLOAD)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .nameEditable(rBool())
                .sortable(rBool())
                .onlyOnSite(rBool())
                .compressType(rEnumOf(FileCompressType.class))
                .max(rInt(FImageUploadControl.MIN_MAX_IMAGE_SIZE, FImageUploadControl.MAX_MAX_IMAGE_SIZE))
                .buttonText("上传图片")
                .buttonStyle(rButtonStyle());
    }

    public static ImageUploadAnswer rAnswer(FImageUploadControl control) {
        return rAnswerBuilder(control).build();
    }

    public static ImageUploadAnswer.ImageUploadAnswerBuilder<?, ?> rAnswerBuilder(FImageUploadControl control) {
        int size = rInt(1, control.getMax());
        List<UploadedFile> files = IntStream.range(0, size).mapToObj(value -> rImageFile()).collect(toList());
        return ImageUploadAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .images(files);
    }

    public static FSignatureControl defaultSignatureControl() {
        return defaultSignatureControlBuilder().build();
    }

    public static FSignatureControl.FSignatureControlBuilder<?, ?> defaultSignatureControlBuilder() {
        return FSignatureControl.builder()
                .type(SIGNATURE)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .buttonText("签名")
                .buttonStyle(rButtonStyle());
    }

    public static SignatureAnswer rAnswer(FSignatureControl control) {
        return rAnswerBuilder(control).build();
    }

    public static SignatureAnswer.SignatureAnswerBuilder<?, ?> rAnswerBuilder(FSignatureControl control) {
        return SignatureAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .signature(rImageFile());
    }

    public static FMultiLevelSelectionControl defaultMultiLevelSelectionControl() {
        return defaultMultiLevelSelectionControlBuilder().build();
    }

    public static FMultiLevelSelectionControl.FMultiLevelSelectionControlBuilder<?, ?> defaultMultiLevelSelectionControlBuilder() {
        String titleText = RandomStringUtils.secure().nextAlphabetic(10) + "/" + RandomStringUtils.secure().nextAlphabetic(10) + "/" + RandomStringUtils.secure().nextAlphabetic(10);

        String optionText = IntStream.range(0, rInt(3, 5)).mapToObj(firstLevel -> {
            String firstLevelName = RandomStringUtils.secure().nextAlphabetic(6);
            return IntStream.range(0, rInt(3, 10)).mapToObj(secondLevel -> {
                String secondLevelName = RandomStringUtils.secure().nextAlphabetic(6);
                return IntStream.range(0, rInt(3, 5)).mapToObj(value -> {
                    String thirdLevelName = RandomStringUtils.secure().nextAlphabetic(6);
                    return firstLevelName + "/" + secondLevelName + "/" + thirdLevelName;
                });
            });
        }).flatMap(Function.identity()).flatMap(Function.identity()).collect(Collectors.joining("\n"));

        return FMultiLevelSelectionControl.builder()
                .type(MULTI_LEVEL_SELECTION)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .filterable(rBool())
                .titleText(titleText)
                .optionText(optionText);
    }

    public static MultiLevelSelectionAnswer rAnswer(FMultiLevelSelectionControl control) {
        return rAnswerBuilder(control).build();
    }

    public static MultiLevelSelectionAnswer.MultiLevelSelectionAnswerBuilder<?, ?> rAnswerBuilder(FMultiLevelSelectionControl control) {
        String level1Name = null;
        String level2Name = null;
        String level3Name = null;
        control.doCorrect();

        MultiLevelOption option = control.getOption();
        List<MultiLevelOption> firstLevelOptions = option.getOptions();
        if (isNotEmpty(firstLevelOptions)) {
            MultiLevelOption firstLevel = firstLevelOptions.get(RandomUtils.secure().randomInt(0, firstLevelOptions.size()));
            level1Name = firstLevel.getName();

            List<MultiLevelOption> secondLevelOptions = firstLevel.getOptions();
            if (isNotEmpty(secondLevelOptions)) {
                MultiLevelOption secondLevel = secondLevelOptions.get(RandomUtils.secure().randomInt(0, secondLevelOptions.size()));
                level2Name = secondLevel.getName();
                List<MultiLevelOption> thirdLevelOptions = secondLevel.getOptions();
                if (isNotEmpty(thirdLevelOptions)) {
                    MultiLevelOption thirdLevel = thirdLevelOptions.get(RandomUtils.secure().randomInt(0, thirdLevelOptions.size()));
                    level3Name = thirdLevel.getName();
                }
            }
        }

        return MultiLevelSelectionAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .selection(MultiLevelSelection.builder()
                        .level1(level1Name)
                        .level2(level2Name)
                        .level3(level3Name)
                        .build());
    }

    public static FAddressControl defaultAddressControl() {
        return defaultAddressControlBuilder().build();
    }

    public static FAddressControl.FAddressControlBuilder<?, ?> defaultAddressControlBuilder() {
        return FAddressControl.builder()
                .type(ADDRESS)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .positionable(rBool())
                .precision(4);
    }

    public static AddressAnswer rAnswer(FAddressControl control) {
        return rAnswerBuilder(control).build();
    }

    public static AddressAnswer.AddressAnswerBuilder<?, ?> rAnswerBuilder(FAddressControl control) {
        return AddressAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .address(rAddress());
    }

    public static FGeolocationControl defaultGeolocationControl() {
        return defaultGeolocationControlBuilder().build();
    }

    public static FGeolocationControl.FGeolocationControlBuilder<?, ?> defaultGeolocationControlBuilder() {
        return FGeolocationControl.builder()
                .type(GEOLOCATION)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .allowRandomPosition(rBool())
                .offsetRestrictionEnabled(false)
                .offsetRestrictionRadius(500)
                .buttonStyle(rButtonStyle());
    }

    public static GeolocationAnswer rAnswer(FGeolocationControl control) {
        return rAnswerBuilder(control)
                .build();
    }

    public static GeolocationAnswer.GeolocationAnswerBuilder<?, ?> rAnswerBuilder(FGeolocationControl control) {
        return GeolocationAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .geolocation(rGeolocation());
    }

    public static FNumberInputControl defaultNumberInputControl() {
        return defaultNumberInputControlBuilder().build();
    }

    public static FNumberInputControl.FNumberInputControlBuilder<?, ?> defaultNumberInputControlBuilder() {
        return FNumberInputControl.builder()
                .type(NUMBER_INPUT)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder())
                .precision(rInt(0, 3))
                .minMaxSetting(minMaxOf(0, FNumberInputControl.MAX_NUMBER))
                .autoCalculateSetting(FNumberInputControl.AutoCalculateSetting.builder()
                        .aliasContext(AutoCalculateAliasContext.builder().controlAliases(newArrayList()).build())
                        .build())
                .autoCalculateEnabled(false)
                .suffix(rInputNumberControlSuffix());
    }

    public static NumberInputAnswer rAnswer(FNumberInputControl control) {
        return rAnswerBuilder(control).build();
    }

    public static NumberInputAnswer.NumberInputAnswerBuilder<?, ?> rAnswerBuilder(FNumberInputControl control) {
        int number = RandomUtils.secure().randomInt(1, (int) control.getMinMaxSetting().getMax());

        return NumberInputAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .number((double) number);
    }

    public static FNumberRankingControl defaultNumberRankingControl() {
        return defaultNumberRankingControlBuilder().build();
    }

    public static FNumberRankingControl.FNumberRankingControlBuilder<?, ?> defaultNumberRankingControlBuilder() {
        return FNumberRankingControl.builder()
                .type(NUMBER_RANKING)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .max(FNumberRankingControl.MAX_RANKING_LIMIT);
    }

    public static NumberRankingAnswer rAnswer(FNumberRankingControl control) {
        return rAnswerBuilder(control).build();
    }

    public static NumberRankingAnswer.NumberRankingAnswerBuilder<?, ?> rAnswerBuilder(FNumberRankingControl control) {
        return NumberRankingAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .rank(rInt(1, control.getMax()));
    }

    public static FMobileNumberControl defaultMobileControl() {
        return defaultMobileNumberControlBuilder().build();
    }

    public static FMobileNumberControl.FMobileNumberControlBuilder<?, ?> defaultMobileNumberControlBuilder() {
        return FMobileNumberControl.builder()
                .type(MOBILE)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder())
                .uniqueType(rEnumOf(AnswerUniqueType.class));
    }

    public static MobileNumberAnswer rAnswer(FMobileNumberControl control) {
        return rAnswerBuilder(control).build();
    }

    public static MobileNumberAnswer.MobileNumberAnswerBuilder<?, ?> rAnswerBuilder(FMobileNumberControl control) {
        return MobileNumberAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .mobileNumber(rMobile());
    }

    public static FEmailControl defaultEmailControl() {
        return defaultEmailControlBuilder().build();
    }

    public static FEmailControl.FEmailControlBuilder<?, ?> defaultEmailControlBuilder() {
        return FEmailControl.builder()
                .type(EMAIL)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder())
                .uniqueType(rEnumOf(AnswerUniqueType.class));
    }

    public static EmailAnswer rAnswer(FEmailControl control) {
        return rAnswerBuilder(control).build();
    }

    public static EmailAnswer.EmailAnswerBuilder<?, ?> rAnswerBuilder(FEmailControl control) {
        return EmailAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .email(rEmail());
    }


    public static FIdentifierControl defaultIdentifierControl() {
        return defaultIdentifierControlBuilder().build();
    }

    public static FIdentifierControl.FIdentifierControlBuilder<?, ?> defaultIdentifierControlBuilder() {
        return FIdentifierControl.builder()
                .type(IDENTIFIER)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder())
                .uniqueType(rEnumOf(AnswerUniqueType.class))
                .identifierFormatType(IdentifierFormatType.NONE)
                .formatRegex(null)
                .minMaxSetting(minMaxOf(FIdentifierControl.MIN_IDENTIFIER_LENGTH, FIdentifierControl.MAX_IDENTIFIER_LENGTH));
    }

    public static IdentifierAnswer rAnswer(FIdentifierControl control) {
        return rAnswerBuilder(control).build();
    }

    public static IdentifierAnswer.IdentifierAnswerBuilder<?, ?> rAnswerBuilder(FIdentifierControl control) {
        MinMaxSetting minMaxSetting = control.getMinMaxSetting();
        int size = rInt(5, (int) minMaxSetting.getMax());

        return IdentifierAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .content(RandomStringUtils.secure().nextAlphabetic(size));
    }

    public static FPersonNameControl defaultPersonNameControl() {
        return defaultPersonNameControlBuilder().build();
    }

    public static FPersonNameControl.FPersonNameControlBuilder<?, ?> defaultPersonNameControlBuilder() {
        return FPersonNameControl.builder()
                .type(PERSON_NAME)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder());
    }

    public static PersonNameAnswer rAnswer(FPersonNameControl control) {
        return rAnswerBuilder(control).build();
    }

    public static PersonNameAnswer.PersonNameAnswerBuilder<?, ?> rAnswerBuilder(FPersonNameControl control) {
        return PersonNameAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .name(rRawMemberName());
    }

    public static FDateControl defaultDateControl() {
        return defaultDateControlBuilder().build();
    }

    public static FDateControl.FDateControlBuilder<?, ?> defaultDateControlBuilder() {
        return FDateControl.builder()
                .type(DATE)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder());
    }

    public static DateAnswer rAnswer(FDateControl control) {
        return rAnswerBuilder(control).build();
    }

    public static DateAnswer.DateAnswerBuilder<?, ?> rAnswerBuilder(FDateControl control) {
        return DateAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .date(rDate());
    }

    public static FTimeControl defaultTimeControl() {
        return defaultTimeControlBuilder().build();
    }

    public static FTimeControl.FTimeControlBuilder<?, ?> defaultTimeControlBuilder() {
        return FTimeControl.builder()
                .type(TIME)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .placeholder(rPlaceholder());
    }


    public static TimeAnswer rAnswer(FTimeControl control) {
        return rAnswerBuilder(control).build();
    }

    public static TimeAnswer.TimeAnswerBuilder<?, ?> rAnswerBuilder(FTimeControl control) {
        return TimeAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .time(rTime());
    }

    public static FDateTimeControl defaultDateTimeControl() {
        return defaultDateTimeControlBuilder().build();
    }

    public static FDateTimeControl.FDateTimeControlBuilder<?, ?> defaultDateTimeControlBuilder() {
        return FDateTimeControl.builder()
                .type(DATE_TIME)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false);
    }

    public static DateTimeAnswer rAnswer(FDateTimeControl control) {
        return rAnswerBuilder(control).build();
    }

    public static DateTimeAnswer.DateTimeAnswerBuilder<?, ?> rAnswerBuilder(FDateTimeControl control) {
        return DateTimeAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .date(rDate())
                .time(rTime());
    }


    public static FItemCountControl defaultItemCountControl() {
        return defaultItemCountControlBuilder().build();
    }

    public static FItemCountControl.FItemCountControlBuilder<?, ?> defaultItemCountControlBuilder() {
        return FItemCountControl.builder()
                .type(ITEM_COUNT)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .options(rTextOptions(rInt(FItemCountControl.MIN_OPTION_SIZE, FItemCountControl.MAX_OPTION_SIZE)))
                .maxItem(rInt(FItemCountControl.MIN_MAX_ITEM_SIZE, FItemCountControl.MAX_MAX_ITEM_SIZE))
                .maxNumberPerItem(rInt(FItemCountControl.MIN_MAX_PER_ITEM_COUNT, FItemCountControl.MAX_MAX_PER_ITEM_COUNT))
                .buttonText("添加物品")
                .buttonStyle(rButtonStyle());
    }

    public static ItemCountAnswer rAnswer(FItemCountControl control) {
        return rAnswerBuilder(control).build();
    }

    public static ItemCountAnswer.ItemCountAnswerBuilder<?, ?> rAnswerBuilder(FItemCountControl control) {
        List<String> optionIds = newArrayList(control.allOptionIds());

        List<CountedItem> items = IntStream.range(0, rInt(1, min(control.getMaxItem(), optionIds.size())))
                .mapToObj(value -> CountedItem.builder()
                        .id(newShortUuid())
                        .optionId(optionIds.get(value))
                        .number(rInt(1, control.getMaxNumberPerItem())).build())
                .collect(toList());

        return ItemCountAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .items(items);
    }

    public static FItemStatusControl defaultItemStatusControl() {
        return defaultItemStatusControlBuilder().build();
    }

    public static FItemStatusControl.FItemStatusControlBuilder<?, ?> defaultItemStatusControlBuilder() {
        return FItemStatusControl.builder()
                .type(ITEM_STATUS)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .options(rTextOptions(rInt(FItemStatusControl.MIN_OPTION_SIZE, FItemStatusControl.MAX_OPTION_SIZE)))
                .placeholder(rPlaceholder())
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .aliasContext(AutoCalculateAliasContext.builder().controlAliases(newArrayList()).build())
                        .records(newArrayList())
                        .build())
                .autoCalculateEnabled(false)
                .initialOptionId(null);
    }

    public static ItemStatusAnswer rAnswer(FItemStatusControl control) {
        return rAnswerBuilder(control).build();
    }

    public static ItemStatusAnswer.ItemStatusAnswerBuilder<?, ?> rAnswerBuilder(FItemStatusControl control) {
        TextOption option = control.getOptions().get(RandomUtils.secure().randomInt(0, control.getOptions().size()));
        return ItemStatusAnswer.builder().controlId(control.getId()).controlType(control.getType()).optionId(option.getId());
    }

    public static FPointCheckControl defaultPointCheckControl() {
        return defaultPointCheckControlBuilder().build();
    }

    public static FPointCheckControl.FPointCheckControlBuilder<?, ?> defaultPointCheckControlBuilder() {
        return FPointCheckControl.builder()
                .type(POINT_CHECK)

                //common settings
                .id(newControlId())
                .name(rControlName())
                .nameSetting(rControlNameSetting())
                .description(rControlDescription())
                .descriptionStyle(rControlDescriptionStyle())
                .styleSetting(rControlStyleSetting())
                .fillableSetting(defaultFillableSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .submitterViewable(false)

                //control specific settings
                .options(rTextOptions(rInt(FPointCheckControl.MIN_OPTION_SIZE, FPointCheckControl.MAX_OPTION_SIZE)));
    }

    public static PointCheckAnswer rAnswer(FPointCheckControl control) {
        return rAnswerBuilder(control).build();
    }

    public static PointCheckAnswer.PointCheckAnswerBuilder<?, ?> rAnswerBuilder(FPointCheckControl control) {
        Map<String, PointCheckValue> checks = newHashMap();

        control.getOptions().forEach(textOption -> checks.put(textOption.getId(), RandomUtils.secure().randomInt(0, 10) > 1 ? PointCheckValue.YES : PointCheckValue.NO));

        return PointCheckAnswer.builder()
                .controlId(control.getId())
                .controlType(control.getType())
                .checks(checks);
    }

    public static Page defaultPage() {
        return defaultPageBuilder().build();
    }

    public static Page defaultPage(Control... controls) {
        return defaultPageBuilder()
                .controls(newArrayList(controls))
                .build();
    }

    public static Page.PageBuilder defaultPageBuilder() {
        return Page.builder()
                .id(newPageId())
                .header(PageHeader.builder()
                        .type(CUSTOM)
                        .showImage(true)
                        .image(null)
                        .imageCropType(NO_CROP)
                        .showText(false)
                        .text("抬头文字")
                        .textStyle(rBoxedTextStyle())
                        .logoImage(null)
                        .logoHeight(36)
                        .logoAlign(MIDDLE)
                        .build())
                .title(defaultPageNameBuilder()
                        .build())
                .controls(newArrayList(defaultSectionTitleControl(), defaultSingleLineTextControl()))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSettingBuilder().build()
                );
    }

    public static PageTitle.PageTitleBuilder defaultPageNameBuilder() {
        return PageTitle.builder()
                .text(rPageName())
                .textStyle(rBoxedTextStyle())
                .description(null)
                .descriptionStyle(rMarkdownStyle());
    }

    public static PageSetting.PageSettingBuilder defaultPageSettingBuilder() {
        return PageSetting.builder()
                .submitType(NEW)
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_APP)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_DAY)
                .approvalSetting(defaultPageApproveSettingBuilder().build())
                .notificationSetting(NotificationSetting.builder()
                        .notificationEnabled(false)
                        .onCreateNotificationRoles(newArrayList())
                        .onUpdateNotificationRoles(newArrayList())
                        .build())
                .submissionWebhookTypes(newArrayList())
                .pageName(null)
                .actionName(null)
                .showAsterisk(false)
                .showControlIndex(false)
                .hideProfileButton(false)
                .hideTopBottomBlank(false)
                .hideTopBar(false)
                .hideHeader(false)
                .hideTitle(false)
                .hideMenu(false)
                .pageMaxWidth(650)
                .contentMaxWidth(650)
                .pageBackgroundColor(rColor())
                .shadow(rShadow())
                .border(rBorder())
                .viewPortBackgroundColor(rColor())
                .viewPortBackgroundImage(rImageFile())
                .afterSubmitBehaviour(defaultAfterSubmitBehaviourBuilder().build());
    }

    public static ApprovalSetting.ApprovalSettingBuilder defaultPageApproveSettingBuilder() {
        return ApprovalSetting.builder()
                .approvalEnabled(false)
                .permission(CAN_MANAGE_APP)
                .passText("通过")
                .notPassText("不通过");
    }

    public static AfterSubmitBehaviour.AfterSubmitBehaviourBuilder defaultAfterSubmitBehaviourBuilder() {
        return AfterSubmitBehaviour.builder()
                .type(DEFAULT)
                .externalUrl(null)
                .internalPageId(null);
    }
}
