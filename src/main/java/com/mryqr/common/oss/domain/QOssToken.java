package com.mryqr.common.oss.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QOssToken {
    private String securityToken;
    private String accessKeySecret;
    private String accessKeyId;
    private String bucket;
    private String endpoint;
    private boolean secure;
    private Instant expiration;
    private String folder;
}
