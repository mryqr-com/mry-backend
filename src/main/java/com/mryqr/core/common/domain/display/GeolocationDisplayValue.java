package com.mryqr.core.common.domain.display;


import com.mryqr.core.common.domain.Geolocation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.core.common.domain.display.DisplayValueType.GEOLOCATION_DISPLAY_VALUE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class GeolocationDisplayValue extends DisplayValue {
    private Geolocation geolocation;

    public GeolocationDisplayValue(String key, Geolocation geolocation) {
        super(key, GEOLOCATION_DISPLAY_VALUE);
        this.geolocation = geolocation;
    }
}
