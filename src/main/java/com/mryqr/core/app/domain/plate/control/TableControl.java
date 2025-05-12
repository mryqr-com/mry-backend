package com.mryqr.core.app.domain.plate.control;

import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.color.Color;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.plate.PlateNamedTextValue;
import com.mryqr.core.app.domain.plate.PlateQrImageSetting;
import com.mryqr.core.app.domain.plate.PlateTextValue;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.app.domain.ui.align.HorizontalAlignType;
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
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.app.domain.plate.PlateQrPropertyType.QR_NAME;
import static com.mryqr.core.app.domain.plate.PlateTextValueType.QR_PROPERTY;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.JUSTIFY;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
@TypeAlias("TABLE")
public class TableControl extends PlateControl {

    private boolean headerEnabled;

    @Valid
    @NotNull
    private PlateTextValue headerTextValue;

    @Valid
    @NotNull
    private FontStyle headerFontStyle;

    @Min(20)
    @Max(200)
    private int headerHeight;

    @NotNull
    private HorizontalAlignType headerAlignType;

    @Valid
    @NotNull
    @NoNullElement
    @Size(min = 1, max = 10)
    private List<PlateNamedTextValue> contentTextValues;

    @Valid
    @NotNull
    private FontStyle contentFontStyle;

    @Min(20)
    @Max(200)
    private int cellHeight;

    @Min(1)
    @Max(5)
    private int borderWidth;

    @Color
    private String borderColor;

    private boolean qrEnabled;

    @Valid
    @NotNull
    private PlateQrImageSetting qrImageSetting;

    @Min(1)
    @Max(10)
    private int qrRows;

    @Override
    protected void doCorrect() {
        if (!headerEnabled) {//没有header时也需要默认值
            headerTextValue = PlateTextValue.builder()
                    .type(QR_PROPERTY)
                    .propertyType(QR_NAME)
                    .build();

            headerFontStyle = FontStyle.builder()
                    .fontFamily("默认")
                    .fontSize(15)
                    .bold(true)
                    .italic(false)
                    .color("#444")
                    .build();

            headerAlignType = JUSTIFY;
        }

        headerTextValue.correct();

        if (!qrEnabled) {
            qrRows = 3;
            qrImageSetting.reset();
        }

        contentTextValues.forEach(PlateNamedTextValue::correct);
        this.complete = headerTextValue.isComplete() &&
                        contentTextValues.stream().allMatch(PlateNamedTextValue::isComplete);
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        headerTextValue.validate(context);
        contentTextValues.forEach(value -> value.validate(context));
    }

    @Override
    public Set<String> referencedAttributeIds() {
        return Stream.concat(headerTextValue.referencedAttributeIds().stream(),
                        contentTextValues.stream().map(PlateNamedTextValue::referencedAttributeIds).flatMap(Collection::stream))
                .collect(toImmutableSet());
    }
}
