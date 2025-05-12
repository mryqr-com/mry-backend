package com.mryqr.common.domain.display;


import com.mryqr.common.domain.Geolocation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class GeolocationDisplayValue extends DisplayValue {
    private Geolocation geolocation;

    public GeolocationDisplayValue(String key, Geolocation geolocation) {
        super(key, DisplayValueType.GEOLOCATION_DISPLAY_VALUE);
        this.geolocation = geolocation;
    }
}
