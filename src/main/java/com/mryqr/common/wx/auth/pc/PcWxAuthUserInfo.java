package com.mryqr.common.wx.auth.pc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class PcWxAuthUserInfo {
    private final String openId;
    private final String nickname;
    private final String headerImageUrl;
}
