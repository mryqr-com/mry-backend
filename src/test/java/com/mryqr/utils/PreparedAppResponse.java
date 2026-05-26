package com.mryqr.utils;

public record PreparedAppResponse(String tenantId,
                                  String memberId,
                                  String appId,
                                  String defaultGroupId,
                                  String homePageId,
                                  String jwt) {
}
