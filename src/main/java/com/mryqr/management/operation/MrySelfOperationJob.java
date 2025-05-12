package com.mryqr.management.operation;

import com.mryqr.common.domain.permission.Permission;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FDateControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.qr.domain.PlatedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrFactory;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionFactory;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import com.mryqr.core.tenant.domain.task.SyncTenantToManagedQrTask;
import com.mryqr.management.platform.domain.PlatformRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.management.operation.MryOperationApp.*;
import static java.time.ZoneId.systemDefault;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class MrySelfOperationJob {
    private final MongoTemplate mongoTemplate;
    private final QrRepository qrRepository;
    private final AppRepository appRepository;
    private final QrFactory qrFactory;
    private final PlateRepository plateRepository;
    private final SubmissionFactory submissionFactory;
    private final SubmissionRepository submissionRepository;
    private final GroupRepository groupRepository;
    private final TenantRepository tenantRepository;
    private final SyncTenantToManagedQrTask syncTenantToManagedQrTask;

    private final PlatformRepository platformRepository;

    public void run() {
        syncOverallStatistics();
        syncAllTenantsToManagedQrs();
    }

    private void syncOverallStatistics() {
        if (!qrRepository.existsByCustomId(OPERATION_QR_CUSTOM_ID, MRY_OPERATION_APP_ID)) {
            createStatisticsQr();
        }

        qrRepository.byCustomIdOptional(MRY_OPERATION_APP_ID, OPERATION_QR_CUSTOM_ID).ifPresent(qr -> {
            App app = appRepository.cachedById(MRY_OPERATION_APP_ID);
            try {
                deltaStatistics(qr, app);
            } catch (Throwable t) {
                log.error("Error happened while do delta statistics.", t);
            }
            try {
                totalStatistics(qr, app);
            } catch (Throwable t) {
                log.error("Error happened while do total statistics.", t);
            }
        });
    }

    private void createStatisticsQr() {
        App app = appRepository.cachedById(MRY_OPERATION_APP_ID);
        Group group = groupRepository.cachedById(MRY_OPERATION_GROUP_ID);

        PlatedQr platedQr = qrFactory.createPlatedQr("码如云运营数据", group, app, OPERATION_QR_CUSTOM_ID, NOUSER);
        QR qr = platedQr.getQr();
        Plate plate = platedQr.getPlate();
        qrRepository.save(qr);
        plateRepository.save(plate);
    }

    private void deltaStatistics(QR qr, App app) {
        Page page = app.pageById(SYNC_DELTA_PAGE_ID);
        Set<Answer> deltaAnswers = buildDeltaAnswers(page);
        Submission submission = submissionFactory.createOrUpdateSubmission(deltaAnswers,
                qr,
                page,
                app,
                Set.of(Permission.values()),
                null,
                NOUSER
        );
        submissionRepository.houseKeepSave(submission, app);
        log.info("Synced mry operation delta statistics to managed QR.");
    }

    private Set<Answer> buildDeltaAnswers(Page page) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Instant yesterdayStart = yesterday.atStartOfDay(systemDefault()).toInstant();
        Instant yesterdayEnd = LocalDate.now().atStartOfDay(systemDefault()).toInstant();
        Query query = Query.query(where("createdAt").gt(yesterdayStart).lt(yesterdayEnd));

        Map<String, Control> allControls = page.getControls().stream().collect(toImmutableMap(Control::getId, identity()));

        FNumberInputControl deltaTenantControl = (FNumberInputControl) allControls.get(DELTA_TENANT_CONTROL_ID);
        NumberInputAnswer deltaTenantAnswer = NumberInputAnswer.answerBuilder(requireNonNull(deltaTenantControl))
                .number((double) mongoTemplate.count(query, Tenant.class))
                .build();

        FNumberInputControl deltaAppControl = (FNumberInputControl) allControls.get(DELTA_APP_CONTROL_ID);
        NumberInputAnswer deltaAppAnswer = NumberInputAnswer.answerBuilder(requireNonNull(deltaAppControl))
                .number((double) mongoTemplate.count(query, App.class))
                .build();

        FNumberInputControl deltaQrControl = (FNumberInputControl) allControls.get(DELTA_QR_CONTROL_ID);
        NumberInputAnswer deltaQrAnswer = NumberInputAnswer.answerBuilder(requireNonNull(deltaQrControl))
                .number((double) mongoTemplate.count(query, QR.class))
                .build();

        FNumberInputControl deltaSubmissionControl = (FNumberInputControl) allControls.get(DELTA_SUBMISSION_CONTROL_ID);
        NumberInputAnswer deltaSubmissionAnswer = NumberInputAnswer.answerBuilder(requireNonNull(deltaSubmissionControl))
                .number((double) mongoTemplate.count(query, Submission.class))
                .build();

        FNumberInputControl deltaMemberControl = (FNumberInputControl) allControls.get(DELTA_MEMBER_CONTROL_ID);
        NumberInputAnswer deltaMemberAnswer = NumberInputAnswer.answerBuilder(requireNonNull(deltaMemberControl))
                .number((double) mongoTemplate.count(query, Member.class))
                .build();

        FDateControl deltaDateControl = (FDateControl) allControls.get(DELTA_DATE_CONTROL_ID);
        DateAnswer deltaDateAnswer = DateAnswer.answerBuilder(requireNonNull(deltaDateControl))
                .date(yesterday.toString())
                .build();

        return Set.of(deltaTenantAnswer, deltaAppAnswer, deltaQrAnswer, deltaSubmissionAnswer, deltaMemberAnswer, deltaDateAnswer);
    }

    private void totalStatistics(QR qr, App app) {
        Page page = app.pageById(SYNC_TOTAL_PAGE_ID);
        Set<Answer> totalAnswers = buildTotalAnswers(page);
        Submission submission = submissionFactory.createOrUpdateSubmission(totalAnswers,
                qr,
                page,
                app,
                Set.of(Permission.values()),
                null,
                NOUSER
        );
        submissionRepository.houseKeepSave(submission, app);
        log.info("Synced mry operation total statistics to managed QR.");
    }

    private Set<Answer> buildTotalAnswers(Page page) {
        Query query = new Query();

        Map<String, Control> allControls = page.getControls().stream().collect(toImmutableMap(Control::getId, identity()));

        FNumberInputControl totalTenantControl = (FNumberInputControl) allControls.get(TOTAL_TENANT_CONTROL_ID);
        NumberInputAnswer totalTenantAnswer = NumberInputAnswer.answerBuilder(requireNonNull(totalTenantControl))
                .number((double) mongoTemplate.count(query, Tenant.class))
                .build();

        FNumberInputControl totalAppControl = (FNumberInputControl) allControls.get(TOTAL_APP_CONTROL_ID);
        NumberInputAnswer totalAppAnswer = NumberInputAnswer.answerBuilder(requireNonNull(totalAppControl))
                .number((double) mongoTemplate.count(query, App.class))
                .build();

        FNumberInputControl totalQrControl = (FNumberInputControl) allControls.get(TOTAL_QR_CONTROL_ID);
        NumberInputAnswer totalQrAnswer = NumberInputAnswer.answerBuilder(requireNonNull(totalQrControl))
                .number((double) mongoTemplate.count(query, QR.class))
                .build();

        FNumberInputControl totalSubmissionControl = (FNumberInputControl) allControls.get(TOTAL_SUBMISSION_CONTROL_ID);
        NumberInputAnswer totalSubmissionAnswer = NumberInputAnswer.answerBuilder(requireNonNull(totalSubmissionControl))
                .number((double) mongoTemplate.count(query, Submission.class))
                .build();

        FNumberInputControl totalMemberControl = (FNumberInputControl) allControls.get(TOTAL_MEMBER_CONTROL_ID);
        NumberInputAnswer totalMemberAnswer = NumberInputAnswer.answerBuilder(requireNonNull(totalMemberControl))
                .number((double) mongoTemplate.count(query, Member.class))
                .build();

        FNumberInputControl totalQrGenerationControl = (FNumberInputControl) allControls.get(TOTAL_QR_GENERATION_ID);
        NumberInputAnswer totalQrGenerationAnswer = NumberInputAnswer.answerBuilder(requireNonNull(totalQrGenerationControl))
                .number((double) platformRepository.getPlatform().getQrGenerationCount())
                .build();

        return Set.of(totalTenantAnswer, totalAppAnswer, totalQrAnswer, totalSubmissionAnswer, totalMemberAnswer, totalQrGenerationAnswer);
    }

    private void syncAllTenantsToManagedQrs() {
        tenantRepository.allTenantIds().forEach(tenantId -> {
            try {
                syncTenantToManagedQrTask.sync(tenantId);
                log.debug("Synced tenant[{}] information to managed QR.", tenantId);
            } catch (Throwable t) {
                log.error("Error happened while sync tenant[{}] to managed QR.", tenantId, t);
            }
        });
    }
}
