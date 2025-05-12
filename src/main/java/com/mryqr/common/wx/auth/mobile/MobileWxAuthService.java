package com.mryqr.common.wx.auth.mobile;

import java.util.Optional;

public interface MobileWxAuthService {
    MobileWxAuthAccessTokenInfo fetchAccessToken(String code);

    MobileWxAuthUserInfo fetchUserInfo(String accessToken, String mobileWxOpenId);

    Optional<String> getAccessToken(String unionId);
}
