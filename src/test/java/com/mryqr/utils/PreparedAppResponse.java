package com.mryqr.utils;

public class PreparedAppResponse {
    private final String tenantId;
    private final String memberId;
    private final String appId;
    private final String defaultGroupId;
    private final String homePageId;
    private final String jwt;

    public PreparedAppResponse(String tenantId,
                               String memberId,
                               String appId,
                               String defaultGroupId,
                               String homePageId,
                               String jwt) {
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.appId = appId;
        this.defaultGroupId = defaultGroupId;
        this.homePageId = homePageId;
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

    public String getDefaultGroupId() {
        return defaultGroupId;
    }

    public String getJwt() {
        return jwt;
    }

    public String getHomePageId() {
        return homePageId;
    }
}
