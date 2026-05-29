package com.mryqr.common.wx;

import com.mryqr.common.properties.PropertyService;
import com.mryqr.common.properties.WxProperties;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.wx.jssdk.FetchWxJsSdkConfigQuery;
import com.mryqr.common.wx.jssdk.QJsSdkConfig;
import com.mryqr.common.wx.jssdk.WxJsSdkService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/wx")
@RequiredArgsConstructor
public class WxController {
    private final WxJsSdkService wxJsSdkService;
    private final MryRateLimiter mryRateLimiter;
    private final WxProperties wxProperties;
    private final PropertyService propertyService;

    @PostMapping(value = "/jssdk-config")
    public QJsSdkConfig fetchJsSdkConfig(@RequestBody @Valid FetchWxJsSdkConfigQuery queryCommand) {
        mryRateLimiter.applyFor("JsSdkConfig", 1000);
        return wxJsSdkService.generateJsSdkConfig(queryCommand.getUrl());
    }

    @GetMapping(value = "/mobile-info")
    public QMobileWxInfo mobileWxInfo() {
        mryRateLimiter.applyFor("MobileWxInfo", 1000);

        return QMobileWxInfo.builder()
                .mobileAppId(wxProperties.getMobileAppId())
                .mobileAuthRedirectUrl(propertyService.mobileWxAuthRedirectUrl())
                .build();
    }

    @GetMapping(value = "/pc-info")
    public QPcWxInfo pcWxInfo() {
        mryRateLimiter.applyFor("PcWxInfo", 100);

        return QPcWxInfo.builder()
                .pcAppId(wxProperties.getPcAppId())
                .pcAuthRedirectUrl(propertyService.pcWxAuthRedirectUrl())
                .build();
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class QMobileWxInfo {
        private final String mobileAppId;
        private final String mobileAuthRedirectUrl;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class QPcWxInfo {
        private final String pcAppId;
        private final String pcAuthRedirectUrl;
    }
}
