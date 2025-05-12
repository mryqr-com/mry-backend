package com.mryqr.core.common.utils;

import com.mryqr.core.common.utils.UuidGenerator;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class UuidGeneratorTest {

    @Test
    public void should_generate_random_base64_uuid() {
        String uuid = UuidGenerator.newShortUuid();
        assertThat(uuid.length(), is(22));
    }

}