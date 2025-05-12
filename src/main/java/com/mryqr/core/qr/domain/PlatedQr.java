package com.mryqr.core.qr.domain;

import com.mryqr.core.plate.domain.Plate;
import lombok.Getter;

@Getter
public class PlatedQr {
    private final QR qr;
    private final Plate plate;

    public PlatedQr(QR qr, Plate plate) {
        if (!qr.getTenantId().equals(plate.getTenantId())) {
            throw new IllegalStateException("QR and Plate are not under the same tenant.");
        }
        this.qr = qr;
        this.plate = plate;
    }
}
