package com.mryqr.core.qr;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.CreateAppResponse;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.FAddressControl;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FDateControl;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.app.domain.page.control.FEmailControl;
import com.mryqr.core.app.domain.page.control.FIdentifierControl;
import com.mryqr.core.app.domain.page.control.FItemCountControl;
import com.mryqr.core.app.domain.page.control.FItemStatusControl;
import com.mryqr.core.app.domain.page.control.FMobileNumberControl;
import com.mryqr.core.app.domain.page.control.FMultiLevelSelectionControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FNumberRankingControl;
import com.mryqr.core.app.domain.page.control.FPersonNameControl;
import com.mryqr.core.app.domain.page.control.FPointCheckControl;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.control.FTimeControl;
import com.mryqr.core.app.domain.page.setting.PageSetting;
import com.mryqr.core.app.domain.plate.PlateNamedTextValue;
import com.mryqr.core.app.domain.plate.PlateQrImageSetting;
import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.app.domain.plate.PlateTextValue;
import com.mryqr.core.app.domain.plate.control.KeyValueControl;
import com.mryqr.core.app.domain.plate.control.SingleRowTextControl;
import com.mryqr.core.app.domain.plate.control.TableControl;
import com.mryqr.core.common.domain.Geolocation;
import com.mryqr.core.common.domain.Geopoint;
import com.mryqr.core.common.domain.TextOption;
import com.mryqr.core.common.domain.display.NumberDisplayValue;
import com.mryqr.core.common.domain.display.TextDisplayValue;
import com.mryqr.core.common.domain.permission.Permission;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.QErrorResponse;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.command.UpdateMemberInfoCommand;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.event.PlateBoundEvent;
import com.mryqr.core.platebatch.PlateBatchApi;
import com.mryqr.core.platebatch.domain.PlateBatch;
import com.mryqr.core.qr.command.ChangeQrsGroupCommand;
import com.mryqr.core.qr.command.CreateQrCommand;
import com.mryqr.core.qr.command.CreateQrFromPlateCommand;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.command.DeleteQrsCommand;
import com.mryqr.core.qr.command.RenameQrCommand;
import com.mryqr.core.qr.command.ResetQrPlateCommand;
import com.mryqr.core.qr.command.UpdateQrBaseSettingCommand;
import com.mryqr.core.qr.command.importqr.QrImportResponse;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrCreatedEvent;
import com.mryqr.core.qr.domain.attribute.CheckboxAttributeValue;
import com.mryqr.core.qr.domain.attribute.CirculationStatusAttributeValue;
import com.mryqr.core.qr.domain.attribute.DoubleAttributeValue;
import com.mryqr.core.qr.domain.attribute.DropdownAttributeValue;
import com.mryqr.core.qr.domain.attribute.EmailAttributeValue;
import com.mryqr.core.qr.domain.attribute.GroupAttributeValue;
import com.mryqr.core.qr.domain.attribute.IdentifierAttributeValue;
import com.mryqr.core.qr.domain.attribute.IntegerAttributeValue;
import com.mryqr.core.qr.domain.attribute.ItemStatusAttributeValue;
import com.mryqr.core.qr.domain.attribute.LocalDateAttributeValue;
import com.mryqr.core.qr.domain.attribute.LocalTimeAttributeValue;
import com.mryqr.core.qr.domain.attribute.MobileAttributeValue;
import com.mryqr.core.qr.domain.attribute.RadioAttributeValue;
import com.mryqr.core.qr.domain.attribute.TextAttributeValue;
import com.mryqr.core.qr.domain.attribute.TimestampAttributeValue;
import com.mryqr.core.qr.domain.event.QrActivatedEvent;
import com.mryqr.core.qr.domain.event.QrBaseSettingUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrCirculationStatusChangedEvent;
import com.mryqr.core.qr.domain.event.QrDeactivatedEvent;
import com.mryqr.core.qr.domain.event.QrDeletedEvent;
import com.mryqr.core.qr.domain.event.QrGroupChangedEvent;
import com.mryqr.core.qr.domain.event.QrMarkedAsTemplateEvent;
import com.mryqr.core.qr.domain.event.QrPlateResetEvent;
import com.mryqr.core.qr.domain.event.QrRenamedEvent;
import com.mryqr.core.qr.domain.event.QrUnMarkedAsTemplateEvent;
import com.mryqr.core.qr.query.QQrBaseSetting;
import com.mryqr.core.qr.query.QQrSummary;
import com.mryqr.core.qr.query.bindplate.QBindPlateInfo;
import com.mryqr.core.qr.query.list.ListViewableQrsQuery;
import com.mryqr.core.qr.query.list.QViewableListQr;
import com.mryqr.core.qr.query.plate.ListPlateAttributeValuesQuery;
import com.mryqr.core.qr.query.submission.QSubmissionAppDetail;
import com.mryqr.core.qr.query.submission.QSubmissionQr;
import com.mryqr.core.qr.query.submission.QSubmissionQrDetail;
import com.mryqr.core.qr.query.submission.QSubmissionQrMemberProfile;
import com.mryqr.core.qr.query.submission.list.ListQrSubmissionsQuery;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.address.AddressAnswer;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.email.EmailAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.itemcount.ItemCountAnswer;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import com.mryqr.core.submission.domain.answer.mobilenumber.MobileNumberAnswer;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelection;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelectionAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.core.submission.domain.answer.pointcheck.PointCheckAnswer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import com.mryqr.core.submission.query.list.QListSubmission;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.ResourceUsage;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.alibaba.excel.support.ExcelTypeEnum.XLSX;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.attribute.AttributeType.DIRECT_INPUT;
import static com.mryqr.core.app.domain.attribute.AttributeType.FIXED;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_ACTIVE_STATUS;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CIRCULATION_STATUS;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CREATE_DATE;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CREATE_TIME;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CREATOR;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CUSTOM_ID;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_GEOLOCATION;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_GROUP;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_GROUP_MANAGERS;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_GROUP_MANAGERS_AND_EMAIL;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_GROUP_MANAGERS_AND_MOBILE;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_NAME;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_PLATE_ID;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_SUBMIT_COUNT;
import static com.mryqr.core.app.domain.attribute.AttributeType.PAGE_LAST_SUBMITTER_AND_EMAIL;
import static com.mryqr.core.app.domain.attribute.AttributeType.PAGE_LAST_SUBMITTER_AND_MOBILE;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.ALL_SUBMIT_HISTORY;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.SUBMITTER_SUBMISSION;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.TO_BE_APPROVED;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.app.domain.plate.PlateTextValueType.FIXED_TEXT;
import static com.mryqr.core.app.domain.plate.PlateTextValueType.QR_ATTRIBUTE;
import static com.mryqr.core.app.domain.plate.control.PlateControlType.KEY_VALUE;
import static com.mryqr.core.app.domain.plate.control.PlateControlType.SINGLE_ROW_TEXT;
import static com.mryqr.core.app.domain.plate.control.PlateControlType.TABLE;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.CENTER;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.JUSTIFY;
import static com.mryqr.core.app.domain.ui.align.HorizontalPositionType.RIGHT;
import static com.mryqr.core.app.domain.ui.align.VerticalAlignType.MIDDLE;
import static com.mryqr.core.app.domain.ui.borderradius.BorderRadius.noBorderRadius;
import static com.mryqr.core.common.domain.ValueType.CIRCULATION_STATUS_VALUE;
import static com.mryqr.core.common.domain.ValueType.DOUBLE_VALUE;
import static com.mryqr.core.common.domain.ValueType.TEXT_VALUE;
import static com.mryqr.core.common.domain.event.DomainEventType.PLATE_BOUND;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_ACTIVATED;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_BASE_SETTING_UPDATED;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_CIRCULATION_STATUS_CHANGED;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_CREATED;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_DEACTIVATED;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_DELETED;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_GROUP_CHANGED;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_MARKED_AS_TEMPLATE;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_PLATE_RESET;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_RENAMED;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_UNMARKED_AS_TEMPLATE;
import static com.mryqr.core.common.domain.permission.Permission.AS_GROUP_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.AS_TENANT_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.core.common.domain.permission.Permission.PUBLIC;
import static com.mryqr.core.common.exception.ErrorCode.ACCESS_DENIED;
import static com.mryqr.core.common.exception.ErrorCode.APP_NOT_ACTIVE;
import static com.mryqr.core.common.exception.ErrorCode.BATCH_QR_IMPORT_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.CIRCULATION_OPTION_NOT_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_NOT_ACTIVE;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_PLATE_NOT_IN_SAME_APP;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_QR_NOT_SAME_APP;
import static com.mryqr.core.common.exception.ErrorCode.INVALID_QR_EXCEL;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.NO_RECORDS_FOR_QR_IMPORT;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_GROUPS;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PERMISSION_FOR_GROUP;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_ALREADY_BOUND;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_COUNT_LIMIT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_NOT_BOUND;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_NOT_EXIT_FOR_BOUND;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_NOT_FOR_APP;
import static com.mryqr.core.common.exception.ErrorCode.QRS_SHOULD_IN_ONE_APP;
import static com.mryqr.core.common.exception.ErrorCode.QR_COUNT_LIMIT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.QR_IMPORT_DUPLICATED_CUSTOM_ID;
import static com.mryqr.core.common.exception.ErrorCode.QR_NOT_ACTIVE;
import static com.mryqr.core.common.exception.ErrorCode.QR_WITH_CUSTOM_ID_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.QR_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.core.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.plan.domain.Plan.FREE_PLAN;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.core.submission.domain.ApprovalStatus.NONE;
import static com.mryqr.utils.RandomTestFixture.defaultAddressControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControl;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultDateControl;
import static com.mryqr.utils.RandomTestFixture.defaultDropdownControl;
import static com.mryqr.utils.RandomTestFixture.defaultDropdownControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultEmailControl;
import static com.mryqr.utils.RandomTestFixture.defaultFillableSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultIdentifierControl;
import static com.mryqr.utils.RandomTestFixture.defaultItemCountControl;
import static com.mryqr.utils.RandomTestFixture.defaultItemStatusControl;
import static com.mryqr.utils.RandomTestFixture.defaultItemStatusControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultMobileControl;
import static com.mryqr.utils.RandomTestFixture.defaultMultiLevelSelectionControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultNumberInputControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultNumberRankingControl;
import static com.mryqr.utils.RandomTestFixture.defaultPage;
import static com.mryqr.utils.RandomTestFixture.defaultPageApproveSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultPageBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultPageSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultPersonNameControl;
import static com.mryqr.utils.RandomTestFixture.defaultPointCheckControl;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControl;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultTimeControl;
import static com.mryqr.utils.RandomTestFixture.rAddress;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static com.mryqr.utils.RandomTestFixture.rBorder;
import static com.mryqr.utils.RandomTestFixture.rColor;
import static com.mryqr.utils.RandomTestFixture.rCustomId;
import static com.mryqr.utils.RandomTestFixture.rEmail;
import static com.mryqr.utils.RandomTestFixture.rFontStyle;
import static com.mryqr.utils.RandomTestFixture.rGeolocation;
import static com.mryqr.utils.RandomTestFixture.rGroupName;
import static com.mryqr.utils.RandomTestFixture.rImageFile;
import static com.mryqr.utils.RandomTestFixture.rMemberName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static com.mryqr.utils.RandomTestFixture.rPlateKeyName;
import static com.mryqr.utils.RandomTestFixture.rQrName;
import static com.mryqr.utils.RandomTestFixture.rSentence;
import static java.lang.Integer.parseInt;
import static java.time.LocalDate.ofInstant;
import static java.time.ZoneId.systemDefault;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QrControllerApiTest extends BaseApiTest {

    @Test
    public void should_create_qr() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String qrName = rQrName();
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), qrName, response.getDefaultGroupId());

        Member member = memberRepository.byId(response.getMemberId());
        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertEquals(qrResponse.getQrId(), qr.getId());
        assertEquals(member.getId(), qr.getCreatedBy());
        assertEquals(member.getName(), qr.getCreator());
        assertEquals(qrResponse.getAppId(), qr.getAppId());
        assertEquals(qrResponse.getGroupId(), qr.getGroupId());
        assertEquals(qrResponse.getPlateId(), qr.getPlateId());
        Plate plate = plateRepository.byId(qrResponse.getPlateId());
        assertTrue(plate.isBound());
        assertFalse(plate.isBatched());
        assertEquals(qrResponse.getQrId(), plate.getQrId());
        assertEquals(qrResponse.getAppId(), plate.getAppId());
        assertEquals(qrResponse.getGroupId(), plate.getGroupId());
    }

    @Test
    public void parent_group_manager_should_create_qr() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManager(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());

        String groupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());
        CreateQrResponse qr = QrApi.createQr(memberResponse.getJwt(), groupId);
        assertNotNull(qr);
    }

    @Test
    public void should_create_qr_with_init_circulation_status() {
        PreparedAppResponse response = setupApi.registerWithApp();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(option1.getId())
                .statusAfterSubmissions(List.of())
                .statusPermissions(List.of())
                .build();
        AppApi.updateCirculationStatusSetting(response.getJwt(), response.getAppId(), setting);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertEquals(option1.getId(), qr.getCirculationOptionId());
    }

    @Test
    public void should_raise_event_when_create_qr() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CREATE_TIME).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        QrCreatedEvent event = domainEventDao.latestEventFor(qrResponse.getQrId(), QR_CREATED, QrCreatedEvent.class);
        assertEquals(qrResponse.getQrId(), event.getQrId());
        assertEquals(qrResponse.getAppId(), event.getAppId());
        assertEquals(qrResponse.getGroupId(), event.getGroupId());
        assertEquals(qrResponse.getPlateId(), event.getPlateId());
        assertEquals(1, tenantRepository.byId(response.getTenantId()).getResourceUsage().getQrCountForApp(response.getAppId()));
        assertEquals(1, tenantRepository.byId(response.getTenantId()).getResourceUsage().getPlateCount());
        QR qr = qrRepository.byId(qrResponse.getQrId());
        TimestampAttributeValue attributeValue = (TimestampAttributeValue) qr.attributeValueOf(attribute.getId());
        assertEquals(qr.getCreatedAt(), attributeValue.getTimestamp());

        PlateBoundEvent plateBoundEvent = domainEventDao.latestEventFor(qrResponse.getPlateId(), PLATE_BOUND, PlateBoundEvent.class);
        assertEquals(qrResponse.getQrId(), plateBoundEvent.getQrId());
    }

    @Test
    public void should_fail_create_qr_if_name_already_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String qrName = rQrName();
        QrApi.createQr(response.getJwt(), qrName, response.getDefaultGroupId());

        CreateQrCommand command = CreateQrCommand.builder().name(qrName).groupId(response.getDefaultGroupId()).build();

        assertError(() -> QrApi.createQrRaw(response.getJwt(), command), QR_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_create_qr_if_qr_count_exceeds_packages_limit() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        tenant.setQrCountForApp(response.getAppId(), FREE_PLAN.getMaxQrCount());
        tenantRepository.save(tenant);

        CreateQrCommand command = CreateQrCommand.builder().name(rQrName()).groupId(response.getDefaultGroupId()).build();

        assertError(() -> QrApi.createQrRaw(response.getJwt(), command), QR_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_fail_create_qr_if_plage_count_exceeds_packages_limit() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        tenant.setPlateCount(PackagesStatus.MAX_PLATE_SIZE);
        tenantRepository.save(tenant);

        CreateQrCommand command = CreateQrCommand.builder().name(rQrName()).groupId(response.getDefaultGroupId()).build();

        assertError(() -> QrApi.createQrRaw(response.getJwt(), command), PLATE_COUNT_LIMIT_REACHED);
    }

    @Test
    public void normal_member_should_fail_create_qr() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        CreateQrCommand command = CreateQrCommand.builder().name(rQrName()).groupId(response.getDefaultGroupId()).build();

        assertError(() -> QrApi.createQrRaw(memberResponse.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void should_fail_create_qr_if_group_is_not_active() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        GroupApi.deactivateGroup(response.getJwt(), groupId);

        CreateQrCommand command = CreateQrCommand.builder().name(rQrName()).groupId(groupId).build();

        assertError(() -> QrApi.createQrRaw(response.getJwt(), command), GROUP_NOT_ACTIVE);
    }

    @Test
    public void should_create_qr_from_plate() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();

        CreateQrResponse createQrResponse = QrApi.createQrFromPlate(response.getJwt(), rQrName(), response.getDefaultGroupId(), plateId);

        QR qr = qrRepository.byId(createQrResponse.getQrId());
        assertEquals(plateId, qr.getPlateId());
        Plate plate = plateRepository.byId(plateId);
        assertTrue(plate.isBound());
        assertEquals(createQrResponse.getQrId(), plate.getQrId());
        assertEquals(createQrResponse.getGroupId(), plate.getGroupId());
    }

    @Test
    public void should_raise_event_when_create_qr_from_plate() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();

        CreateQrResponse qrResponse = QrApi.createQrFromPlate(response.getJwt(), rQrName(), response.getDefaultGroupId(), plateId);
        QrCreatedEvent event = domainEventDao.latestEventFor(qrResponse.getQrId(), QR_CREATED, QrCreatedEvent.class);
        assertEquals(qrResponse.getQrId(), event.getQrId());
        assertEquals(qrResponse.getAppId(), event.getAppId());
        assertEquals(qrResponse.getGroupId(), event.getGroupId());
        assertEquals(qrResponse.getPlateId(), event.getPlateId());
        PlateBoundEvent plateBoundEvent = domainEventDao.latestEventFor(qrResponse.getPlateId(), PLATE_BOUND, PlateBoundEvent.class);
        assertEquals(qrResponse.getQrId(), plateBoundEvent.getQrId());
        assertEquals(9, plateBatchRepository.byId(plateBatchId).getAvailableCount());
    }

    @Test
    public void should_fail_create_qr_from_plate_if_group_not_with_same_app_with_plate() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateAppResponse anotherResponse = AppApi.createApp(response.getJwt());
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();

        CreateQrFromPlateCommand command = CreateQrFromPlateCommand.builder().plateId(plateId).name(rQrName()).groupId(anotherResponse.getDefaultGroupId()).build();

        assertError(() -> QrApi.createQrFromPlateRaw(response.getJwt(), command), GROUP_PLATE_NOT_IN_SAME_APP);
    }

    @Test
    public void should_fail_create_qr_from_plate_if_plate_already_bound() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        CreateQrFromPlateCommand command = CreateQrFromPlateCommand.builder().plateId(qrResponse.getPlateId()).name(rQrName()).groupId(response.getDefaultGroupId()).build();

        assertError(() -> QrApi.createQrFromPlateRaw(response.getJwt(), command), PLATE_ALREADY_BOUND);
    }

    @Test
    public void should_failed_create_qr_from_template_if_name_already_exists() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();
        String qrName = rQrName();
        QrApi.createQr(response.getJwt(), qrName, response.getDefaultGroupId());

        CreateQrFromPlateCommand command = CreateQrFromPlateCommand.builder().plateId(plateId).name(qrName).groupId(response.getDefaultGroupId()).build();

        assertError(() -> QrApi.createQrFromPlateRaw(response.getJwt(), command), QR_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_import_qrs_via_excel() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        Page homePage = defaultPageBuilder().setting(defaultPageSettingBuilder().submitType(ONCE_PER_INSTANCE).build()).build();
        Page childPage = defaultPageBuilder().setting(defaultPageSettingBuilder().submitType(ONCE_PER_INSTANCE).build()).build();

        FCheckboxControl checkboxControl = defaultCheckboxControlBuilder().options(sampleTextOptions()).build();
        Attribute checkboxAttribute = Attribute.builder().name("Checkbox").id(newAttributeId()).type(CONTROL_LAST).pageId(homePage.getId()).controlId(checkboxControl.getId()).range(NO_LIMIT).build();

        FRadioControl radioControl = defaultRadioControlBuilder().options(sampleTextOptions()).build();
        Attribute radioAttribute = Attribute.builder().name("Radio").id(newAttributeId()).type(CONTROL_LAST).pageId(homePage.getId()).controlId(radioControl.getId()).range(NO_LIMIT).build();

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        Attribute singleTextAttribute = Attribute.builder().name("Text").id(newAttributeId()).type(CONTROL_LAST).pageId(homePage.getId()).controlId(singleLineTextControl.getId()).range(NO_LIMIT).build();

        FDropdownControl dropdownControl = defaultDropdownControlBuilder().options(sampleTextOptions()).build();
        Attribute dropdownAttribute = Attribute.builder().name("Dropdown").id(newAttributeId()).type(CONTROL_LAST).pageId(homePage.getId()).controlId(dropdownControl.getId()).range(NO_LIMIT).build();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        Attribute numberInputAttribute = Attribute.builder().name("Number").id(newAttributeId()).type(CONTROL_LAST).pageId(homePage.getId()).controlId(numberInputControl.getId()).range(NO_LIMIT).build();

        FNumberRankingControl numberRankingControl = defaultNumberRankingControl();
        Attribute numberRankingAttribute = Attribute.builder().name("Ranking").id(newAttributeId()).type(CONTROL_LAST).pageId(homePage.getId()).controlId(numberRankingControl.getId()).range(NO_LIMIT).build();

        homePage.getControls().addAll(List.of(checkboxControl, radioControl, singleLineTextControl, dropdownControl, numberInputControl, numberRankingControl));

        FMobileNumberControl mobileNumberControl = defaultMobileControl();
        Attribute mobileNumberAttribute = Attribute.builder().name("Mobile").id(newAttributeId()).type(CONTROL_LAST).pageId(childPage.getId()).controlId(mobileNumberControl.getId()).range(NO_LIMIT).build();

        FIdentifierControl identifierControl = defaultIdentifierControl();
        Attribute identifierAttribute = Attribute.builder().name("Identifier").id(newAttributeId()).type(CONTROL_LAST).pageId(childPage.getId()).controlId(identifierControl.getId()).range(NO_LIMIT).build();

        FPersonNameControl personNameControl = defaultPersonNameControl();
        Attribute personNameAttribute = Attribute.builder().name("PersonName").id(newAttributeId()).type(CONTROL_LAST).pageId(childPage.getId()).controlId(personNameControl.getId()).range(NO_LIMIT).build();

        FEmailControl emailControl = defaultEmailControl();
        Attribute emailAttribute = Attribute.builder().name("Email").id(newAttributeId()).type(CONTROL_LAST).pageId(childPage.getId()).controlId(emailControl.getId()).range(NO_LIMIT).build();

        FDateControl dateControl = defaultDateControl();
        Attribute dateAttribute = Attribute.builder().name("Date").id(newAttributeId()).type(CONTROL_LAST).pageId(childPage.getId()).controlId(dateControl.getId()).range(NO_LIMIT).build();

        FTimeControl timeControl = defaultTimeControl();
        Attribute timeAttribute = Attribute.builder().name("Time").id(newAttributeId()).type(CONTROL_LAST).pageId(childPage.getId()).controlId(timeControl.getId()).range(NO_LIMIT).build();

        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder().options(sampleTextOptions()).build();
        Attribute itemStatusAttribute = Attribute.builder().name("ItemStatus").id(newAttributeId()).type(CONTROL_LAST).pageId(childPage.getId()).controlId(itemStatusControl.getId()).range(NO_LIMIT).build();

        childPage.getControls().addAll(List.of(mobileNumberControl, identifierControl, personNameControl, emailControl, dateControl, timeControl, itemStatusControl));
        AppApi.updateAppPages(response.getJwt(), response.getAppId(), homePage, childPage);

        Attribute directTextAttribute = Attribute.builder().id(newAttributeId()).name("直接文本属性").type(DIRECT_INPUT).valueType(TEXT_VALUE).build();
        Attribute directNumberAttribute = Attribute.builder().id(newAttributeId()).name("直接数字属性").type(DIRECT_INPUT).valueType(DOUBLE_VALUE).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(),
                checkboxAttribute, radioAttribute, dropdownAttribute, singleTextAttribute, numberInputAttribute, numberRankingAttribute,
                mobileNumberAttribute, identifierAttribute, personNameAttribute, emailAttribute, dateAttribute, timeAttribute, itemStatusAttribute,
                directTextAttribute, directNumberAttribute);

        ClassPathResource resource = new ClassPathResource("testdata/qr/normal-qrs-import.xlsx");
        QrImportResponse importResponse = QrApi.importQrExcel(response.getJwt(), response.getDefaultGroupId(), resource.getFile());

        assertEquals(6, importResponse.getReadCount());
        assertEquals(3, importResponse.getImportedCount());
        assertEquals(3, importResponse.getErrorRecords().size());
        assertEquals(4, submissionRepository.count(response.getTenantId()));
        assertEquals(3, qrRepository.count(response.getTenantId()));

        QrImportResponse.QrImportErrorRecord nameEmptyErrorRecord = importResponse.getErrorRecords().get(0);
        assertTrue(nameEmptyErrorRecord.getErrors().contains("名称不能为空"));
        assertEquals(4, nameEmptyErrorRecord.getRowIndex());

        QrImportResponse.QrImportErrorRecord noCustomIdErrorRecord = importResponse.getErrorRecords().get(1);
        assertTrue(noCustomIdErrorRecord.getErrors().contains("自定义编号不能为空"));
        assertEquals(5, noCustomIdErrorRecord.getRowIndex());

        QrImportResponse.QrImportErrorRecord mobileFormatErrorRecord = importResponse.getErrorRecords().get(2);
        assertTrue(mobileFormatErrorRecord.getErrors().contains("Mobile格式错误"));
        assertEquals(7, mobileFormatErrorRecord.getRowIndex());

        QR qr = qrRepository.byCustomId(response.getAppId(), "qr-custom-id-1");
        assertEquals("qr1", qr.getName());
        assertEquals("qr-custom-id-1", qr.getCustomId());

        TextAttributeValue directTextAttributeValue = (TextAttributeValue) qr.attributeValueOf(directTextAttribute.getId());
        assertEquals("text1", directTextAttributeValue.getText());

        DoubleAttributeValue directNumberAttributeValue = (DoubleAttributeValue) qr.attributeValueOf(directNumberAttribute.getId());
        assertEquals(123, directNumberAttributeValue.getNumber());

        ItemStatusAttributeValue itemStatusAttributeValue = (ItemStatusAttributeValue) qr.attributeValueOf(itemStatusAttribute.getId());
        assertTrue(itemStatusControl.allOptionIds().contains(itemStatusAttributeValue.getOptionId()));

        LocalTimeAttributeValue timeAttributeValue = (LocalTimeAttributeValue) qr.attributeValueOf(timeAttribute.getId());
        assertEquals("12:00", timeAttributeValue.getTime());

        LocalDateAttributeValue dateAttributeValue = (LocalDateAttributeValue) qr.attributeValueOf(dateAttribute.getId());
        assertEquals("2001-10-23", dateAttributeValue.getDate());

        EmailAttributeValue emailAttributeValue = (EmailAttributeValue) qr.attributeValueOf(emailAttribute.getId());
        assertEquals("bob@mryqr.com", emailAttributeValue.getEmail());

        IdentifierAttributeValue personNameAttributeValue = (IdentifierAttributeValue) qr.attributeValueOf(personNameAttribute.getId());
        assertEquals("Alice", personNameAttributeValue.getContent());

        IdentifierAttributeValue identifierAttributeValue = (IdentifierAttributeValue) qr.attributeValueOf(identifierAttribute.getId());
        assertEquals("abcd", identifierAttributeValue.getContent());

        MobileAttributeValue mobileAttributeValue = (MobileAttributeValue) qr.attributeValueOf(mobileNumberAttribute.getId());
        assertEquals("15000000000", mobileAttributeValue.getMobile());

        DoubleAttributeValue numberInputAttributeValue = (DoubleAttributeValue) qr.attributeValueOf(numberInputAttribute.getId());
        assertEquals(100, numberInputAttributeValue.getNumber());

        IntegerAttributeValue numberRankingAttributeValue = (IntegerAttributeValue) qr.attributeValueOf(numberRankingAttribute.getId());
        assertEquals(2, numberRankingAttributeValue.getNumber());

        TextAttributeValue singleTextAttributeValue = (TextAttributeValue) qr.attributeValueOf(singleTextAttribute.getId());
        assertEquals("abcd", singleTextAttributeValue.getText());

        CheckboxAttributeValue checkboxAttributeValue = (CheckboxAttributeValue) qr.attributeValueOf(checkboxAttribute.getId());
        assertTrue(checkboxControl.allOptionIds().containsAll(checkboxAttributeValue.getOptionIds()));
        assertEquals(3, checkboxAttributeValue.getOptionIds().size());

        DropdownAttributeValue dropdownAttributeValue = (DropdownAttributeValue) qr.attributeValueOf(dropdownAttribute.getId());
        assertTrue(dropdownControl.allOptionIds().containsAll(dropdownAttributeValue.getOptionIds()));

        RadioAttributeValue radioAttributeValue = (RadioAttributeValue) qr.attributeValueOf(radioAttribute.getId());
        assertTrue(radioControl.allOptionIds().contains(radioAttributeValue.getOptionId()));

        QR qr3 = qrRepository.byCustomId(response.getAppId(), "qr-custom-id-3");
        assertTrue(qr3.getAttributeValues().isEmpty());

        QR qr6 = qrRepository.byCustomId(response.getAppId(), "qr-custom-id-6");
        assertNull(qr6.attributeValueOf(itemStatusAttribute.getId()));
        assertNotNull(qr6.attributeValueOf(radioAttribute.getId()));
    }

    private static List<TextOption> sampleTextOptions() {
        return IntStream.range(0, 10).mapToObj(value -> TextOption.builder()
                        .id(newShortUuid())
                        .color(rColor())
                        .name("选项" + value)
                        .build())
                .collect(toList());
    }

    @Test
    public void should_skip_already_exists_custom_id() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        ClassPathResource resource = new ClassPathResource("testdata/qr/simple-qrs-import.xlsx");
        QrApi.importQrExcel(response.getJwt(), response.getDefaultGroupId(), resource.getFile());
        QrImportResponse importResponse = QrApi.importQrExcel(response.getJwt(), response.getDefaultGroupId(), resource.getFile());
        assertEquals(1, importResponse.getReadCount());
        assertEquals(0, importResponse.getImportedCount());
        assertEquals(1, importResponse.getErrorRecords().size());
        assertTrue(importResponse.getErrorRecords().get(0).getErrors().contains("自定义编号已经存在"));
    }

    @Test
    public void should_fail_import_qr_excel_if_package_too_low() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();
        ClassPathResource resource = new ClassPathResource("testdata/qr/simple-qrs-import.xlsx");
        File file = resource.getFile();

        assertError(() -> QrApi.importQrExcelRaw(response.getJwt(), response.getDefaultGroupId(), file), BATCH_QR_IMPORT_NOT_ALLOWED);
    }

    @Test
    public void should_fail_import_qr_excel_if_wrong_format() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        ClassPathResource resource = new ClassPathResource("testdata/qr/a-text-file.txt");
        File file = resource.getFile();
        assertError(() -> QrApi.importQrExcelRaw(response.getJwt(), response.getDefaultGroupId(), file), INVALID_QR_EXCEL);
    }

    @Test
    public void should_fail_import_qrs_excel_if_max_qr_count_reached() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        ResourceUsage resourceUsage = tenant.getResourceUsage();
        ReflectionTestUtils.setField(resourceUsage, "qrCountPerApp", Map.of(response.getAppId(), 10000000));
        tenantRepository.save(tenant);

        ClassPathResource resource = new ClassPathResource("testdata/qr/simple-qrs-import.xlsx");
        File file = resource.getFile();
        assertError(() -> QrApi.importQrExcelRaw(response.getJwt(), response.getDefaultGroupId(), file), QR_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_fail_import_qrs_if_no_name_field() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        ClassPathResource resource = new ClassPathResource("testdata/qr/no-name-qrs-import.xlsx");
        File file = resource.getFile();
        assertError(() -> QrApi.importQrExcelRaw(response.getJwt(), response.getDefaultGroupId(), file), INVALID_QR_EXCEL);
    }

    @Test
    public void should_fail_import_qrs_if_no_custom_id_field() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        ClassPathResource resource = new ClassPathResource("testdata/qr/no-custom-id-qrs-import.xlsx");
        File file = resource.getFile();
        assertError(() -> QrApi.importQrExcelRaw(response.getJwt(), response.getDefaultGroupId(), file), INVALID_QR_EXCEL);
    }

    @Test
    public void should_fail_import_qrs_if_no_records() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        ClassPathResource resource = new ClassPathResource("testdata/qr/no-record-qrs-import.xlsx");
        File file = resource.getFile();
        assertError(() -> QrApi.importQrExcelRaw(response.getJwt(), response.getDefaultGroupId(), file), NO_RECORDS_FOR_QR_IMPORT);
    }

    @Test
    public void should_fail_import_qrs_if_custom_id_duplicates() throws IOException {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        ClassPathResource resource = new ClassPathResource("testdata/qr/duplicated-qrs-import.xlsx");
        File file = resource.getFile();
        assertError(() -> QrApi.importQrExcelRaw(response.getJwt(), response.getDefaultGroupId(), file), QR_IMPORT_DUPLICATED_CUSTOM_ID);
    }

    @Test
    public void should_rename_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String qrName = rQrName();
        QrApi.renameQr(response.getJwt(), response.getQrId(), qrName);

        QR qr = qrRepository.byId(response.getQrId());
        assertEquals(qrName, qr.getName());
    }

    @Test
    public void should_raise_event_when_rename_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_NAME).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        String qrName = rQrName();
        QrApi.renameQr(response.getJwt(), response.getQrId(), qrName);

        QrRenamedEvent qrRenamedEvent = domainEventDao.latestEventFor(response.getQrId(), QR_RENAMED, QrRenamedEvent.class);
        assertEquals(response.getQrId(), qrRenamedEvent.getQrId());
        TextAttributeValue attributeValue = (TextAttributeValue) qrRepository.byId(response.getQrId()).attributeValueOf(attribute.getId());
        assertEquals(qrName, attributeValue.getText());
    }

    @Test
    public void should_fail_rename_qr_if_name_already_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String qrName = rQrName();
        QrApi.createQr(response.getJwt(), qrName, response.getDefaultGroupId());

        RenameQrCommand command = RenameQrCommand.builder().name(qrName).build();
        assertError(() -> QrApi.renameQrRaw(response.getJwt(), response.getQrId(), command), QR_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_reset_qr_plate() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();

        QrApi.resetPlate(response.getJwt(), response.getQrId(), plateId);

        QR qr = qrRepository.byId(response.getQrId());
        assertEquals(plateId, qr.getPlateId());
        Plate oldPlate = plateRepository.byId(response.getPlateId());
        assertFalse(oldPlate.isBound());
        Plate newPlate = plateRepository.byId(plateId);
        assertTrue(newPlate.isBound());
        assertEquals(response.getQrId(), newPlate.getQrId());
        assertEquals(response.getDefaultGroupId(), newPlate.getGroupId());
        PlateBatch plateBatch = plateBatchRepository.byId(plateBatchId);
        assertEquals(9, plateBatch.getAvailableCount());
    }

    @Test
    public void should_raise_event_when_reset_qr_plate() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        SingleLineTextAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_PLATE_ID).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);
        String oldPlateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String oldPlateId = plateRepository.allPlateIdsUnderPlateBatch(oldPlateBatchId).stream().findAny().get();
        String newPlateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String newPlateId = plateRepository.allPlateIdsUnderPlateBatch(newPlateBatchId).stream().findAny().get();
        QrApi.resetPlate(response.getJwt(), response.getQrId(), oldPlateId);

        QrApi.resetPlate(response.getJwt(), response.getQrId(), newPlateId);

        QrPlateResetEvent qrPlateResetEvent = domainEventDao.latestEventFor(response.getQrId(), QR_PLATE_RESET, QrPlateResetEvent.class);
        assertEquals(newPlateId, qrPlateResetEvent.getNewPlateId());
        assertEquals(oldPlateId, qrPlateResetEvent.getOldPlateId());
        assertEquals(newPlateId, submissionRepository.byId(submissionId).getPlateId());
        QR qr = qrRepository.byId(response.getQrId());
        IdentifierAttributeValue attributeValue = (IdentifierAttributeValue) qr.attributeValueOf(attribute.getId());
        assertEquals(newPlateId, attributeValue.getContent());
        assertEquals(10, plateBatchRepository.byId(oldPlateBatchId).getAvailableCount());
        assertEquals(9, plateBatchRepository.byId(newPlateBatchId).getAvailableCount());
    }

    @Test
    public void should_fail_reset_plate_if_plate_not_exist() {
        PreparedQrResponse response = setupApi.registerWithQr();

        ResetQrPlateCommand command = ResetQrPlateCommand.builder().plateId(Plate.newPlateId()).build();

        assertError(() -> QrApi.resetPlateRaw(response.getJwt(), response.getQrId(), command), PLATE_NOT_EXIT_FOR_BOUND);
    }

    @Test
    public void should_fail_reset_plate_if_plate_belongs_to_another_app() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateAppResponse appResponse = AppApi.createApp(response.getJwt());
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), appResponse.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();

        ResetQrPlateCommand command = ResetQrPlateCommand.builder().plateId(plateId).build();

        assertError(() -> QrApi.resetPlateRaw(response.getJwt(), response.getQrId(), command), PLATE_NOT_FOR_APP);
    }

    @Test
    public void should_reset_circulation_status() {
        PreparedQrResponse response = setupApi.registerWithQr();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .statusAfterSubmissions(List.of())
                .statusPermissions(List.of())
                .build();
        AppApi.updateCirculationStatusSetting(response.getJwt(), response.getAppId(), setting);

        assertNull(qrRepository.byId(response.getQrId()).getCirculationOptionId());
        QrApi.resetCirculationStatus(response.getJwt(), response.getQrId(), option1.getId());
        assertEquals(option1.getId(), qrRepository.byId(response.getQrId()).getCirculationOptionId());

        assertError(() -> QrApi.resetCirculationStatusRaw(response.getJwt(), response.getQrId(), newShortUuid()), CIRCULATION_OPTION_NOT_EXISTS);
    }

    @Test
    public void reset_circulation_status_should_also_raise_event() {
        PreparedQrResponse response = setupApi.registerWithQr();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .statusAfterSubmissions(List.of())
                .statusPermissions(List.of())
                .build();
        AppApi.updateCirculationStatusSetting(response.getJwt(), response.getAppId(), setting);

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CIRCULATION_STATUS).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        QrApi.resetCirculationStatus(response.getJwt(), response.getQrId(), option1.getId());
        QrCirculationStatusChangedEvent theEvent = domainEventDao.latestEventFor(response.getQrId(), QR_CIRCULATION_STATUS_CHANGED, QrCirculationStatusChangedEvent.class);
        assertEquals(response.getQrId(), theEvent.getQrId());

        QR qr = qrRepository.byId(response.getQrId());
        CirculationStatusAttributeValue attributeValue = (CirculationStatusAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_CIRCULATION_STATUS, attributeValue.getAttributeType());
        assertEquals(CIRCULATION_STATUS_VALUE, attributeValue.getValueType());
        assertEquals(option1.getId(), attributeValue.getOptionId());
    }

    @Test
    public void should_delete_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();

        QrApi.deleteQr(response.getJwt(), response.getQrId());

        assertFalse(qrRepository.byIdOptional(response.getQrId()).isPresent());
    }

    @Test
    public void should_raise_event_when_delete_qr() {
        PreparedAppResponse response = setupApi.registerWithApp();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();
        CreateQrResponse qrResponse = QrApi.createQrFromPlate(response.getJwt(), rQrName(), response.getDefaultGroupId(), plateId);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), rAnswer(control));

        QrApi.deleteQr(response.getJwt(), qrResponse.getQrId());

        QrDeletedEvent qrDeletedEvent = domainEventDao.latestEventFor(qrResponse.getQrId(), QR_DELETED, QrDeletedEvent.class);
        assertEquals(qrResponse.getQrId(), qrDeletedEvent.getQrId());
        assertEquals(response.getAppId(), qrDeletedEvent.getAppId());
        assertEquals(qrResponse.getPlateId(), qrDeletedEvent.getPlateId());
        assertEquals(qrResponse.getGroupId(), qrDeletedEvent.getGroupId());
        assertFalse(submissionRepository.byIdOptional(submissionId).isPresent());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(0, tenant.getResourceUsage().getQrCountForApp(response.getAppId()));
        assertEquals(0, tenant.getResourceUsage().getSubmissionCountForApp(response.getAppId()));
    }

    @Test
    public void should_batch_delete_qrs() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateQrResponse qrResponse1 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        QrApi.deleteQrs(response.getJwt(), qrResponse1.getQrId(), qrResponse2.getQrId());

        assertFalse(qrRepository.byIdOptional(qrResponse1.getQrId()).isPresent());
        assertFalse(qrRepository.byIdOptional(qrResponse2.getQrId()).isPresent());
    }

    @Test
    public void should_fail_batch_delete_qrs_if_qrs_not_under_same_app() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateQrResponse qrResponse1 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateAppResponse appResponse = AppApi.createApp(response.getJwt());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.getJwt(), appResponse.getDefaultGroupId());

        DeleteQrsCommand command = DeleteQrsCommand.builder().qrIds(newHashSet(qrResponse1.getQrId(), qrResponse2.getQrId())).build();

        assertError(() -> QrApi.deleteQrsRaw(response.getJwt(), command), QRS_SHOULD_IN_ONE_APP);
    }

    @Test
    public void should_change_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateQrResponse qrResponse1 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        String groupId = GroupApi.createGroup(response.getJwt(), response.getAppId(), rGroupName());

        QrApi.changeQrsGroup(response.getJwt(), groupId, qrResponse1.getQrId(), qrResponse2.getQrId());

        assertEquals(groupId, qrRepository.byId(qrResponse1.getQrId()).getGroupId());
        assertEquals(groupId, qrRepository.byId(qrResponse2.getQrId()).getGroupId());
    }

    @Test
    public void should_raise_event_when_change_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String groupId = GroupApi.createGroup(response.getJwt(), response.getAppId(), rGroupName());


        QrApi.changeQrsGroup(response.getJwt(), groupId, response.getQrId());

        QrGroupChangedEvent qrGroupChangedEvent = domainEventDao.latestEventFor(response.getQrId(), QR_GROUP_CHANGED, QrGroupChangedEvent.class);
        assertEquals(response.getQrId(), qrGroupChangedEvent.getQrId());
        assertEquals(response.getDefaultGroupId(), qrGroupChangedEvent.getOldGroupId());
        assertEquals(groupId, qrGroupChangedEvent.getNewGroupId());
        assertEquals(groupId, submissionRepository.byId(submissionId).getGroupId());
        assertEquals(groupId, plateRepository.byId(response.getPlateId()).getGroupId());
        GroupAttributeValue attributeValue = (GroupAttributeValue) qrRepository.byId(response.getQrId()).attributeValueOf(attribute.getId());
        assertEquals(groupId, attributeValue.getGroupId());
    }

    @Test
    public void should_fail_change_group_if_qrs_not_in_same_app() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        CreateAppResponse appResponse1 = AppApi.createApp(response.getJwt());
        CreateQrResponse qrResponse1 = QrApi.createQr(response.getJwt(), appResponse1.getDefaultGroupId());
        CreateAppResponse appResponse2 = AppApi.createApp(response.getJwt());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.getJwt(), appResponse2.getDefaultGroupId());

        ChangeQrsGroupCommand command = ChangeQrsGroupCommand.builder().groupId(response.getDefaultGroupId()).qrIds(newHashSet(qrResponse1.getQrId(), qrResponse2.getQrId())).build();

        assertError(() -> QrApi.changeQrsGroupRaw(response.getJwt(), command), QRS_SHOULD_IN_ONE_APP);
    }

    @Test
    public void should_fail_change_group_if_qr_group_not_in_same_app() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateAppResponse appResponse = AppApi.createApp(response.getJwt());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), appResponse.getDefaultGroupId());

        ChangeQrsGroupCommand command = ChangeQrsGroupCommand.builder().groupId(response.getDefaultGroupId()).qrIds(newHashSet(qrResponse.getQrId())).build();

        assertError(() -> QrApi.changeQrsGroupRaw(response.getJwt(), command), GROUP_QR_NOT_SAME_APP);
    }

    @Test
    public void should_mark_qr_as_template() {
        PreparedQrResponse response = setupApi.registerWithQr();

        QrApi.markTemplate(response.getJwt(), response.getQrId());

        assertTrue(qrRepository.byId(response.getQrId()).isTemplate());
    }

    @Test
    public void should_raise_event_when_mark_as_template() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QrApi.markTemplate(response.getJwt(), response.getQrId());

        QrMarkedAsTemplateEvent qrMarkedAsTemplateEvent = domainEventDao.latestEventFor(response.getQrId(), QR_MARKED_AS_TEMPLATE, QrMarkedAsTemplateEvent.class);
        assertEquals(response.getQrId(), qrMarkedAsTemplateEvent.getQrId());
        assertFalse(submissionRepository.byIdOptional(submissionId).isPresent());
        assertEquals(0, tenantRepository.byId(response.getTenantId()).getResourceUsage().getSubmissionCountForApp(response.getAppId()));
    }

    @Test
    public void should_unmark_qr_as_template() {
        PreparedQrResponse response = setupApi.registerWithQr();
        QrApi.markTemplate(response.getJwt(), response.getQrId());

        QrApi.unmarkTemplate(response.getJwt(), response.getQrId());

        assertFalse(qrRepository.byId(response.getQrId()).isTemplate());
        QrUnMarkedAsTemplateEvent event = domainEventDao.latestEventFor(response.getQrId(), QR_UNMARKED_AS_TEMPLATE, QrUnMarkedAsTemplateEvent.class);
        assertEquals(response.getQrId(), event.getQrId());
    }

    @Test
    public void should_deactivate_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        QrApi.deactivate(response.getJwt(), response.getQrId());

        QR qr = qrRepository.byId(response.getQrId());
        assertFalse(qr.isActive());
        QrDeactivatedEvent event = domainEventDao.latestEventFor(response.getQrId(), QR_DEACTIVATED, QrDeactivatedEvent.class);
        assertEquals(response.getQrId(), event.getQrId());
    }

    @Test
    public void should_activate_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        QrApi.deactivate(response.getJwt(), response.getQrId());
        assertFalse(qrRepository.byId(response.getQrId()).isActive());

        QrApi.activate(response.getJwt(), response.getQrId());
        assertTrue(qrRepository.byId(response.getQrId()).isActive());
        QrActivatedEvent event = domainEventDao.latestEventFor(response.getQrId(), QR_ACTIVATED, QrActivatedEvent.class);
        assertEquals(response.getQrId(), event.getQrId());
    }

    @Test
    public void should_update_qr_base_setting() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        UpdateQrBaseSettingCommand command = UpdateQrBaseSettingCommand.builder()
                .name(rQrName())
                .description(rSentence(100))
                .headerImage(rImageFile())
                .manualAttributeValues(Map.of(attribute.getId(), "hello"))
                .geolocation(rGeolocation())
                .customId(rCustomId())
                .build();
        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(), command);

        QR qr = qrRepository.byId(response.getQrId());
        assertEquals(command.getName(), qr.getName());
        assertEquals(command.getDescription(), qr.getDescription());
        assertEquals(command.getHeaderImage(), qr.getHeaderImage());
        assertEquals(command.getGeolocation(), qr.getGeolocation());
        assertEquals(command.getCustomId(), qr.getCustomId());
        TextAttributeValue attributeValue = (TextAttributeValue) qr.attributeValueOf(attribute.getId());
        assertEquals("hello", attributeValue.getText());
    }

    @Test
    public void should_raise_event_when_update_base_setting() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        String qrId = response.getQrId();

        Attribute instanceNameAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(INSTANCE_NAME).build();
        Attribute instanceCustomIdAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(INSTANCE_CUSTOM_ID).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), instanceCustomIdAttribute, instanceNameAttribute);

        String qrName = rQrName();
        String customId = rCustomId();
        UpdateQrBaseSettingCommand command = UpdateQrBaseSettingCommand.builder()
                .name(qrName)
                .description(rSentence(100))
                .headerImage(rImageFile())
                .manualAttributeValues(newHashMap())
                .geolocation(rGeolocation())
                .customId(customId)
                .build();

        QrApi.updateQrBaseSetting(response.getJwt(), qrId, command);

        QrBaseSettingUpdatedEvent event = domainEventDao.latestEventFor(qrId, QR_BASE_SETTING_UPDATED, QrBaseSettingUpdatedEvent.class);
        assertEquals(qrId, event.getQrId());

        QR qr = qrRepository.byId(qrId);
        TextAttributeValue nameAttributeValue = (TextAttributeValue) qr.attributeValueOf(instanceNameAttribute.getId());
        assertEquals(qrName, nameAttributeValue.getText());

        IdentifierAttributeValue customIdAttributeValue = (IdentifierAttributeValue) qr.attributeValueOf(instanceCustomIdAttribute.getId());
        assertEquals(customId, customIdAttributeValue.getContent());
    }

    @Test
    public void should_update_qr_base_setting_with_number_direct_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).precision(2).manualInput(true).valueType(DOUBLE_VALUE).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        UpdateQrBaseSettingCommand command = UpdateQrBaseSettingCommand.builder()
                .name(rQrName())
                .description(rSentence(100))
                .headerImage(rImageFile())
                .manualAttributeValues(Map.of(attribute.getId(), "12.343"))
                .geolocation(rGeolocation())
                .customId(rCustomId())
                .build();
        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(), command);

        QR qr = qrRepository.byId(response.getQrId());

        DoubleAttributeValue attributeValue = (DoubleAttributeValue) qr.attributeValueOf(attribute.getId());
        assertEquals(12.34, attributeValue.getNumber());
    }

    @Test
    public void should_fail_update_base_setting_if_name_already_occupied() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        String name = rQrName();
        UpdateQrBaseSettingCommand command = UpdateQrBaseSettingCommand.builder()
                .name(name)
                .description(rSentence(100))
                .headerImage(rImageFile())
                .manualAttributeValues(newHashMap())
                .geolocation(rGeolocation())
                .customId(rCustomId())
                .build();
        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(), command);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        UpdateQrBaseSettingCommand newCommand = UpdateQrBaseSettingCommand.builder()
                .name(name)
                .description(rSentence(100))
                .headerImage(rImageFile())
                .manualAttributeValues(newHashMap())
                .geolocation(rGeolocation())
                .customId(rCustomId())
                .build();
        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(), newCommand);
        assertError(() -> QrApi.updateQrBaseSettingRaw(response.getJwt(), qrResponse.getQrId(), command), QR_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_update_base_setting_if_custom_id_already_occupied() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        String customId = rCustomId();
        UpdateQrBaseSettingCommand command = UpdateQrBaseSettingCommand.builder()
                .name(rQrName())
                .description(rSentence(100))
                .headerImage(rImageFile())
                .manualAttributeValues(newHashMap())
                .geolocation(rGeolocation())
                .customId(customId)
                .build();
        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(), command);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        UpdateQrBaseSettingCommand newCommand = UpdateQrBaseSettingCommand.builder()
                .name(rQrName())
                .description(rSentence(100))
                .headerImage(rImageFile())
                .manualAttributeValues(newHashMap())
                .geolocation(rGeolocation())
                .customId(customId)
                .build();
        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(), newCommand);
        assertError(() -> QrApi.updateQrBaseSettingRaw(response.getJwt(), qrResponse.getQrId(), command), QR_WITH_CUSTOM_ID_ALREADY_EXISTS);
    }

    @Test
    public void should_list_qrs() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(response.getJwt(), queryCommand);

        assertEquals(2, qrs.getTotalNumber());
        assertEquals(20, qrs.getPageSize());
        assertEquals(1, qrs.getPageIndex());
        assertEquals(2, qrs.getData().size());
        QViewableListQr item1 = qrs.getData().stream().filter(item -> item.getId().equals(qr1Response.getQrId())).findFirst().get();

        assertEquals(qr1Response.getQrId(), item1.getId());
        assertEquals(response.getAppId(), item1.getAppId());
        assertEquals(response.getDefaultGroupId(), item1.getGroupId());
        assertEquals(response.getMemberId(), item1.getCreatedBy());
        assertEquals(qr1Response.getPlateId(), item1.getPlateId());
        assertNotNull(item1.getCreatedAt());
        assertNotNull(item1.getName());
    }

    @Test
    public void should_list_paged_qrs() {
        PreparedAppResponse response = setupApi.registerWithApp();
        IntStream.range(0, 30).forEach(value -> QrApi.createQr(response.getJwt(), response.getDefaultGroupId()));

        ListViewableQrsQuery queryCommand1 = ListViewableQrsQuery.builder().appId(response.getAppId()).pageIndex(1).pageSize(20).build();
        assertEquals(20, QrApi.listQrs(response.getJwt(), queryCommand1).getData().size());

        ListViewableQrsQuery queryCommand2 = ListViewableQrsQuery.builder().appId(response.getAppId()).pageIndex(2).pageSize(20).build();
        assertEquals(10, QrApi.listQrs(response.getJwt(), queryCommand2).getData().size());
    }

    @Test
    public void should_list_template_only_qrs() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        QrApi.markTemplate(response.getJwt(), qr1Response.getQrId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).templateOnly(true).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(response.getJwt(), queryCommand);

        assertEquals(1, qrs.getData().size());
        assertEquals(qr1Response.getQrId(), qrs.getData().get(0).getId());
    }

    @Test
    public void should_list_inactive_only_qrs() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        QrApi.deactivate(response.getJwt(), qr1Response.getQrId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).inactiveOnly(true).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(response.getJwt(), queryCommand);

        assertEquals(1, qrs.getData().size());
        assertEquals(qr1Response.getQrId(), qrs.getData().get(0).getId());
    }

    @Test
    public void should_list_inactive_qrs_for_app_manager() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        QrApi.deactivate(response.getJwt(), qr1Response.getQrId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(response.getJwt(), queryCommand);
        assertEquals(2, qrs.getData().size());
    }


    @Test
    public void should_list_inactive_qrs_for_group_manger() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        QrApi.deactivate(response.getJwt(), qr1Response.getQrId());

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).groupId(response.getDefaultGroupId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(memberResponse.getJwt(), queryCommand);
        assertEquals(2, qrs.getData().size());
    }

    @Test
    public void should_list_only_active_qrs_for_non_app_managers() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        QrApi.deactivate(response.getJwt(), qr1Response.getQrId());

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(memberResponse.getJwt(), queryCommand);
        assertEquals(1, qrs.getData().size());
        assertEquals(qr2Response.getQrId(), qrs.getData().get(0).getId());
    }

    @Test
    public void should_list_only_active_qrs_for_non_group_managers() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER);

        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        QrApi.deactivate(response.getJwt(), qr1Response.getQrId());

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).groupId(response.getDefaultGroupId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(memberResponse.getJwt(), queryCommand);
        assertEquals(1, qrs.getData().size());
        assertEquals(qr2Response.getQrId(), qrs.getData().get(0).getId());
    }

    @Test
    public void should_list_qrs_for_all_viewable_groups() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        CreateAppResponse appResponse = AppApi.createApp(loginResponse.getJwt(), AS_GROUP_MEMBER);
        AppApi.updateAppOperationPermission(loginResponse.getJwt(), appResponse.getAppId(), AS_GROUP_MEMBER);

        String groupId1 = GroupApi.createGroup(loginResponse.getJwt(), appResponse.getAppId());
        String groupId2 = GroupApi.createGroup(loginResponse.getJwt(), appResponse.getAppId());
        CreateMemberResponse memberResponse1 = MemberApi.createMemberAndLogin(loginResponse.getJwt());
        CreateMemberResponse memberResponse2 = MemberApi.createMemberAndLogin(loginResponse.getJwt());
        GroupApi.addGroupMembers(loginResponse.getJwt(), groupId1, memberResponse1.getMemberId());
        GroupApi.addGroupMembers(loginResponse.getJwt(), groupId2, memberResponse2.getMemberId());
        CreateQrResponse qrResponse1 = QrApi.createQr(loginResponse.getJwt(), groupId1);
        CreateQrResponse qrResponse2 = QrApi.createQr(loginResponse.getJwt(), groupId2);

        ListViewableQrsQuery queryCommand1 = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs1 = QrApi.listQrs(memberResponse1.getJwt(), queryCommand1);
        assertEquals(1, qrs1.getData().size());
        assertEquals(qrResponse1.getQrId(), qrs1.getData().get(0).getId());

        ListViewableQrsQuery queryCommand2 = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs2 = QrApi.listQrs(memberResponse2.getJwt(), queryCommand2);
        assertEquals(1, qrs2.getData().size());
        assertEquals(qrResponse2.getQrId(), qrs2.getData().get(0).getId());
    }

    @Test
    public void should_list_qrs_for_given_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), anotherGroupId);

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).groupId(anotherGroupId).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(response.getJwt(), queryCommand);

        assertEquals(1, qrs.getData().size());
        assertEquals(qr2Response.getQrId(), qrs.getData().get(0).getId());
    }

    @Test
    public void should_list_qrs_near_current_position() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.enableAppPosition(response.getJwt(), response.getAppId());
        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr3Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        Geolocation geolocation1 = Geolocation.builder()
                .address(rAddress())
                .point(Geopoint.builder().longitude(120f).latitude(31f).build())
                .build();
        Geolocation geolocation2 = Geolocation.builder()
                .address(rAddress())
                .point(Geopoint.builder().longitude(120f).latitude(33f).build())
                .build();
        Geolocation geolocation3 = Geolocation.builder()
                .address(rAddress())
                .point(Geopoint.builder().longitude(120f).latitude(30f).build())
                .build();
        QrApi.updateQrBaseSetting(response.getJwt(), qr1Response.getQrId(), UpdateQrBaseSettingCommand.builder().name(rQrName()).geolocation(geolocation1).build());
        QrApi.updateQrBaseSetting(response.getJwt(), qr2Response.getQrId(), UpdateQrBaseSettingCommand.builder().name(rQrName()).geolocation(geolocation2).build());
        QrApi.updateQrBaseSetting(response.getJwt(), qr3Response.getQrId(), UpdateQrBaseSettingCommand.builder().name(rQrName()).geolocation(geolocation3).build());

        Geopoint currentPoint = Geopoint.builder().longitude(120f).latitude(29f).build();
        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).nearestPointEnabled(true).currentPoint(currentPoint).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(response.getJwt(), queryCommand);

        assertEquals(3, qrs.getData().size());
        assertEquals(qr3Response.getQrId(), qrs.getData().get(0).getId());
        assertEquals(qr1Response.getQrId(), qrs.getData().get(1).getId());
        assertEquals(qr2Response.getQrId(), qrs.getData().get(2).getId());
    }

    @Test
    public void should_list_qrs_based_on_filterables() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        FCheckboxControl control = defaultCheckboxControl();
        AppApi.updateAppControls(appResponse.getJwt(), appResponse.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(appResponse.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(appResponse.getJwt(), appResponse.getAppId(), attribute);

        CreateQrResponse qrResponse1 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());

        String optionId1 = control.getOptions().get(0).getId();
        CheckboxAnswer answer1 = rAnswerBuilder(control).optionIds(newArrayList(optionId1)).build();
        SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse1.getQrId(), appResponse.getHomePageId(), answer1);

        String optionId2 = control.getOptions().get(1).getId();
        CheckboxAnswer answer2 = rAnswerBuilder(control).optionIds(newArrayList(optionId2)).build();
        SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse2.getQrId(), appResponse.getHomePageId(), answer2);

        Map<String, Set<String>> filterables = Maps.of(attribute.getId(), newHashSet(optionId1));
        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).filterables(filterables).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand);

        assertEquals(1, qrs.getData().size());
        assertEquals(qrResponse1.getQrId(), qrs.getData().get(0).getId());
    }

    @Test
    public void should_list_qrs_based_on_no_space_search_equal_to_qr_name() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        CreateQrResponse qrResponse1 = QrApi.createQr(appResponse.getJwt(), "成都锻压机", appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(appResponse.getJwt(), "成都切割机", appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(appResponse.getJwt(), "重庆切割机", appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse4 = QrApi.createQr(appResponse.getJwt(), "西安锻压机", appResponse.getDefaultGroupId());

        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), ListViewableQrsQuery.builder().appId(appResponse.getAppId()).search("重庆").pageIndex(1).pageSize(20).build());
        assertEquals(1, qrs.getData().size());
        assertEquals(qrResponse3.getQrId(), qrs.getData().get(0).getId());

        PagedList<QViewableListQr> qrs1 = QrApi.listQrs(appResponse.getJwt(), ListViewableQrsQuery.builder().appId(appResponse.getAppId()).search("成都").pageIndex(1).pageSize(20).build());
        assertEquals(2, qrs1.getData().size());
        Set<String> ids = qrs1.getData().stream().map(QViewableListQr::getId).collect(toSet());
        assertTrue(ids.contains(qrResponse1.getQrId()));
        assertTrue(ids.contains(qrResponse2.getQrId()));
    }

    @Test
    public void should_list_qrs_based_on_no_space_search_equal_to_searchables() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        FEmailControl control = defaultEmailControl();
        AppApi.updateAppControls(appResponse.getJwt(), appResponse.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(appResponse.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(appResponse.getJwt(), appResponse.getAppId(), attribute);
        CreateQrResponse qrResponse1 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());

        EmailAnswer answer1 = rAnswerBuilder(control).email("aa1@aa.com").build();
        SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse1.getQrId(), appResponse.getHomePageId(), answer1);

        EmailAnswer answer2 = rAnswerBuilder(control).email("aa2@aa.com").build();
        SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse2.getQrId(), appResponse.getHomePageId(), answer2);

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).search("aa1@aa.com").pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand);

        assertEquals(1, qrs.getData().size());
        assertEquals(qrResponse1.getQrId(), qrs.getData().get(0).getId());
    }

    @Test
    public void should_list_qrs_based_on_spaced_search() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        CreateQrResponse qrResponse1 = QrApi.createQr(appResponse.getJwt(), "成都一号锻压机", appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(appResponse.getJwt(), "成都二号切割机", appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(appResponse.getJwt(), "重庆一号切割机", appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse4 = QrApi.createQr(appResponse.getJwt(), "西安一号锻压机", appResponse.getDefaultGroupId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).search("成都 锻压机").pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand);
        assertEquals(1, qrs.getData().size());
        assertEquals(qrResponse1.getQrId(), qrs.getData().get(0).getId());

        ListViewableQrsQuery queryCommand1 = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).search("一号 锻压机").pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs1 = QrApi.listQrs(appResponse.getJwt(), queryCommand1);
        assertEquals(2, qrs1.getData().size());
        Set<String> ids1 = qrs1.getData().stream().map(QViewableListQr::getId).collect(toSet());
        assertTrue(ids1.contains(qrResponse1.getQrId()));
        assertTrue(ids1.contains(qrResponse4.getQrId()));
    }

    @Test
    public void should_search_directly_by_qr_id() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        CreateQrResponse qrResponse1 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse4 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).search(qrResponse1.getQrId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand);
        assertEquals(1, qrs.getData().size());
        assertEquals(qrResponse1.getQrId(), qrs.getData().get(0).getId());
    }

    @Test
    public void should_search_directly_by_qr_custom_id() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        CreateQrResponse qrResponse1 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        QR qr1 = qrRepository.byId(qrResponse1.getQrId());
        String customId = newShortUuid();
        qr1.updateCustomId(customId, User.NOUSER);
        qrRepository.save(qr1);

        CreateQrResponse qrResponse2 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse4 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).search(customId).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand);
        assertEquals(1, qrs.getData().size());
        assertEquals(qrResponse1.getQrId(), qrs.getData().get(0).getId());
    }

    @Test
    public void should_search_directly_by_qr_plate_id() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        CreateQrResponse qrResponse1 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse4 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).search(qrResponse1.getPlateId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand);
        assertEquals(1, qrs.getData().size());
        assertEquals(qrResponse1.getQrId(), qrs.getData().get(0).getId());
    }

    @Test
    public void should_list_qrs_based_on_sort() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(appResponse.getJwt(), appResponse.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(appResponse.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(appResponse.getJwt(), appResponse.getAppId(), attribute);

        CreateQrResponse qrResponse1 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse1.getQrId(), appResponse.getHomePageId(), rAnswerBuilder(control).number(2d).build());
        SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse2.getQrId(), appResponse.getHomePageId(), rAnswerBuilder(control).number(1d).build());
        SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse3.getQrId(), appResponse.getHomePageId(), rAnswerBuilder(control).number(3d).build());

        ListViewableQrsQuery queryCommand1 = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).sortedBy(attribute.getId()).ascSort(true).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand1);
        assertEquals(3, qrs.getData().size());
        assertEquals(qrResponse2.getQrId(), qrs.getData().get(0).getId());
        assertEquals(qrResponse1.getQrId(), qrs.getData().get(1).getId());
        assertEquals(qrResponse3.getQrId(), qrs.getData().get(2).getId());
    }

    @Test
    public void should_list_qrs_with_only_summary_eligible_attributes() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(appResponse.getJwt(), appResponse.getAppId(), control);
        Attribute summaryEligibleAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).pcListEligible(true).type(CONTROL_LAST).pageId(appResponse.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        Attribute summaryNonEligibleAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).pcListEligible(false).type(CONTROL_LAST).pageId(appResponse.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(appResponse.getJwt(), appResponse.getAppId(), summaryEligibleAttribute, summaryNonEligibleAttribute);

        CreateQrResponse qrResponse = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse.getQrId(), appResponse.getHomePageId(), rAnswer(control));

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand);
        assertEquals(1, qrs.getData().size());
        QViewableListQr qViewableListQr = qrs.getData().get(0);
        assertTrue(qViewableListQr.getAttributeDisplayValues().containsKey(summaryEligibleAttribute.getId()));
        assertFalse(qViewableListQr.getAttributeDisplayValues().containsKey(summaryNonEligibleAttribute.getId()));
    }

    @Test
    public void should_list_qrs_with_fixed_attribute_values() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        Attribute summaryEligibleAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).pcListEligible(true).type(FIXED).fixedValue("someFixedValue").range(NO_LIMIT).build();
        Attribute summaryNotEligibleAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).pcListEligible(false).type(FIXED).fixedValue("someOtherFixedValue").range(NO_LIMIT).build();

        AppApi.updateAppAttributes(appResponse.getJwt(), appResponse.getAppId(), summaryEligibleAttribute, summaryNotEligibleAttribute);
        CreateQrResponse qrResponse = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand);

        assertEquals(1, qrs.getData().size());
        QViewableListQr qViewableListQr = qrs.getData().get(0);
        TextDisplayValue attributeValue = (TextDisplayValue) qViewableListQr.getAttributeDisplayValues().get(summaryEligibleAttribute.getId());
        assertEquals("someFixedValue", attributeValue.getText());
    }

    @Test
    public void should_list_qrs_with_group_referenced_values() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP).pcListEligible(true).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(response.getJwt(), queryCommand);

        Group group = groupRepository.byId(response.getDefaultGroupId());
        assertEquals(group.getName(), ((TextDisplayValue) qrs.getData().get(0).getAttributeDisplayValues().get(attribute.getId())).getText());
    }

    @Test
    public void should_list_qrs_with_member_referenced_values() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CREATOR).pcListEligible(true).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        Member member = memberRepository.byId(response.getMemberId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(response.getJwt(), queryCommand);

        assertEquals(member.getName(), ((TextDisplayValue) qrs.getData().get(0).getAttributeDisplayValues().get(attribute.getId())).getText());
    }

    @Test
    public void should_list_qrs_by_createdBy() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(appResponse.getJwt());
        AppApi.setAppManager(appResponse.getJwt(), appResponse.getAppId(), memberResponse.getMemberId());
        CreateQrResponse qr1 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qr2 = QrApi.createQr(memberResponse.getJwt(), appResponse.getDefaultGroupId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).createdBy(memberResponse.getMemberId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand);

        assertEquals(1, qrs.getData().size());
        QViewableListQr qViewableListQr = qrs.getData().get(0);
        assertEquals(qr2.getQrId(), qViewableListQr.getId());
    }

    @Test
    public void should_list_qrs_by_date_range() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        CreateQrResponse qrResponse1 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());

        QR qr1 = qrRepository.byId(qrResponse1.getQrId());
        ReflectionTestUtils.setField(qr1, "createdAt", LocalDate.of(2011, 3, 3).atStartOfDay(systemDefault()).toInstant());
        qrRepository.save(qr1);

        QR qr2 = qrRepository.byId(qrResponse2.getQrId());
        ReflectionTestUtils.setField(qr2, "createdAt", LocalDate.of(2011, 3, 6).atStartOfDay(systemDefault()).toInstant());
        qrRepository.save(qr2);

        QR qr3 = qrRepository.byId(qrResponse3.getQrId());
        ReflectionTestUtils.setField(qr3, "createdAt", LocalDate.of(2011, 3, 9).atStartOfDay(systemDefault()).toInstant());
        qrRepository.save(qr3);

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).startDate("2011-03-04").endDate("2011-03-07").pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(appResponse.getJwt(), queryCommand);

        assertEquals(1, qrs.getData().size());
        QViewableListQr qViewableListQr = qrs.getData().get(0);
        assertEquals(qrResponse2.getQrId(), qViewableListQr.getId());
    }

    @Test
    public void should_list_qrs_with_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());
        CreateQrResponse qr1Response = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qr2Response = QrApi.createQr(response.getJwt(), subGroupId);

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).pageIndex(1).pageSize(20).build();
        PagedList<QViewableListQr> qrs = QrApi.listQrs(response.getJwt(), queryCommand);

        assertEquals(2, qrs.getTotalNumber());
        assertTrue(qrs.getData().stream().map(QViewableListQr::getId).toList().containsAll(List.of(qr1Response.getQrId(), qr2Response.getQrId())));
    }

    @Test
    public void should_fail_to_list_qrs_if_no_viewable_groups() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        AppApi.updateAppPermission(appResponse.getJwt(), appResponse.getAppId(), CAN_MANAGE_GROUP);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(appResponse.getJwt());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).pageIndex(1).pageSize(20).build();
        assertError(() -> QrApi.listQrsRaw(memberResponse.getJwt(), queryCommand), NO_VIEWABLE_GROUPS);
    }

    @Test
    public void should_fail_to_list_qrs_if_no_permission_for_given_group() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        AppApi.updateAppPermission(appResponse.getJwt(), appResponse.getAppId(), CAN_MANAGE_GROUP);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(appResponse.getJwt());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(appResponse.getAppId()).groupId(appResponse.getDefaultGroupId()).pageIndex(1).pageSize(20).build();
        assertError(() -> QrApi.listQrsRaw(memberResponse.getJwt(), queryCommand), NO_VIEWABLE_PERMISSION_FOR_GROUP);
    }

    @Test
    public void should_download_qrs_for_non_control_ref_values() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);
        AppApi.enableAppPosition(response.getJwt(), response.getAppId());

        Geolocation qrGeolocation = rGeolocation();
        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(), UpdateQrBaseSettingCommand.builder()
                .customId(rCustomId())
                .name(rQrName())
                .geolocation(qrGeolocation)
                .build());

        String memberEmail = rEmail();
        String memberMobile = rMobile();
        String memberName = rMemberName();
        MemberApi.updateMember(response.getJwt(), response.getMemberId(), UpdateMemberInfoCommand.builder()
                .email(memberEmail)
                .mobile(memberMobile)
                .name(memberName)
                .departmentIds(List.of())
                .build());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), response.getMemberId());

        Attribute instanceGeolocationAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GEOLOCATION).range(NO_LIMIT).build();
        Attribute instanceGroupAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP).range(NO_LIMIT).build();
        Attribute instanceSubmitCountAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_SUBMIT_COUNT).range(NO_LIMIT).build();
        Attribute instanceCreateDateAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CREATE_DATE).range(NO_LIMIT).build();
        Attribute instanceCreateTimeAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CREATE_TIME).range(NO_LIMIT).build();
        Attribute instanceNameAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_NAME).range(NO_LIMIT).build();
        Attribute instanceCreatorAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CREATOR).range(NO_LIMIT).build();
        Attribute instanceGroupManagersAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP_MANAGERS).range(NO_LIMIT).build();
        Attribute instanceGroupManagersMobileAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP_MANAGERS_AND_MOBILE).range(NO_LIMIT).build();
        Attribute instanceGroupManagersEmailAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP_MANAGERS_AND_EMAIL).range(NO_LIMIT).build();
        Attribute pageLastSubmiterMobileAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(PAGE_LAST_SUBMITTER_AND_MOBILE).pageId(response.getHomePageId()).build();
        Attribute pageLastSubmiterEmailAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(PAGE_LAST_SUBMITTER_AND_EMAIL).pageId(response.getHomePageId()).build();
        Attribute instanceActiveStatusAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_ACTIVE_STATUS).range(NO_LIMIT).build();

        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(),
                instanceGeolocationAttribute,
                instanceGroupAttribute,
                instanceSubmitCountAttribute,
                instanceCreateDateAttribute,
                instanceCreateTimeAttribute,
                instanceNameAttribute,
                instanceCreatorAttribute,
                instanceGroupManagersAttribute,
                instanceGroupManagersMobileAttribute,
                instanceGroupManagersEmailAttribute,
                pageLastSubmiterMobileAttribute,
                pageLastSubmiterEmailAttribute,
                instanceActiveStatusAttribute
        );

        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).pageIndex(1).pageSize(20).build();
        byte[] exportBytes = QrApi.exportQrsAsExcel(response.getJwt(), queryCommand);
        List<Map<Integer, String>> result = newArrayList();
        EasyExcel.read(new ByteArrayInputStream(exportBytes), new AnalysisEventListener<Map<Integer, String>>() {

            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                result.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {

            }
        }).excelType(XLSX).sheet().doRead();

        QR qr = qrRepository.byId(response.getQrId());
        Group group = groupRepository.byId(response.getDefaultGroupId());
        Map<Integer, String> record = result.get(0);
        assertEquals(response.getQrId(), record.get(0));
        assertEquals(qr.getName(), record.get(1));
        assertEquals(qr.getGeolocation().toText(), record.get(2));

        //属性栏
        assertEquals(qr.getGeolocation().toText(), record.get(3));
        assertEquals(group.getName(), record.get(4));
        assertEquals(1, parseInt(record.get(5)));
        assertEquals(ofInstant(qr.getCreatedAt(), systemDefault()).toString(), record.get(6));
        assertEquals(MRY_DATE_TIME_FORMATTER.format(qr.getCreatedAt()), record.get(7));
        assertEquals(qr.getName(), record.get(8));
        assertEquals(memberName, record.get(9));
        assertEquals(memberName, record.get(10));
        assertEquals(memberName + "(" + memberMobile + ")", record.get(11));
        assertEquals(memberName + "(" + memberEmail + ")", record.get(12));
        assertEquals(memberName + "(" + memberMobile + ")", record.get(13));
        assertEquals(memberName + "(" + memberEmail + ")", record.get(14));
        assertEquals("是", record.get(15));

        //固定尾部栏
        assertEquals("是", record.get(16));
        assertEquals("否", record.get(17));
        assertEquals(qr.getPlateId(), record.get(18));
        assertEquals(qr.getCustomId(), record.get(19));
        assertEquals(group.getId(), record.get(20));
    }

    @Test
    public void should_export_control_ref_attribute_values() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        RadioAnswer radioAnswer = rAnswer(radioControl);
        Attribute radioControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(radioControl.getId()).range(NO_LIMIT).build();

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        CheckboxAnswer checkboxAnswer = rAnswer(checkboxControl);
        Attribute checkboxControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).range(NO_LIMIT).build();

        FDropdownControl dropdownControl = defaultDropdownControl();
        DropdownAnswer dropdownAnswer = rAnswer(dropdownControl);
        Attribute dropdownControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(dropdownControl.getId()).range(NO_LIMIT).build();


        FAddressControl addressControl = defaultAddressControlBuilder().precision(4).build();
        AddressAnswer addressAnswer = rAnswer(addressControl);
        Attribute addressControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(addressControl.getId()).range(NO_LIMIT).build();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        NumberInputAnswer numberInputAnswer = rAnswer(numberInputControl);
        Attribute numberInputControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(numberInputControl.getId()).range(NO_LIMIT).build();

        FMobileNumberControl mobileNumberControl = defaultMobileControl();
        MobileNumberAnswer mobileNumberAnswer = rAnswer(mobileNumberControl);
        Attribute mobileNumberControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(mobileNumberControl.getId()).range(NO_LIMIT).build();

        FIdentifierControl identifierControl = defaultIdentifierControl();
        IdentifierAnswer identifierAnswer = rAnswer(identifierControl);
        Attribute identifierControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(identifierControl.getId()).range(NO_LIMIT).build();

        FEmailControl emailControl = defaultEmailControl();
        EmailAnswer emailAnswer = rAnswer(emailControl);
        Attribute emailControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(emailControl.getId()).range(NO_LIMIT).build();

        FDateControl dateControl = defaultDateControl();
        DateAnswer dateAnswer = rAnswer(dateControl);
        Attribute dateControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(dateControl.getId()).range(NO_LIMIT).build();

        FTimeControl timeControl = defaultTimeControl();
        TimeAnswer timeAnswer = rAnswer(timeControl);
        Attribute timeControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(timeControl.getId()).range(NO_LIMIT).build();

        FItemCountControl itemCountControl = defaultItemCountControl();
        ItemCountAnswer itemCountAnswer = rAnswer(itemCountControl);
        Attribute itemCountControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(itemCountControl.getId()).range(NO_LIMIT).build();

        FItemStatusControl itemStatusControl = defaultItemStatusControl();
        ItemStatusAnswer itemStatusAnswer = rAnswer(itemStatusControl);
        Attribute itemStatusControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(itemStatusControl.getId()).range(NO_LIMIT).build();

        FPointCheckControl pointCheckControl = defaultPointCheckControl();
        PointCheckAnswer pointCheckAnswer = rAnswer(pointCheckControl);
        Attribute pointCheckControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(pointCheckControl.getId()).range(NO_LIMIT).build();

        FMultiLevelSelectionControl multiLevelSelectionControl = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市")
                .optionText("四川省/成都市\n四川省/绵阳市")
                .build();

        MultiLevelSelectionAnswer multiLevelSelectionAnswer = rAnswerBuilder(multiLevelSelectionControl).selection(MultiLevelSelection.builder()
                .level1("四川省")
                .level2("成都市")
                .build()).build();
        Attribute multiLevelSelectionControlRefAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(multiLevelSelectionControl.getId()).range(NO_LIMIT).build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(),
                radioControl,
                checkboxControl,
                dropdownControl,
                addressControl,
                numberInputControl,
                mobileNumberControl,
                identifierControl,
                emailControl,
                dateControl,
                timeControl,
                itemCountControl,
                itemStatusControl,
                pointCheckControl,
                multiLevelSelectionControl
        );

        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(),
                radioControlRefAttribute,
                checkboxControlRefAttribute,
                dropdownControlRefAttribute,
                addressControlRefAttribute,
                numberInputControlRefAttribute,
                mobileNumberControlRefAttribute,
                identifierControlRefAttribute,
                emailControlRefAttribute,
                dateControlRefAttribute,
                timeControlRefAttribute,
                itemCountControlRefAttribute,
                itemStatusControlRefAttribute,
                pointCheckControlRefAttribute,
                multiLevelSelectionControlRefAttribute
        );

        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                radioAnswer,
                checkboxAnswer,
                dropdownAnswer,
                addressAnswer,
                numberInputAnswer,
                mobileNumberAnswer,
                identifierAnswer,
                emailAnswer,
                dateAnswer,
                timeAnswer,
                itemCountAnswer,
                itemStatusAnswer,
                pointCheckAnswer,
                multiLevelSelectionAnswer
        );

        ListViewableQrsQuery queryCommand = ListViewableQrsQuery.builder().appId(response.getAppId()).pageIndex(1).pageSize(20).build();
        byte[] exportBytes = QrApi.exportQrsAsExcel(response.getJwt(), queryCommand);
        List<Map<Integer, String>> result = newArrayList();
        EasyExcel.read(new ByteArrayInputStream(exportBytes), new AnalysisEventListener<Map<Integer, String>>() {

            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                result.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {

            }
        }).excelType(XLSX).sheet().doRead();
        Map<Integer, String> record = result.get(0);
        assertEquals(radioControl.getOptions().stream().collect(toMap(TextOption::getId, TextOption::getName)).get(radioAnswer.getOptionId()), record.get(2));

        Map<String, String> checkboxOptionMap = checkboxControl.getOptions().stream().collect(toMap(TextOption::getId, TextOption::getName));
        assertEquals(checkboxAnswer.getOptionIds().stream().map(checkboxOptionMap::get).collect(Collectors.joining(", ")), record.get(3));

        Map<String, String> dropdownOptionMap = dropdownControl.getOptions().stream().collect(toMap(TextOption::getId, TextOption::getName));
        assertEquals(dropdownAnswer.getOptionIds().stream().map(dropdownOptionMap::get).collect(Collectors.joining(", ")), record.get(4));

        assertEquals(addressAnswer.getAddress().toText(), record.get(5));
        assertEquals(numberInputAnswer.getNumber(), Double.valueOf(record.get(6)));
        assertEquals(mobileNumberAnswer.getMobileNumber(), record.get(7));
        assertEquals(identifierAnswer.getContent(), record.get(8));
        assertEquals(emailAnswer.getEmail(), record.get(9));
        assertEquals(dateAnswer.getDate(), record.get(10));
        assertEquals(timeAnswer.getTime(), record.get(11));

        Map<String, String> itemCountOptionMap = itemCountControl.getOptions().stream().collect(toMap(TextOption::getId, TextOption::getName));
        assertEquals(itemCountAnswer.getItems().stream().map(countedItem -> itemCountOptionMap.get(countedItem.getOptionId()) + "x" + countedItem.getNumber()).collect(Collectors.joining(", ")), record.get(12));

        assertEquals(itemStatusControl.getOptions().stream().collect(toMap(TextOption::getId, TextOption::getName)).get(itemStatusAnswer.getOptionId()), record.get(13));
        assertEquals(pointCheckAnswer.isPassed() ? "正常" : "异常", record.get(14));
        assertEquals(multiLevelSelectionAnswer.getSelection().toText(), record.get(15));
    }


    @Test
    public void should_fetch_submission_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();

        QSubmissionQr submissionQr = QrApi.fetchSubmissionQr(response.getJwt(), response.getPlateId());

        QR qr = qrRepository.byId(response.getQrId());
        App app = appRepository.byId(response.getAppId());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        Member member = memberRepository.byId(response.getMemberId());

        QSubmissionQrDetail submissionQrDetail = submissionQr.getQr();
        QSubmissionAppDetail submissionAppDetail = submissionQr.getApp();
        QSubmissionQrMemberProfile memberProfile = submissionQr.getSubmissionQrMemberProfile();
        assertNotNull(submissionQrDetail);
        assertNotNull(submissionAppDetail);
        assertNotNull(memberProfile);
        assertNotNull(submissionQr.getPermissions());
        assertTrue(submissionQr.isCanOperateApp());

        assertEquals(qr.getAppId(), submissionQrDetail.getAppId());
        assertEquals(qr.getDescription(), submissionQrDetail.getDescription());
        assertEquals(qr.getGroupId(), submissionQrDetail.getGroupId());
        assertEquals(qr.getId(), submissionQrDetail.getId());
        assertEquals(qr.getName(), submissionQrDetail.getName());
        assertEquals(qr.getPlateId(), submissionQrDetail.getPlateId());
        assertEquals(qr.getTenantId(), submissionQrDetail.getTenantId());
        assertEquals(qr.getGeolocation(), submissionQrDetail.getGeolocation());
        assertEquals(qr.getHeaderImage(), submissionQrDetail.getHeaderImage());
        assertEquals(qr.isTemplate(), submissionQrDetail.isTemplate());

        assertEquals(app.getSetting(), submissionAppDetail.getSetting());
        assertEquals(app.getId(), submissionAppDetail.getId());
        assertEquals(app.getName(), submissionAppDetail.getName());
        assertEquals(app.getVersion(), submissionAppDetail.getVersion());

        assertEquals(response.getMemberId(), memberProfile.getMemberId());
        assertEquals(tenant.getTenantId(), memberProfile.getMemberTenantId());
        assertEquals(tenant.getName(), memberProfile.getTenantName());
        assertEquals(member.getName(), memberProfile.getMemberName());
    }

    @Test
    public void tenant_admin_should_fetch_submission_qr() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());

        QSubmissionQr qr = QrApi.fetchSubmissionQr(response.getJwt(), response.getPlateId());

        assertNotNull(qr.getApp());
        assertNotNull(qr.getSubmissionQrMemberProfile());
        assertNotNull(qr.getSubmissionQrMemberProfile().getMemberId());
        assertTrue(qr.getPermissions().containsAll(Arrays.asList(Permission.values())));
    }

    @Test
    public void app_manager_should_fetch_submission_qr() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        AppApi.setAppManager(response.getJwt(), response.getAppId(), memberResponse.getMemberId());

        QSubmissionQr qr = QrApi.fetchSubmissionQr(memberResponse.getJwt(), response.getPlateId());

        assertNotNull(qr.getApp());
        assertNotNull(qr.getSubmissionQrMemberProfile());
        assertNotNull(qr.getSubmissionQrMemberProfile().getMemberId());
        assertTrue(qr.getPermissions().containsAll(Arrays.asList(Permission.values())));
    }

    @Test
    public void group_manager_should_fetch_submission_qr() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rEmail(), rPassword());
        CreateAppResponse createAppResponse = AppApi.createApp(loginResponse.getJwt(), AS_GROUP_MEMBER);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(loginResponse.getJwt(), rMemberName(), rMobile(), rPassword());
        GroupApi.addGroupManagers(loginResponse.getJwt(), createAppResponse.getDefaultGroupId(), memberResponse.getMemberId());
        CreateQrResponse qrsResponse = QrApi.createQr(loginResponse.getJwt(), rQrName(), createAppResponse.getDefaultGroupId());

        QSubmissionQr qr = QrApi.fetchSubmissionQr(memberResponse.getJwt(), qrsResponse.getPlateId());

        assertNotNull(qr.getApp());
        assertNotNull(qr.getSubmissionQrMemberProfile());
        assertNotNull(qr.getSubmissionQrMemberProfile().getMemberId());
        assertTrue(qr.getPermissions().containsAll(Arrays.asList(PUBLIC, AS_TENANT_MEMBER, AS_GROUP_MEMBER, CAN_MANAGE_GROUP)));
        assertFalse(qr.getPermissions().contains(CAN_MANAGE_APP));

    }

    @Test
    public void common_group_member_should_fetch_submission_qr() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rEmail(), rPassword());
        CreateAppResponse createAppResponse = AppApi.createApp(loginResponse.getJwt(), AS_GROUP_MEMBER);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(loginResponse.getJwt(), rMemberName(), rMobile(), rPassword());
        GroupApi.addGroupMembers(loginResponse.getJwt(), createAppResponse.getDefaultGroupId(), memberResponse.getMemberId());
        CreateQrResponse qrsResponse = QrApi.createQr(loginResponse.getJwt(), rQrName(), createAppResponse.getDefaultGroupId());

        QSubmissionQr qr = QrApi.fetchSubmissionQr(memberResponse.getJwt(), qrsResponse.getPlateId());

        assertNotNull(qr.getApp());
        assertNotNull(qr.getSubmissionQrMemberProfile());
        assertNotNull(qr.getSubmissionQrMemberProfile().getMemberId());
        assertTrue(qr.getPermissions().containsAll(Arrays.asList(PUBLIC, AS_TENANT_MEMBER, AS_GROUP_MEMBER)));
        assertFalse(qr.getPermissions().contains(CAN_MANAGE_APP));
        assertFalse(qr.getPermissions().contains(CAN_MANAGE_GROUP));
    }

    @Test
    public void tenant_member_should_fetch_submission_qr() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rEmail(), rPassword());
        CreateAppResponse createAppResponse = AppApi.createApp(loginResponse.getJwt(), AS_TENANT_MEMBER);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(loginResponse.getJwt(), rMemberName(), rMobile(), rPassword());
        CreateQrResponse qrsResponse = QrApi.createQr(loginResponse.getJwt(), rQrName(), createAppResponse.getDefaultGroupId());

        QSubmissionQr qr = QrApi.fetchSubmissionQr(memberResponse.getJwt(), qrsResponse.getPlateId());

        assertNotNull(qr.getApp());
        assertNotNull(qr.getSubmissionQrMemberProfile());
        assertNotNull(qr.getSubmissionQrMemberProfile().getMemberId());
        assertTrue(qr.getPermissions().containsAll(Arrays.asList(PUBLIC, AS_TENANT_MEMBER)));
        assertFalse(qr.getPermissions().contains(CAN_MANAGE_APP));
        assertFalse(qr.getPermissions().contains(CAN_MANAGE_GROUP));
        assertFalse(qr.getPermissions().contains(AS_GROUP_MEMBER));
        assertFalse(qr.isCanOperateApp());
    }

    @Test
    public void public_user_should_fetch_submission_qr() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rEmail(), rPassword());
        CreateAppResponse createAppResponse = AppApi.createApp(loginResponse.getJwt(), PUBLIC);
        CreateQrResponse qrsResponse = QrApi.createQr(loginResponse.getJwt(), rQrName(), createAppResponse.getDefaultGroupId());

        QSubmissionQr qr = QrApi.fetchSubmissionQr(null, qrsResponse.getPlateId());

        assertNotNull(qr.getApp());
        assertNotNull(qr.getSubmissionQrMemberProfile());
        assertNull(qr.getSubmissionQrMemberProfile().getMemberId());
        assertTrue(qr.getPermissions().contains(PUBLIC));
        assertFalse(qr.getPermissions().contains(CAN_MANAGE_APP));
        assertFalse(qr.getPermissions().contains(CAN_MANAGE_GROUP));
        assertFalse(qr.getPermissions().contains(AS_GROUP_MEMBER));
        assertFalse(qr.getPermissions().contains(AS_TENANT_MEMBER));
        assertFalse(qr.isCanOperateApp());
    }

    @Test
    public void should_fetch_submission_qr_for_parent_group_manager() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String groupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManager(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), groupId);

        QSubmissionQr qr = QrApi.fetchSubmissionQr(memberResponse.getJwt(), qrResponse.getPlateId());
        assertTrue(qr.getPermissions().contains(CAN_MANAGE_GROUP));
    }

    @Test
    public void should_fetch_submission_qr_for_sub_group_member() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String groupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupMembers(response.getJwt(), groupId, memberResponse.getMemberId());

        QSubmissionQr qr = QrApi.fetchSubmissionQr(memberResponse.getJwt(), response.getPlateId());
        assertTrue(qr.getPermissions().contains(AS_GROUP_MEMBER));
    }

    @Test
    public void another_account_user_can_fetch_public_submission_qr() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rEmail(), rPassword());
        CreateAppResponse createAppResponse = AppApi.createApp(loginResponse.getJwt(), PUBLIC);
        CreateQrResponse qrsResponse = QrApi.createQr(loginResponse.getJwt(), rQrName(), createAppResponse.getDefaultGroupId());
        LoginResponse anotherResponse = setupApi.registerWithLogin(rEmail(), rPassword());

        QSubmissionQr qr = QrApi.fetchSubmissionQr(anotherResponse.getJwt(), qrsResponse.getPlateId());

        assertNotNull(qr.getApp());
        assertNotNull(qr.getSubmissionQrMemberProfile());
        assertNotNull(qr.getSubmissionQrMemberProfile().getMemberId());
        assertTrue(qr.getPermissions().contains(PUBLIC));
        assertFalse(qr.getPermissions().contains(CAN_MANAGE_APP));
        assertFalse(qr.getPermissions().contains(CAN_MANAGE_GROUP));
        assertFalse(qr.getPermissions().contains(AS_GROUP_MEMBER));
        assertFalse(qr.getPermissions().contains(AS_TENANT_MEMBER));
    }

    @Test
    public void should_fetch_submission_qr_with_page_ids() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse normalMemberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        CreateMemberResponse groupMemberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        CreateMemberResponse groupManagerResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupMembers(response.getJwt(), response.getDefaultGroupId(), groupManagerResponse.getMemberId(), groupMemberResponse.getMemberId());
        GroupApi.addGroupManager(response.getJwt(), response.getDefaultGroupId(), groupManagerResponse.getMemberId());

        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_GROUP_MEMBER);

        Page nonFillablePage = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page publicPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(PUBLIC).build()).build();
        Page tenantMemberRequiredPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).build()).build();
        Page groupManagerRequiredPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        Page appManagerRequiredPage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();
        Page groupManagerApprovablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_GROUP).build()).build()).build();
        Page appManagerApprovablePage = defaultPageBuilder().setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build()).build();
        AppApi.updateAppPages(response.getJwt(), response.getAppId(),
                groupManagerRequiredPage,
                nonFillablePage,
                publicPage,
                tenantMemberRequiredPage,
                appManagerRequiredPage,
                groupManagerApprovablePage,
                appManagerApprovablePage);

        QSubmissionQr tenantAdminSubmissionQr = QrApi.fetchSubmissionQr(response.getJwt(), response.getPlateId());
        assertEquals(5, tenantAdminSubmissionQr.getCanViewFillablePageIds().size());
        assertEquals(6, tenantAdminSubmissionQr.getCanManageFillablePageIds().size());
        assertEquals(2, tenantAdminSubmissionQr.getCanApproveFillablePageIds().size());

        QSubmissionQr groupManagerSubmissionQr = QrApi.fetchSubmissionQr(groupManagerResponse.getJwt(), response.getPlateId());
        assertEquals(4, groupManagerSubmissionQr.getCanViewFillablePageIds().size());
        assertTrue(groupManagerSubmissionQr.getCanViewFillablePageIds().contains(tenantMemberRequiredPage.getId()));
        assertTrue(groupManagerSubmissionQr.getCanViewFillablePageIds().contains(groupManagerRequiredPage.getId()));
        assertTrue(groupManagerSubmissionQr.getCanViewFillablePageIds().contains(groupManagerApprovablePage.getId()));
        assertTrue(groupManagerSubmissionQr.getCanViewFillablePageIds().contains(appManagerApprovablePage.getId()));
        assertEquals(5, groupManagerSubmissionQr.getCanManageFillablePageIds().size());
        assertTrue(groupManagerSubmissionQr.getCanManageFillablePageIds().contains(publicPage.getId()));
        assertTrue(groupManagerSubmissionQr.getCanManageFillablePageIds().contains(tenantMemberRequiredPage.getId()));
        assertTrue(groupManagerSubmissionQr.getCanManageFillablePageIds().contains(groupManagerRequiredPage.getId()));
        assertTrue(groupManagerSubmissionQr.getCanManageFillablePageIds().contains(groupManagerApprovablePage.getId()));
        assertTrue(groupManagerSubmissionQr.getCanManageFillablePageIds().contains(appManagerApprovablePage.getId()));
        assertEquals(1, groupManagerSubmissionQr.getCanApproveFillablePageIds().size());
        assertTrue(groupManagerSubmissionQr.getCanApproveFillablePageIds().contains(groupManagerApprovablePage.getId()));

        QSubmissionQr groupMemberSubmissionQr = QrApi.fetchSubmissionQr(groupMemberResponse.getJwt(), response.getPlateId());
        assertEquals(3, groupMemberSubmissionQr.getCanViewFillablePageIds().size());
        assertTrue(groupMemberSubmissionQr.getCanViewFillablePageIds().contains(tenantMemberRequiredPage.getId()));
        assertTrue(groupMemberSubmissionQr.getCanViewFillablePageIds().contains(groupManagerApprovablePage.getId()));
        assertTrue(groupMemberSubmissionQr.getCanViewFillablePageIds().contains(appManagerApprovablePage.getId()));
        assertEquals(0, groupMemberSubmissionQr.getCanManageFillablePageIds().size());
        assertEquals(0, groupMemberSubmissionQr.getCanApproveFillablePageIds().size());

        QSubmissionQr tenantMemberSubmissionQr = QrApi.fetchSubmissionQr(normalMemberResponse.getJwt(), response.getPlateId());
        assertEquals(3, tenantMemberSubmissionQr.getCanViewFillablePageIds().size());
        assertEquals(0, tenantMemberSubmissionQr.getCanManageFillablePageIds().size());
        assertEquals(0, tenantMemberSubmissionQr.getCanApproveFillablePageIds().size());
    }

    @Test
    public void should_fail_fetch_submission_qr_if_not_logged_in() {
        PreparedQrResponse loginResponse = setupApi.registerWithQr(rEmail(), rPassword());

        BaseApiTest.given(null)
                .when()
                .get("/qrs/submission-qrs/{plateId}", loginResponse.getPlateId())
                .then()
                .statusCode(401);
    }

    @Test
    public void should_fail_fetch_submission_qr_if_permission_not_enough_due_to_another_account_member() {
        PreparedQrResponse loginResponse = setupApi.registerWithQr(rEmail(), rPassword());
        PreparedQrResponse anotherResponse = setupApi.registerWithQr(rEmail(), rPassword());

        BaseApiTest.given(anotherResponse.getJwt())
                .when()
                .get("/qrs/submission-qrs/{plateId}", loginResponse.getPlateId())
                .then()
                .statusCode(403);
    }

    @Test
    public void should_fail_fetch_submission_qr_if_permission_not_enough() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rEmail(), rPassword());
        CreateAppResponse createAppResponse = AppApi.createApp(loginResponse.getJwt(), AS_GROUP_MEMBER);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(loginResponse.getJwt(), rMemberName(), rMobile(), rPassword());
        CreateQrResponse qrsResponse = QrApi.createQr(loginResponse.getJwt(), rQrName(), createAppResponse.getDefaultGroupId());

        BaseApiTest.given(memberResponse.getJwt())
                .when()
                .get("/qrs/submission-qrs/{plateId}", qrsResponse.getPlateId())
                .then()
                .statusCode(403);
    }

    @Test
    public void should_fail_fetch_submission_qr_if_plate_not_found() {
        PreparedQrResponse loginResponse = setupApi.registerWithQr(rEmail(), rPassword());

        BaseApiTest.given(loginResponse.getJwt())
                .when()
                .get("/qrs/submission-qrs/{plateId}", Plate.newPlateId())
                .then()
                .statusCode(404);
    }

    @Test
    public void should_fail_fetch_submission_qr_if_plate_not_bound() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        QrApi.deleteQrs(response.getJwt(), response.getQrId());

        QErrorResponse error = BaseApiTest.given(response.getJwt())
                .when()
                .get("/qrs/submission-qrs/{plateId}", response.getPlateId())
                .then()
                .statusCode(409)
                .extract()
                .as(QErrorResponse.class);
        assertEquals(PLATE_NOT_BOUND, error.getError().getCode());
    }

    @Test
    public void normal_members_should_fail_fetch_submission_qr_if_app_is_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        AppApi.deactivateApp(response.getJwt(), response.getAppId());

        QErrorResponse error = BaseApiTest.given(memberResponse.getJwt())
                .when()
                .get("/qrs/submission-qrs/{plateId}", response.getPlateId())
                .then()
                .statusCode(409)
                .extract()
                .as(QErrorResponse.class);
        assertEquals(APP_NOT_ACTIVE, error.getError().getCode());
    }

    @Test
    public void app_manager_should_fetch_submission_qr_even_if_deactivated() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        AppApi.deactivateApp(response.getJwt(), response.getAppId());
        QrApi.fetchSubmissionQr(response.getJwt(), response.getPlateId());
    }

    @Test
    public void should_fail_fetch_submission_qr_if_qr_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr();
        QrApi.deactivate(response.getJwt(), response.getQrId());
        assertError(() -> QrApi.fetchQrSummaryRaw(response.getJwt(), response.getQrId()), QR_NOT_ACTIVE);
    }

    @Test
    public void should_fail_fetch_submission_qr_if_group_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr();
        GroupApi.createGroup(response.getJwt(), response.getAppId());
        GroupApi.deactivateGroup(response.getJwt(), response.getDefaultGroupId());
        assertError(() -> QrApi.fetchQrSummaryRaw(response.getJwt(), response.getQrId()), GROUP_NOT_ACTIVE);
    }

    @Test
    public void should_fetch_listed_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();

        QViewableListQr listQr = QrApi.fetchListedQr(response.getJwt(), response.getQrId());

        QR qr = qrRepository.byId(response.getQrId());
        assertEquals(qr.getAppId(), listQr.getAppId());
        assertEquals(qr.getGroupId(), listQr.getGroupId());
        assertEquals(qr.getId(), listQr.getId());
        assertEquals(qr.getName(), listQr.getName());
        assertEquals(qr.getPlateId(), listQr.getPlateId());
        assertEquals(qr.getGeolocation(), listQr.getGeolocation());
        assertEquals(qr.getHeaderImage(), listQr.getHeaderImage());
        assertEquals(qr.isTemplate(), listQr.isTemplate());
    }

    @Test
    public void should_fetch_listed_qr_with_attribute_values() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(appResponse.getJwt(), appResponse.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).pcListEligible(true).type(CONTROL_LAST).pageId(appResponse.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(appResponse.getJwt(), appResponse.getAppId(), attribute);

        CreateQrResponse qrResponse = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        NumberInputAnswer numberInputAnswer = rAnswer(control);
        SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse.getQrId(), appResponse.getHomePageId(), numberInputAnswer);

        QViewableListQr listQr = QrApi.fetchListedQr(appResponse.getJwt(), qrResponse.getQrId());
        assertTrue(listQr.getAttributeDisplayValues().containsKey(attribute.getId()));
        assertEquals(numberInputAnswer.getNumber(), ((NumberDisplayValue) listQr.getAttributeDisplayValues().get(attribute.getId())).getNumber());
    }

    @Test
    public void should_fetch_listed_qr_with_only_summary_eligible_attribute_values() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(appResponse.getJwt(), appResponse.getAppId(), control);
        Attribute summaryEligibleAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).pcListEligible(true).type(CONTROL_LAST).pageId(appResponse.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        Attribute summaryNonEligibleAttribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).pcListEligible(false).type(CONTROL_LAST).pageId(appResponse.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(appResponse.getJwt(), appResponse.getAppId(), summaryEligibleAttribute, summaryNonEligibleAttribute);

        CreateQrResponse qrResponse = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse.getQrId(), appResponse.getHomePageId(), rAnswer(control));

        QViewableListQr listQr = QrApi.fetchListedQr(appResponse.getJwt(), qrResponse.getQrId());
        assertTrue(listQr.getAttributeDisplayValues().containsKey(summaryEligibleAttribute.getId()));
        assertFalse(listQr.getAttributeDisplayValues().containsKey(summaryNonEligibleAttribute.getId()));
    }

    @Test
    public void should_fail_fetch_listed_qr_if_no_permission() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rEmail(), rPassword());
        CreateAppResponse createAppResponse = AppApi.createApp(loginResponse.getJwt(), AS_GROUP_MEMBER);
        CreateQrResponse qrResponse = QrApi.createQr(loginResponse.getJwt(), createAppResponse.getDefaultGroupId());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(loginResponse.getJwt(), rMemberName(), rMobile(), rPassword());

        assertError(() -> QrApi.fetchListedQrRaw(memberResponse.getJwt(), qrResponse.getQrId()), ACCESS_DENIED);
    }

    @Test
    public void should_fetch_qr_base_setting() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).manualInput(true).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        UpdateQrBaseSettingCommand command = UpdateQrBaseSettingCommand.builder()
                .name(rQrName())
                .description(rSentence(100))
                .headerImage(rImageFile())
                .manualAttributeValues(Map.of(attribute.getId(), "hello"))
                .geolocation(rGeolocation())
                .customId(rCustomId())
                .build();
        QrApi.updateQrBaseSetting(response.getJwt(), response.getQrId(), command);

        QQrBaseSetting qrBaseSetting = QrApi.fetchQrBaseSetting(response.getJwt(), response.getQrId());
        QR qr = qrRepository.byId(response.getQrId());
        assertEquals(qr.getName(), qrBaseSetting.getName());
        assertEquals(qr.getDescription(), qrBaseSetting.getDescription());
        assertEquals(qr.getHeaderImage(), qrBaseSetting.getHeaderImage());
        assertEquals(qr.getGeolocation(), qrBaseSetting.getGeolocation());
        assertEquals(qr.getCustomId(), qrBaseSetting.getCustomId());
        assertEquals("hello", qrBaseSetting.getManualAttributeValues().get(attribute.getId()));
    }

    @Test
    public void should_fetch_qr_summary() {
        PreparedQrResponse qrResponse = setupApi.registerWithQr();
        QQrSummary qQrSummary = QrApi.fetchQrSummary(qrResponse.getJwt(), qrResponse.getQrId());

        QR qr = qrRepository.byId(qrResponse.getQrId());
        assertEquals(qr.getName(), qQrSummary.getName());
        assertEquals(qr.getAppId(), qQrSummary.getAppId());
        assertEquals(qr.getGroupId(), qQrSummary.getGroupId());
        assertFalse(qQrSummary.isTemplate());
        assertEquals(qr.getId(), qQrSummary.getId());
        assertEquals(qr.getPlateId(), qQrSummary.getPlateId());
    }

    @Test
    public void should_fail_fetch_qr_summary_if_inactive() {
        PreparedQrResponse qrResponse = setupApi.registerWithQr();
        QrApi.deactivate(qrResponse.getJwt(), qrResponse.getQrId());
        assertError(() -> QrApi.fetchQrSummaryRaw(qrResponse.getJwt(), qrResponse.getQrId()), QR_NOT_ACTIVE);
    }

    @Test
    public void should_list_qr_submissions_for_submitter_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse anotherMember = MemberApi.createMemberAndLogin(response.getJwt());
        FSingleLineTextControl control1 = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        Page page1 = defaultPageBuilder().controls(newArrayList(control1)).setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).build()).build();

        FSingleLineTextControl control2 = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        Page page2 = defaultPageBuilder().controls(newArrayList(control2)).setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).build()).build();

        AppApi.updateAppPages(response.getJwt(), response.getAppId(), page1, page2);

        IntStream.range(1, 11).forEach(value -> SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), page1.getId(), rAnswer(control1)));
        IntStream.range(1, 11).forEach(value -> SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), page2.getId(), rAnswer(control2)));
        IntStream.range(1, 2).forEach(value -> SubmissionApi.newSubmission(anotherMember.getJwt(), response.getQrId(), page2.getId(), rAnswer(control2)));
        SingleLineTextAnswer lastAnswer = rAnswer(control1);
        String lastSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), page1.getId(), lastAnswer);

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(SUBMITTER_SUBMISSION).pageId(null).pageIndex(1).pageSize(10).build());
        assertEquals(10, submissions.getData().size());
        Set<String> memberIds = submissions.getData().stream().map(QListSubmission::getCreatedBy).collect(toSet());
        assertFalse(memberIds.contains(anotherMember.getMemberId()));

        Set<String> pageIds = submissions.getData().stream().map(QListSubmission::getPageId).collect(toSet());
        assertTrue(pageIds.contains(page1.getId()));
        assertTrue(pageIds.contains(page2.getId()));

        QListSubmission firstSubmission = submissions.getData().get(0);
        assertEquals(lastSubmissionId, firstSubmission.getId());
        assertEquals(response.getAppId(), firstSubmission.getAppId());
        assertEquals(response.getMemberId(), firstSubmission.getCreatedBy());
        assertEquals(response.getDefaultGroupId(), firstSubmission.getGroupId());
        assertEquals(page1.getId(), firstSubmission.getPageId());
        assertEquals(response.getPlateId(), firstSubmission.getPlateId());
        assertEquals(response.getQrId(), firstSubmission.getQrId());
        assertEquals(NONE, firstSubmission.getApprovalStatus());
        assertNotNull(firstSubmission.getCreatedAt());
        assertEquals(1, firstSubmission.getDisplayAnswers().size());
        assertEquals(lastAnswer.getContent(), ((TextDisplayValue) firstSubmission.getDisplayAnswers().get(control1.getId())).getText());
    }

    @Test
    public void should_list_qr_submissions_for_submission_history() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(ALL_SUBMIT_HISTORY).pageId(null).pageIndex(1).pageSize(30).build());

        assertEquals(submissionId, submissions.getData().get(0).getId());
    }

    @Test
    public void should_list_qr_submissions_for_to_be_approved_submissions() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        PageSetting pageSetting = defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(TO_BE_APPROVED).pageId(null).pageIndex(1).pageSize(30).build());
        assertEquals(submissionId, submissions.getData().get(0).getId());

        SubmissionApi.approveSubmission(response.getJwt(), submissionId, true);
        PagedList<QListSubmission> updatedSubmissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(TO_BE_APPROVED).pageId(null).pageIndex(1).pageSize(30).build());
        assertEquals(0, updatedSubmissions.getData().size());
    }

    @Test
    public void should_contain_only_submission_eligible_control_answers() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl eligibleControl = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        FSingleLineTextControl nonEligibleControl = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(false).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), eligibleControl, nonEligibleControl);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(eligibleControl), rAnswer(nonEligibleControl));

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(ALL_SUBMIT_HISTORY).pageId(null).pageIndex(1).pageSize(30).build());

        QListSubmission qListSubmission = submissions.getData().get(0);
        assertEquals(1, qListSubmission.getDisplayAnswers().size());
        assertTrue(qListSubmission.getDisplayAnswers().containsKey(eligibleControl.getId()));
        assertFalse(qListSubmission.getDisplayAnswers().containsKey(nonEligibleControl.getId()));
    }

    @Test
    public void managers_can_view_all_control_answers_even_if_no_permission_to_submit() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse groupManager = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), groupManager.getMemberId());

        FSingleLineTextControl permissionedControl = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        FSingleLineTextControl nonPermissionedControl = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_APP).fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), permissionedControl, nonPermissionedControl);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(permissionedControl), rAnswer(nonPermissionedControl));

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(groupManager.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(ALL_SUBMIT_HISTORY).pageId(null).pageIndex(1).pageSize(30).build());

        QListSubmission qListSubmission = submissions.getData().get(0);
        assertEquals(2, qListSubmission.getDisplayAnswers().size());
        assertTrue(qListSubmission.getDisplayAnswers().containsKey(permissionedControl.getId()));
        assertTrue(qListSubmission.getDisplayAnswers().containsKey(nonPermissionedControl.getId()));
    }

    @Test
    public void should_list_qr_submissions_for_all_pages() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Page page1 = defaultPage();
        Page page2 = defaultPage();
        AppApi.updateAppPages(response.getJwt(), response.getAppId(), page1, page2);

        String submission1Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), page1.getId());
        String submission2Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), page2.getId());
        ListQrSubmissionsQuery queryCommand = ListQrSubmissionsQuery.builder()
                .type(ALL_SUBMIT_HISTORY)
                .pageId(null)
                .pageIndex(1)
                .pageSize(30)
                .build();
        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), queryCommand);
        Set<String> submissionIds = submissions.getData().stream().map(QListSubmission::getId).collect(toSet());
        assertTrue(submissionIds.contains(submission1Id));
        assertTrue(submissionIds.contains(submission2Id));
    }

    @Test
    public void should_list_qr_submissions_for_specific_page() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Page page1 = defaultPage();
        Page page2 = defaultPage();
        AppApi.updateAppPages(response.getJwt(), response.getAppId(), page1, page2);

        String submission1Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), page1.getId());
        String submission2Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), page2.getId());
        ListQrSubmissionsQuery queryCommand = ListQrSubmissionsQuery.builder()
                .type(ALL_SUBMIT_HISTORY)
                .pageId(page1.getId())
                .pageIndex(1)
                .pageSize(30)
                .build();
        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), queryCommand);
        Set<String> submissionIds = submissions.getData().stream().map(QListSubmission::getId).collect(toSet());
        assertTrue(submissionIds.contains(submission1Id));
        assertFalse(submissionIds.contains(submission2Id));
    }

    @Test
    public void should_list_qr_submissions_with_created_by() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        String submission1Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        String submission2Id = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());

        ListQrSubmissionsQuery queryCommand = ListQrSubmissionsQuery.builder()
                .type(ALL_SUBMIT_HISTORY)
                .createdBy(memberResponse.getMemberId())
                .pageIndex(1)
                .pageSize(30)
                .build();

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), queryCommand);
        Set<String> submissionIds = submissions.getData().stream().map(QListSubmission::getId).collect(toSet());
        assertFalse(submissionIds.contains(submission1Id));
        assertTrue(submissionIds.contains(submission2Id));
    }

    @Test
    public void should_list_qr_submissions_with_control_option_filters() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl control = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        String answer1OptionId = control.getOptions().get(0).getId();
        CheckboxAnswer answer1 = rAnswerBuilder(control).optionIds(newArrayList(answer1OptionId)).build();
        CheckboxAnswer answer2 = rAnswerBuilder(control).optionIds(newArrayList(control.getOptions().get(1).getId())).build();
        String submission1Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer1);
        String submission2Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer2);

        ListQrSubmissionsQuery queryCommand = ListQrSubmissionsQuery.builder()
                .type(ALL_SUBMIT_HISTORY)
                .pageId(response.getHomePageId())
                .filterables(Map.of(control.getId(), Set.of(answer1OptionId)))
                .pageIndex(1)
                .pageSize(30)
                .build();

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), queryCommand);
        Set<String> submissionIds = submissions.getData().stream().map(QListSubmission::getId).collect(toSet());
        assertTrue(submissionIds.contains(submission1Id));
        assertFalse(submissionIds.contains(submission2Id));
    }

    @Test
    public void should_list_qr_submissions_with_approval_filters() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        PageSetting pageSetting = defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));

        String submission1Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.approveSubmission(response.getJwt(), submission1Id, true);

        String submission2Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.approveSubmission(response.getJwt(), submission2Id, false);

        ListQrSubmissionsQuery queryCommand = ListQrSubmissionsQuery.builder()
                .type(ALL_SUBMIT_HISTORY)
                .pageId(response.getHomePageId())
                .filterables(Map.of("approval", Set.of("YES")))
                .pageIndex(1)
                .pageSize(30)
                .build();

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), queryCommand);
        Set<String> submissionIds = submissions.getData().stream().map(QListSubmission::getId).collect(toSet());
        assertTrue(submissionIds.contains(submission1Id));
        assertFalse(submissionIds.contains(submission2Id));
    }

    @Test
    public void should_list_qr_submissions_with_sort_by_created_at() {
        PreparedQrResponse response = setupApi.registerWithQr();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        String submission1Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        String submission2Id = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());

        ListQrSubmissionsQuery queryCommand = ListQrSubmissionsQuery.builder()
                .type(ALL_SUBMIT_HISTORY)
                .sortedBy("createdAt")
                .ascSort(true)
                .pageIndex(1)
                .pageSize(30)
                .build();

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), queryCommand);
        assertEquals(submission1Id, submissions.getData().get(0).getId());
        assertEquals(submission2Id, submissions.getData().get(1).getId());
    }

    @Test
    public void should_list_qr_submissions_with_sort_by_control_answer() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        String submission1Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(control).number(10d).build());
        String submission2Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(control).number(20d).build());

        ListQrSubmissionsQuery queryCommand = ListQrSubmissionsQuery.builder()
                .type(ALL_SUBMIT_HISTORY)
                .pageId(response.getHomePageId())
                .sortedBy(control.getId())
                .ascSort(false)
                .pageIndex(1)
                .pageSize(30)
                .build();

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), queryCommand);
        assertEquals(submission2Id, submissions.getData().get(0).getId());
        assertEquals(submission1Id, submissions.getData().get(1).getId());
    }

    @Test
    public void should_list_qr_submissions_with_search() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMobileNumberControl control = defaultMobileControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        String submission1Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(control).mobileNumber("15111111111").build());
        String submission2Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(control).mobileNumber("15222222222").build());

        ListQrSubmissionsQuery queryCommand = ListQrSubmissionsQuery.builder()
                .type(ALL_SUBMIT_HISTORY)
                .search("15111111111")
                .pageIndex(1)
                .pageSize(30)
                .build();

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), queryCommand);
        Set<String> submissionIds = submissions.getData().stream().map(QListSubmission::getId).collect(toSet());
        assertTrue(submissionIds.contains(submission1Id));
        assertFalse(submissionIds.contains(submission2Id));
    }

    @Test
    public void should_list_qr_submissions_with_date_range() {
        PreparedQrResponse response = setupApi.registerWithQr();

        String submission1Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        Submission submission1 = submissionRepository.byId(submission1Id);
        ReflectionTestUtils.setField(submission1, "createdAt", LocalDate.of(2011, 3, 6).atStartOfDay(systemDefault()).toInstant());
        submissionRepository.save(submission1);

        String submission2Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        Submission submission2 = submissionRepository.byId(submission2Id);
        ReflectionTestUtils.setField(submission2, "createdAt", LocalDate.of(2011, 4, 6).atStartOfDay(systemDefault()).toInstant());
        submissionRepository.save(submission2);

        String submission3Id = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        Submission submission3 = submissionRepository.byId(submission3Id);
        ReflectionTestUtils.setField(submission3, "createdAt", LocalDate.of(2011, 5, 6).atStartOfDay(systemDefault()).toInstant());
        submissionRepository.save(submission3);

        ListQrSubmissionsQuery queryCommand = ListQrSubmissionsQuery.builder()
                .type(ALL_SUBMIT_HISTORY)
                .startDate("2011-03-10")
                .endDate("2011-04-10")
                .pageIndex(1)
                .pageSize(30)
                .build();

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), queryCommand);
        Set<String> submissionIds = submissions.getData().stream().map(QListSubmission::getId).collect(toSet());
        assertTrue(submissionIds.contains(submission2Id));
        assertFalse(submissionIds.contains(submission1Id));
        assertFalse(submissionIds.contains(submission3Id));
    }

    @Test
    public void should_fail_list_submitter_submissions_if_not_contain_viewable_page() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER);
        CreateMemberResponse tenantMember = MemberApi.createMemberAndLogin(response.getJwt());
        FSingleLineTextControl control1 = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        Page page1 = defaultPageBuilder().controls(newArrayList(control1)).setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).build()).build();

        FSingleLineTextControl control2 = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        Page page2 = defaultPageBuilder().controls(newArrayList(control2)).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();

        AppApi.updateAppPages(response.getJwt(), response.getAppId(), page1, page2);

        String submissionId = SubmissionApi.newSubmission(tenantMember.getJwt(), response.getQrId(), page1.getId(), rAnswer(control1));

        PagedList<QListSubmission> submissions = QrApi.listQrSubmissions(tenantMember.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(SUBMITTER_SUBMISSION).pageId(page1.getId()).pageIndex(1).pageSize(30).build());
        assertEquals(1, submissions.getData().size());
        assertEquals(submissionId, submissions.getData().get(0).getId());

        assertError(() -> QrApi.listQrSubmissionsRaw(tenantMember.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(SUBMITTER_SUBMISSION).pageId(page2.getId()).pageIndex(1).pageSize(10).build()), NO_VIEWABLE_PERMISSION_FOR_PAGE);
    }

    @Test
    public void should_fail_list_history_submissions_if_no_managable_pages() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER);
        CreateMemberResponse tenantMember = MemberApi.createMemberAndLogin(response.getJwt());
        CreateMemberResponse groupManager = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), groupManager.getMemberId());
        FSingleLineTextControl control1 = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        Page page1 = defaultPageBuilder().controls(newArrayList(control1)).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();

        FSingleLineTextControl control2 = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        Page page2 = defaultPageBuilder().controls(newArrayList(control2)).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();

        AppApi.updateAppPages(response.getJwt(), response.getAppId(), page1, page2);

        QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(ALL_SUBMIT_HISTORY).pageId(null).pageIndex(1).pageSize(30).build());
        QrApi.listQrSubmissions(groupManager.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(ALL_SUBMIT_HISTORY).pageId(null).pageIndex(1).pageSize(30).build());
        assertError(() -> QrApi.listQrSubmissionsRaw(tenantMember.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(ALL_SUBMIT_HISTORY).pageId(null).pageIndex(1).pageSize(10).build()), NO_MANAGABLE_PAGES);
        assertError(() -> QrApi.listQrSubmissionsRaw(groupManager.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(ALL_SUBMIT_HISTORY).pageId(page2.getId()).pageIndex(1).pageSize(10).build()), NO_MANAGABLE_PERMISSION_FOR_PAGE);
    }

    @Test
    public void should_fail_list_to_be_approved_submissions_if_no_approvable_pages() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER);
        CreateMemberResponse tenantMember = MemberApi.createMemberAndLogin(response.getJwt());
        CreateMemberResponse groupManager = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), groupManager.getMemberId());
        FSingleLineTextControl control1 = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        Page page1 = defaultPageBuilder().controls(newArrayList(control1)).setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_GROUP).build()).build()).build();

        FSingleLineTextControl control2 = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().submissionSummaryEligible(true).build()).build();
        Page page2 = defaultPageBuilder().controls(newArrayList(control2)).setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build()).build();

        AppApi.updateAppPages(response.getJwt(), response.getAppId(), page1, page2);

        QrApi.listQrSubmissions(response.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(TO_BE_APPROVED).pageId(null).pageIndex(1).pageSize(30).build());
        QrApi.listQrSubmissions(groupManager.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(TO_BE_APPROVED).pageId(null).pageIndex(1).pageSize(30).build());
        assertError(() -> QrApi.listQrSubmissionsRaw(tenantMember.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(TO_BE_APPROVED).pageId(null).pageIndex(1).pageSize(10).build()), NO_APPROVABLE_PAGES);
        assertError(() -> QrApi.listQrSubmissionsRaw(groupManager.getJwt(), response.getQrId(), ListQrSubmissionsQuery.builder().type(TO_BE_APPROVED).pageId(page2.getId()).pageIndex(1).pageSize(10).build()), NO_APPROVABLE_PERMISSION_FOR_PAGE);
    }

    @Test
    public void should_fetch_bind_plate_info() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();
        App app = appRepository.byId(response.getAppId());
        String newGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());

        QBindPlateInfo bindPlateInfo = QrApi.fetchBindPlateInfo(response.getJwt(), plateId);
        assertEquals(plateId, bindPlateInfo.getPlateId());
        assertEquals(response.getAppId(), bindPlateInfo.getAppId());
        assertEquals(app.getName(), bindPlateInfo.getAppName());
        assertEquals(app.homePageId(), bindPlateInfo.getHomePageId());
        assertEquals(app.instanceDesignation(), bindPlateInfo.getInstanceDesignation());
        assertEquals(app.groupDesignation(), bindPlateInfo.getGroupDesignation());
        assertEquals(response.getMemberId(), bindPlateInfo.getMemberId());
        assertEquals(2, bindPlateInfo.getSelectableGroups().size());
        assertTrue(bindPlateInfo.getSelectableGroups().containsKey(response.getDefaultGroupId()));
        assertTrue(bindPlateInfo.getSelectableGroups().containsKey(newGroupId));
    }

    @Test
    public void group_manager_should_fetch_bind_plate_info() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();
        App app = appRepository.byId(response.getAppId());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        String groupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        GroupApi.addGroupManagers(response.getJwt(), groupId, memberResponse.getMemberId());

        QBindPlateInfo bindPlateInfo = QrApi.fetchBindPlateInfo(memberResponse.getJwt(), plateId);
        assertEquals(plateId, bindPlateInfo.getPlateId());
        assertEquals(response.getAppId(), bindPlateInfo.getAppId());
        assertEquals(app.getName(), bindPlateInfo.getAppName());
        assertEquals(memberResponse.getMemberId(), bindPlateInfo.getMemberId());
        assertEquals(1, bindPlateInfo.getSelectableGroups().size());
        assertTrue(bindPlateInfo.getSelectableGroups().containsKey(groupId));
    }

    @Test
    public void should_fail_fetch_bind_plate_info_if_no_managable_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        assertError(() -> QrApi.fetchBindPlateInfoRaw(memberResponse.getJwt(), plateId), ACCESS_DENIED);
    }

    @Test
    public void should_fail_fetch_bind_plate_info_if_already_bound() {
        PreparedQrResponse response = setupApi.registerWithQr();

        assertError(() -> QrApi.fetchBindPlateInfoRaw(response.getJwt(), response.getPlateId()), PLATE_ALREADY_BOUND);
    }

    @Test
    public void should_list_plate_attribute_values() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl checkboxControl = defaultCheckboxControl();
        FRadioControl radioControl = defaultRadioControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), checkboxControl, radioControl);

        Attribute fixedAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(FIXED).fixedValue("FIXED_VALUE").build();
        Attribute instanceNameAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_NAME).build();
        Attribute instanceSubmitCountAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(INSTANCE_SUBMIT_COUNT).build();
        Attribute instanceCreatorAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_CREATOR).range(NO_LIMIT).build();
        Attribute instanceGroupAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP).range(NO_LIMIT).build();
        Attribute checkboxAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(checkboxControl.getId()).build();
        Attribute radioAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).range(NO_LIMIT).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(radioControl.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), fixedAttribute, instanceNameAttribute, instanceSubmitCountAttribute, instanceCreatorAttribute, instanceGroupAttribute, checkboxAttribute, radioAttribute);

        SingleRowTextControl singleRowTextControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .borderRadius(noBorderRadius())
                .border(rBorder())
                .textValue(PlateTextValue.builder()
                        .type(QR_ATTRIBUTE)
                        .attributeId(fixedAttribute.getId())
                        .build())
                .alignType(CENTER)
                .fontStyle(rFontStyle())
                .height(50)
                .logoHeight(30)
                .build();

        SingleRowTextControl creatorSingleRowTextControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .borderRadius(noBorderRadius())
                .border(rBorder())
                .textValue(PlateTextValue.builder()
                        .type(QR_ATTRIBUTE)
                        .attributeId(instanceCreatorAttribute.getId())
                        .build())
                .alignType(CENTER)
                .fontStyle(rFontStyle())
                .height(50)
                .logoHeight(30)
                .build();

        SingleRowTextControl groupSingleRowTextControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .borderRadius(noBorderRadius())
                .border(rBorder())
                .textValue(PlateTextValue.builder()
                        .type(QR_ATTRIBUTE)
                        .attributeId(instanceGroupAttribute.getId())
                        .build())
                .alignType(CENTER)
                .fontStyle(rFontStyle())
                .height(50)
                .logoHeight(30)
                .build();

        SingleRowTextControl checkBoxSingleRowTextControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .borderRadius(noBorderRadius())
                .border(rBorder())
                .textValue(PlateTextValue.builder()
                        .type(QR_ATTRIBUTE)
                        .attributeId(checkboxAttribute.getId())
                        .build())
                .alignType(CENTER)
                .fontStyle(rFontStyle())
                .height(50)
                .logoHeight(30)
                .build();

        SingleRowTextControl radioBoxSingleRowTextControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .borderRadius(noBorderRadius())
                .border(rBorder())
                .textValue(PlateTextValue.builder()
                        .type(QR_ATTRIBUTE)
                        .attributeId(radioAttribute.getId())
                        .build())
                .alignType(CENTER)
                .fontStyle(rFontStyle())
                .height(50)
                .logoHeight(30)
                .build();

        KeyValueControl keyValueControl = KeyValueControl.builder()
                .id(newShortUuid())
                .type(KEY_VALUE)
                .borderRadius(noBorderRadius())
                .border(rBorder())
                .textValues(List.of(PlateNamedTextValue.builder()
                        .id(newShortUuid())
                        .name(rPlateKeyName())
                        .value(PlateTextValue.builder()
                                .type(QR_ATTRIBUTE)
                                .attributeId(instanceNameAttribute.getId())
                                .build())
                        .build()))
                .fontStyle(rFontStyle())
                .lineHeight(50)
                .textHorizontalAlignType(JUSTIFY)
                .verticalAlignType(MIDDLE)
                .horizontalPositionType(RIGHT)
                .horizontalGutter(0)
                .qrEnabled(true)
                .qrImageSetting(PlateQrImageSetting.builder()
                        .width(500)
                        .build())
                .build();

        TableControl tableControl = TableControl.builder()
                .id(newShortUuid())
                .type(TABLE)
                .borderRadius(noBorderRadius())
                .border(rBorder())
                .headerEnabled(true)
                .headerTextValue(PlateTextValue.builder()
                        .type(FIXED_TEXT)
                        .text("表头")
                        .build())
                .headerFontStyle(rFontStyle())
                .headerHeight(50)
                .headerAlignType(CENTER)
                .contentTextValues(List.of(PlateNamedTextValue.builder()
                        .id(newShortUuid())
                        .name(rPlateKeyName())
                        .value(PlateTextValue.builder()
                                .type(QR_ATTRIBUTE)
                                .attributeId(instanceSubmitCountAttribute.getId())
                                .build())
                        .build()))
                .contentFontStyle(rFontStyle())
                .cellHeight(30)
                .borderWidth(1)
                .qrEnabled(true)
                .qrImageSetting(PlateQrImageSetting.builder()
                        .width(500)
                        .build())
                .qrRows(3)
                .build();

        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        PlateSetting plateSetting = setting.getPlateSetting();

        plateSetting.getControls().clear();
        plateSetting.getControls().add(keyValueControl);
        plateSetting.getControls().add(singleRowTextControl);
        plateSetting.getControls().add(tableControl);
        plateSetting.getControls().add(creatorSingleRowTextControl);
        plateSetting.getControls().add(groupSingleRowTextControl);
        plateSetting.getControls().add(checkBoxSingleRowTextControl);
        plateSetting.getControls().add(radioBoxSingleRowTextControl);
        AppApi.updateAppSetting(response.getJwt(), appId, app.getVersion(), setting);
        CheckboxAnswer checkboxAnswer = rAnswer(checkboxControl);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), checkboxAnswer);

        ListPlateAttributeValuesQuery query = ListPlateAttributeValuesQuery.builder().appId(response.getAppId()).qrIds(Set.of(response.getQrId())).build();
        Map<String, Map<String, String>> attributeValues = QrApi.listPlateAttributeValues(response.getJwt(), query);

        QR qr = qrRepository.byId(response.getQrId());
        Group group = groupRepository.byId(response.getDefaultGroupId());
        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(1, attributeValues.size());
        Map<String, String> qrAttributeValues = attributeValues.get(response.getQrId());
        assertEquals(7, qrAttributeValues.size());
        assertEquals("FIXED_VALUE", qrAttributeValues.get(fixedAttribute.getId()));
        assertEquals(qr.getName(), qrAttributeValues.get(instanceNameAttribute.getId()));
        assertEquals("1", qrAttributeValues.get(instanceSubmitCountAttribute.getId()));
        assertEquals(member.getName(), qrAttributeValues.get(instanceCreatorAttribute.getId()));
        assertEquals(group.getName(), qrAttributeValues.get(instanceGroupAttribute.getId()));
        assertEquals(checkboxControl.exportedValueFor(checkboxAnswer.getOptionIds()), qrAttributeValues.get(checkboxAttribute.getId()));
        assertNull(qrAttributeValues.get(radioAttribute.getId()));
    }

}