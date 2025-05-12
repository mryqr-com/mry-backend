package com.mryqr.common.infrastracture;

import com.mryqr.BaseApiTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("经常挂")
@Slf4j
@TestPropertySource(properties = {"mry.common.limitRate = true"})
public class RateLimiterApiTest extends BaseApiTest {

    @Test
    public void should_apply_rate_limiter() {
        Set<Integer> statusCodes = IntStream.rangeClosed(1, 100)
                .mapToObj(index -> supplyAsync(() -> given().when().get("/about").then().extract().statusCode()))
                .map(CompletableFuture::join)
                .collect(toSet());

        assertEquals(Set.of(200, 429), statusCodes);
    }
}