package com.mryqr.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class TestUtils {
    public static String dateStringOf(Instant instant) {
        Date myDate = Date.from(instant);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        return formatter.format(myDate);
    }
}
