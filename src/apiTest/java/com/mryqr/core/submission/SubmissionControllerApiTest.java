package com.mryqr.core.submission;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.circulation.StatusAfterSubmission;
import com.mryqr.core.app.domain.circulation.StatusPermission;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.AutoCalculateAliasContext;
import com.mryqr.core.app.domain.page.control.ControlFillableSetting;
import com.mryqr.core.app.domain.page.control.FAddressControl;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FDateControl;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.app.domain.page.control.FEmailControl;
import com.mryqr.core.app.domain.page.control.FGeolocationControl;
import com.mryqr.core.app.domain.page.control.FIdentifierControl;
import com.mryqr.core.app.domain.page.control.FImageUploadControl;
import com.mryqr.core.app.domain.page.control.FItemCountControl;
import com.mryqr.core.app.domain.page.control.FItemStatusControl;
import com.mryqr.core.app.domain.page.control.FMemberSelectControl;
import com.mryqr.core.app.domain.page.control.FMobileNumberControl;
import com.mryqr.core.app.domain.page.control.FMultiLevelSelectionControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FNumberRankingControl;
import com.mryqr.core.app.domain.page.control.FPointCheckControl;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.control.FTimeControl;
import com.mryqr.core.app.domain.page.setting.ApprovalSetting;
import com.mryqr.core.app.domain.page.setting.PageSetting;
import com.mryqr.core.common.domain.TextOption;
import com.mryqr.core.common.domain.display.NumberDisplayValue;
import com.mryqr.core.common.domain.display.TextDisplayValue;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.plan.domain.Plan;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.CirculationStatusAttributeValue;
import com.mryqr.core.qr.domain.attribute.IntegerAttributeValue;
import com.mryqr.core.qr.domain.attribute.TextAttributeValue;
import com.mryqr.core.submission.command.ApproveSubmissionCommand;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.command.UpdateSubmissionCommand;
import com.mryqr.core.submission.domain.ApprovalStatus;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionApproval;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.address.AddressAnswer;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.email.EmailAnswer;
import com.mryqr.core.submission.domain.answer.geolocation.GeolocationAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.itemcount.ItemCountAnswer;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import com.mryqr.core.submission.domain.answer.memberselect.MemberSelectAnswer;
import com.mryqr.core.submission.domain.answer.mobilenumber.MobileNumberAnswer;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelection;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelectionAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.core.submission.domain.answer.numberranking.NumberRankingAnswer;
import com.mryqr.core.submission.domain.answer.pointcheck.PointCheckAnswer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import com.mryqr.core.submission.domain.event.SubmissionApprovedEvent;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.submission.domain.event.SubmissionDeletedEvent;
import com.mryqr.core.submission.domain.event.SubmissionUpdatedEvent;
import com.mryqr.core.submission.query.QDetailedSubmission;
import com.mryqr.core.submission.query.QSubmissionApproval;
import com.mryqr.core.submission.query.autocalculate.AutoCalculateQuery;
import com.mryqr.core.submission.query.autocalculate.ItemStatusAutoCalculateResponse;
import com.mryqr.core.submission.query.autocalculate.NumberInputAutoCalculateResponse;
import com.mryqr.core.submission.query.list.ListSubmissionsQuery;
import com.mryqr.core.submission.query.list.QListSubmission;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alibaba.excel.support.ExcelTypeEnum.XLSX;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CIRCULATION_STATUS;
import static com.mryqr.core.app.domain.attribute.AttributeType.PAGE_SUBMIT_COUNT;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.ALL_SUBMIT_HISTORY;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.SUBMITTER_SUBMISSION;
import static com.mryqr.core.app.domain.operationmenu.SubmissionListType.TO_BE_APPROVED;
import static com.mryqr.core.app.domain.page.setting.SubmitType.NEW;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_MEMBER;
import static com.mryqr.core.app.domain.page.setting.SubmitterUpdateRange.IN_1_HOUR;
import static com.mryqr.core.common.domain.ValueType.CIRCULATION_STATUS_VALUE;
import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_APPROVED;
import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_CREATED;
import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_DELETED;
import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_UPDATED;
import static com.mryqr.core.common.domain.permission.Permission.AS_GROUP_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.AS_TENANT_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.core.common.domain.permission.Permission.PUBLIC;
import static com.mryqr.core.common.exception.ErrorCode.ACCESS_DENIED;
import static com.mryqr.core.common.exception.ErrorCode.ANSWERS_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.APPROVAL_NOT_ENABLED;
import static com.mryqr.core.common.exception.ErrorCode.APP_NOT_ACTIVE;
import static com.mryqr.core.common.exception.ErrorCode.AUTHENTICATION_FAILED;
import static com.mryqr.core.common.exception.ErrorCode.CANNOT_UPDATE_APPROVED_SUBMISSION;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_EXIST_FOR_ANSWER;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_NOT_ACTIVE;
import static com.mryqr.core.common.exception.ErrorCode.MANDATORY_ANSWER_REQUIRED;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_GROUPS;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PERMISSION_FOR_GROUP;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PERMISSION_FOR_QR;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_GROUPS;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PERMISSION_FOR_GROUP;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PERMISSION_FOR_QR;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_GROUPS;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PERMISSION_FOR_GROUP;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PERMISSION_FOR_QR;
import static com.mryqr.core.common.exception.ErrorCode.PAGE_NOT_ALLOW_CHANGE_BY_SUBMITTER;
import static com.mryqr.core.common.exception.ErrorCode.PAGE_NOT_FILLABLE;
import static com.mryqr.core.common.exception.ErrorCode.QR_NOT_ACTIVE;
import static com.mryqr.core.common.exception.ErrorCode.SUBMISSION_ALREADY_APPROVED;
import static com.mryqr.core.common.exception.ErrorCode.SUBMISSION_ALREADY_EXISTS_FOR_INSTANCE;
import static com.mryqr.core.common.exception.ErrorCode.SUBMISSION_ALREADY_EXISTS_FOR_MEMBER;
import static com.mryqr.core.common.exception.ErrorCode.SUBMISSION_APPROVE_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.SUBMISSION_COUNT_LIMIT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.SUBMISSION_NOT_ALLOWED_BY_CIRCULATION;
import static com.mryqr.core.common.exception.ErrorCode.UPDATE_PERIOD_EXPIRED;
import static com.mryqr.core.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.core.submission.SubmissionApi.newSubmissionRaw;
import static com.mryqr.core.submission.SubmissionApi.updateSubmissionRaw;
import static com.mryqr.core.submission.SubmissionUtils.approveSubmissionCommand;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.core.submission.SubmissionUtils.updateSubmissionCommand;
import static com.mryqr.core.submission.domain.ApprovalStatus.NONE;
import static com.mryqr.utils.RandomTestFixture.defaultAddressControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControl;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultDateControl;
import static com.mryqr.utils.RandomTestFixture.defaultDropdownControl;
import static com.mryqr.utils.RandomTestFixture.defaultEmailControl;
import static com.mryqr.utils.RandomTestFixture.defaultFillableSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultGeolocationControl;
import static com.mryqr.utils.RandomTestFixture.defaultIdentifierControl;
import static com.mryqr.utils.RandomTestFixture.defaultImageUploadControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultItemCountControl;
import static com.mryqr.utils.RandomTestFixture.defaultItemStatusControl;
import static com.mryqr.utils.RandomTestFixture.defaultItemStatusControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultMemberSelectControl;
import static com.mryqr.utils.RandomTestFixture.defaultMobileControl;
import static com.mryqr.utils.RandomTestFixture.defaultMultiLevelSelectionControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultNumberInputControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultNumberRankingControl;
import static com.mryqr.utils.RandomTestFixture.defaultPageApproveSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultPageBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultPageSettingBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultPointCheckControl;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControl;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultTimeControl;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static com.mryqr.utils.RandomTestFixture.rEmail;
import static com.mryqr.utils.RandomTestFixture.rMemberName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static com.mryqr.utils.RandomTestFixture.rTextOptions;
import static java.time.ZoneId.systemDefault;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmissionControllerApiTest extends BaseApiTest {

    @Test
    public void should_create_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        SingleLineTextAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(1, submission.allAnswers().size());
        assertEquals(response.getAppId(), submission.getAppId());
        assertEquals(response.getQrId(), submission.getQrId());
        assertEquals(response.getHomePageId(), submission.getPageId());
        assertEquals(response.getMemberId(), submission.getCreatedBy());
        assertEquals(response.getDefaultGroupId(), submission.getGroupId());
        assertEquals(response.getTenantId(), submission.getTenantId());
        assertEquals(control.getId(), submission.allAnswers().values().stream().findAny().get().getControlId());
        assertNull(submission.getApproval());
    }

    @Test
    public void should_raise_event_when_create_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(PAGE_SUBMIT_COUNT).pageId(response.getHomePageId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        SingleLineTextAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        SubmissionCreatedEvent submissionCreatedEvent = domainEventDao.latestEventFor(submissionId, SUBMISSION_CREATED, SubmissionCreatedEvent.class);
        assertEquals(submissionId, submissionCreatedEvent.getSubmissionId());
        assertEquals(response.getAppId(), submissionCreatedEvent.getAppId());
        assertEquals(response.getHomePageId(), submissionCreatedEvent.getPageId());
        assertEquals(response.getQrId(), submissionCreatedEvent.getQrId());
        IntegerAttributeValue attributeValue = (IntegerAttributeValue) qrRepository.byId(response.getQrId()).attributeValueOf(attribute.getId());
        assertEquals(1, attributeValue.getNumber());
        assertEquals(1, tenantRepository.byId(response.getTenantId()).getResourceUsage().getSubmissionCountForApp(response.getAppId()));
    }

    @Test
    public void should_calculate_qr_circulation_status_after_create_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .statusAfterSubmissions(List.of(StatusAfterSubmission.builder().id(newShortUuid()).optionId(option1.getId()).pageId(response.getHomePageId()).build()))
                .statusPermissions(List.of())
                .build();
        AppApi.updateCirculationStatusSetting(response.getJwt(), response.getAppId(), setting);

        String attributeId = newAttributeId();
        Attribute attribute = Attribute.builder().id(attributeId).name(rAttributeName()).type(INSTANCE_CIRCULATION_STATUS).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        QR qr = qrRepository.byId(response.getQrId());
        assertEquals(option1.getId(), qr.getCirculationOptionId());
        CirculationStatusAttributeValue attributeValue = (CirculationStatusAttributeValue) qr.getAttributeValues().get(attributeId);
        assertEquals(attributeId, attributeValue.getAttributeId());
        assertEquals(INSTANCE_CIRCULATION_STATUS, attributeValue.getAttributeType());
        assertEquals(CIRCULATION_STATUS_VALUE, attributeValue.getValueType());
        assertEquals(option1.getId(), attributeValue.getOptionId());
    }

    @Test
    public void should_also_save_indexed_values_when_create_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        FRadioControl radioControl = defaultRadioControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, radioControl);

        NumberInputAnswer numberInputAnswer = rAnswer(numberInputControl);
        RadioAnswer radioAnswer = rAnswer(radioControl);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), numberInputAnswer, radioAnswer);

        Submission submission = submissionRepository.byId(submissionId);
        App app = appRepository.byId(response.getAppId());
        IndexedField numberInputIndexedField = app.indexedFieldForControlOptional(response.getHomePageId(), numberInputControl.getId()).get();
        IndexedField radioIndexedField = app.indexedFieldForControlOptional(response.getHomePageId(), radioControl.getId()).get();
        assertEquals(numberInputAnswer.getNumber(), submission.getIndexedValues().valueOf(numberInputIndexedField).getSv());
        assertTrue(submission.getIndexedValues().valueOf(radioIndexedField).getTv().contains(radioAnswer.getOptionId()));
    }

    @Test
    public void should_create_both_submission_and_qr_for_template_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        QrApi.markTemplate(response.getJwt(), response.getQrId());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        Submission submission = submissionRepository.byId(submissionId);
        assertNotEquals(response.getQrId(), submission.getQrId());
        QR qr = qrRepository.byId(submission.getQrId());
        assertEquals(response.getAppId(), qr.getAppId());
        assertEquals(response.getDefaultGroupId(), qr.getGroupId());
        assertNotEquals(response.getPlateId(), qr.getPlateId());
        Plate plate = plateRepository.byId(qr.getPlateId());
        assertEquals(response.getDefaultGroupId(), plate.getGroupId());
        assertEquals(qr.getId(), plate.getQrId());
        assertEquals(qr.getAppId(), plate.getAppId());
    }

    @Test
    public void should_not_track_submitter_for_public_pages() {
        PreparedQrResponse loginResponse = setupApi.registerWithQr(rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(loginResponse.getJwt(), loginResponse.getAppId(), PUBLIC, control);

        NewSubmissionCommand submissionCommand = newSubmissionCommand(loginResponse.getQrId(), loginResponse.getHomePageId(), rAnswer(control));
        String submissionId = SubmissionApi.newSubmission(loginResponse.getJwt(), submissionCommand);

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(1, submission.allAnswers().size());
        assertNull(submission.getCreatedBy());
    }


    @Test
    public void non_login_user_should_create_submission_for_public_page() {
        PreparedQrResponse loginResponse = setupApi.registerWithQr(rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(loginResponse.getJwt(), loginResponse.getAppId(), PUBLIC, newArrayList(control));

        NewSubmissionCommand submissionCommand = newSubmissionCommand(loginResponse.getQrId(), loginResponse.getHomePageId(), rAnswer(control));
        String submissionId = SubmissionApi.newSubmission(null, submissionCommand);

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(1, submission.allAnswers().size());
    }

    @Test
    public void should_track_reference_id_when_new_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        SingleLineTextAnswer answer = rAnswer(control);
        NewSubmissionCommand command = NewSubmissionCommand.builder()
                .qrId(response.getQrId())
                .pageId(response.getHomePageId())
                .answers(Set.of(answer))
                .referenceData("someReferenceData")
                .build();

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), command);

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals("someReferenceData", submission.getReferenceData());
    }

    @Test
    public void should_not_save_answer_at_all_if_not_filled() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        SingleLineTextAnswer answer = rAnswerBuilder(control).content(null).build();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(0, submission.allAnswers().size());
    }

    @Test
    public void should_fail_create_submission_if_count_exceeds_packages_limit() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        tenant.setSubmissionCountForApp(response.getAppId(), Plan.FREE_PLAN.getMaxSubmissionCount());
        tenantRepository.save(tenant);

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), rAnswer(control));

        assertError(() -> newSubmissionRaw(response.getJwt(), command), SUBMISSION_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_fail_create_submission_if_answer_duplicated() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), rAnswer(control), rAnswer(control));

        assertError(() -> newSubmissionRaw(response.getJwt(), command), ANSWERS_DUPLICATED);
    }

    @Test
    public void should_fail_create_submission_for_no_fillable_page() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId());

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), rAnswer(control));
        assertError(() -> newSubmissionRaw(response.getJwt(), command), PAGE_NOT_FILLABLE);
    }

    @Test
    public void should_fail_create_submission_if_no_control_exists_for_answer() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), defaultSingleLineTextControl());

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), rAnswer(control));
        assertError(() -> newSubmissionRaw(response.getJwt(), command), CONTROL_NOT_EXIST_FOR_ANSWER);
    }

    @Test
    public void should_fail_create_submission_if_app_is_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        AppApi.deactivateApp(response.getJwt(), response.getAppId());

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), rAnswer(control));
        assertError(() -> newSubmissionRaw(response.getJwt(), command), APP_NOT_ACTIVE);
    }

    @Test
    public void should_fail_create_submission_for_once_per_instance_page_if_submission_already_exists() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder().submitType(ONCE_PER_INSTANCE).build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), rAnswer(control));

        assertError(() -> newSubmissionRaw(response.getJwt(), command), SUBMISSION_ALREADY_EXISTS_FOR_INSTANCE);
    }

    @Test
    public void should_fail_create_submission_for_once_per_member_page_if_submission_already_exists() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder().submitType(ONCE_PER_MEMBER).build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), rAnswer(control));

        assertError(() -> newSubmissionRaw(response.getJwt(), command), SUBMISSION_ALREADY_EXISTS_FOR_MEMBER);
    }

    @Test
    public void should_fail_create_submission_if_not_answer_mandatory_control() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId());

        assertError(() -> newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_fail_create_submission_if_member_not_login_but_required() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER, newArrayList(control));

        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), rAnswer(control));

        assertError(() -> newSubmissionRaw(null, command), AUTHENTICATION_FAILED);
    }

    @Test
    public void should_fail_create_submission_if_permission_for_page_not_enough() {
        PreparedQrResponse loginResponse = setupApi.registerWithQr(rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(loginResponse.getJwt(), loginResponse.getAppId(), AS_GROUP_MEMBER, newArrayList(control));
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(loginResponse.getJwt(), rMemberName(), rMobile(), rPassword());

        NewSubmissionCommand command = newSubmissionCommand(loginResponse.getQrId(), loginResponse.getHomePageId(), rAnswer(control));

        assertError(() -> newSubmissionRaw(memberResponse.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void should_fail_create_submission_if_permission_for_control_not_enough() {
        PreparedQrResponse loginResponse = setupApi.registerWithQr(rMobile(), rPassword());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(loginResponse.getJwt(), rMemberName(), rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_APP).build();
        AppApi.updateAppControls(loginResponse.getJwt(), loginResponse.getAppId(), control);

        NewSubmissionCommand command = newSubmissionCommand(loginResponse.getQrId(), loginResponse.getHomePageId(), rAnswer(control));

        assertError(() -> newSubmissionRaw(memberResponse.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void should_fail_create_submission_if_qr_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr();
        QrApi.deactivate(response.getJwt(), response.getQrId());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), rAnswer(control));

        assertError(() -> newSubmissionRaw(response.getJwt(), command), QR_NOT_ACTIVE);
    }

    @Test
    public void should_fail_create_submission_if_group_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr();
        GroupApi.createGroup(response.getJwt(), response.getAppId());

        GroupApi.deactivateGroup(response.getJwt(), response.getDefaultGroupId());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), rAnswer(control));

        assertError(() -> newSubmissionRaw(response.getJwt(), command), GROUP_NOT_ACTIVE);
    }

    @Test
    public void should_fail_create_submission_if_not_allowed_by_circulation() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Page page1 = defaultPageBuilder().build();
        Page page2 = defaultPageBuilder().build();
        AppApi.updateAppPages(response.getJwt(), response.getAppId(), page1, page2);

        TextOption option1 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(10) + "选项").build();
        CirculationStatusSetting setting = CirculationStatusSetting.builder()
                .options(List.of(option1, option2))
                .initOptionId(option1.getId())
                .statusAfterSubmissions(List.of())
                .statusPermissions(List.of(
                        StatusPermission.builder().id(newShortUuid()).optionId(option1.getId()).notAllowedPageIds(List.of(page1.getId())).build()))
                .build();
        AppApi.updateCirculationStatusSetting(response.getJwt(), response.getAppId(), setting);

        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        NewSubmissionCommand command = newSubmissionCommand(qrResponse.getQrId(), page1.getId());
        assertError(() -> newSubmissionRaw(response.getJwt(), command), SUBMISSION_NOT_ALLOWED_BY_CIRCULATION);
    }

    @Test
    public void should_update_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SingleLineTextAnswer updateAnswer = rAnswer(control);
        SubmissionApi.updateSubmission(response.getJwt(), submissionId, updateAnswer);

        Submission submission = submissionRepository.byId(submissionId);
        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(updateAnswer, submission.answerForControlOptional(control.getId()).get());
        assertEquals(member.getId(), submission.getUpdatedBy());
        assertEquals(member.getName(), submission.getUpdater());
    }

    @Test
    public void tenant_admin_can_update_submission_even_after_approved() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(true)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.approveSubmission(response.getJwt(), submissionId, true);

        SingleLineTextAnswer updateAnswer = rAnswer(control);
        SubmissionApi.updateSubmission(response.getJwt(), submissionId, updateAnswer);

        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(updateAnswer, submission.answerForControlOptional(control.getId()).get());
    }


    @Test
    public void should_not_update_answer_if_answer_not_provided() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        SubmissionApi.updateSubmission(response.getJwt(), submissionId);

        Submission submission = submissionRepository.byId(submissionId);
        assertFalse(submission.allAnswers().isEmpty());
    }

    @Test
    public void should_update_answer_with_answer_provided_but_not_filled() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        SingleLineTextAnswer updateAnswer = rAnswerBuilder(control).content(null).build();
        SubmissionApi.updateSubmission(response.getJwt(), submissionId, updateAnswer);

        Submission submission = submissionRepository.byId(submissionId);
        assertTrue(submission.allAnswers().isEmpty());
    }

    @Test
    public void should_raise_event_when_update_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        SingleLineTextAnswer updateAnswer = rAnswer(control);
        SubmissionApi.updateSubmission(response.getJwt(), submissionId, updateAnswer);

        SubmissionUpdatedEvent submissionUpdatedEvent = domainEventDao.latestEventFor(submissionId, SUBMISSION_UPDATED, SubmissionUpdatedEvent.class);
        assertEquals(submissionId, submissionUpdatedEvent.getSubmissionId());
        QR qr = qrRepository.byId(response.getQrId());
        TextAttributeValue attributeValue = (TextAttributeValue) qr.attributeValueOf(attribute.getId());
        assertEquals(updateAnswer.getContent(), attributeValue.getText());
    }

    @Test
    public void submitter_should_able_to_update_their_own_submission_if_change_allowed() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(true)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(false).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        SingleLineTextAnswer updatedAnswer = rAnswer(control);
        SubmissionApi.updateSubmission(memberResponse.getJwt(), submissionId, updatedAnswer);

        Submission submission = submissionRepository.byId(submissionId);
        SingleLineTextAnswer loadedAnswer = (SingleLineTextAnswer) submission.allAnswers().get(control.getId());
        assertEquals(updatedAnswer.getContent(), loadedAnswer.getContent());
    }

    @Test
    public void submitter_should_fail_update_own_submission_if_update_not_allowed() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(false).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        UpdateSubmissionCommand updateSubmissionCommand = updateSubmissionCommand(rAnswer(control));

        assertError(() -> SubmissionApi.updateSubmissionRaw(memberResponse.getJwt(), submissionId, updateSubmissionCommand), PAGE_NOT_ALLOW_CHANGE_BY_SUBMITTER);
    }

    @Test
    public void submitter_should_fail_update_own_submission_if_update_allowed_but_allowed_period_expired() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(true)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(false).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        Submission submission = submissionRepository.byId(submissionId);
        ReflectionTestUtils.setField(submission, "createdAt", Instant.now().minusSeconds(7200));
        submissionRepository.save(submission);

        UpdateSubmissionCommand updateSubmissionCommand = updateSubmissionCommand(rAnswer(control));

        assertError(() -> SubmissionApi.updateSubmissionRaw(memberResponse.getJwt(), submissionId, updateSubmissionCommand), UPDATE_PERIOD_EXPIRED);
    }

    @Test
    public void update_submission_should_require_login_even_for_public_page() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(PUBLIC)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(false).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        UpdateSubmissionCommand updateSubmissionCommand = updateSubmissionCommand(rAnswer(control));

        assertError(() -> SubmissionApi.updateSubmissionRaw(null, submissionId, updateSubmissionCommand), AUTHENTICATION_FAILED);
    }

    @Test
    public void un_permissioned_member_should_fail_to_update_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();

        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(false).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        UpdateSubmissionCommand updateSubmissionCommand = updateSubmissionCommand(rAnswer(control));

        assertError(() -> SubmissionApi.updateSubmissionRaw(memberResponse.getJwt(), submissionId, updateSubmissionCommand), ACCESS_DENIED);
    }

    @Test
    public void submitter_should_fail_update_submission_once_approved() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(true)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.approveSubmission(response.getJwt(), submissionId, true);

        UpdateSubmissionCommand updateSubmissionCommand = updateSubmissionCommand(rAnswer(control));

        assertError(() -> SubmissionApi.updateSubmissionRaw(memberResponse.getJwt(), submissionId, updateSubmissionCommand), CANNOT_UPDATE_APPROVED_SUBMISSION);
    }

    @Test
    public void should_fail_update_submission_if_qr_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QrApi.deactivate(response.getJwt(), response.getQrId());

        UpdateSubmissionCommand updateSubmissionCommand = updateSubmissionCommand(rAnswer(control));
        assertError(() -> updateSubmissionRaw(response.getJwt(), submissionId, updateSubmissionCommand), QR_NOT_ACTIVE);
    }

    @Test
    public void should_approve_submission() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        ApproveSubmissionCommand command = approveSubmissionCommand(true);
        SubmissionApi.approveSubmission(response.getJwt(), submissionId, command);
        SubmissionApprovedEvent approvedEvent = domainEventDao.latestEventFor(submissionId, SUBMISSION_APPROVED, SubmissionApprovedEvent.class);
        assertEquals(submissionId, approvedEvent.getSubmissionId());

        Submission submission = submissionRepository.byId(submissionId);
        SubmissionApproval approval = submission.getApproval();
        assertNotNull(approval);
        assertEquals(command.isPassed(), approval.isPassed());
        assertEquals(command.getNote(), approval.getNote());
        assertEquals(response.getMemberId(), approval.getApprovedBy());
    }

    @Test
    public void un_permissioned_member_should_fail_approve_submission() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        ApproveSubmissionCommand command = approveSubmissionCommand(true);

        assertError(() -> SubmissionApi.approveSubmissionRaw(memberResponse.getJwt(), submissionId, command), ACCESS_DENIED);
    }


    @Test
    public void should_fail_to_approve_already_approved_submission() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        ApproveSubmissionCommand command = approveSubmissionCommand(true);
        SubmissionApi.approveSubmission(response.getJwt(), submissionId, command);

        assertError(() -> SubmissionApi.approveSubmissionRaw(response.getJwt(), submissionId, command), SUBMISSION_ALREADY_APPROVED);
    }


    @Test
    public void should_fail_to_approve_submission_if_approval_not_enabled() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(false).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        ApproveSubmissionCommand command = approveSubmissionCommand(true);

        assertError(() -> SubmissionApi.approveSubmissionRaw(response.getJwt(), submissionId, command), APPROVAL_NOT_ENABLED);
    }

    @Test
    public void should_fail_to_approve_submission_if_package_approval_not_enabled() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(false).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        ApproveSubmissionCommand command = approveSubmissionCommand(true);

        assertError(() -> SubmissionApi.approveSubmissionRaw(response.getJwt(), submissionId, command), SUBMISSION_APPROVE_NOT_ALLOWED);
    }


    @Test
    public void should_fail_approve_submission_if_qr_inactive() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        QrApi.deactivate(response.getJwt(), response.getQrId());

        ApproveSubmissionCommand command = approveSubmissionCommand(true);
        assertError(() -> SubmissionApi.approveSubmissionRaw(response.getJwt(), submissionId, command), QR_NOT_ACTIVE);
    }

    @Test
    public void should_delete_submission() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        Submission submission = submissionRepository.byId(submissionId);
        assertEquals(1, submission.allAnswers().size());
        assertEquals(response.getAppId(), submission.getAppId());

        SubmissionApi.deleteSubmission(response.getJwt(), submissionId);

        Optional<Submission> deleted = submissionRepository.byIdOptional(submissionId);
        assertTrue(deleted.isEmpty());
    }

    @Test
    public void should_raise_event_when_delete_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(PAGE_SUBMIT_COUNT).pageId(response.getHomePageId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        SubmissionApi.deleteSubmission(response.getJwt(), submissionId);

        SubmissionDeletedEvent submissionDeletedEvent = domainEventDao.latestEventFor(submissionId, SUBMISSION_DELETED, SubmissionDeletedEvent.class);
        assertEquals(submissionId, submissionDeletedEvent.getSubmissionId());
        QR qr = qrRepository.byId(response.getQrId());
        IntegerAttributeValue attributeValue = (IntegerAttributeValue) qr.attributeValueOf(attribute.getId());
        assertEquals(0, attributeValue.getNumber());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(0, tenant.getResourceUsage().getSubmissionCountForApp(response.getAppId()));
    }

    @Test
    public void should_list_submit_history_submissions() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER, newArrayList(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);
        assertEquals(1, list.getData().size());
    }

    @Test
    public void should_list_my_submitted_submissions() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER);
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER, control);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(memberResponse.getJwt(), command);

        assertEquals(1, list.getData().size());
        assertEquals(submissionId, list.getData().get(0).getId());
    }

    @Test
    public void should_list_approvable_submissions() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String approvedSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.approveSubmission(response.getJwt(), approvedSubmissionId, approveSubmissionCommand(true));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(TO_BE_APPROVED)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);
        assertEquals(1, list.getData().size());
        assertEquals(submissionId, list.getData().get(0).getId());
    }

    @Test
    public void should_list_my_submissions_for_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER, control);
        String newGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), newGroupId);
        String defaultGroupedSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String newGroupedSubmissionId = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .groupId(newGroupId)
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(1, list.getData().size());
        assertEquals(newGroupedSubmissionId, list.getData().get(0).getId());
    }

    @Test
    public void should_list_my_submissions_with_group_filters() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(appResponse.getJwt(), appResponse.getAppId(), AS_TENANT_MEMBER, control);

        String groupId1 = GroupApi.createGroup(appResponse.getJwt(), appResponse.getAppId());
        CreateQrResponse qrResponse1 = QrApi.createQr(appResponse.getJwt(), groupId1);
        String submissionId1 = SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse1.getQrId(), appResponse.getHomePageId(), rAnswer(control));

        String groupId2 = GroupApi.createGroup(appResponse.getJwt(), appResponse.getAppId());
        CreateQrResponse qrResponse2 = QrApi.createQr(appResponse.getJwt(), groupId2);
        String submissionId2 = SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse2.getQrId(), appResponse.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(appResponse.getAppId())
                .type(SUBMITTER_SUBMISSION)
                .filterables(Map.of("groupId", newHashSet(groupId1)))
                .pageIndex(1)
                .pageSize(20)
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(appResponse.getJwt(), command);

        assertEquals(1, list.getData().size());
        assertEquals(submissionId1, list.getData().get(0).getId());
    }


    @Test
    public void should_list_my_submissions_with_created_filters() {
        PreparedAppResponse appResponse = setupApi.registerWithApp();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(appResponse.getJwt(), appResponse.getAppId(), AS_TENANT_MEMBER, control);

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(appResponse.getJwt());

        CreateQrResponse qrResponse = QrApi.createQr(appResponse.getJwt(), appResponse.getDefaultGroupId());
        String submissionId1 = SubmissionApi.newSubmission(appResponse.getJwt(), qrResponse.getQrId(), appResponse.getHomePageId(), rAnswer(control));
        String submissionId2 = SubmissionApi.newSubmission(memberResponse.getJwt(), qrResponse.getQrId(), appResponse.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(appResponse.getAppId())
                .type(ALL_SUBMIT_HISTORY)
                .filterables(Map.of("createdBy", newHashSet(memberResponse.getMemberId())))
                .pageIndex(1)
                .pageSize(20)
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(appResponse.getJwt(), command);

        assertEquals(1, list.getData().size());
        assertEquals(submissionId2, list.getData().get(0).getId());
    }


    @Test
    public void should_list_my_submissions_for_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER, control);
        String newGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), newGroupId);
        String defaultGroupedSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String newGroupedSubmissionId = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .qrId(qrResponse.getQrId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(1, list.getData().size());
        QListSubmission submission = list.getData().get(0);
        assertEquals(newGroupedSubmissionId, submission.getId());
        assertEquals(qrResponse.getQrId(), submission.getQrId());
    }

    @Test
    public void should_list_my_submissions_for_page() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl homePageControl = defaultSingleLineTextControl();
        Page homePage = defaultPageBuilder().controls(newArrayList(homePageControl)).build();
        FSingleLineTextControl childPageControl = defaultSingleLineTextControl();
        Page childPage = defaultPageBuilder().controls(newArrayList(childPageControl)).build();
        AppApi.updateAppPages(response.getJwt(), response.getAppId(), homePage, childPage);
        String homePageSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), homePage.getId(), rAnswer(homePageControl));
        String childPageSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), childPage.getId(), rAnswer(childPageControl));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(homePage.getId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(1, list.getData().size());
        QListSubmission submission = list.getData().get(0);
        assertEquals(homePageSubmissionId, submission.getId());
    }

    @Test
    public void should_fail_list_my_submissions_if_no_permission_for_qr_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        Page page = defaultPageBuilder().controls(newArrayList(control)).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .qrId(response.getQrId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_VIEWABLE_PERMISSION_FOR_QR);
    }

    @Test
    public void should_fail_list_my_submission_if_no_groups() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        Page page = defaultPageBuilder().controls(newArrayList(control)).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_VIEWABLE_GROUPS);
    }

    @Test
    public void should_fail_list_my_submissions_if_no_permission_for_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        Page page = defaultPageBuilder().controls(newArrayList(control)).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .groupId(response.getDefaultGroupId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_VIEWABLE_PERMISSION_FOR_GROUP);
    }

    @Test
    public void should_fail_list_my_submissions_if_no_pages() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Page page = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(response.getJwt(), command), NO_VIEWABLE_PAGES);
    }

    @Test
    public void should_fail_list_my_submissions_if_no_permission_for_page() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_GROUP_MEMBER);
        Page homePage = defaultPageBuilder().controls(newArrayList(defaultSingleLineTextControl())).setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).build()).build();
        Page childPage = defaultPageBuilder().controls(newArrayList(defaultSingleLineTextControl())).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_APP).build()).build();
        AppApi.updateAppPages(response.getJwt(), response.getAppId(), homePage, childPage);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupMembers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(childPage.getId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_VIEWABLE_PERMISSION_FOR_PAGE);
    }

    @Test
    public void should_list_history_submissions_for_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER, control);
        String newGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), newGroupId);
        String defaultGroupedSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String newGroupedSubmissionId = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .groupId(newGroupId)
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(1, list.getData().size());
        assertEquals(newGroupedSubmissionId, list.getData().get(0).getId());
    }

    @Test
    public void should_list_history_submissions_for_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER, control);
        String newGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), newGroupId);
        String defaultGroupedSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String newGroupedSubmissionId = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .qrId(qrResponse.getQrId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(1, list.getData().size());
        QListSubmission submission = list.getData().get(0);
        assertEquals(newGroupedSubmissionId, submission.getId());
        assertEquals(qrResponse.getQrId(), submission.getQrId());
    }

    @Test
    public void should_list_history_submissions_for_page() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl homePageControl = defaultSingleLineTextControl();
        Page homePage = defaultPageBuilder().controls(newArrayList(homePageControl)).build();
        FSingleLineTextControl childPageControl = defaultSingleLineTextControl();
        Page childPage = defaultPageBuilder().controls(newArrayList(childPageControl)).build();
        AppApi.updateAppPages(response.getJwt(), response.getAppId(), homePage, childPage);
        String homePageSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), homePage.getId(), rAnswer(homePageControl));
        String childPageSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), childPage.getId(), rAnswer(childPageControl));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(homePage.getId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(1, list.getData().size());
        QListSubmission submission = list.getData().get(0);
        assertEquals(homePageSubmissionId, submission.getId());
    }

    @Test
    public void should_fail_list_history_submissions_if_no_permission_for_qr_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        Page page = defaultPageBuilder().controls(newArrayList(control)).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .qrId(response.getQrId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_MANAGABLE_PERMISSION_FOR_QR);
    }

    @Test
    public void should_fail_list_history_submission_if_no_groups() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        Page page = defaultPageBuilder().controls(newArrayList(control)).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_MANAGABLE_GROUPS);
    }

    @Test
    public void should_fail_list_history_submissions_if_no_permission_for_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        Page page = defaultPageBuilder().controls(newArrayList(control)).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .groupId(response.getDefaultGroupId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_MANAGABLE_PERMISSION_FOR_GROUP);
    }

    @Test
    public void should_fail_list_history_submissions_if_no_pages() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Page page = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(CAN_MANAGE_GROUP).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(response.getJwt(), command), NO_MANAGABLE_PAGES);
    }

    @Test
    public void should_fail_list_history_submissions_if_no_permission_for_page() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Page homePage = defaultPageBuilder().controls(newArrayList()).setting(defaultPageSettingBuilder().permission(AS_TENANT_MEMBER).build()).build();
        AppApi.updateAppPages(response.getJwt(), response.getAppId(), homePage);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(homePage.getId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_MANAGABLE_PERMISSION_FOR_PAGE);
    }

    @Test
    public void should_list_approvable_submissions_for_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String newGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), newGroupId);

        String defaultGroupSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String newGroupSubmissionId = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .groupId(newGroupId)
                .type(TO_BE_APPROVED)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);
        assertEquals(1, list.getData().size());
        assertEquals(newGroupSubmissionId, list.getData().get(0).getId());
    }

    @Test
    public void should_list_approvable_submissions_for_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String newGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), newGroupId);

        String defaultGroupSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String newGroupSubmissionId = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .qrId(qrResponse.getQrId())
                .type(TO_BE_APPROVED)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);
        assertEquals(1, list.getData().size());
        assertEquals(newGroupSubmissionId, list.getData().get(0).getId());
    }

    @Test
    public void should_list_approvable_submissions_for_page() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control1 = defaultSingleLineTextControl();
        Page approvablePage1 = defaultPageBuilder().controls(newArrayList(control1))
                .setting(defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).build()).build()).build();
        FSingleLineTextControl control2 = defaultSingleLineTextControl();
        Page approvablePage2 = defaultPageBuilder().controls(newArrayList(control2))
                .setting(defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).build()).build()).build();
        AppApi.updateAppPages(response.getJwt(), response.getAppId(), approvablePage1, approvablePage2);
        String submissionId1 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), approvablePage1.getId(), rAnswer(control1));
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), approvablePage2.getId(), rAnswer(control2));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(approvablePage1.getId())
                .type(TO_BE_APPROVED)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(1, list.getData().size());
        assertEquals(submissionId1, list.getData().get(0).getId());
    }

    @Test
    public void should_fail_list_approvable_submissions_if_no_permission_for_qr_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        Page page = defaultPageBuilder().controls(newArrayList(control))
                .setting(defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().permission(CAN_MANAGE_GROUP).approvalEnabled(true).build()).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .qrId(response.getQrId())
                .type(TO_BE_APPROVED)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_APPROVABLE_PERMISSION_FOR_QR);
    }

    @Test
    public void should_fail_list_approvable_submission_if_no_groups() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        Page page = defaultPageBuilder().controls(newArrayList(control))
                .setting(defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().permission(CAN_MANAGE_GROUP).approvalEnabled(true).build()).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(TO_BE_APPROVED)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_APPROVABLE_GROUPS);
    }

    @Test
    public void should_fail_list_approvable_submissions_if_no_permission_for_group() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        Page page = defaultPageBuilder().controls(newArrayList(control))
                .setting(defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().permission(CAN_MANAGE_GROUP).approvalEnabled(true).build()).build()).build();
        AppApi.updateAppPage(response.getJwt(), response.getAppId(), page);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .groupId(response.getDefaultGroupId())
                .type(TO_BE_APPROVED)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(memberResponse.getJwt(), command), NO_APPROVABLE_PERMISSION_FOR_GROUP);
    }

    @Test
    public void should_fail_approvable_history_submissions_if_no_pages() {
        PreparedQrResponse response = setupApi.registerWithQr();

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(TO_BE_APPROVED)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(response.getJwt(), command), NO_APPROVABLE_PAGES);
    }

    @Test
    public void should_fail_list_approvable_submissions_if_no_permission_for_page() {
        PreparedQrResponse response = setupApi.registerWithQr();

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .type(TO_BE_APPROVED)
                .pageIndex(1)
                .pageSize(20)
                .build();

        assertError(() -> SubmissionApi.listSubmissionsRaw(response.getJwt(), command), NO_APPROVABLE_PERMISSION_FOR_PAGE);
    }

    @Test
    public void should_list_submissions_with_filters() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FRadioControl radioControl = defaultRadioControlBuilder().options(rTextOptions(10)).build();
        FCheckboxControl checkboxControl = defaultCheckboxControlBuilder().options(rTextOptions(10)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), radioControl, checkboxControl);

        RadioAnswer firstRadioAnswer = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(0).getId()).build();
        CheckboxAnswer firstCheckboxAnswer = rAnswerBuilder(checkboxControl).optionIds(newArrayList(checkboxControl.getOptions().get(0).getId(), checkboxControl.getOptions().get(1).getId())).build();
        String firstSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), firstRadioAnswer, firstCheckboxAnswer);

        RadioAnswer secondRadioAnswer = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(1).getId()).build();
        CheckboxAnswer secondCheckboxAnswer = rAnswerBuilder(checkboxControl).optionIds(newArrayList(checkboxControl.getOptions().get(1).getId(), checkboxControl.getOptions().get(2).getId())).build();
        String secondSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), secondRadioAnswer, secondCheckboxAnswer);

        RadioAnswer thirdRadioAnswer = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(2).getId()).build();
        CheckboxAnswer thirdCheckboxAnswer = rAnswerBuilder(checkboxControl).optionIds(newArrayList(checkboxControl.getOptions().get(2).getId(), checkboxControl.getOptions().get(3).getId())).build();
        String thirdSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), thirdRadioAnswer, thirdCheckboxAnswer);

        RadioAnswer fourthRadioAnswer = rAnswerBuilder(radioControl).optionId(radioControl.getOptions().get(3).getId()).build();
        CheckboxAnswer fourthCheckboxAnswer = rAnswerBuilder(checkboxControl).optionIds(newArrayList(checkboxControl.getOptions().get(4).getId(), checkboxControl.getOptions().get(5).getId())).build();
        String fourthSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), fourthRadioAnswer, fourthCheckboxAnswer);

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .type(ALL_SUBMIT_HISTORY)
                .filterables(Map.of(
                        radioControl.getId(), newHashSet(firstRadioAnswer.getOptionId(), secondRadioAnswer.getOptionId(), thirdRadioAnswer.getOptionId()),
                        checkboxControl.getId(), newHashSet(firstCheckboxAnswer.getOptionIds().get(0), secondCheckboxAnswer.getOptionIds().get(0), fourthCheckboxAnswer.getOptionIds().get(0))))
                .pageIndex(1)
                .pageSize(20)
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(2, list.getData().size());
        Set<String> submissionIds = list.getData().stream().map(QListSubmission::getId).collect(toSet());
        assertTrue(submissionIds.contains(firstSubmissionId));
        assertTrue(submissionIds.contains(secondSubmissionId));
    }

    @Test
    public void should_list_submissions_with_approval_filters() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder().approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));

        String approvedYesSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.approveSubmission(response.getJwt(), approvedYesSubmissionId, approveSubmissionCommand(true));
        String approvedNoSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.approveSubmission(response.getJwt(), approvedNoSubmissionId, approveSubmissionCommand(false));
        String notApprovedSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .type(ALL_SUBMIT_HISTORY)
                .filterables(Map.of(
                        "approval", newHashSet("YES", "NONE")
                ))
                .pageIndex(1)
                .pageSize(20)
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);
        assertEquals(2, list.getData().size());
        Set<String> submissionIds = list.getData().stream().map(QListSubmission::getId).collect(toSet());
        assertTrue(submissionIds.contains(approvedYesSubmissionId));
        assertTrue(submissionIds.contains(notApprovedSubmissionId));
    }

    @Test
    public void should_list_submissions_with_search() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FMobileNumberControl control = defaultMobileControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        MobileNumberAnswer firstAnswer = rAnswerBuilder(control).mobileNumber(rMobile()).build();
        String firstSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), firstAnswer);
        MobileNumberAnswer secondAnswer = rAnswerBuilder(control).mobileNumber(rMobile()).build();
        String secondSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), secondAnswer);

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(ALL_SUBMIT_HISTORY)
                .search(firstAnswer.getMobileNumber())
                .pageIndex(1)
                .pageSize(20)
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(1, list.getData().size());
        assertEquals(firstSubmissionId, list.getData().get(0).getId());
    }

    @Test
    public void should_list_submissions_with_date_range() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String submissionId1 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String submissionId3 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        Submission submission1 = submissionRepository.byId(submissionId1);
        ReflectionTestUtils.setField(submission1, "createdAt", LocalDate.of(2011, 3, 3).atStartOfDay(systemDefault()).toInstant());
        submissionRepository.save(submission1);

        Submission submission2 = submissionRepository.byId(submissionId2);
        ReflectionTestUtils.setField(submission2, "createdAt", LocalDate.of(2011, 3, 6).atStartOfDay(systemDefault()).toInstant());
        submissionRepository.save(submission2);

        Submission submission3 = submissionRepository.byId(submissionId3);
        ReflectionTestUtils.setField(submission3, "createdAt", LocalDate.of(2011, 3, 9).atStartOfDay(systemDefault()).toInstant());
        submissionRepository.save(submission3);

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .startDate("2011-03-04")
                .endDate("2011-03-07")
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);
        assertEquals(1, list.getData().size());
        assertEquals(submission2.getId(), list.getData().get(0).getId());
    }


    @Test
    public void should_list_submissions_with_default_sort_by_created_at() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String firstSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String secondSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);
        assertEquals(secondSubmissionId, list.getData().get(0).getId());
    }

    @Test
    public void should_list_submissions_with_sort_on_control() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String firstSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(control).number(1d).build());
        String secondSubmissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(control).number(2d).build());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(ALL_SUBMIT_HISTORY)
                .sortedBy(control.getId())
                .ascSort(true)
                .pageIndex(1)
                .pageSize(20)
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);
        assertEquals(firstSubmissionId, list.getData().get(0).getId());
    }

    @Test
    public void should_list_submissions_with_only_permissioned_answers() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER);
        FSingleLineTextControl control = defaultSingleLineTextControl();
        FSingleLineTextControl permissionedControl = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_APP).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control, permissionedControl);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.updateSubmission(response.getJwt(), submissionId, rAnswer(control), rAnswer(permissionedControl));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(memberResponse.getJwt(), command);

        assertEquals(1, list.getData().size());
        QListSubmission submission = list.getData().get(0);
        assertEquals(1, submission.getDisplayAnswers().size());
        assertTrue(submission.getDisplayAnswers().containsKey(control.getId()));
        assertFalse(submission.getDisplayAnswers().containsKey(permissionedControl.getId()));
    }

    @Test
    public void should_list_submissions_with_submitter_viewable_answers() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER);
        FSingleLineTextControl control = defaultSingleLineTextControl();
        FSingleLineTextControl permissionedControl = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_APP).submitterViewable(true).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control, permissionedControl);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.updateSubmission(response.getJwt(), submissionId, rAnswer(control), rAnswer(permissionedControl));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(memberResponse.getJwt(), command);

        assertEquals(1, list.getData().size());
        QListSubmission submission = list.getData().get(0);
        assertEquals(2, submission.getDisplayAnswers().size());
        assertTrue(submission.getDisplayAnswers().containsKey(control.getId()));
        assertTrue(submission.getDisplayAnswers().containsKey(permissionedControl.getId()));
    }

    @Test
    public void managers_should_always_view_control_answers_even_if_no_permission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl permissionedControl = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_APP).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), permissionedControl);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(permissionedControl));

        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());
        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();
        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(memberResponse.getJwt(), command);

        assertEquals(1, list.getData().size());
        QListSubmission submission = list.getData().get(0);
        assertEquals(1, submission.getDisplayAnswers().size());
        assertTrue(submission.getDisplayAnswers().containsKey(permissionedControl.getId()));
    }

    @Test
    public void should_not_filter_if_control_permission_enabled_but_no_submitter_viewable_permission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl permissionedControl = defaultCheckboxControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_GROUP).submitterViewable(false).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), permissionedControl);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());

        CheckboxAnswer checkboxAnswer = rAnswerBuilder(permissionedControl).optionIds(List.of(permissionedControl.getOptions().get(0).getId())).build();
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), checkboxAnswer);

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .type(SUBMITTER_SUBMISSION)
                .filterables(Map.of(permissionedControl.getId(), newHashSet(permissionedControl.getOptions().get(1).getId())))
                .pageIndex(1)
                .pageSize(20)
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(memberResponse.getJwt(), command);
        assertEquals(1, list.getData().size());
    }


    @Test
    public void should_not_sort_if_control_permission_enabled_but_no_submitter_viewable_permission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FNumberInputControl permissionedControl = defaultNumberInputControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_GROUP).submitterViewable(false).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), permissionedControl);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());

        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(permissionedControl).number(10d).build());
        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswerBuilder(permissionedControl).number(11d).build());

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .type(SUBMITTER_SUBMISSION)
                .sortedBy(permissionedControl.getId())
                .ascSort(false)
                .pageIndex(1)
                .pageSize(20)
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(memberResponse.getJwt(), command);
        assertEquals(2, list.getData().size());
        assertEquals(10, ((NumberDisplayValue) list.getData().get(0).getDisplayAnswers().get(permissionedControl.getId())).getNumber());
    }

    @Test
    public void list_viewable_submissions_should_include_sub_groups() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER);
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER, control);

        String submissionId1 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), subGroupId);
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(SUBMITTER_SUBMISSION)
                .pageIndex(1)
                .pageSize(20)
                .groupId(response.getDefaultGroupId())
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(2, list.getData().size());
        List<String> submissionIds = list.getData().stream().map(QListSubmission::getId).toList();
        assertTrue(submissionIds.contains(submissionId1));
        assertTrue(submissionIds.contains(submissionId2));
    }

    @Test
    public void list_history_submissions_should_include_sub_groups() {
        PreparedQrResponse response = setupApi.registerWithQr();
        AppApi.updateAppOperationPermission(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER);
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), AS_TENANT_MEMBER, control);

        String submissionId1 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), subGroupId);
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .groupId(response.getDefaultGroupId())
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(2, list.getData().size());
        List<String> submissionIds = list.getData().stream().map(QListSubmission::getId).toList();
        assertTrue(submissionIds.contains(submissionId1));
        assertTrue(submissionIds.contains(submissionId2));
    }

    @Test
    public void should_list_approvable_submissions_with_sub_groups() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder().approvalSetting(defaultPageApproveSettingBuilder().approvalEnabled(true).permission(CAN_MANAGE_APP).build()).build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));

        String submissionId1 = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), subGroupId);
        String submissionId2 = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), rAnswer(control));

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .type(TO_BE_APPROVED)
                .pageIndex(1)
                .pageSize(20)
                .groupId(response.getDefaultGroupId())
                .build();

        PagedList<QListSubmission> list = SubmissionApi.listSubmissions(response.getJwt(), command);

        assertEquals(2, list.getData().size());
        List<String> submissionIds = list.getData().stream().map(QListSubmission::getId).toList();
        assertTrue(submissionIds.contains(submissionId1));
        assertTrue(submissionIds.contains(submissionId2));
    }

    @Test
    public void should_export_submissions_to_excel() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        FRadioControl radioControl = defaultRadioControl();
        RadioAnswer radioAnswer = rAnswer(radioControl);

        FCheckboxControl checkboxControl = defaultCheckboxControl();
        CheckboxAnswer checkboxAnswer = rAnswer(checkboxControl);

        FSingleLineTextControl singleLineTextControl = defaultSingleLineTextControl();
        SingleLineTextAnswer singleLineTextAnswer = rAnswer(singleLineTextControl);

        FDropdownControl dropdownControl = defaultDropdownControl();
        DropdownAnswer dropdownAnswer = rAnswer(dropdownControl);

        FMemberSelectControl memberSelectControl = defaultMemberSelectControl();
        MemberSelectAnswer memberSelectAnswer = rAnswer(memberSelectControl, response.getMemberId());

        FAddressControl addressControl = defaultAddressControlBuilder().precision(4).build();
        AddressAnswer addressAnswer = rAnswer(addressControl);

        FGeolocationControl geolocationControl = defaultGeolocationControl();
        GeolocationAnswer geolocationAnswer = rAnswer(geolocationControl);

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        NumberInputAnswer numberInputAnswer = rAnswer(numberInputControl);

        FNumberRankingControl numberRankingControl = defaultNumberRankingControl();
        NumberRankingAnswer numberRankingAnswer = rAnswer(numberRankingControl);

        FMobileNumberControl mobileNumberControl = defaultMobileControl();
        MobileNumberAnswer mobileNumberAnswer = rAnswer(mobileNumberControl);

        FIdentifierControl identifierControl = defaultIdentifierControl();
        IdentifierAnswer identifierAnswer = rAnswer(identifierControl);

        FEmailControl emailControl = defaultEmailControl();
        EmailAnswer emailAnswer = rAnswer(emailControl);

        FDateControl dateControl = defaultDateControl();
        DateAnswer dateAnswer = rAnswer(dateControl);

        FTimeControl timeControl = defaultTimeControl();
        TimeAnswer timeAnswer = rAnswer(timeControl);

        FItemCountControl itemCountControl = defaultItemCountControl();
        ItemCountAnswer itemCountAnswer = rAnswer(itemCountControl);

        FItemStatusControl itemStatusControl = defaultItemStatusControl();
        ItemStatusAnswer itemStatusAnswer = rAnswer(itemStatusControl);

        FPointCheckControl pointCheckControl = defaultPointCheckControl();
        PointCheckAnswer pointCheckAnswer = rAnswer(pointCheckControl);

        FMultiLevelSelectionControl multiLevelSelectionControl = defaultMultiLevelSelectionControlBuilder()
                .titleText("省份/城市")
                .optionText("四川省/成都市\n四川省/绵阳市")
                .build();

        MultiLevelSelectionAnswer multiLevelSelectionAnswer = rAnswerBuilder(multiLevelSelectionControl).selection(MultiLevelSelection.builder()
                .level1("四川省")
                .level2("成都市")
                .build()).build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(),
                radioControl,
                checkboxControl,
                singleLineTextControl,
                dropdownControl,
                memberSelectControl,
                addressControl,
                geolocationControl,
                numberInputControl,
                numberRankingControl,
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

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                radioAnswer,
                checkboxAnswer,
                singleLineTextAnswer,
                dropdownAnswer,
                memberSelectAnswer,
                addressAnswer,
                geolocationAnswer,
                numberInputAnswer,
                numberRankingAnswer,
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

        ListSubmissionsQuery command = ListSubmissionsQuery.builder()
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .type(ALL_SUBMIT_HISTORY)
                .pageIndex(1)
                .pageSize(20)
                .build();
        byte[] exportBytes = SubmissionApi.exportSubmissionsAsExcel(response.getJwt(), command);
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
        Submission submission = submissionRepository.byId(submissionId);
        Member member = memberRepository.byId(response.getMemberId());
        Map<Integer, String> record = result.get(0);
        assertEquals(submissionId, record.get(0));
        assertEquals(qr.getName(), record.get(1));
        assertEquals(group.getName(), record.get(2));
        assertEquals(MRY_DATE_TIME_FORMATTER.format(submission.getCreatedAt()), record.get(3));
        assertEquals(member.getName(), record.get(4));

        assertEquals(radioControl.getOptions().stream().collect(toMap(TextOption::getId, TextOption::getName)).get(radioAnswer.getOptionId()), record.get(5));

        Map<String, String> checkboxOptionMap = checkboxControl.getOptions().stream().collect(toMap(TextOption::getId, TextOption::getName));
        assertEquals(checkboxAnswer.getOptionIds().stream().map(checkboxOptionMap::get).collect(Collectors.joining(", ")), record.get(6));

        assertEquals(singleLineTextAnswer.getContent(), record.get(7));

        Map<String, String> dropdownOptionMap = dropdownControl.getOptions().stream().collect(toMap(TextOption::getId, TextOption::getName));
        assertEquals(dropdownAnswer.getOptionIds().stream().map(dropdownOptionMap::get).collect(Collectors.joining(", ")), record.get(8));

        assertEquals(member.getName(), record.get(9));
        assertEquals(addressAnswer.getAddress().toText(), record.get(10));
        assertEquals(geolocationAnswer.getGeolocation().toText(), record.get(11));
        assertEquals(numberInputAnswer.getNumber(), Double.valueOf(record.get(12)));
        assertEquals(numberRankingAnswer.getRank(), Integer.valueOf(record.get(13)));
        assertEquals(mobileNumberAnswer.getMobileNumber(), record.get(14));
        assertEquals(identifierAnswer.getContent(), record.get(15));
        assertEquals(emailAnswer.getEmail(), record.get(16));
        assertEquals(dateAnswer.getDate(), record.get(17));
        assertEquals(timeAnswer.getTime(), record.get(18));

        Map<String, String> itemCountOptionMap = itemCountControl.getOptions().stream().collect(toMap(TextOption::getId, TextOption::getName));
        assertEquals(itemCountAnswer.getItems().stream().map(countedItem -> itemCountOptionMap.get(countedItem.getOptionId()) + "x" + countedItem.getNumber()).collect(Collectors.joining(", ")), record.get(19));

        assertEquals(itemStatusControl.getOptions().stream().collect(toMap(TextOption::getId, TextOption::getName)).get(itemStatusAnswer.getOptionId()), record.get(20));
        assertEquals(pointCheckAnswer.isPassed() ? "正常" : "异常", record.get(21));
        assertEquals(multiLevelSelectionAnswer.getSelection().toText(), record.get(22));

        assertEquals(response.getQrId(), record.get(23));
        assertEquals(response.getDefaultGroupId(), record.get(24));
    }

    @Test
    public void should_fetch_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QDetailedSubmission submissionDetail = SubmissionApi.fetchSubmission(response.getJwt(), submissionId);

        Submission submission = submissionRepository.byId(submissionId);
        Member member = memberRepository.byId(response.getMemberId());
        assertEquals(submissionId, submissionDetail.getId());
        assertEquals(1, submissionDetail.getAnswers().size());
        assertEquals(response.getAppId(), submissionDetail.getAppId());
        assertEquals(response.getDefaultGroupId(), submissionDetail.getGroupId());
        assertEquals(response.getTenantId(), submissionDetail.getTenantId());
        assertEquals(response.getHomePageId(), submissionDetail.getPageId());
        assertNull(submissionDetail.getApproval());
        assertEquals(submission.getCreatedAt(), submissionDetail.getCreatedAt());
        assertEquals(submission.getCreatedBy(), submissionDetail.getCreatedBy());
        assertEquals(member.getName(), submissionDetail.getCreatorName());
        assertTrue(submissionDetail.isCanUpdate());
        assertFalse(submissionDetail.isCanApprove());
    }

    @Test
    public void should_fetch_submission_with_only_viewable_answers() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl permissonedControl = defaultSingleLineTextControl();
        FSingleLineTextControl onlyViewableControl = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_APP).submitterViewable(true).build();
        FSingleLineTextControl nonPermissionedControl = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_APP).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), permissonedControl, onlyViewableControl, nonPermissionedControl);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        SingleLineTextAnswer permissionedAnswer = rAnswer(permissonedControl);
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), permissionedAnswer);
        QDetailedSubmission submissionDetail = SubmissionApi.fetchSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(1, submissionDetail.getAnswers().size());
        assertEquals(permissionedAnswer, submissionDetail.getAnswers().stream().findAny().get());

        SingleLineTextAnswer updatedPermissionedAnswer = rAnswer(permissonedControl);
        SingleLineTextAnswer onlyViewableAnswer = rAnswer(onlyViewableControl);
        SubmissionApi.updateSubmission(response.getJwt(), submissionId, updatedPermissionedAnswer, onlyViewableAnswer, rAnswer(nonPermissionedControl));
        QDetailedSubmission updatedSubmissionDetail = SubmissionApi.fetchSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(2, updatedSubmissionDetail.getAnswers().size());
        assertTrue(updatedSubmissionDetail.getAnswers().contains(updatedPermissionedAnswer));
        assertTrue(updatedSubmissionDetail.getAnswers().contains(onlyViewableAnswer));
    }

    @Test
    public void managers_should_fetch_submission_answers_even_with_no_permission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_APP).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());

        SingleLineTextAnswer permissionedAnswer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), permissionedAnswer);
        QDetailedSubmission submissionDetail = SubmissionApi.fetchSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(1, submissionDetail.getAnswers().size());
        assertEquals(permissionedAnswer, submissionDetail.getAnswers().stream().findAny().get());
    }

    @Test
    public void should_fetch_submission_with_approve_info() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        ApproveSubmissionCommand command = approveSubmissionCommand(true);
        SubmissionApi.approveSubmission(response.getJwt(), submissionId, command);

        QDetailedSubmission submissionDetail = SubmissionApi.fetchSubmission(response.getJwt(), submissionId);

        Submission submission = submissionRepository.byId(submissionId);
        QSubmissionApproval qSubmissionApproval = submissionDetail.getApproval();
        assertNotNull(qSubmissionApproval);
        assertTrue(qSubmissionApproval.isPassed());
        SubmissionApproval approval = submission.getApproval();
        assertEquals(approval.getApprovedAt(), qSubmissionApproval.getApprovedAt());
        assertEquals(approval.getApprovedBy(), qSubmissionApproval.getApprovedBy());
        assertEquals(approval.getNote(), qSubmissionApproval.getNote());
    }

    @Test
    public void should_fetch_own_submitted_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QDetailedSubmission submissionDetail = SubmissionApi.fetchSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(submissionId, submissionDetail.getId());
    }

    @Test
    public void should_fetch_submission_if_can_manage_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QDetailedSubmission submissionDetail = SubmissionApi.fetchSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(submissionId, submissionDetail.getId());
    }

    @Test
    public void should_fail_fetch_submission_if_not_submitted_by_myself() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        assertError(() -> SubmissionApi.fetchSubmissionRaw(memberResponse.getJwt(), submissionId), ACCESS_DENIED);
    }

    @Test
    public void should_fetch_submission_with_can_update_status_if_can_modify_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        PageSetting pageSetting = defaultPageSettingBuilder().modifyPermission(CAN_MANAGE_GROUP).build();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QDetailedSubmission submissionDetail = SubmissionApi.fetchSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(submissionId, submissionDetail.getId());
        assertTrue(submissionDetail.isCanUpdate());
    }

    @Test
    public void should_fetch_submission_with_can_update_status_if_can_modify_submission_by_myself() {
        PreparedQrResponse response = setupApi.registerWithQr();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(true)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QDetailedSubmission submissionDetail = SubmissionApi.fetchSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(submissionId, submissionDetail.getId());
        assertTrue(submissionDetail.isCanUpdate());
    }

    @Test
    public void should_fetch_submission_with_can_approve_status_if_can_approve() {
        PreparedQrResponse response = setupApi.registerWithQr();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(true)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QDetailedSubmission submissionDetail = SubmissionApi.fetchSubmission(response.getJwt(), submissionId);
        assertEquals(submissionId, submissionDetail.getId());
        assertTrue(submissionDetail.isCanApprove());
    }

    @Test
    public void should_fetch_submission_with_can_approve_status_if_cannot_approve() {
        PreparedQrResponse response = setupApi.registerWithQr();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QDetailedSubmission submissionDetail = SubmissionApi.fetchSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(submissionId, submissionDetail.getId());
        assertFalse(submissionDetail.isCanApprove());
    }

    @Test
    public void should_fetch_listed_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QListSubmission listedSubmission = SubmissionApi.fetchListedSubmission(response.getJwt(), submissionId);

        Submission submission = submissionRepository.byId(submissionId);
        QR qr = qrRepository.byId(response.getQrId());
        assertEquals(submissionId, listedSubmission.getId());
        assertEquals(1, listedSubmission.getDisplayAnswers().size());
        assertEquals(response.getAppId(), listedSubmission.getAppId());
        assertEquals(qr.getName(), listedSubmission.getQrName());
        assertEquals(response.getDefaultGroupId(), listedSubmission.getGroupId());
        assertEquals(response.getHomePageId(), listedSubmission.getPageId());
        assertEquals(NONE, listedSubmission.getApprovalStatus());
        assertEquals(submission.getCreatedAt(), listedSubmission.getCreatedAt());
        assertEquals(submission.getCreatedBy(), listedSubmission.getCreatedBy());
    }

    @Test
    public void should_fetch_listed_submission_with_only_viewable_answers() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl permissonedControl = defaultSingleLineTextControl();
        FSingleLineTextControl onlyViewableControl = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_APP).submitterViewable(true).build();
        FSingleLineTextControl nonPermissionedControl = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(CAN_MANAGE_APP).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), permissonedControl, onlyViewableControl, nonPermissionedControl);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        SingleLineTextAnswer permissionedAnswer = rAnswer(permissonedControl);
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), permissionedAnswer);
        QListSubmission listSubmission1 = SubmissionApi.fetchListedSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(1, listSubmission1.getDisplayAnswers().size());
        assertEquals(permissionedAnswer.getContent(), ((TextDisplayValue) listSubmission1.getDisplayAnswers().values().stream().findAny().get()).getText());

        SingleLineTextAnswer updatedPermissionedAnswer = rAnswer(permissonedControl);
        SingleLineTextAnswer onlyViewableAnswer = rAnswer(onlyViewableControl);
        SubmissionApi.updateSubmission(response.getJwt(), submissionId, updatedPermissionedAnswer, onlyViewableAnswer, rAnswer(nonPermissionedControl));
        QListSubmission listSubmission2 = SubmissionApi.fetchListedSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(2, listSubmission2.getDisplayAnswers().size());
        assertEquals(updatedPermissionedAnswer.getContent(), ((TextDisplayValue) listSubmission2.getDisplayAnswers().get(updatedPermissionedAnswer.getControlId())).getText());
        assertEquals(onlyViewableAnswer.getContent(), ((TextDisplayValue) listSubmission2.getDisplayAnswers().get(onlyViewableAnswer.getControlId())).getText());
    }


    @Test
    public void should_fetch_listed_submission_with_approve_info() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).build())
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        ApproveSubmissionCommand command = approveSubmissionCommand(true);
        SubmissionApi.approveSubmission(response.getJwt(), submissionId, command);

        QListSubmission listSubmission = SubmissionApi.fetchListedSubmission(response.getJwt(), submissionId);

        Submission submission = submissionRepository.byId(submissionId);
        ApprovalStatus approvalStatus = listSubmission.getApprovalStatus();
        assertNotNull(approvalStatus);
        assertEquals(ApprovalStatus.PASSED, approvalStatus);
        SubmissionApproval approval = submission.getApproval();
    }

    @Test
    public void should_fetch_own_listed_submitted_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        String submissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QListSubmission listSubmission = SubmissionApi.fetchListedSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(submissionId, listSubmission.getId());
    }

    @Test
    public void should_fetch_listed_submission_if_can_manage_qr() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberResponse.getMemberId());
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QListSubmission listSubmission = SubmissionApi.fetchListedSubmission(memberResponse.getJwt(), submissionId);
        assertEquals(submissionId, listSubmission.getId());
    }

    @Test
    public void should_fail_fetch_listed_submission_if_not_submitted_by_myself() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        assertError(() -> SubmissionApi.fetchListedSubmissionRaw(memberResponse.getJwt(), submissionId), ACCESS_DENIED);
    }


    @Test
    public void should_fetch_instance_last_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());
        assertEquals("", SubmissionApi.tryFetchInstanceLastSubmissionRaw(memberResponse.getJwt(), response.getQrId(), response.getHomePageId()).asString());

        String firstSubmissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String secondSubmissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QDetailedSubmission submissionDetail = SubmissionApi.tryFetchInstanceLastSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        assertEquals(secondSubmissionId, submissionDetail.getId());
        QDetailedSubmission memberSubmissionDetail = SubmissionApi.tryFetchInstanceLastSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());
        assertEquals(secondSubmissionId, memberSubmissionDetail.getId());
    }

    @Test
    public void should_fetch_my_last_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        String firstSubmissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        String secondSubmissionId = SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        QDetailedSubmission submissionDetail = SubmissionApi.tryFetchMyLastSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());
        assertEquals(secondSubmissionId, submissionDetail.getId());
        assertEquals("", SubmissionApi.tryFetchMyLastSubmissionRaw(response.getJwt(), response.getQrId(), response.getHomePageId()).asString());
    }

    @Test
    public void should_fetch_my_last_submission_for_auto_fill() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FSingleLineTextControl autoFillControl = defaultSingleLineTextControlBuilder().fillableSetting(ControlFillableSetting.builder().autoFill(true).build()).build();
        FSingleLineTextControl nonAutoFillControl = defaultSingleLineTextControlBuilder().fillableSetting(ControlFillableSetting.builder().autoFill(false).build()).build();
        FImageUploadControl nonAutoFillEligibleControl = defaultImageUploadControlBuilder().fillableSetting(ControlFillableSetting.builder().autoFill(true).build()).build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), autoFillControl, nonAutoFillControl, nonAutoFillEligibleControl);
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(autoFillControl), rAnswer(nonAutoFillControl));
        SingleLineTextAnswer lastAnswer = rAnswer(autoFillControl);
        SubmissionApi.newSubmission(memberResponse.getJwt(), response.getQrId(), response.getHomePageId(), lastAnswer, rAnswer(nonAutoFillControl), rAnswer(nonAutoFillEligibleControl));

        Set<Answer> answers = SubmissionApi.tryFetchSubmissionAnswersForAutoFill(memberResponse.getJwt(), response.getQrId(), response.getHomePageId());
        assertEquals(1, answers.size());
        assertEquals(lastAnswer, answers.stream().findFirst().get());
        assertTrue(SubmissionApi.tryFetchSubmissionAnswersForAutoFill(response.getJwt(), response.getQrId(), response.getHomePageId()).isEmpty());
    }


    @Test
    public void should_fetch_auto_calculate_number_input_value() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl dependantControl = defaultNumberInputControlBuilder().precision(3).build();
        FNumberInputControl calculatedControl = defaultNumberInputControlBuilder()
                .precision(2)
                .autoCalculateEnabled(true)
                .autoCalculateSetting(FNumberInputControl.AutoCalculateSetting.builder()
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(dependantControl.getId())
                                        .build()))
                                .build())
                        .expression("#number * 2")
                        .build())
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), dependantControl, calculatedControl);
        NumberInputAnswer answer = rAnswerBuilder(dependantControl).number(11.123).build();

        NumberInputAutoCalculateResponse calculateResponse = SubmissionApi.autoCalculateNumberInput(response.getJwt(), AutoCalculateQuery.builder()
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .controlId(calculatedControl.getId())
                .answers(newArrayList(answer))
                .build());
        assertEquals(22.25, calculateResponse.getNumber());
    }


    @Test
    public void should_fetch_auto_calculated_item_status_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();

        String option1Id = newShortUuid();
        TextOption option1 = TextOption.builder().id(option1Id).name(randomAlphabetic(5) + "选项").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name(randomAlphabetic(5) + "选项").build();

        FItemStatusControl itemStatusControl = defaultItemStatusControlBuilder()
                .autoCalculateEnabled(true)
                .options(newArrayList(option1, option2))
                .autoCalculateSetting(FItemStatusControl.AutoCalculateSetting.builder()
                        .records(newArrayList(FItemStatusControl.AutoCalculateRecord.builder()
                                .id(newShortUuid())
                                .expression("#number > 10")
                                .optionId(option1Id)
                                .build()))
                        .aliasContext(AutoCalculateAliasContext.builder()
                                .controlAliases(newArrayList(AutoCalculateAliasContext.ControlAlias.builder()
                                        .id(newShortUuid())
                                        .alias("number")
                                        .controlId(numberInputControl.getId())
                                        .build()))
                                .build())
                        .build())
                .build();

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, itemStatusControl);
        NumberInputAnswer answer = rAnswerBuilder(numberInputControl).number(11.0).build();
        ItemStatusAutoCalculateResponse calculateResponse = SubmissionApi.autoCalculateItemStatus(response.getJwt(), AutoCalculateQuery.builder()
                .appId(response.getAppId())
                .pageId(response.getHomePageId())
                .controlId(itemStatusControl.getId())
                .answers(newArrayList(answer))
                .build());
        assertEquals(option1Id, calculateResponse.getOptionId());
    }

}