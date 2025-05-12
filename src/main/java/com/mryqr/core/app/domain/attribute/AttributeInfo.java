package com.mryqr.core.app.domain.attribute;

import com.mryqr.common.domain.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AttributeInfo {
    private String attributeId;
    private AttributeType attributeType;
    private ValueType valueType;
}
