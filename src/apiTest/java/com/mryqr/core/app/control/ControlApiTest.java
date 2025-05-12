package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.UpdateAppReportSettingCommand;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeType;
import com.mryqr.core.app.domain.event.AppControlOptionsDeletedEvent;
import com.mryqr.core.app.domain.event.AppControlsDeletedEvent;
import com.mryqr.core.app.domain.event.DeletedControlInfo;
import com.mryqr.core.app.domain.event.DeletedTextOptionInfo;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FItemCountControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.control.PSectionTitleViewControl;
import com.mryqr.core.app.domain.report.ReportSetting;
import com.mryqr.core.app.domain.report.chart.ChartReportConfiguration;
import com.mryqr.core.app.domain.report.chart.ChartReportSetting;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.app.domain.report.number.NumberReportConfiguration;
import com.mryqr.core.app.domain.report.number.NumberReportSetting;
import com.mryqr.core.app.domain.report.number.control.ControlNumberReport;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import com.mryqr.utils.RandomTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.page.control.ControlType.SECTION_TITLE;
import static com.mryqr.core.app.domain.report.number.NumberReportType.CONTROL_NUMBER_REPORT;
import static com.mryqr.core.common.domain.event.DomainEventType.CONTROLS_DELETED;
import static com.mryqr.core.common.domain.event.DomainEventType.CONTROL_OPTIONS_DELETED;
import static com.mryqr.core.common.domain.permission.Permission.AS_TENANT_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.PUBLIC;
import static com.mryqr.core.common.domain.report.NumberAggregationType.AVG;
import static com.mryqr.core.common.domain.report.ReportRange.NO_LIMIT;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_ID_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_PERMISSION_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_TYPES_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_TYPE_NOT_MATCH;
import static com.mryqr.core.common.exception.ErrorCode.EMPTY_FILLABLE_SETTING;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultFillableSetting;
import static com.mryqr.utils.RandomTestFixture.defaultItemCountControl;
import static com.mryqr.utils.RandomTestFixture.defaultNumberInputControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControl;
import static com.mryqr.utils.RandomTestFixture.defaultSectionTitleControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControlBuilder;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static com.mryqr.utils.RandomTestFixture.rReportName;
import static com.mryqr.utils.RandomTestFixture.rTextOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ControlApiTest extends BaseApiTest {

    @Test
    public void should_raise_event_when_delete_control() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String appId = response.getAppId();
        FRadioControl control = defaultRadioControl();
        AppApi.updateAppControls(response.getJwt(), appId, control);
        App app = appRepository.byId(appId);
        IndexedField indexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
        RadioAnswer radioAnswer = RandomTestFixture.rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), radioAnswer);
        Submission submission = submissionRepository.byId(submissionId);
        RadioAnswer loadedAnswer = (RadioAnswer) submission.getAnswers().get(control.getId());
        assertEquals(radioAnswer.getOptionId(), loadedAnswer.getOptionId());
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertEquals(radioAnswer.getOptionId(), indexedValue.getTv().stream().findFirst().get());

        AppApi.updateAppControls(response.getJwt(), appId);

        AppControlsDeletedEvent controlDeletedEvent = domainEventDao.latestEventFor(app.getId(), CONTROLS_DELETED, AppControlsDeletedEvent.class);
        assertEquals(1, controlDeletedEvent.getControls().size());
        assertTrue(controlDeletedEvent.getControls().contains(DeletedControlInfo.builder()
                .pageId(response.getHomePageId())
                .controlId(control.getId())
                .controlType(control.getType())
                .indexedField(indexedField)
                .build()));
        Submission updatedSubmission = submissionRepository.byId(submissionId);
        assertNull(updatedSubmission.getAnswers().get(control.getId()));
        assertNull(updatedSubmission.getIndexedValues().valueOf(indexedField));
    }

    @Test
    public void delete_control_should_also_delete_control_aware_number_reports() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        String appId = response.getAppId();
        FNumberInputControl control = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), appId, control);

        ControlNumberReport controlNumberReport = ControlNumberReport.builder()
                .id(newShortUuid())
                .name(rReportName())
                .type(CONTROL_NUMBER_REPORT)
                .range(NO_LIMIT)
                .numberAggregationType(AVG)
                .pageId(response.getHomePageId())
                .controlId(control.getId())
                .build();

        List<NumberReport> reports = newArrayList(controlNumberReport);
        UpdateAppReportSettingCommand command = UpdateAppReportSettingCommand.builder()
                .setting(ReportSetting.builder()
                        .chartReportSetting(ChartReportSetting.builder().reports(newArrayList()).configuration(ChartReportConfiguration.builder().gutter(10).build()).build())
                        .numberReportSetting(NumberReportSetting.builder().reports(reports).configuration(NumberReportConfiguration.builder().gutter(10).height(100).reportPerLine(6).build()).build()).build())
                .build();

        AppApi.updateAppReportSetting(response.getJwt(), response.getAppId(), command);
        assertEquals(1, appRepository.byId(response.getAppId())
                .getReportSetting().getNumberReportSetting().getReports().size());

        AppApi.updateAppControls(response.getJwt(), appId);
        assertEquals(0, appRepository.byId(response.getAppId())
                .getReportSetting().getNumberReportSetting().getReports().size());
    }

    @Test
    public void should_raise_event_when_delete_control_option() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FCheckboxControl control = defaultCheckboxControlBuilder().options(rTextOptions(10)).build();
        CheckboxAnswer answer = RandomTestFixture.rAnswer(control);
        String tobeDeletedOptionId = answer.getOptionIds().get(0);
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(AttributeType.CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);
        App app = appRepository.byId(response.getAppId());
        IndexedField controlIndexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
        IndexedField attributeIndexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        Submission submission = submissionRepository.byId(submissionId);
        assertTrue(submission.getIndexedValues().valueOf(controlIndexedField).getTv().contains(tobeDeletedOptionId));
        QR qr = qrRepository.byId(response.getQrId());
        assertTrue(qr.getIndexedValues().valueOf(attributeIndexedField).getTv().contains(tobeDeletedOptionId));
        control.getOptions().removeIf(textOption -> textOption.getId().equals(tobeDeletedOptionId));

        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        AppControlOptionsDeletedEvent event = domainEventDao.latestEventFor(response.getAppId(), CONTROL_OPTIONS_DELETED, AppControlOptionsDeletedEvent.class);
        assertEquals(1, event.getControlOptions().size());
        DeletedTextOptionInfo controlOptionInfo = event.getControlOptions().stream().findFirst().get();
        assertEquals(control.getId(), controlOptionInfo.getControlId());
        assertEquals(control.getType(), controlOptionInfo.getControlType());
        assertEquals(tobeDeletedOptionId, controlOptionInfo.getOptionId());
        Submission updatedSubmission = submissionRepository.byId(submissionId);
        assertFalse(updatedSubmission.getIndexedValues().valueOf(controlIndexedField).getTv().contains(tobeDeletedOptionId));
        QR updatedQr = qrRepository.byId(response.getQrId());
        assertFalse(updatedQr.getIndexedValues().valueOf(attributeIndexedField).getTv().contains(tobeDeletedOptionId));
    }

    @Test
    public void update_app_setting_should_reset_submitter_viewable_for_public_fillable_controls() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String appId = response.getAppId();
        AppApi.updateAppPermission(response.getJwt(), appId, PUBLIC);
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().submitterViewable(true).build();
        AppApi.updateAppControls(response.getJwt(), appId, control);

        App app = appRepository.byId(appId);
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isSubmitterViewable());
    }

    @Test
    public void update_app_setting_should_reset_for_non_fillable_controls() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String appId = response.getAppId();
        PSectionTitleViewControl control = defaultSectionTitleControlBuilder()
                .fillableSetting(defaultFillableSetting()).submitterViewable(true).build();
        AppApi.updateAppControls(response.getJwt(), appId, control);

        App app = appRepository.byId(appId);
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isSubmitterViewable());
        assertNull(updatedControl.getFillableSetting());
    }

    @Test
    public void update_app_setting_should_reset_for_permission_not_enabled() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String appId = response.getAppId();
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().permissionEnabled(false).submitterViewable(true).build();
        AppApi.updateAppControls(response.getJwt(), appId, control);

        App app = appRepository.byId(appId);
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isSubmitterViewable());
    }

    @Test
    public void should_fail_update_app_setting_if_new_control_in_not_supported_by_packages() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());

        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        Page page = setting.homePage();
        FItemCountControl higherPackagesRequiredControl = defaultItemCountControl();
        page.getControls().add(higherPackagesRequiredControl);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), CONTROL_TYPES_NOT_ALLOWED);
    }

    @Test
    public void should_fail_update_app_setting_if_control_type_changes() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), appId, control);

        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        List<Control> controls = setting.homePage().getControls();
        controls.clear();
        ReflectionTestUtils.setField(control, "type", SECTION_TITLE);
        controls.add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), CONTROL_TYPE_NOT_MATCH);
    }

    @Test
    public void should_failed_update_app_setting_if_no_fillable_settings_provided_for_fillable_controls() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());

        String appId = response.getAppId();
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().fillableSetting(null).build();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        List<Control> controls = setting.homePage().getControls();
        controls.add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), EMPTY_FILLABLE_SETTING);
    }

    @Test
    public void should_failed_update_app_setting_if_permission_not_allowed_for_control() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());

        String appId = response.getAppId();
        FSingleLineTextControl control = defaultSingleLineTextControlBuilder().permissionEnabled(true).permission(AS_TENANT_MEMBER).build();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        List<Control> controls = setting.homePage().getControls();
        controls.add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), CONTROL_PERMISSION_NOT_ALLOWED);
    }

    @Test
    public void should_fail_update_app_setting_if_control_id_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());

        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        Page page = setting.homePage();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        page.getControls().add(control);
        page.getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), CONTROL_ID_DUPLICATED);
    }

}
