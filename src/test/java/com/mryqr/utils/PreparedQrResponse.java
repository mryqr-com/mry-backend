package com.mryqr.utils;

public record PreparedQrResponse(String tenantId, String memberId, String appId, String defaultGroupId,
                                 String homePageId, String qrId, String plateId, String jwt) {
}
