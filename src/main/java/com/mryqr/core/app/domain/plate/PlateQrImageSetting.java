package com.mryqr.core.app.domain.plate;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.validation.color.Color;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class PlateQrImageSetting {
    @Valid
    private final UploadedFile logo;

    @Min(100)
    @Max(800)
    private int width;

    @Color
    private String color;

    @Min(0)
    @Max(4)
    private int margin;

    public void reset() {
        width = 100;
        color = "#000000";
        margin = 2;
    }
}
