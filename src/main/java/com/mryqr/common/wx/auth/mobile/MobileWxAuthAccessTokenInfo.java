package com.mryqr.common.wx.auth.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MobileWxAuthAccessTokenInfo {
    private final String accessToken;
    private final String openId;
    private final String unionId;
}
