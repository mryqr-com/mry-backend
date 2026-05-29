package com.mryqr.core.authentication;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.query.ListMyManagedAppsQuery;
import com.mryqr.core.login.command.JwtTokenResponse;
import com.mryqr.core.login.command.MobileOrEmailLoginCommand;
import com.mryqr.core.register.command.RegisterResponse;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedAppResponse;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.common.utils.MryConstants.AUTH_COOKIE_NAME;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuthenticationCommonApiTest extends BaseApiTest {

    @Test
    public void should_fail_authentication_without_jwt() {
        BaseApiTest.given().when()
                .put("/refresh-token")
                .then()
                .statusCode(401);
    }

    @Test
    public void should_fail_authentication_with_bad_jwt() {
        BaseApiTest.given("some bad jwt").when()
                .put("/refresh-token")
                .then()
                .statusCode(401);
    }

    @Test
    public void should_fail_authentication_with_expired_jwt() {
        BaseApiTest.given(
                        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJNQlI5MzI1NTc4MDIyNTIwMTE1MiIsInJvbGUiOiJURU5BTlRfQURNSU4iLCJ0ZW5hbnQiOiJUTlQ5MzI1NTc4MDIyMTAwNjg0OCIsImlzcyI6Im1yeS1sb2NhbC1pc3N1ZXIiLCJpYXQiOjE2MDAwNzQzNzIsImV4cCI6MTYwMDA4MTU3Mn0.OWS9GefYwNnFY7lImxqpIFujDUNlmZkpHm9CMJ8v2T8YyByKulIpYjStt5IHqRZYaW6dek0GC7HcjZG-abykCQ")
                .when()
                .put("/refresh-token")
                .then()
                .statusCode(401);
    }

    @Test
    public void should_authenticate_with_api_key() {
        PreparedAppResponse response = setupApi.registerWithApp();

        Tenant tenant = tenantRepository.byId(response.getTenantId());

        BaseApiTest.given()
                .auth().preemptive()
                .basic(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret())
                .when()
                .get("/integration/apps/{appId}", response.getAppId())
                .then()
                .statusCode(200);
    }

    @Test
    public void api_key_should_not_call_non_integration_controllers() {
        String mobile = rMobile();
        String password = rPassword();
        RegisterResponse response = setupApi.register(mobile, password);

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        BaseApiTest.given()
                .auth().preemptive()
                .basic(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret())
                .when()
                .get("/apps/my-viewable-apps")
                .then()
                .statusCode(401);
    }

    @Test
    public void should_fail_api_authentication_if_developer_not_enabled() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant theTenant = tenantRepository.byId(response.getTenantId());
        setupApi.updateTenantPlan(theTenant, theTenant.currentPlan().withDeveloperAllowed(false));

        BaseApiTest.given()
                .auth().preemptive()
                .basic(theTenant.getApiSetting().getApiKey(), theTenant.getApiSetting().getApiSecret())
                .when()
                .get("/integration/apps/{appId}", response.getAppId())
                .then()
                .statusCode(401);
    }

    @Test
    public void should_fail_api_authentication_if_credential_not_match() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Tenant tenant = tenantRepository.byId(response.getTenantId());

        BaseApiTest.given()
                .auth().preemptive()
                .basic(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret() + "random")
                .when()
                .get("/integration/apps/{appId}", response.getAppId())
                .then()
                .statusCode(401);
    }

    @Test
    public void should_fail_api_authentication_if_tenant_not_active() {
        PreparedAppResponse response = setupApi.registerWithApp();

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        tenant.deactivate(NO_USER);
        tenantRepository.save(tenant);

        BaseApiTest.given()
                .auth().preemptive()
                .basic(tenant.getApiSetting().getApiKey(), tenant.getApiSetting().getApiSecret())
                .when()
                .get("/integration/apps/{appId}", response.getAppId())
                .then()
                .statusCode(401);
    }

    @Test
    public void should_auto_refresh_jwt_if_near_expire() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        String nearExpireJwt = jwtService.generateJwt(response.getMemberId(), new Date(new Date().getTime() + 60L * 1000L));

        ListMyManagedAppsQuery queryCommand = ListMyManagedAppsQuery.builder().pageIndex(1).pageSize(10).build();
        Cookie cookie = BaseApiTest.given(nearExpireJwt)
                .body(queryCommand)
                .when()
                .post("/apps/my-managed-apps")
                .then()
                .statusCode(200)
                .extract()
                .detailedCookie(AUTH_COOKIE_NAME);

        assertNotNull(cookie);
        assertNotEquals(nearExpireJwt, cookie.getValue());

        BaseApiTest.givenBearer(cookie.getValue())
                .when()
                .get("/members/me")
                .then()
                .statusCode(200);
    }

    @Test
    public void should_use_bearer_token() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);
        MobileOrEmailLoginCommand command = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(mobile)
                .password(password)
                .build();

        JwtTokenResponse jwtTokenResponse = BaseApiTest.given()
                .body(command)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .as(JwtTokenResponse.class);

        BaseApiTest.givenBearer(jwtTokenResponse.getToken())
                .when()
                .get("/members/me")
                .then()
                .statusCode(200);
    }
}
