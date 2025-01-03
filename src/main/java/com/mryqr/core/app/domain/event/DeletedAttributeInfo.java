package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.ValueType;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.core.app.domain.attribute.AttributeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class DeletedAttributeInfo {
    private String attributeId;
    private AttributeType type;
    private ValueType valueType;
    private IndexedField indexedField;
}
