package com.mryqr.common.aliyun.oss;

import com.mryqr.BaseApiTest;
import com.mryqr.common.oss.command.RequestOssTokenCommand;
import com.mryqr.common.oss.domain.QOssToken;
import io.restassured.response.Response;

public class OssTokenApi {
    public static Response getOssTokenRaw(String jwt, RequestOssTokenCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/aliyun/oss-token-requisitions");
    }

    public static QOssToken getOssToken(String jwt, RequestOssTokenCommand command) {
        return getOssTokenRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(QOssToken.class);
    }

}
