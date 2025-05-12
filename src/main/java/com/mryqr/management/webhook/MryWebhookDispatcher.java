package com.mryqr.management.webhook;

import com.mryqr.common.webhook.WebhookPayload;
import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.management.crm.webhook.TenantWebhookHandler;
import com.mryqr.management.offencereport.webhook.OffenceWebhookHandler;
import com.mryqr.management.order.webhook.OrderWebhookHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.mryqr.management.crm.MryTenantManageApp.MRY_TENANT_MANAGE_APP_ID;
import static com.mryqr.management.offencereport.MryOffenceReportApp.MRY_OFFENCE_APP_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_APP_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MryWebhookDispatcher {
    private final List<OrderWebhookHandler> orderWebhookHandlers;
    private final List<TenantWebhookHandler> tenantWebhookHandlers;
    private final List<OffenceWebhookHandler> offenceWebhookHandlers;

    public void dispatch(WebhookPayload payload) {
        if (Objects.equals(payload.getAppId(), MRY_TENANT_MANAGE_APP_ID)) {
            if (payload instanceof BaseSubmissionWebhookPayload thePayload) {
                tenantWebhookHandlers.stream()
                        .filter(handler -> handler.canHandle(thePayload))
                        .findFirst().ifPresent(handler -> handler.handle(thePayload));
            }
            return;
        }

        if (Objects.equals(payload.getAppId(), ORDER_APP_ID)) {
            if (payload instanceof BaseSubmissionWebhookPayload thePayload) {
                orderWebhookHandlers.stream()
                        .filter(handler -> handler.canHandle(thePayload))
                        .findFirst().ifPresent(handler -> handler.handle(thePayload));
            }
            return;
        }

        if (Objects.equals(payload.getAppId(), MRY_OFFENCE_APP_ID)) {
            if (payload instanceof BaseSubmissionWebhookPayload thePayload) {
                offenceWebhookHandlers.stream()
                        .filter(handler -> handler.canHandle(thePayload))
                        .findFirst().ifPresent(handler -> handler.handle(thePayload));
            }
        }
    }
}
