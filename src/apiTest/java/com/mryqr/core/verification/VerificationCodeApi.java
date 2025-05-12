package com.mryqr.core.verification;

import com.mryqr.BaseApiTest;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.verification.command.CreateChangeMobileVerificationCodeCommand;
import com.mryqr.core.verification.command.CreateFindbackPasswordVerificationCodeCommand;
import com.mryqr.core.verification.command.CreateLoginVerificationCodeCommand;
import com.mryqr.core.verification.command.CreateRegisterVerificationCodeCommand;
import com.mryqr.core.verification.command.IdentifyMobileVerificationCodeCommand;
import io.restassured.response.Response;

public class VerificationCodeApi {
    public static Response createVerificationCodeForRegisterRaw(CreateRegisterVerificationCodeCommand command) {
        return BaseApiTest.given()
                .body(command)
                .when()
                .post("/verification-codes/for-register");
    }

    public static String createVerificationCodeForRegister(CreateRegisterVerificationCodeCommand command) {
        return createVerificationCodeForRegisterRaw(command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static String createVerificationCodeForRegister(String mobileOrEmail) {
        return createVerificationCodeForRegister(CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(mobileOrEmail).build());
    }

    public static Response createVerificationCodeForLoginRaw(CreateLoginVerificationCodeCommand command) {
        return BaseApiTest.given()
                .body(command)
                .when()
                .post("/verification-codes/for-login");
    }

    public static String createVerificationCodeForLogin(CreateLoginVerificationCodeCommand command) {
        return createVerificationCodeForLoginRaw(command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }


    public static Response createVerificationCodeForFindbackPasswordRaw(CreateFindbackPasswordVerificationCodeCommand command) {
        return BaseApiTest.given()
                .body(command)
                .when()
                .post("/verification-codes/for-findback-password");
    }

    public static String createVerificationCodeForFindbackPassword(CreateFindbackPasswordVerificationCodeCommand command) {
        return createVerificationCodeForFindbackPasswordRaw(command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static Response createVerificationCodeForChangeMobileRaw(String jwt, CreateChangeMobileVerificationCodeCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/verification-codes/for-change-mobile");
    }

    public static String createVerificationCodeForChangeMobile(String jwt, CreateChangeMobileVerificationCodeCommand command) {
        return createVerificationCodeForChangeMobileRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static Response createVerificationCodeForIdentifyMobileRaw(String jwt, IdentifyMobileVerificationCodeCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/verification-codes/for-identify-mobile");
    }

    public static String createVerificationCodeForIdentifyMobile(String jwt, IdentifyMobileVerificationCodeCommand command) {
        return createVerificationCodeForIdentifyMobileRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }
}
