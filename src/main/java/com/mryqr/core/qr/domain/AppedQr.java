package com.mryqr.core.qr.domain;

import com.mryqr.core.app.domain.App;

public record AppedQr(QR qr, App app) {
    public AppedQr {
        if (!qr.getTenantId().equals(app.getTenantId())) {
            throw new IllegalStateException("QR and App are not under the same tenant.");
        }
    }
}
