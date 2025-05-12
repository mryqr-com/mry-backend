package com.mryqr.core.app.domain.ui.borderradius;

import com.mryqr.core.common.validation.collection.NoNullElement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static com.mryqr.core.common.utils.MryConstants.MAX_BORDER_RADIUS;
import static com.mryqr.core.common.utils.MryConstants.MIN_BORDER_RADIUS;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class BorderRadius {
    @Min(MIN_BORDER_RADIUS)
    @Max(MAX_BORDER_RADIUS)
    private int radius;

    @NotNull
    @NoNullElement
    private final Set<BorderRadiusCorner> corners;

    public static BorderRadius noBorderRadius() {
        return BorderRadius.builder().radius(0).corners(Set.of(BorderRadiusCorner.values())).build();
    }

    public enum BorderRadiusCorner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
