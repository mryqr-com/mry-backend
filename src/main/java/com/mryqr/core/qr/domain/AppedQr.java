package com.mryqr.core.qr.domain;

import com.mryqr.core.app.domain.App;
import lombok.Getter;

@Getter
public class AppedQr {
    private final QR qr;
    private final App app;

    public AppedQr(QR qr, App app) {
        if (!qr.getTenantId().equals(app.getTenantId())) {
            throw new IllegalStateException("QR and App are not under the same tenant.");
        }
        this.qr = qr;
        this.app = app;
    }
}
