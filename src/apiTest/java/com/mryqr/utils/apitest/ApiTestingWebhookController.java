package com.mryqr.utils.apitest;

import com.mryqr.common.webhook.WebhookPayload;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping(value = "/api-testing/webhook")
public class ApiTestingWebhookController {
    public static WebhookPayload lastPayload;
    public static String lastAuthString;

    @PostMapping
    public void receiveWebhook(@RequestHeader("Authorization") String authorization,
                               @RequestBody WebhookPayload payload) {
        lastPayload = payload;
        lastAuthString = authorization;
    }
}
