package com.mryqr.common.wx.jssdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mryqr.common.wx.accesstoken.WxAccessTokenService;
import com.mryqr.core.common.properties.WxProperties;
import com.mryqr.core.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class WxJsSdkService {
    private static final String JSAPI_TICKET = "Wx:JsApiTicket";
    private final RestTemplate restTemplate;
    private final WxProperties wxProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final MryObjectMapper objectMapper;
    private final WxAccessTokenService wxAccessTokenService;

    @Retryable(backoff = @Backoff(delay = 1000, multiplier = 3))
    public void refreshJsApiTicket() {
        if (!wxProperties.isMobileWxEnabled()) {
            return;
        }

        String ticket = this.fetchJsApiTicket();
        stringRedisTemplate.opsForValue().set(JSAPI_TICKET, ticket);
        log.info("Refreshed JS API ticket.");
    }

    public String getJsapiTicket() {
        return stringRedisTemplate.opsForValue().get(JSAPI_TICKET);
    }

    private String fetchJsApiTicket() {
        String accessToken = wxAccessTokenService.getAccessToken();
        requireNonBlank(accessToken, "Wx access token must not be blank.");

        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + accessToken + "&type=jsapi";
        String resultString = restTemplate.getForObject(url, String.class);
        Map<String, String> resultMap = objectMapper.readValue(resultString, new TypeReference<>() {
        });

        String ticket = resultMap.get("ticket");
        if (isBlank(ticket)) {
            log.error("Error while fetch js api ticket{}.", resultMap);
            throw new RuntimeException("Failed to refresh JS API ticket.");
        }

        return ticket;
    }

    public QJsSdkConfig generateJsSdkConfig(String url) {
        String jsapiTicket = getJsapiTicket();
        requireNonBlank(jsapiTicket, "JS API ticket must not be blank.");

        String nonce = UUID.randomUUID().toString();
        long timestamp = Instant.now().getEpochSecond();
        String originalString = "jsapi_ticket=" + jsapiTicket +
                "&noncestr=" + nonce +
                "&timestamp=" + timestamp +
                "&url=" + url;
        String signature = sha1Hex(originalString);

        return QJsSdkConfig.builder()
                .url(url)
                .appId(wxProperties.getMobileAppId())
                .timestamp(timestamp)
                .nonce(nonce)
                .signature(signature)
                .build();
    }

}
