package com.mryqr.core.common.domain;

import com.mryqr.core.common.utils.Identified;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.app.domain.page.control.FItemCountControl.MAX_MAX_PER_ITEM_COUNT;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CountedItem implements Identified {
    @NotNull
    @ShortUuid
    private final String id;

    @NotNull
    @ShortUuid
    private final String optionId;

    @Min(1)
    @Max(MAX_MAX_PER_ITEM_COUNT)
    private final int number;

    @Override
    public String getIdentifier() {
        return id;
    }
}
