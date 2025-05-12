package com.mryqr.core.app.domain;

import com.mryqr.core.app.domain.page.control.ControlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateAppSettingResult {
    private final Set<ControlType> newlyAddedControlTypes;
}
