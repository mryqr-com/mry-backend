package com.mryqr.management.offencereport.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FIdentifierControl;
import com.mryqr.core.app.domain.page.control.FMultiLineTextControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.common.domain.permission.Permission;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionFactory;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.multilinetext.MultiLineTextAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.management.offencereport.MryOffenceReportApp.MRY_OFFENCE_APP_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_FORM_PAGE_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_APP_ID_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_APP_NAME_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_PAGE_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_QR_ID_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_QR_NAME_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_TENANT_ID_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_TENANT_NAME_CONTROL_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.OFFENCE_SYNC_URL_CONTROL_ID;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substringBetween;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncInfoWebhookHandler implements OffenceWebhookHandler {
    private final QrRepository qrRepository;
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionFactory submissionFactory;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload.getPageId().equals(OFFENCE_FORM_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String targetUrl = payload.getReferenceData();
        if (isBlank(targetUrl)) {
            return;
        }

        String plateId = substringBetween(targetUrl, "/r/", "/pages/");
        if (isBlank(plateId)) {
            return;
        }

        qrRepository.byPlateIdOptional(plateId).ifPresent(targetQr -> {
            App targetApp = appRepository.cachedById(targetQr.getAppId());
            Tenant targetTenant = tenantRepository.cachedById(targetQr.getTenantId());

            String offenceQrId = payload.getQrId();
            QR offenceQr = qrRepository.byId(offenceQrId);
            App offenceApp = appRepository.byId(MRY_OFFENCE_APP_ID);
            Page page = offenceApp.pageById(OFFENCE_SYNC_PAGE_ID);
            Set<Answer> answers = buildAnswers(targetUrl, targetQr, targetApp, targetTenant, page);

            Submission submission = submissionFactory.createOrUpdateSubmission(answers,
                    offenceQr,
                    page,
                    offenceApp,
                    Set.of(Permission.values()),
                    null,
                    NOUSER
            );
            submissionRepository.houseKeepSave(submission, offenceApp);
            log.info("Synced offence target QR[{}] to managed offence QR.", targetQr.getId());
        });

    }

    private Set<Answer> buildAnswers(String targetUrl, QR targetQr, App targetApp, Tenant targetTenant, Page page) {
        Map<String, Control> allControls = page.getControls().stream().collect(toImmutableMap(Control::getId, identity()));

        FSingleLineTextControl tenantNameControl = (FSingleLineTextControl) allControls.get(OFFENCE_SYNC_TENANT_NAME_CONTROL_ID);
        SingleLineTextAnswer tenantNameAnswer = SingleLineTextAnswer.answerBuilder(requireNonNull(tenantNameControl)).
                content(targetTenant.getName())
                .build();

        FIdentifierControl tenantIdControl = (FIdentifierControl) allControls.get(OFFENCE_SYNC_TENANT_ID_CONTROL_ID);
        IdentifierAnswer tenantIdAnswer = IdentifierAnswer.answerBuilder(requireNonNull(tenantIdControl))
                .content(targetTenant.getId())
                .build();

        FSingleLineTextControl appNameControl = (FSingleLineTextControl) allControls.get(OFFENCE_SYNC_APP_NAME_CONTROL_ID);
        SingleLineTextAnswer appNameAnswer = SingleLineTextAnswer.answerBuilder(requireNonNull(appNameControl))
                .content(targetApp.getName())
                .build();

        FIdentifierControl appIdControl = (FIdentifierControl) allControls.get(OFFENCE_SYNC_APP_ID_CONTROL_ID);
        IdentifierAnswer appIdAnswer = IdentifierAnswer.answerBuilder(requireNonNull(appIdControl))
                .content(targetApp.getId())
                .build();

        FSingleLineTextControl qrNameControl = (FSingleLineTextControl) allControls.get(OFFENCE_SYNC_QR_NAME_CONTROL_ID);
        SingleLineTextAnswer qrNameAnswer = SingleLineTextAnswer.answerBuilder(requireNonNull(qrNameControl))
                .content(targetQr.getName())
                .build();

        FIdentifierControl qrIdControl = (FIdentifierControl) allControls.get(OFFENCE_SYNC_QR_ID_CONTROL_ID);
        IdentifierAnswer qrIdAnswer = IdentifierAnswer.answerBuilder(requireNonNull(qrIdControl))
                .content(targetQr.getId())
                .build();

        FMultiLineTextControl urlControl = (FMultiLineTextControl) allControls.get(OFFENCE_SYNC_URL_CONTROL_ID);
        MultiLineTextAnswer urlAnswer = MultiLineTextAnswer.answerBuilder(requireNonNull(urlControl))
                .content(targetUrl)
                .build();

        return Set.of(tenantNameAnswer, tenantIdAnswer, appNameAnswer, appIdAnswer, qrNameAnswer, qrIdAnswer, urlAnswer);
    }
}
