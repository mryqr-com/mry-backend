package com.mryqr.core.qr.domain;

import com.mryqr.common.domain.indexedfield.IndexedValues;
import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.App;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.collections4.SetUtils.emptyIfNull;

@Component
@RequiredArgsConstructor
public class QrHouseKeeper {
    public void perform(QR qr, App app, User user) {
        qr.cleanAttributeValues(app, user);
        qr.setIndexedValues(calculateIndexedValues(qr, app));
        qr.setSearchableValues(calculateSearchableAttributeValues(qr));
    }

    private IndexedValues calculateIndexedValues(QR qr, App app) {
        if (qr.isAttributeValuesEmpty()) {
            return null;
        }

        IndexedValues indexedValues = new IndexedValues();
        qr.getAttributeValues().values().forEach(value ->
                app.indexedFieldForAttributeOptional(value.getAttributeId())
                        .ifPresent(indexedField -> indexedValues.setFieldValue(indexedField, value.indexedValue())));

        return indexedValues;
    }

    private Set<String> calculateSearchableAttributeValues(QR qr) {
        if (qr.isAttributeValuesEmpty()) {
            return null;
        }

        return qr.getAttributeValues().values().stream()
                .map(value -> emptyIfNull(value.searchableValues()))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(toImmutableSet());
    }
}
