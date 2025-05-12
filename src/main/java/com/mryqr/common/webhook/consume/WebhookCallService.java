package com.mryqr.common.webhook.consume;

import com.mryqr.common.email.MryEmailSender;
import com.mryqr.common.properties.CommonProperties;
import com.mryqr.common.utils.CommonUtils;
import com.mryqr.common.utils.MryObjectMapper;
import com.mryqr.common.webhook.WebhookPayload;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.WebhookSetting;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.member.domain.TenantCachedMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static java.lang.Integer.parseInt;
import static java.util.Set.copyOf;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookCallService {
    private static final int MAX_ALLOWED_FAILURE_COUNT = 500;
    private final WebhookCaller webhookCaller;
    private final MryObjectMapper mryObjectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AppRepository appRepository;
    private final MemberRepository memberRepository;
    private final MryEmailSender mryEmailSender;

    private final CommonProperties commonProperties;

    public void call(WebhookPayload payload, String appId, WebhookSetting setting) {
        if (!setting.isEnabled()) {
            log.warn("Webhook is not enabled, skip call webhook for: {}.", mryObjectMapper.writeValueAsString(payload));
            return;
        }

        if (setting.isNotAccessible()) {
            log.warn("Webhook is marked as not accessible, skip call webhook for: {}.", mryObjectMapper.writeValueAsString(payload));
            return;
        }

        if (isBlank(setting.getUrl())) {
            log.warn("Webhook URL is empty, skip call webhook for: {}.", mryObjectMapper.writeValueAsString(payload));
            return;
        }

        if (!commonProperties.isWebhookAllowLocalhost() &&
            !Objects.equals(payload.getTenantId(), MRY_MANAGE_TENANT_ID) &&
            setting.getUrl().contains("localhost")) {
            log.warn("Webhook URL cannot include localhost, skip call webhook for: {}", mryObjectMapper.writeValueAsString(payload));
            return;
        }

        try {
            webhookCaller.call(payload, setting);//将重试3次
            log.debug("Called app[{}] webhook[{}] with payload:{}.", appId, setting.getUrl(), mryObjectMapper.writeValueAsString(payload));
            resetFailureCountFor(appId);//只要成功即清空，让后续webhook可正常访问
        } catch (RestClientException ex) {
            log.error("Error while call app[{}] webhook[{}] with payload:{}.",
                    appId, setting.getUrl(), mryObjectMapper.writeValueAsString(payload), ex);

            stringRedisTemplate.opsForValue().increment(failureCountKey(appId));
            String countString = stringRedisTemplate.opsForValue().get(failureCountKey(appId));
            if (countString != null) {
                int failureCount = parseInt(countString);
                if (failureCount >= MAX_ALLOWED_FAILURE_COUNT) {
                    appRepository.cachedByIdOptional(appId).ifPresent(app -> {
                        log.warn("App[{}] webhook has failed continuously for more than {} times, deactivate it.", app.getId(), appId);
                        app.deactivateWebhook(NO_USER);
                        appRepository.save(app);
                        emailNotifyNotAccessible(setting, app);
                    });
                }
            }
        }
    }

    private void emailNotifyNotAccessible(WebhookSetting setting, App app) {
        List<TenantCachedMember> appManagers = memberRepository.cachedByIds(copyOf(app.getManagers()), app.getTenantId());
        List<TenantCachedMember> tenantAdmins = memberRepository.cachedAllActiveTenantAdmins(app.getTenantId());
        List<String> emails = Stream.concat(appManagers.stream(), tenantAdmins.stream())
                .map(TenantCachedMember::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(toImmutableList());

        if (isEmpty(emails)) {
            return;
        }

        List<String> maskedEmails = emails.stream().map(CommonUtils::maskMobileOrEmail).collect(toImmutableList());
        log.info("Email notify app[{}] webhook not accessible: {}.", app.getId(), maskedEmails);
        mryEmailSender.notifyWebhookNotAccessible(emails, app.getName(), setting.getUrl());
    }

    public void resetFailureCountFor(String appId) {
        stringRedisTemplate.opsForValue().set(failureCountKey(appId), "0");
    }

    private String failureCountKey(String appId) {
        return "WebhookFailCount:" + appId;
    }

}
