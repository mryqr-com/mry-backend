package com.mryqr.core.app.domain.plate.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.plate.PlateQrImageSetting;
import com.mryqr.core.app.domain.ui.align.HorizontalAlignType;
import jakarta.validation.Valid;
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
@TypeAlias("QR_IMAGE")
public class QrImageControl extends PlateControl {
    @Valid
    @NotNull
    private PlateQrImageSetting setting;

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
