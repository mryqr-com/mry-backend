package com.mryqr.core.app.notification;

import com.mryqr.BaseApiTest;
import com.mryqr.common.notification.FakeNotificationService;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.setting.ApprovalSetting;
import com.mryqr.core.app.domain.page.setting.PageSetting;
import com.mryqr.core.app.domain.page.setting.notification.NotificationSetting;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.ApproveSubmissionCommand;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.domain.permission.Permission.*;
import static com.mryqr.core.app.domain.page.setting.SubmitType.NEW;
import static com.mryqr.core.app.domain.page.setting.SubmitterUpdateRange.IN_1_HOUR;
import static com.mryqr.core.app.domain.page.setting.notification.NotificationRole.SUBMITTER;
import static com.mryqr.core.submission.SubmissionUtils.approveSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class AppNotificationApiTest extends BaseApiTest {

    @Test
    public void should_notify_when_create_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();

        NotificationSetting notificationSetting = NotificationSetting.builder()
                .notificationEnabled(true)
                .onCreateNotificationRoles(List.of(SUBMITTER))
                .onUpdateNotificationRoles(List.of())
                .build();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .notificationSetting(notificationSetting)
                .build();
        AppApi.updateAppHomePageSetting(response.getJwt(), response.getAppId(), pageSetting);

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        assertEquals(submissionId, FakeNotificationService.id);
    }

    @Test
    public void should_notify_when_update_submission() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FCheckboxControl checkboxControl = defaultCheckboxControl();

        NotificationSetting notificationSetting = NotificationSetting.builder()
                .notificationEnabled(true)
                .onCreateNotificationRoles(List.of())
                .onUpdateNotificationRoles(List.of(SUBMITTER))
                .build();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .notificationSetting(notificationSetting)
                .build();
        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, checkboxControl);

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());

        SubmissionApi.updateSubmission(response.getJwt(), submissionId, rAnswer(checkboxControl));
        assertEquals(submissionId, FakeNotificationService.id);
    }

    @Test
    public void should_notify_when_approve_submission() {
        PreparedQrResponse response = setupApi.registerWithQr(rEmail(), rPassword());

        FSingleLineTextControl control = defaultSingleLineTextControl();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .permission(AS_TENANT_MEMBER)
                .modifyPermission(CAN_MANAGE_GROUP)
                .submitType(NEW)
                .submitterUpdatable(false)
                .submitterUpdateRange(IN_1_HOUR)
                .approvalSetting(ApprovalSetting.builder().approvalEnabled(true).permission(CAN_MANAGE_APP).notifySubmitter(true).build())
                .build();

        AppApi.updateAppHomePageSettingAndControls(response.getJwt(), response.getAppId(), pageSetting, newArrayList(control));
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        ApproveSubmissionCommand command = approveSubmissionCommand(true);
        SubmissionApi.approveSubmission(response.getJwt(), submissionId, command);
        assertEquals(submissionId, FakeNotificationService.id);
    }

    @Test
    public void should_not_notify_if_package_too_low() {
        PreparedQrResponse response = setupApi.registerWithQr();
        Tenant theTenant = tenantRepository.byId(response.getTenantId());
        setupApi.updateTenantPlan(theTenant, theTenant.currentPlan().withSubmissionNotifyAllowed(false));

        NotificationSetting notificationSetting = NotificationSetting.builder()
                .notificationEnabled(true)
                .onCreateNotificationRoles(List.of(SUBMITTER))
                .onUpdateNotificationRoles(List.of())
                .build();
        PageSetting pageSetting = defaultPageSettingBuilder()
                .notificationSetting(notificationSetting)
                .build();
        AppApi.updateAppHomePageSetting(response.getJwt(), response.getAppId(), pageSetting);

        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId());
        assertNotEquals(submissionId, FakeNotificationService.id);
    }
}
