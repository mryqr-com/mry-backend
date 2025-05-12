package com.mryqr.core.common.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Geopoint {
    private static final float EARTH_RADIUS_METERS = 6371000;

    @Min(-180)
    @Max(180)
    private final Float longitude;//注意：记录处于mongodb的限制，longitude必须在前面

    @Min(-90)
    @Max(90)
    private final Float latitude;

    public float distanceFrom(Geopoint that) {
        return distanceBetween(this.longitude, this.latitude, that.longitude, that.latitude);
    }

    private float distanceBetween(float lng1, float lat1, float lng2, float lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return (float) (EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    public boolean isPositioned() {
        return longitude != null && latitude != null;
    }
}
