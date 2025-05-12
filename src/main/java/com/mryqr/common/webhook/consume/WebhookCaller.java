package com.mryqr.common.webhook.consume;

import com.mryqr.common.webhook.WebhookPayload;
import com.mryqr.core.app.domain.WebhookSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.time.Duration.ofSeconds;
import static java.util.Base64.getEncoder;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class WebhookCaller {
    private final RestTemplate restTemplate;

    public WebhookCaller(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .errorHandler(new WebhookErrorHandler())
                .connectTimeout(ofSeconds(10))
                .readTimeout(ofSeconds(10))
                .build();
    }

    @Retryable(backoff = @Backoff(delay = 500, multiplier = 2, maxDelay = 2000))
    public void call(WebhookPayload payload, WebhookSetting setting) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        if (isNotBlank(setting.getUsername()) && isNotBlank(setting.getPassword())) {
            headers.set("Authorization", createBasicAuth(setting));
        }

        restTemplate.exchange(setting.getUrl(), POST, new HttpEntity<>(payload, headers), String.class);
    }

    private String createBasicAuth(WebhookSetting setting) {
        String auth = setting.getUsername() + ":" + setting.getPassword();
        return "Basic " + getEncoder().encodeToString(auth.getBytes(US_ASCII));
    }

    public static class WebhookErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return !response.getStatusCode().is2xxSuccessful();
        }
    }
}
