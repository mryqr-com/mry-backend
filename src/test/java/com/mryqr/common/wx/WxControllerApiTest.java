package com.mryqr.common.wx;

import com.mryqr.BaseApiTest;
import com.mryqr.common.wx.jssdk.QJsSdkConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WxControllerApiTest extends BaseApiTest {

    @Test
    @Disabled("由于CI环境未配置mobileAppId和mobileAppSecret，因此无法运行")
    public void should_create_wx_jssdk_config() {
        String url = "http://www.whatever.com";

        QJsSdkConfig response = WxApi.fetchJsSdkConfig(url);
        assertNotNull(response.getAppId());
        assertNotNull(response.getNonce());
        assertNotNull(response.getSignature());
        assertEquals(url, response.getUrl());
    }

    @Test
    public void should_fetch_mobile_wx_info() {
        WxController.QMobileWxInfo wxInfo = WxApi.getMobileWxInfo();

        assertNotNull(wxInfo);
        assertNotNull(wxInfo.getMobileAppId());
        assertNotNull(wxInfo.getMobileAuthRedirectUrl());
    }

    @Test
    public void should_fetch_pc_wx_info() {
        WxController.QPcWxInfo wxInfo = WxApi.getPcWxInfo();

        assertNotNull(wxInfo);
        assertNotNull(wxInfo.getPcAppId());
        assertNotNull(wxInfo.getPcAuthRedirectUrl());
    }

}
