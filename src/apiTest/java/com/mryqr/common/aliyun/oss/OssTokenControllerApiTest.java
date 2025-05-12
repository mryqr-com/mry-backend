package com.mryqr.common.aliyun.oss;

import com.google.common.collect.Lists;
import com.mryqr.BaseApiTest;
import com.mryqr.common.oss.command.RequestOssTokenCommand;
import com.mryqr.common.oss.domain.QOssToken;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.CreateAppResponse;
import com.mryqr.core.app.command.SetAppManagersCommand;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.mryqr.common.oss.domain.OssTokenRequestType.APP_EDIT;
import static com.mryqr.common.oss.domain.OssTokenRequestType.MEMBER_INFO;
import static com.mryqr.common.oss.domain.OssTokenRequestType.QR_MANAGE;
import static com.mryqr.common.oss.domain.OssTokenRequestType.SUBMISSION;
import static com.mryqr.common.oss.domain.OssTokenRequestType.TENANT_EDIT;
import static com.mryqr.common.oss.domain.OssTokenRequestType.TENANT_ORDER;
import static com.mryqr.core.common.domain.permission.Permission.AS_TENANT_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.PUBLIC;
import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.core.common.exception.ErrorCode.ACCESS_DENIED;
import static com.mryqr.core.common.exception.ErrorCode.AUTHENTICATION_FAILED;
import static com.mryqr.core.common.exception.ErrorCode.MAX_STORAGE_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.QR_NOT_BELONG_TO_APP;
import static com.mryqr.core.common.exception.ErrorCode.USER_NOT_CURRENT_MEMBER;
import static com.mryqr.utils.RandomTestFixture.rMemberName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static com.mryqr.utils.RandomTestFixture.rQrName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("由于ci环境未配置阿里云ak，aks和role，因此无法运行")
class OssTokenControllerApiTest extends BaseApiTest {

    @Test
    public void tenant_admin_should_fetch_oss_token_for_tenant_edit() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(TENANT_EDIT)
                .tenantId(response.getTenantId())
                .build();

        QOssToken token = OssTokenApi.getOssToken(response.getJwt(), command);

