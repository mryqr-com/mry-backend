package com.mryqr.common.utils;

import java.util.Collection;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public interface Identified {
    static boolean isDuplicated(Collection<? extends Identified> collection) {
        if (isEmpty(collection)) {
            return false;
        }

        long count = collection.stream().map(Identified::getIdentifier).distinct().count();
        return count != collection.size();
    }

    String getIdentifier();
}
