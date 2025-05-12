package com.mryqr.core.tenant;

import com.mryqr.BaseApiTest;
import com.mryqr.core.order.domain.delivery.Consignee;
import com.mryqr.core.tenant.command.AddConsigneeCommand;
import com.mryqr.core.tenant.command.UpdateConsigneeCommand;
import com.mryqr.core.tenant.command.UpdateTenantBaseSettingCommand;
import com.mryqr.core.tenant.command.UpdateTenantInvoiceTitleCommand;
import com.mryqr.core.tenant.command.UpdateTenantLogoCommand;
import com.mryqr.core.tenant.command.UpdateTenantSubdomainCommand;
import com.mryqr.core.tenant.query.QTenantApiSetting;
import com.mryqr.core.tenant.query.QTenantBaseSetting;
import com.mryqr.core.tenant.query.QTenantInfo;
import com.mryqr.core.tenant.query.QTenantInvoiceTitle;
import com.mryqr.core.tenant.query.QTenantLogo;
import com.mryqr.core.tenant.query.QTenantPublicProfile;
import com.mryqr.core.tenant.query.QTenantSubdomain;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

public class TenantApi {
    public static void updateBaseSetting(String jwt, UpdateTenantBaseSettingCommand command) {
        BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/tenants/current/base-setting")
                .then()
                .statusCode(200);
    }

    public static Response updateLogoRaw(String jwt, UpdateTenantLogoCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/tenants/current/logo");
    }

    public static void updateLogo(String jwt, UpdateTenantLogoCommand command) {
        updateLogoRaw(jwt, command)
                .then()
                .statusCode(200);
    }

    public static Response updateSubdomainRaw(String jwt, UpdateTenantSubdomainCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/tenants/current/subdomain");
    }

    public static void updateSubdomain(String jwt, UpdateTenantSubdomainCommand command) {
        updateSubdomainRaw(jwt, command)
                .then()
                .statusCode(200);
    }

    public static void updateInvoiceTitle(String jwt, UpdateTenantInvoiceTitleCommand command) {
        BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/tenants/current/invoice-title")
                .then()
                .statusCode(200);
    }

    public static Response refreshApiSecretRaw(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .put("/tenants/current/api-secret");
    }

    public static String refreshApiSecret(String jwt) {
        return refreshApiSecretRaw(jwt)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<Map<String, String>>() {
                })
                .get("secret");
    }

    public static QTenantInfo fetchTenantInfo(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/tenants/current/info")
                .then()
                .statusCode(200)
                .extract()
                .as(QTenantInfo.class);
    }

    public static QTenantBaseSetting fetchBaseSetting(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/tenants/current/base-setting")
                .then()
                .statusCode(200)
                .extract()
                .as(QTenantBaseSetting.class);
    }

    public static QTenantLogo fetchLogo(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/tenants/current/logo")
                .then()
                .statusCode(200)
                .extract()
                .as(QTenantLogo.class);
    }

    public static QTenantSubdomain fetchSubdomain(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/tenants/current/subdomain")
                .then()
                .statusCode(200)
                .extract()
                .as(QTenantSubdomain.class);
    }

    public static QTenantApiSetting fetchApiSetting(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/tenants/current/api-setting")
                .then()
                .statusCode(200)
                .extract()
                .as(QTenantApiSetting.class);
    }

    public static QTenantInvoiceTitle fetchInvoiceTitle(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/tenants/current/invoice-title")
                .then()
                .statusCode(200)
                .extract()
                .as(QTenantInvoiceTitle.class);
    }

    public static QTenantPublicProfile fetchTenantPublicProfile(String subdomain) {
        return BaseApiTest.given()
                .when()
                .get("/tenants/public-profile/{subdomain}", subdomain)
                .then()
                .statusCode(200)
                .extract()
                .as(QTenantPublicProfile.class);
    }

    public static List<Consignee> listConsignees(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/tenants/current/consignees")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static Response addConsigneeRaw(String jwt, AddConsigneeCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/tenants/current/consignees");
    }

    public static void addConsignee(String jwt, AddConsigneeCommand command) {
        addConsigneeRaw(jwt, command)
                .then()
                .statusCode(201);
    }

    public static Response updateConsigneeRaw(String jwt, UpdateConsigneeCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/tenants/current/consignees");
    }

    public static void updateConsignee(String jwt, UpdateConsigneeCommand command) {
        updateConsigneeRaw(jwt, command)
                .then()
                .statusCode(200);
    }

    public static Response deleteConsigneeRaw(String jwt, String id) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/tenants/current/consignees/{id}", id);
    }

    public static void deleteConsignee(String jwt, String id) {
        deleteConsigneeRaw(jwt, id)
                .then()
                .statusCode(200);
    }

}
