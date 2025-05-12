package com.mryqr.utils;

import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.CreateAppResponse;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.plan.domain.PlanType;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.register.RegisterApi;
import com.mryqr.core.register.command.RegisterCommand;
import com.mryqr.core.register.command.RegisterResponse;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import com.mryqr.core.verification.VerificationCodeApi;
import com.mryqr.core.verification.domain.VerificationCodeRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.utils.RandomTestFixture.rAppName;
import static com.mryqr.utils.RandomTestFixture.rMemberName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static com.mryqr.utils.RandomTestFixture.rQrName;
import static com.mryqr.utils.RandomTestFixture.rTenantName;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;

@Component
public class SetupApi {
    private final VerificationCodeRepository verificationCodeRepository;
    private final TenantRepository tenantRepository;

    public SetupApi(VerificationCodeRepository verificationCodeRepository,
                    TenantRepository tenantRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.tenantRepository = tenantRepository;
    }

    public RegisterResponse register(String mobileOrEmail, String password) {
        return register(rMemberName(), mobileOrEmail, password);
    }

    public RegisterResponse register(String memberName, String mobileOrEmail, String password) {
        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobileOrEmail);
        String code = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(mobileOrEmail)
                .verification(code)
                .password(password)
                .username(memberName)
                .tenantName(rTenantName())
                .agreement(true)
                .build();

        return RegisterApi.register(command);
    }

    public RegisterResponse register() {
        return register(rMobile(), rPassword());
    }

    public LoginResponse registerWithLogin(String mobileOrEmail, String password) {
        RegisterResponse response = register(mobileOrEmail, password);
        String jwt = LoginApi.loginWithMobileOrEmail(mobileOrEmail, password);
        return new LoginResponse(response.getTenantId(), response.getMemberId(), jwt);
    }

    public LoginResponse registerWithLogin(String memberName, String mobileOrEmail, String password) {
        RegisterResponse response = register(memberName, mobileOrEmail, password);
        String jwt = LoginApi.loginWithMobileOrEmail(mobileOrEmail, password);
        return new LoginResponse(response.getTenantId(), response.getMemberId(), jwt);
    }

    public LoginResponse registerWithLogin() {
        return registerWithLogin(rMobile(), rPassword());
    }

    public PreparedAppResponse registerWithApp(String mobileOrEmail, String password) {
        LoginResponse response = registerWithLogin(mobileOrEmail, password);
        CreateAppResponse appResponse = AppApi.createApp(response.getJwt(), rAppName());
        return new PreparedAppResponse(response.getTenantId(),
                response.getMemberId(),
                appResponse.getAppId(),
                appResponse.getDefaultGroupId(),
                appResponse.getHomePageId(),
                response.getJwt());
    }

    public PreparedAppResponse registerWithApp() {
        return registerWithApp(rMobile(), rPassword());
    }

    public PreparedQrResponse registerWithQr(String mobileOrEmail, String password) {
        PreparedAppResponse response = registerWithApp(mobileOrEmail, password);
        CreateQrResponse createQrResponse = QrApi.createQr(response.getJwt(), rQrName(), response.getDefaultGroupId());
        return new PreparedQrResponse(response.getTenantId(),
                response.getMemberId(),
                response.getAppId(),
                response.getDefaultGroupId(),
                response.getHomePageId(),
                createQrResponse.getQrId(),
                createQrResponse.getPlateId(),
                response.getJwt());
    }

    public PreparedQrResponse registerWithQr() {
        return registerWithQr(rMobile(), rPassword());
    }

    public void updateTenantPackages(Tenant tenant, PlanType planType, Instant expireAt) {
        tenant.updatePlanType(planType, expireAt, NOUSER);
        tenantRepository.save(tenant);
    }

    public void updateTenantPackages(String tenantId, PlanType planType) {
        Tenant tenant = tenantRepository.byId(tenantId);
        tenant.updatePlanType(planType, now().plus(365, DAYS), NOUSER);
        tenantRepository.save(tenant);
    }
}
