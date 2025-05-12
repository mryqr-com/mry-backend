package com.mryqr.core.common.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Objects;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Geolocation {
    public static final int MAX_NOTE_LENGTH = 100;

    @Valid
    @NotNull
    private final Geopoint point;

    @Valid
    @NotNull
    private final Address address;

    @Size(max = MAX_NOTE_LENGTH)
    private final String note;

    public float distanceFrom(Geolocation geolocation) {
        return this.point.distanceFrom(geolocation.getPoint());
    }

    public boolean isPositioned() {
        return point != null && point.isPositioned();
    }

    public Set<String> indexedValues() {
        if (address == null || !isPositioned()) {
            return null;
        }
        return address.indexedValues();
    }

    public String toText() {
        String pureAddressText = getPureText();
        return isNotBlank(note) ? pureAddressText + "(" + note + ")" : pureAddressText;
    }

    private String getPureText() {
        if (Objects.equals(address.getProvince(), address.getCity())) {//直辖市省份和城市同名
            return address.getCity() + address.getDistrict() + address.getAddress();
        }

        return address.getProvince() + address.getCity() + address.getDistrict() + address.getAddress();
    }

}
