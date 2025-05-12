package com.mryqr.common.wx;

import com.mryqr.BaseApiTest;
import com.mryqr.common.wx.jssdk.FetchWxJsSdkConfigQuery;
import com.mryqr.common.wx.jssdk.QJsSdkConfig;

public class WxApi {

    public static WxController.QMobileWxInfo getMobileWxInfo() {
        return BaseApiTest.given().when()
                .get("/wx/mobile-info").then()
                .statusCode(200)
                .extract()
                .as(WxController.QMobileWxInfo.class);

    }

    public static WxController.QPcWxInfo getPcWxInfo() {
        return BaseApiTest.given().when()
                .get("/wx/pc-info").then()
                .statusCode(200)
                .extract()
                .as(WxController.QPcWxInfo.class);

    }

    public static QJsSdkConfig fetchJsSdkConfig(String url) {
        return BaseApiTest.given().body(FetchWxJsSdkConfigQuery.builder().url(url).build())
                .when()
                .post("/wx/jssdk-config")
                .then()
                .statusCode(200)
                .extract()
                .as(QJsSdkConfig.class);

    }

}
