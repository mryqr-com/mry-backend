package com.mryqr.common.domain.display;


import com.mryqr.common.domain.CountedItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.common.domain.display.DisplayValueType.ITEM_COUNT_DISPLAY_VALUE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class ItemCountDisplayValue extends DisplayValue {
    private List<CountedItem> items;

    public ItemCountDisplayValue(String key, List<CountedItem> items) {
        super(key, ITEM_COUNT_DISPLAY_VALUE);
        this.items = items;
    }
}
