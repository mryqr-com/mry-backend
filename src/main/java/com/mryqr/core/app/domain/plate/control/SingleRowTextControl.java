package com.mryqr.core.app.domain.plate.control;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.plate.PlateTextValue;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.app.domain.ui.align.HorizontalAlignType;
import jakarta.validation.Valid;
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
@TypeAlias("SINGLE_ROW_TEXT")
public class SingleRowTextControl extends PlateControl {
    @Valid
    @NotNull
    private PlateTextValue textValue;

    @Valid
    @NotNull
    private FontStyle fontStyle;

    @NotNull
    private HorizontalAlignType alignType;

    @Min(0)
    @Max(50)
    private int letterSpacing;

    @Min(20)
    @Max(500)
    private int height;

    @Valid
    private UploadedFile logo;

    @Min(10)
    @Max(100)
    private int logoHeight;

    @Min(0)
    @Max(100)
    private int logoTextSpacing;

    @Override
    protected void doCorrect() {
        textValue.correct();
        this.complete = textValue.isComplete();
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        textValue.validate(context);
    }

    @Override
    public Set<String> referencedAttributeIds() {
        return textValue.referencedAttributeIds();
    }
}
