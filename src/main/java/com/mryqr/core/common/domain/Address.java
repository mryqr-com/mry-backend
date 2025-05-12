package com.mryqr.core.common.domain;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Address {
    private static final String ADDRESS_JOINNER = "/";

    @Size(max = 20)
    private final String province;

    @Size(max = 20)
    private final String city;

    @Size(max = 20)
    private final String district;

    @Size(max = 100)
    private final String address;

    public static String joinAddress(String... addressPart) {
        return String.join(ADDRESS_JOINNER, addressPart);
    }

    public boolean isFilled() {
        return isNotBlank(province) ||
                isNotBlank(city) ||
                isNotBlank(district) ||
                isNotBlank(address);
    }

    public Set<String> indexedValues() {
        if (isBlank(province)) {
            return null;
        }

        Set<String> results = new HashSet<>();
        results.add(province);
        if (isNotBlank(city)) {
            results.add(joinAddress(province, city));
        }

        if (isNotBlank(city) && isNotBlank(district)) {
            results.add(joinAddress(province, city, district));
        }
        return Set.copyOf(results);
    }

    public String toText() {
        return Stream.of(province,
                        city,
                        district,
                        address)
                .filter(StringUtils::isNotBlank).collect(joining());
    }

}
