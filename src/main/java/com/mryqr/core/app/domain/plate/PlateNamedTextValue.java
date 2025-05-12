package com.mryqr.core.app.domain.plate;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

import static com.mryqr.core.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class PlateNamedTextValue {
    @NotBlank
    @ShortUuid
    private String id;

    @Size(max = MAX_SHORT_NAME_LENGTH)
    private String name;

    @Valid
    @NotNull
    private PlateTextValue value;

    @EqualsAndHashCode.Exclude
    private boolean complete;

    public void correct() {
        if (isBlank(name)) {
            name = "未命名";
        }

        this.value.correct();
        this.complete = value.isComplete();
    }

    public void validate(AppSettingContext context) {
        this.value.validate(context);
    }

    public boolean isAttributeReferenced() {
        return value.isAttributeReferenced();
    }

    public Set<String> referencedAttributeIds() {
        return value.referencedAttributeIds();
    }
}
