package com.mryqr.utils;

import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.CreateAppResponse;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.plan.domain.Plan;
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

import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.core.plan.domain.Plan.FLAGSHIP_PLAN;
import static com.mryqr.utils.RandomTestFixture.*;

@Component
public class SetupApi {
    private final VerificationCodeRepository verificationCodeRepository;
    private final TenantRepository tenantRepository;

    public SetupApi(
            VerificationCodeRepository verificationCodeRepository,
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

        RegisterResponse response = RegisterApi.register(command);
        this.updateTenantPlan(tenantRepository.byId(response.getTenantId()), FLAGSHIP_PLAN); // 测试时，默认创建最高级别的套餐
        return response;
    }

    public LoginResponse registerWithLogin(String mobileOrEmail, String password) {
        RegisterResponse response = register(mobileOrEmail, password);
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
        tenant.updatePlanType(planType, expireAt, NO_USER);
        tenantRepository.save(tenant);
    }

    public void updateTenantPlan(Tenant tenant, Plan plan) {
        tenant.updatePlan(plan, NO_USER);
        tenantRepository.save(tenant);
    }
}
