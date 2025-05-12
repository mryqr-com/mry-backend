package com.mryqr.core.app.domain.plate;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.plate.control.PlateControl;
import com.mryqr.core.app.domain.plate.control.QrImageControl;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import static com.mryqr.core.app.domain.plate.PlateConfig.defaultPlateConfig;
import static com.mryqr.core.app.domain.plate.control.PlateControlType.QR_IMAGE;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.CENTER;
import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static com.mryqr.core.app.domain.ui.borderradius.BorderRadius.noBorderRadius;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_CONTROL_ID_DUPLICATED;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class PlateSetting {
    @Valid
    @NotNull
    private PlateConfig config;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = 10)
    private List<PlateControl> controls;

    @EqualsAndHashCode.Exclude
    protected boolean complete;

    public static PlateSetting create() {
        return PlateSetting.builder()
                .config(defaultPlateConfig())
                .controls(List.of())
                .build();
    }

    public void correct() {
        config.correct();

        if (isEmpty(controls)) {//没有控件时，默认正方形二维码
            config = defaultPlateConfig();
            controls = List.of(QrImageControl.builder()
                    .id(newShortUuid())
                    .type(QR_IMAGE)
                    .marginTop(10)
                    .marginBottom(10)
                    .marginLeft(10)
                    .marginRight(10)
                    .border(noBorder())
                    .borderRadius(noBorderRadius())
                    .setting(PlateQrImageSetting.builder()
                            .width(460)
                            .color("#000000")
                            .margin(1)
                            .build())
                    .alignType(CENTER)
                    .build());
        }

        controls.forEach(PlateControl::correct);
        this.complete = controls.stream().allMatch(PlateControl::isComplete);
    }

    public void validate(AppSettingContext context) {
        if (isDuplicated(controls)) {
            throw new MryException(PLATE_CONTROL_ID_DUPLICATED, "码牌控件不能包含重复ID。");
        }

        controls.forEach(control -> control.validate(context));
    }

    public boolean isAttributeReferenced() {
        return controls.stream().anyMatch(control -> !control.referencedAttributeIds().isEmpty());
    }
}
