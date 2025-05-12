package com.mryqr.core.app.domain.page;

import com.mryqr.core.app.domain.page.control.ControlInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class PageInfo {
    private String pageId;
    private Set<ControlInfo> controlInfos;

    public boolean isFillable() {
        return controlInfos.stream().anyMatch(info -> info.getControlType().isFillable());
    }
}
