package com.mryqr.common.infrastracture;

import com.mryqr.BaseApiTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mryqr.core.common.exception.ErrorCode.TOO_MANY_REQUEST;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("随机挂")
public class RateLimiterApiTest extends BaseApiTest {

    @Test
    public void should_apply_rate_limiter() {

        List<Integer> statuses = new ArrayList<>();

        for (int i = 0; i < 111; i++) {
            statuses.add(given().when().get("/about").then().extract().statusCode());
        }

        System.out.println(statuses);
        assertTrue(statuses.stream().anyMatch(status -> status == TOO_MANY_REQUEST.getStatus()));
    }

}