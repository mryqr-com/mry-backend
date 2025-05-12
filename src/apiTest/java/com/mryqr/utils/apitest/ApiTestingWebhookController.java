package com.mryqr.utils.apitest;

import com.mryqr.common.webhook.WebhookPayload;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
