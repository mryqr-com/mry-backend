package com.mryqr.core.register;

import com.mryqr.BaseApiTest;
import com.mryqr.core.register.command.RegisterCommand;
import com.mryqr.core.register.command.RegisterResponse;
import io.restassured.response.Response;

public class RegisterApi {
    public static RegisterResponse register(RegisterCommand command) {
        return registerRaw(command)
                .then()
                .statusCode(201)
                .extract()
                .as(RegisterResponse.class);
    }

    public static Response registerRaw(RegisterCommand command) {
        return BaseApiTest.given()
                .body(command)
                .when()
                .post("/registration");
    }
}
