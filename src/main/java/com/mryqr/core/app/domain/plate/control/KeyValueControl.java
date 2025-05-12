package com.mryqr.core.app.domain.plate.control;

import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.plate.PlateNamedTextValue;
import com.mryqr.core.app.domain.plate.PlateQrImageSetting;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.app.domain.ui.align.HorizontalAlignType;
import com.mryqr.core.app.domain.ui.align.HorizontalPositionType;
import com.mryqr.core.app.domain.ui.align.VerticalAlignType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
@TypeAlias("KEY_VALUE")
public class KeyValueControl extends PlateControl {
    @Valid
    @NotNull
    @NoNullElement
    @Size(min = 1, max = 10)
    private List<PlateNamedTextValue> textValues;

    @Valid
    @NotNull
    private FontStyle fontStyle;

    @Min(20)
    @Max(200)
    private int lineHeight;

    @NotNull
    private HorizontalAlignType textHorizontalAlignType;

    @NotNull
    private VerticalAlignType verticalAlignType;

    @NotNull
    private HorizontalPositionType horizontalPositionType;

    @Min(0)
    @Max(100)
    private int horizontalGutter;

    private boolean qrEnabled;

    @Valid
    @NotNull
    private PlateQrImageSetting qrImageSetting;

    @Override
    protected void doCorrect() {
        textValues.forEach(PlateNamedTextValue::correct);

        if (!qrEnabled) {
            qrImageSetting.reset();
        }

        this.complete = textValues.stream().allMatch(PlateNamedTextValue::isComplete);
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        textValues.forEach(value -> value.validate(context));
    }

    @Override
    public Set<String> referencedAttributeIds() {
        return textValues.stream().map(PlateNamedTextValue::referencedAttributeIds)
                .flatMap(Collection::stream).collect(toImmutableSet());
    }
}
