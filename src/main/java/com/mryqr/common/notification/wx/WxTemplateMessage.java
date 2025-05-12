package com.mryqr.common.notification.wx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class WxTemplateMessage {
    private final String touser;
    private final String template_id;
    private final String url;
    private final Map<String, ValueItem> data;

    public static ValueItem valueItemOf(String value, String color) {
        return ValueItem.builder().value(isNotBlank(value) ? value : "无").color(color).build();
    }

    public static ValueItem valueItemOf(String value) {
        return valueItemOf(value, "#173177");
    }

}
