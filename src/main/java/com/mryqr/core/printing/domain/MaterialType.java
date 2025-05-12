package com.mryqr.core.printing.domain;

public enum MaterialType {
    TRANSPARENT_ACRYLIC("透明亚克力"),
    PORCELAIN_ACRYLIC("瓷白亚克力"),
    PVC_CARD("PVC卡"),
    SYNTHETIC_ADHESIVE("PVC不干胶"),
    ARGENTOUS_ADHESIVE("亚银不干胶");

    private final String name;

    MaterialType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
