package com.mryqr.utils;

import com.mryqr.core.app.domain.page.control.ControlType;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

public class TestUtils {
    public static String dateStringOf(Instant instant) {
        Date myDate = Date.from(instant);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        return formatter.format(myDate);
    }

    public static Set<ControlType> allControlTypesExcept(ControlType... controlTypes) {
        HashSet<ControlType> finalControlTypes = Arrays.stream(ControlType.values()).collect(toCollection(HashSet::new));
        finalControlTypes.removeAll(Set.of(controlTypes));
        return finalControlTypes;
    }
}
