package com.mryqr.core.app.domain.plate;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.id.attribute.AttributeId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_ATTRIBUTE_NOT_EXIST;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class PlateTextValue {
    @NotNull
    private final PlateTextValueType type;

    @Size(max = 50)
    private String text;

    @AttributeId
    private String attributeId;

    private PlateQrPropertyType propertyType;

    @EqualsAndHashCode.Exclude
    private boolean complete;

    public void correct() {
        switch (type) {
            case FIXED_TEXT -> {
                attributeId = null;
                propertyType = null;
                complete = true;
            }
            case QR_PROPERTY -> {
                text = null;
                attributeId = null;
                complete = propertyType != null;
            }
            case QR_ATTRIBUTE -> {
                text = null;
                propertyType = null;
                complete = isNotBlank(attributeId);
            }
        }
    }

    public void validate(AppSettingContext context) {
        if (isNotBlank(attributeId)) {
            if (context.attributeNotExists(attributeId)) {
                throw new MryException(VALIDATION_ATTRIBUTE_NOT_EXIST, "属性项不存在。",
                        mapOf("attributeId", attributeId));
            }
        }
    }

    public boolean isAttributeReferenced() {
        return isNotBlank(attributeId);
    }

    public Set<String> referencedAttributeIds() {
        return isNotBlank(attributeId) ? Set.of(attributeId) : Set.of();
    }
}
