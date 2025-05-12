package com.mryqr.common.properties;

import com.mryqr.core.assignment.domain.Assignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PropertyService {
    private static final String HTTPS = "https";
    private static final String HTTP = "http";
    private final CommonProperties commonProperties;

    public String rootDomainName() {
        return commonProperties.getBaseDomainName().replaceAll(".*\\.(?=.*\\.)", "");
    }

    public String protocol() {
        return commonProperties.isHttpsEnabled() ? HTTPS : HTTP;
    }

    public String subdomainFor(String prefix) {
        return prefix + "." + commonProperties.getBaseDomainName();
    }

    public String apiBaseUrl() {
        return protocol() + "://" + subdomainFor("api");
    }

    public String consoleBaseUrl() {
        return protocol() + "://" + subdomainFor("console");
    }

    public String clientBaseUrl() {
        return protocol() + "://" + subdomainFor("m");
    }

    public String mobileWxAuthRedirectUrl() {
        return apiBaseUrl() + "/mobile-wx/auth2-callback";
    }

    public String pcWxAuthRedirectUrl() {
        return apiBaseUrl() + "/pc-wx/auth2-callback";
    }

    public String consoleDefaultHomeUrl() {
        return consoleBaseUrl() + "/management/my-apps";
    }

    public String clientDefaultHomeUrl() {
        return clientBaseUrl() + "/operations/my-apps";
    }

    public String consoleLoginUrl() {
        return consoleBaseUrl() + "/login";
    }

    public String clientLoginUrl() {
        return clientBaseUrl() + "/login";
    }

    public String wxPayNotifyUrl() {
        return apiBaseUrl() + "/preorders/pay-callback/wxpay";
    }

    public String submissionUrlOf(String submissionId, String pageId, String plateId) {
        return this.clientBaseUrl() + "/r/" + plateId + "/pages/" + pageId + "/" + submissionId;
    }

    public String mobileAssignmentUrlOf(Assignment assignment) {
        return this.clientBaseUrl() + "/operations/" + assignment.getAppId() + "/my-assignments/" + assignment.getId() + "/qrs";
    }

    public String pcAssignmentUrlOf(Assignment assignment) {
        return this.consoleBaseUrl() + "/apps/" + assignment.getAppId() + "/operations/my-assignments/" + assignment.getId() + "/qrs";
    }

}
