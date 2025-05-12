package com.mryqr.core.app.domain.ui;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MarkdownStyle {
    @Valid
    @NotNull
    private final FontStyle fontStyle;//字体类型

    @Min(1)
    @Max(2)
    private final float lineHeight;//行高

}
