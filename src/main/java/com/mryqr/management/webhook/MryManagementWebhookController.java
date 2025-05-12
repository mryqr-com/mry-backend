package com.mryqr.management.webhook;

import com.mryqr.common.utils.MryObjectMapper;
import com.mryqr.common.webhook.WebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/webhook")
public class MryManagementWebhookController {
    private final MryWebhookDispatcher mryWebhookDispatcher;
    private final MryObjectMapper objectMapper;

    @PostMapping
    public void receiveWebhook(@RequestBody WebhookPayload payload) {
        try {
            mryWebhookDispatcher.dispatch(payload);
        } catch (Throwable t) {
            log.error("Error process webhook: {}", objectMapper.writeValueAsString(payload), t);
        }
    }
}
