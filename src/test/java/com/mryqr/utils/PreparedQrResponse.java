package com.mryqr.utils;

public class PreparedQrResponse {
    private final String tenantId;
    private final String memberId;
    private final String appId;
    private final String defaultGroupId;
    private final String homePageId;
    private final String qrId;
    private final String plateId;
    private final String jwt;

    public PreparedQrResponse(String tenantId,
                              String memberId,
                              String appId,
                              String defaultGroupId,
                              String homePageId,
                              String qrId,
                              String plateId,
                              String jwt) {
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.appId = appId;
        this.homePageId = homePageId;
        this.qrId = qrId;
        this.plateId = plateId;
        this.defaultGroupId = defaultGroupId;
        this.jwt = jwt;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getAppId() {
        return appId;
    }

    public String getQrId() {
        return qrId;
    }

    public String getDefaultGroupId() {
        return defaultGroupId;
    }

    public String getJwt() {
        return jwt;
    }

    public String getPlateId() {
        return plateId;
    }

    public String getHomePageId() {
        return homePageId;
    }
}
