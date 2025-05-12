package com.mryqr.core.common.domain.display;


import com.mryqr.core.common.domain.Address;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class AddressDisplayValue extends DisplayValue {
    private Address address;

    public AddressDisplayValue(String key, Address address) {
        super(key, DisplayValueType.ADDRESS_DISPLAY_VALUE);
        this.address = address;
    }
}
