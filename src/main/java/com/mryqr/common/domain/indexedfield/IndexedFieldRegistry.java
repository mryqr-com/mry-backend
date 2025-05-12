package com.mryqr.common.domain.indexedfield;

import lombok.NoArgsConstructor;

import java.util.*;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class IndexedFieldRegistry {
    private Map<String, IndexedField> fieldMap;

    public static IndexedFieldRegistry create() {
        IndexedFieldRegistry registry = new IndexedFieldRegistry();
        registry.fieldMap = new HashMap<>();
        return registry;
    }

    public void registerKey(String fieldKey) {
        requireNonBlank(fieldKey, "Field key must not be null.");

        if (fieldMap.containsKey(fieldKey)) {
            return;
        }

        IndexedField field = nextAvailableField();
        if (field == null) {
            throw new RuntimeException("Cannot add key[" + fieldKey + "] as the registry is full.");
        }

        fieldMap.put(fieldKey, field);
    }

    private IndexedField nextAvailableField() {
        Set<IndexedField> all = new HashSet<>(asList(IndexedField.values()));
        all.removeAll(fieldMap.values());
        if (all.isEmpty()) {
            return null;
        }

        return all.iterator().next();
    }

    public void removeKey(String fieldKey) {
        requireNonBlank(fieldKey, "Field key must not be blank.");
        fieldMap.remove(fieldKey);
    }

    public Optional<IndexedField> fieldByKeyOptional(String fieldKey) {
        requireNonBlank(fieldKey, "Field key must not be blank.");
        return ofNullable(fieldMap.get(fieldKey));
    }

    public IndexedField fieldByKey(String fieldKey) {
        requireNonBlank(fieldKey, "Field key must not be blank.");

        IndexedField indexedField = fieldMap.get(fieldKey);
        if (indexedField == null) {
            throw new RuntimeException("IndexedField not found for key[" + fieldKey + "].");
        }
        return indexedField;
    }

    public boolean hasKey(String fieldKey) {
        requireNonBlank(fieldKey, "Field key must not be blank.");
        return fieldMap.containsKey(fieldKey);
    }

    public boolean hasField(IndexedField field) {
        requireNonNull(field, "Field must not be null.");
        return fieldMap.containsValue(field);
    }

    public int size() {
        return fieldMap.size();
    }
}
