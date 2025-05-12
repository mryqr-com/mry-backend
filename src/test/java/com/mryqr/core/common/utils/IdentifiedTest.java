package com.mryqr.core.common.utils;

import com.mryqr.core.common.utils.Identified;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Sets.newHashSet;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifiedTest {

    @Test
    public void should_check_duplication() {
        String id = newShortUuid();
        FakeIdentified entity1 = new FakeIdentified(id);
        FakeIdentified entity2 = new FakeIdentified(id);
        assertTrue(isDuplicated(newHashSet(entity1, entity2)));
    }

    @Test
    public void should_check_no_duplication() {
        FakeIdentified entity1 = new FakeIdentified(newShortUuid());
        FakeIdentified entity2 = new FakeIdentified(newShortUuid());
        assertFalse(isDuplicated(newHashSet(entity1, entity2)));
    }

    @Test
    public void should_check_no_duplication_given_empty() {
        assertFalse(isDuplicated(newHashSet()));
        assertFalse(isDuplicated(null));
    }

    private class FakeIdentified implements Identified {
        private final String id;

        public FakeIdentified(String id) {
            this.id = id;
        }

        @Override
        public String getIdentifier() {
            return id;
        }
    }
}