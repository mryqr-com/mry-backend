package com.mryqr.core.login.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class WxIdInfo {
    private String pcWxOpenId;
    private String mobileWxOpenId;
    private String wxUnionId;
}
