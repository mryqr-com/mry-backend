package com.mryqr.common.wx.auth.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MobileWxAuthUserInfo {
    private final String openId;
    private final String nickname;
    private final String headerImageUrl;
}
