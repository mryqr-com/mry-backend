package com.mryqr.common.webhook.consume;

import com.mryqr.common.webhook.WebhookPayload;
import com.mryqr.core.app.domain.WebhookSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Base64.getEncoder;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class WebhookCaller {
    private final RestClient restClient;

    public WebhookCaller(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = builder
                .requestFactory(requestFactory)
                .build();
    }

    @Retryable(delay = 500, multiplier = 2, maxDelay = 2000, maxRetries = 2)
    public void call(WebhookPayload payload, WebhookSetting setting) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        if (isNotBlank(setting.getUsername()) && isNotBlank(setting.getPassword())) {
            headers.set("Authorization", createBasicAuth(setting));
        }

        restClient.post().uri(setting.getUrl()).body(payload).headers(httpHeaders -> httpHeaders.addAll(headers)).retrieve().toBodilessEntity();
    }

    private String createBasicAuth(WebhookSetting setting) {
        String auth = setting.getUsername() + ":" + setting.getPassword();
        return "Basic " + getEncoder().encodeToString(auth.getBytes(US_ASCII));
    }

}
