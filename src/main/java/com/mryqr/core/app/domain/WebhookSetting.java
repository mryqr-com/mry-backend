package com.mryqr.core.app.domain;

import com.mryqr.core.common.validation.url.http.HttpUrl;
import com.mryqr.core.common.validation.url.webhook.WebhookUrl;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.core.common.exception.MryException.requestValidationException;
import static com.mryqr.core.common.utils.MryConstants.MAX_URL_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class WebhookSetting {
    private final boolean enabled;

    private boolean notAccessible;

    @HttpUrl
    @WebhookUrl
    @Size(max = MAX_URL_LENGTH)
    private String url;

    @Size(max = 50)
    private String username;

    @Size(min = 8, max = 50)
    private String password;

    public static WebhookSetting create() {
        return WebhookSetting.builder().build();
    }

    public void correct() {
        this.notAccessible = false;//每次保存时自动清空

        if (enabled) {
            if (isBlank(url)) {
                throw requestValidationException("url must not be blank.");
            }

            if (isBlank(username)) {
                throw requestValidationException("username must not be blank.");
            }

            if (isBlank(password)) {
                throw requestValidationException("password must not be blank.");
            }
        } else {
            url = null;
            username = null;
            password = null;
        }
    }

    public void deactivate() {
        this.notAccessible = true;
    }

}
