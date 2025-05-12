package com.mryqr.common.wx.accesstoken;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mryqr.common.properties.WxProperties;
import com.mryqr.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class WxAccessTokenService {
    private static final String WX_ACCESS_TOKEN = "Wx:AccessToken";
    private final RestTemplate restTemplate;
    private final WxProperties wxProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final MryObjectMapper objectMapper;

    @Retryable(backoff = @Backoff(delay = 1000, multiplier = 3))
    public void refreshAccessToken() {
        if (!wxProperties.isMobileWxEnabled()) {
            return;
        }

        String access_token = fetchAccessToken();
        stringRedisTemplate.opsForValue().set(WX_ACCESS_TOKEN, access_token);
        log.info("Refreshed wx access token.");
    }

    public String getAccessToken() {
        return stringRedisTemplate.opsForValue().get(WX_ACCESS_TOKEN);
    }

    private String fetchAccessToken() {
        String appId = wxProperties.getMobileAppId();
        String secret = wxProperties.getMobileAppSecret();
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + secret;
        String resultString = restTemplate.getForObject(url, String.class);
        Map<String, String> resultMap = objectMapper.readValue(resultString, new TypeReference<>() {
        });

        String token = resultMap.get("access_token");
        if (isBlank(token)) {
            throw new RuntimeException("Failed to refresh wx access token: " + resultString);
        }

        return token;
    }

}
