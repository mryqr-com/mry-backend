package com.mryqr.core.app.domain.plate;

public enum PlateSize {
    //横版
    MM_50x40(400, 320),
    MM_60x40(480, 320),
    MM_70x50(560, 400),
    MM_80x50(640, 400),
    MM_80x60(640, 480),
    MM_90x60(720, 480),
    MM_100x70(800, 560),

    //竖版
    MM_40x50(320, 400),
    MM_40x60(320, 480),
    MM_50x70(400, 560),
    MM_50x80(400, 640),
    MM_60x80(480, 640),
    MM_60x90(480, 720),
    MM_70x100(560, 800),

    //方版
    MM_50x50(400, 400),
    MM_60x60(480, 480),
    MM_70x70(560, 560),

    CUSTOM(0, 0);

    private final int width;
    private final int height;

    PlateSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
