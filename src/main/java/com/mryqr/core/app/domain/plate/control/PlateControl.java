package com.mryqr.core.app.domain.plate.control;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.color.Color;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.borderradius.BorderRadius;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = SingleRowTextControl.class, name = "SINGLE_ROW_TEXT"),
        @JsonSubTypes.Type(value = QrImageControl.class, name = "QR_IMAGE"),
        @JsonSubTypes.Type(value = TableControl.class, name = "TABLE"),
        @JsonSubTypes.Type(value = KeyValueControl.class, name = "KEY_VALUE"),
        @JsonSubTypes.Type(value = HeaderImageControl.class, name = "HEADER_IMAGE"),
})

@Getter
@SuperBuilder
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public abstract class PlateControl implements Identified {
    @EqualsAndHashCode.Exclude
    protected boolean complete;
    @NotBlank
    @ShortUuid
    private String id;
    @NotNull
    private PlateControlType type;
    @Valid
    @NotNull
    private Border border;
    @Valid
    @NotNull
    private BorderRadius borderRadius;
    @Color
    private String backgroundColor;
    @Min(-500)
    @Max(500)
    private int marginTop;
    @Min(0)
    @Max(500)
    private int marginBottom;
    @Min(0)
    @Max(500)
    private int marginLeft;
    @Min(0)
    @Max(500)
    private int marginRight;
    @Min(0)
    @Max(500)
    private int paddingTop;
    @Min(0)
    @Max(500)
    private int paddingBottom;
    @Min(0)
    @Max(500)
    private int paddingLeft;
    @Min(0)
    @Max(500)
    private int paddingRight;

    public void correct() {
        this.complete = true;//先设为true，然后在各个控件中自行设置实际值
        doCorrect();
    }

    public void validate(AppSettingContext context) {
        try {
            doValidate(context);
        } catch (MryException ex) {
            ex.addData("plateControlId", this.getId());
            ex.addData("plateControlType", this.getType());
            throw ex;
        }
    }

    protected abstract void doCorrect();

    protected abstract void doValidate(AppSettingContext context);


    @Override
    public String getIdentifier() {
        return id;
    }

    public abstract Set<String> referencedAttributeIds();
}
