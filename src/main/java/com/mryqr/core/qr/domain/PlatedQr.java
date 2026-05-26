package com.mryqr.core.qr.domain;

import com.mryqr.core.plate.domain.Plate;

public record PlatedQr(QR qr, Plate plate) {
    public PlatedQr {
        if (!qr.getTenantId().equals(plate.getTenantId())) {
            throw new IllegalStateException("QR and Plate are not under the same tenant.");
        }
    }
}
