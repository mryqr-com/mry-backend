package com.mryqr;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.mryqr.core.common.utils.MryConstants.CHINA_TIME_ZONE;
import static java.time.ZoneId.of;
import static java.util.TimeZone.getTimeZone;
import static java.util.TimeZone.setDefault;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class MryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MryApplication.class, args);
    }

    @PostConstruct
    void init() {
        setDefault(getTimeZone(of(CHINA_TIME_ZONE)));
    }

}
