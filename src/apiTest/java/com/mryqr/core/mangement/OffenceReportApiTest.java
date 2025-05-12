package com.mryqr.core.mangement;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.UpdateAppWebhookSettingCommand;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.WebhookSetting;
import com.mryqr.core.app.domain.page.control.FImageUploadControl;
import com.mryqr.core.app.domain.page.control.FMobileNumberControl;
import com.mryqr.core.app.domain.page.control.FMultiLineTextControl;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.common.properties.CommonProperties;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static com.mryqr.core.common.domain.permission.Permission.PUBLIC;
import static com.mryqr.management.MryManageTenant.ADMIN_INIT_MOBILE;
import static com.mryqr.management.MryManageTenant.ADMIN_INIT_PASSWORD;
import static com.mryqr.management.offencereport.MryOffenceReportApp.MRY_OFFENCE_APP_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_DETAIL_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_FILES_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_FORM_PAGE_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_MOBILE_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_REASON_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_APP_ID_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_APP_NAME_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_PAGE_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_QR_ID_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_QR_NAME_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_TENANT_ID_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_TENANT_NAME_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_TEMPLATE_PLATE_ID;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class OffenceReportApiTest extends BaseApiTest {
    @Autowired
    private CommonProperties commonProperties;

    @Test
    public void should_report_offence() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, MRY_OFFENCE_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());

        PreparedQrResponse response = setupApi.registerWithQr();

        Tenant targetTenant = tenantRepository.byId(response.getTenantId());
        App targetApp = appRepository.byId(response.getAppId());
        QR targetQr = qrRepository.byId(response.getQrId());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppPermissionAndControls(response.getJwt(), response.getAppId(), PUBLIC, control);

        QR offenceTemplateQr = qrRepository.byPlateIdOptional(OFFENCE_TEMPLATE_PLATE_ID).get();
        App offenceApp = appRepository.byId(MRY_OFFENCE_APP_ID);
        FRadioControl reasonControl = (FRadioControl) offenceApp.controlById(OFFENCE_REASON_CONTROL_ID);
        FMultiLineTextControl detailControl = (FMultiLineTextControl) offenceApp.controlById(OFFENCE_DETAIL_CONTROL_ID);
        FImageUploadControl filesControl = (FImageUploadControl) offenceApp.controlById(OFFENCE_FILES_CONTROL_ID);
        FMobileNumberControl mobileControl = (FMobileNumberControl) offenceApp.controlById(OFFENCE_MOBILE_CONTROL_ID);

        NewSubmissionCommand submissionCommand = NewSubmissionCommand.builder()
                .qrId(offenceTemplateQr.getId())
                .pageId(OFFENCE_FORM_PAGE_ID)
                .answers(Set.of(rAnswer(reasonControl), rAnswer(detailControl), rAnswer(filesControl), rAnswer(mobileControl)))
                .referenceData("http://m.mryqr.com/r/" + targetQr.getPlateId() + "/pages/" + response.getHomePageId())
                .build();
        String submissionId = SubmissionApi.newSubmission(null, submissionCommand);

        Submission submission = submissionRepository.byId(submissionId);
        QR offenceQr = qrRepository.byId(submission.getQrId());
        Submission syncSubmission = submissionRepository.lastInstanceSubmission(offenceQr.getId(), OFFENCE_SYNC_PAGE_ID).get();

        SingleLineTextAnswer tenantNameAnswer = (SingleLineTextAnswer) syncSubmission.getAnswers().get(OFFENCE_SYNC_TENANT_NAME_CONTROL_ID);
        SingleLineTextAnswer appNameAnswer = (SingleLineTextAnswer) syncSubmission.getAnswers().get(OFFENCE_SYNC_APP_NAME_CONTROL_ID);
        SingleLineTextAnswer qrNameAnswer = (SingleLineTextAnswer) syncSubmission.getAnswers().get(OFFENCE_SYNC_QR_NAME_CONTROL_ID);
        IdentifierAnswer tenantIdAnswer = (IdentifierAnswer) syncSubmission.getAnswers().get(OFFENCE_SYNC_TENANT_ID_CONTROL_ID);
        IdentifierAnswer appIdAnswer = (IdentifierAnswer) syncSubmission.getAnswers().get(OFFENCE_SYNC_APP_ID_CONTROL_ID);
        IdentifierAnswer qrIdAnswer = (IdentifierAnswer) syncSubmission.getAnswers().get(OFFENCE_SYNC_QR_ID_CONTROL_ID);

        assertEquals(targetTenant.getName(), tenantNameAnswer.getContent());
        assertEquals(targetApp.getName(), appNameAnswer.getContent());
        assertEquals(targetQr.getName(), qrNameAnswer.getContent());
        assertEquals(targetTenant.getId(), tenantIdAnswer.getContent());
        assertEquals(targetApp.getId(), appIdAnswer.getContent());
        assertEquals(targetQr.getId(), qrIdAnswer.getContent());
    }
}
