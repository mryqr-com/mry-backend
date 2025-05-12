package com.mryqr.core.app.domain.attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AttributeCheckChangeResult {
    private final Set<AttributeInfo> createdAttributes;
    private final Set<AttributeInfo> deletedAttributes;
}
