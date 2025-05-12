package com.mryqr.core.app.query;

import com.mryqr.core.app.domain.WebhookSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAppWebhookSetting {
    private final WebhookSetting webhookSetting;
}
