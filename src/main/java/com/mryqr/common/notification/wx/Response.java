package com.mryqr.common.notification.wx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Response {
    private final int errcode;
    private final String errmsg;
    private final long msgid;
}
