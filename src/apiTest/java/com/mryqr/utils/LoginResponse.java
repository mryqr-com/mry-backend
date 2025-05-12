package com.mryqr.utils;

public class LoginResponse {
    private final String tenantId;
    private final String memberId;
    private final String jwt;

    public LoginResponse(String tenantId, String memberId, String jwt) {
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.jwt = jwt;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getJwt() {
        return jwt;
    }
}
