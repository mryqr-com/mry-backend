package com.mryqr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneId;

import static com.mryqr.common.utils.MryConstants.CHINA_TIME_ZONE;
import static java.util.TimeZone.getTimeZone;
import static java.util.TimeZone.setDefault;

@SpringBootApplication
public class MryApplication {

    public static void main(String[] args) {
        setDefault(getTimeZone(ZoneId.of(CHINA_TIME_ZONE)));
        SpringApplication.run(MryApplication.class, args);
    }
}
