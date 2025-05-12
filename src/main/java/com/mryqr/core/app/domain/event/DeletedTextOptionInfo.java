package com.mryqr.core.app.domain.event;

import com.mryqr.core.app.domain.page.control.ControlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class DeletedTextOptionInfo {
    private String controlId;
    private String pageId;
    private ControlType controlType;
    private String optionId;
}
