package com.mryqr.common.wx.auth.mobile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.properties.WxProperties;
import com.mryqr.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static com.mryqr.common.exception.ErrorCode.SYSTEM_ERROR;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static java.time.Duration.ofHours;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultMobileWxAuthService implements MobileWxAuthService {
    private static final String MOBILE_WX_AUTH_ACCESS_TOKEN_PREFIX = "MobileWxAuthAccessToken:";
    private final RestTemplate restTemplate;
    private final WxProperties wxProperties;
    private final MryObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public MobileWxAuthAccessTokenInfo fetchAccessToken(String code) {
        String appId = wxProperties.getMobileAppId();
        String secret = wxProperties.getMobileAppSecret();
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret=" + secret + "&code=" + code + "&grant_type=authorization_code";
        String resultString = restTemplate.getForObject(url, String.class);
        Map<String, String> resultMap = objectMapper.readValue(resultString, new TypeReference<>() {
        });

        String openId = resultMap.get("openid");
        String unionId = resultMap.get("unionid");
        String accessToken = resultMap.get("access_token");
        if (isBlank(openId) || isBlank(unionId) || isBlank(accessToken)) {
            throw new MryException(SYSTEM_ERROR, "Failed to get mobile wx web auth access token.", mapOf("response", resultString));
        }

        //每次获取access token时，保存到redis中，便于首次登录绑定微信时获取昵称和头像
        stringRedisTemplate.opsForValue().set(MOBILE_WX_AUTH_ACCESS_TOKEN_PREFIX + unionId, accessToken, ofHours(1));

        return MobileWxAuthAccessTokenInfo.builder()
                .accessToken(accessToken)
                .openId(openId)
                .unionId(unionId)
                .build();
    }

    @Override
    public MobileWxAuthUserInfo fetchUserInfo(String accessToken, String mobileWxOpenId) {
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid=" + mobileWxOpenId + "&lang=zh_CN";
        String resultString = restTemplate.getForObject(url, String.class);
        JsonNode jsonNode = objectMapper.readTree(resultString);

        JsonNode nicknameNode = jsonNode.get("nickname");
        JsonNode headImageNode = jsonNode.get("headimgurl");
        if (nicknameNode == null || isBlank(nicknameNode.textValue())) {
            throw new MryException(SYSTEM_ERROR, "Failed to get mobile wx user info.", mapOf("response", resultString));
        }

        return MobileWxAuthUserInfo.builder()
                .openId(mobileWxOpenId)
                .nickname(nicknameNode.textValue())
                .headerImageUrl(headImageNode != null ? headImageNode.textValue() : null)
                .build();
    }

    @Override
    public Optional<String> getAccessToken(String unionId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(MOBILE_WX_AUTH_ACCESS_TOKEN_PREFIX + unionId));
    }

}
