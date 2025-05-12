package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.core.app.domain.page.control.ControlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class DeletedControlInfo {
    private String pageId;
    private String controlId;
    private IndexedField indexedField;
    private ControlType controlType;
}
