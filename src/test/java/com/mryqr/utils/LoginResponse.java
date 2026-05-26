package com.mryqr.utils;

public record LoginResponse(String tenantId, String memberId, String jwt) {
}
