package com.mryqr.core.app.domain.plate.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.align.HorizontalAlignType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
@TypeAlias("HEADER_IMAGE")
public class HeaderImageControl extends PlateControl {

    @Min(50)
    @Max(500)
    private int width;

    @Min(50)
    @Max(500)
    private int height;

    @NotNull
    private HorizontalAlignType alignType;

    @Override
    protected void doCorrect() {

    }

    @Override
    protected void doValidate(AppSettingContext context) {

    }

    @Override
    public Set<String> referencedAttributeIds() {
        return Set.of();
    }
}
