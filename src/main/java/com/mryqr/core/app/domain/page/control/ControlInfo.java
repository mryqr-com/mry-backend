package com.mryqr.core.app.domain.page.control;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ControlInfo {
    private String pageId;
    private String controlId;
    private ControlType controlType;
}
