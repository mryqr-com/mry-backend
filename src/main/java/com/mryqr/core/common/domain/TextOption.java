package com.mryqr.core.common.domain;

import com.mryqr.core.common.utils.Identified;
import com.mryqr.core.common.validation.color.Color;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class TextOption implements Identified {
    @NotBlank
    @ShortUuid
    private final String id;

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String name;

    private final double numericalValue;

    @Color
    private final String color;

    public void correct() {
        if (isBlank(name)) {
            this.name = "未命名选项";
        }
    }

    @Override
    public String getIdentifier() {
        return id;
    }
}