        assertNotNull(token.getAccessKeyId());
        assertNotNull(token.getAccessKeySecret());
        assertNotNull(token.getBucket());
        assertNotNull(token.getFolder());
        assertEquals(response.getTenantId() + "/_TENANT_EDIT/" + LocalDate.now(), token.getFolder());
        assertNotNull(token.getEndpoint());
        assertNotNull(token.getSecurityToken());
        assertNotNull(token.getExpiration());
    }

    @Test
    public void normal_member_should_not_fetch_oss_token_for_tenant_edit() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());
        CreateMemberResponse member = MemberApi.createMemberAndLogin(loginResponse.getJwt(), rMemberName(), rMobile(), rPassword());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(TENANT_EDIT)
                .tenantId(loginResponse.getTenantId())
                .build();

        assertError(() -> OssTokenApi.getOssTokenRaw(member.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void anonymous_member_should_not_fetch_oss_token_for_tenant_edit() {
        LoginResponse loginResponse = setupApi.registerWithLogin(rMobile(), rPassword());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(TENANT_EDIT)
                .tenantId(loginResponse.getTenantId())
                .build();

        assertError(() -> OssTokenApi.getOssTokenRaw(null, command), AUTHENTICATION_FAILED);
    }

    @Test
    public void tenant_admin_should_fetch_oss_token_for_tenant_order() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(TENANT_ORDER)
                .tenantId(response.getTenantId())
                .build();

        QOssToken token = OssTokenApi.getOssToken(response.getJwt(), command);

        assertNotNull(token.getAccessKeyId());
        assertNotNull(token.getAccessKeySecret());
        assertNotNull(token.getBucket());
        assertNotNull(token.getFolder());
        assertEquals(response.getTenantId() + "/_TENANT_ORDER/" + LocalDate.now(), token.getFolder());
        assertNotNull(token.getEndpoint());
        assertNotNull(token.getSecurityToken());
        assertNotNull(token.getExpiration());
    }

    @Test
    public void tenant_admin_should_fetch_oss_token_for_app_edit() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(APP_EDIT)
                .tenantId(response.getTenantId())
                .appId(response.getAppId())
                .build();

        QOssToken token = OssTokenApi.getOssToken(response.getJwt(), command);

        assertNotNull(token.getAccessKeyId());
        assertNotNull(token.getAccessKeySecret());
        assertNotNull(token.getBucket());
        assertNotNull(token.getFolder());
        assertEquals(response.getTenantId() + "/" + response.getAppId() + "/_APP_EDIT/" + LocalDate.now(), token.getFolder());
        assertNotNull(token.getEndpoint());
        assertNotNull(token.getSecurityToken());
        assertNotNull(token.getExpiration());
    }

    @Test
    public void app_manager_should_fetch_oss_token_for_app_edit() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());

        AppApi.setAppManagers(response.getJwt(), response.getAppId(), SetAppManagersCommand.builder().managers(Lists.newArrayList(memberResponse.getMemberId())).build());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(APP_EDIT)
                .tenantId(response.getTenantId())
                .appId(response.getAppId())
                .build();

        QOssToken token = OssTokenApi.getOssToken(memberResponse.getJwt(), command);

        assertNotNull(token.getAccessKeyId());
        assertNotNull(token.getAccessKeySecret());
        assertNotNull(token.getBucket());
        assertNotNull(token.getFolder());
        assertEquals(response.getTenantId() + "/" + response.getAppId() + "/_APP_EDIT/" + LocalDate.now(), token.getFolder());
        assertNotNull(token.getEndpoint());
        assertNotNull(token.getSecurityToken());
        assertNotNull(token.getExpiration());
    }

    @Test
    public void normal_member_should_not_fetch_oss_token_for_app_edit() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(APP_EDIT)
                .tenantId(response.getTenantId())
                .appId(response.getAppId())
                .build();

        assertError(() -> OssTokenApi.getOssTokenRaw(memberResponse.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void anonymous_member_should_not_fetch_oss_token_for_app_edit() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(APP_EDIT)
                .tenantId(response.getTenantId())
                .appId(response.getAppId())
                .build();

        assertError(() -> OssTokenApi.getOssTokenRaw(null, command), AUTHENTICATION_FAILED);
    }

    @Test
    public void tenant_admin_should_fetch_oss_token_for_qr_manage() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(QR_MANAGE)
                .tenantId(response.getTenantId())
                .appId(response.getAppId())
                .qrId(response.getQrId())
                .build();

        QOssToken token = OssTokenApi.getOssToken(response.getJwt(), command);

        assertNotNull(token.getAccessKeyId());
        assertNotNull(token.getAccessKeySecret());
        assertNotNull(token.getBucket());
        assertNotNull(token.getFolder());
        assertEquals(response.getTenantId() + "/" + response.getAppId() + "/" + response.getQrId() + "/_QR_MANAGE/" + LocalDate.now(), token.getFolder());
        assertNotNull(token.getEndpoint());
        assertNotNull(token.getSecurityToken());
        assertNotNull(token.getExpiration());
    }

    @Test
    public void app_manager_should_fetch_oss_token_for_qr_manage() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());

        AppApi.setAppManagers(response.getJwt(), response.getAppId(), SetAppManagersCommand.builder().managers(Lists.newArrayList(memberResponse.getMemberId())).build());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(QR_MANAGE)
                .tenantId(response.getTenantId())
                .appId(response.getAppId())
                .qrId(response.getQrId())
                .build();

        QOssToken token = OssTokenApi.getOssToken(memberResponse.getJwt(), command);

        assertNotNull(token.getAccessKeyId());
        assertNotNull(token.getAccessKeySecret());
        assertNotNull(token.getBucket());
        assertNotNull(token.getFolder());
        assertNotNull(token.getEndpoint());
        assertNotNull(token.getSecurityToken());
        assertNotNull(token.getExpiration());
    }

    @Test
    public void should_fail_fetch_oss_token_for_qr_manage_if_qr_not_under_app() {
        PreparedQrResponse response = setupApi.registerWithQr();
        PreparedAppResponse anotherApp = setupApi.registerWithApp();

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(QR_MANAGE)
                .tenantId(response.getTenantId())
                .appId(anotherApp.getAppId())
                .qrId(response.getQrId())
                .build();

        assertError(() -> OssTokenApi.getOssTokenRaw(response.getJwt(), command), QR_NOT_BELONG_TO_APP);
    }

    @Test
    public void group_manager_should_fetch_oss_token_for_qr_manage() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());

        String memberId = memberResponse.getMemberId();
        GroupApi.addGroupMembers(response.getJwt(), response.getDefaultGroupId(), memberId);
        GroupApi.addGroupManager(response.getJwt(), response.getDefaultGroupId(), memberId);

        RequestOssTokenCommand ossCommand = RequestOssTokenCommand.builder()
                .type(QR_MANAGE)
                .tenantId(response.getTenantId())
                .appId(response.getAppId())
                .qrId(response.getQrId())
                .build();

        QOssToken token = OssTokenApi.getOssToken(memberResponse.getJwt(), ossCommand);

        assertNotNull(token.getAccessKeyId());
        assertNotNull(token.getAccessKeySecret());
        assertNotNull(token.getBucket());
        assertNotNull(token.getFolder());
        assertNotNull(token.getEndpoint());
        assertNotNull(token.getSecurityToken());
        assertNotNull(token.getExpiration());
    }

    @Test
    public void normal_member_should_not_fetch_oss_token_for_qr_manage() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(QR_MANAGE)
                .tenantId(response.getTenantId())
                .appId(response.getAppId())
                .qrId(response.getQrId())
                .build();

        assertError(() -> OssTokenApi.getOssTokenRaw(memberResponse.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void anonymous_member_should_not_fetch_oss_token_for_qr_manage() {
        PreparedQrResponse response = setupApi.registerWithQr(rMobile(), rPassword());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(QR_MANAGE)
                .tenantId(response.getTenantId())
                .appId(response.getAppId())
                .qrId(response.getQrId())
                .build();

        assertError(() -> OssTokenApi.getOssTokenRaw(null, command), AUTHENTICATION_FAILED);
    }

    @Test
    public void member_should_fetch_oss_token_for_permissioned_app() {
        LoginResponse admin = setupApi.registerWithLogin(rMobile(), rPassword());
        CreateAppResponse app = AppApi.createApp(admin.getJwt(), AS_TENANT_MEMBER);
        CreateQrResponse qr = QrApi.createQr(admin.getJwt(), rQrName(), app.getDefaultGroupId());
        CreateMemberResponse member = MemberApi.createMemberAndLogin(admin.getJwt(), rMemberName(), rMobile(), rPassword());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(SUBMISSION)
                .tenantId(admin.getTenantId())
                .appId(app.getAppId())
                .qrId(qr.getQrId())
                .pageId(app.getHomePageId())
                .build();

        QOssToken token = OssTokenApi.getOssToken(member.getJwt(), command);

        assertNotNull(token.getAccessKeyId());
        assertNotNull(token.getAccessKeySecret());
        assertNotNull(token.getBucket());
        assertNotNull(token.getFolder());
        assertEquals(admin.getTenantId() + "/" + app.getAppId() + "/" + qr.getQrId() + "/" + app.getHomePageId() + "/_SUBMISSION/" + LocalDate.now(), token.getFolder());
        assertNotNull(token.getEndpoint());
        assertNotNull(token.getSecurityToken());
        assertNotNull(token.getExpiration());
    }

    @Test
    public void anonymous_member_should_not_fetch_oss_token_for_non_public_app() {
        LoginResponse admin = setupApi.registerWithLogin(rMobile(), rPassword());
        CreateAppResponse app = AppApi.createApp(admin.getJwt(), AS_TENANT_MEMBER);
        CreateQrResponse qr = QrApi.createQr(admin.getJwt(), rQrName(), app.getDefaultGroupId());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(SUBMISSION)
                .tenantId(admin.getTenantId())
                .appId(app.getAppId())
                .qrId(qr.getQrId())
                .pageId(app.getHomePageId())
                .build();
        assertError(() -> OssTokenApi.getOssTokenRaw(null, command), AUTHENTICATION_FAILED);
    }

    @Test
    public void anonymous_member_should_fetch_oss_token_for_public_app() {
        LoginResponse admin = setupApi.registerWithLogin(rMobile(), rPassword());
        CreateAppResponse app = AppApi.createApp(admin.getJwt(), PUBLIC);
        CreateQrResponse qr = QrApi.createQr(admin.getJwt(), rQrName(), app.getDefaultGroupId());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(SUBMISSION)
                .tenantId(admin.getTenantId())
                .appId(app.getAppId())
                .qrId(qr.getQrId())
                .pageId(app.getHomePageId())
                .build();

        QOssToken token = OssTokenApi.getOssToken(null, command);

        assertNotNull(token.getAccessKeyId());
        assertNotNull(token.getAccessKeySecret());
        assertNotNull(token.getBucket());
        assertNotNull(token.getFolder());
        assertEquals(admin.getTenantId() + "/" + app.getAppId() + "/" + qr.getQrId() + "/" + app.getHomePageId() + "/_SUBMISSION/" + LocalDate.now(), token.getFolder());
        assertNotNull(token.getEndpoint());
        assertNotNull(token.getSecurityToken());
        assertNotNull(token.getExpiration());
    }

    @Test
    public void should_fail_fetch_oss_token_for_permissioned_app_if_qr_not_under_app() {
        PreparedQrResponse response = setupApi.registerWithQr();
        PreparedAppResponse anotherApp = setupApi.registerWithApp();

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(SUBMISSION)
                .tenantId(response.getTenantId())
                .appId(anotherApp.getAppId())
                .qrId(response.getQrId())
                .pageId(response.getHomePageId())
                .build();

        assertError(() -> OssTokenApi.getOssTokenRaw(response.getJwt(), command), QR_NOT_BELONG_TO_APP);
    }

    @Test
    public void member_should_fetch_member_info_oss_token() {
        PreparedAppResponse response = setupApi.registerWithApp();

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(MEMBER_INFO)
                .tenantId(response.getTenantId())
                .memberId(response.getMemberId())
                .build();

        QOssToken token = OssTokenApi.getOssToken(response.getJwt(), command);
        assertNotNull(token.getAccessKeyId());
        assertNotNull(token.getAccessKeySecret());
        assertNotNull(token.getBucket());
        assertNotNull(token.getFolder());
        assertEquals(response.getTenantId() + "/_MEMBER_INFO/" + response.getMemberId() + "/" + LocalDate.now(), token.getFolder());
        assertNotNull(token.getEndpoint());
        assertNotNull(token.getSecurityToken());
        assertNotNull(token.getExpiration());
    }

    @Test
    public void one_member_should_not_fetch_member_info_oss_token_for_another() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(MEMBER_INFO)
                .tenantId(response.getTenantId())
                .memberId(memberResponse.getMemberId())
                .build();

        assertError(() -> OssTokenApi.getOssTokenRaw(response.getJwt(), command), USER_NOT_CURRENT_MEMBER);
    }

    @Test
    public void should_fail_generate_oss_token_if_max_storage_reached() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        float maxAllowedStorage = tenant.getPackages().effectiveMaxStorage();
        tenant.setStorage(maxAllowedStorage, NOUSER);
        tenantRepository.save(tenant);

        RequestOssTokenCommand command = RequestOssTokenCommand.builder()
                .type(TENANT_EDIT)
                .tenantId(response.getTenantId())
                .build();

        assertError(() -> OssTokenApi.getOssTokenRaw(response.getJwt(), command), MAX_STORAGE_REACHED);
    }

}