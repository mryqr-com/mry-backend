package com.mryqr.core.app.domain.plate;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.validation.color.Color;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.core.app.domain.plate.PlateSize.CUSTOM;
import static com.mryqr.core.app.domain.plate.PlateSize.MM_60x60;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class PlateConfig {
    @NotNull
    private final PlateSize size;

    @Min(320)
    @Max(1200)
    private int width;

    @Min(320)
    @Max(1200)
    private int height;

    @Valid
    private final UploadedFile backgroundImage;

    @Color
    private final String backgroundColor;

    @Min(0)
    @Max(100)
    private final int borderRadius;

    @Min(0)
    @Max(100)
    private final int padding;

    public void correct() {
        if (size != CUSTOM) {
            this.width = size.getWidth();
            this.height = size.getHeight();
        }
    }

    public static PlateConfig defaultPlateConfig() {
        return PlateConfig.builder()
                .size(MM_60x60)
                .width(480)
                .height(480)
                .build();
    }
}
