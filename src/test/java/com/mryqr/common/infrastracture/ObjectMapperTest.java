package com.mryqr.common.infrastracture;

import com.mryqr.BaseApiTest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectMapperTest extends BaseApiTest {

    @Test
    public void should_create_json() {
        Map<String, String> anyData = Map.of("key", "12345");
        String result = objectMapper.writeValueAsString(anyData);
        assertThat(result, notNullValue());
        Map map = objectMapper.readValue(result, Map.class);
        assertThat(map.get("key"), is("12345"));
    }

    @Test
    public void should_use_long_for_Instant() {
        Instant now = Instant.ofEpochMilli(1779809689641L);
        String result = objectMapper.writeValueAsString(now);
        assertEquals("1779809689641", result);
    }

}
