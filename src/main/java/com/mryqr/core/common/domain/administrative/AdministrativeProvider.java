package com.mryqr.core.common.domain.administrative;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdministrativeProvider {
    public static Administrative CHINA;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public void init() {
        this.loadChinaAdministrative();
        log.info("Initialised China administratives.");
    }

    private void loadChinaAdministrative() {
        if (CHINA != null) {
            return;
        }

        try {
            Resource resource = resourceLoader.getResource("classpath:administrative.json");
            CHINA = objectMapper.readValue(resource.getInputStream(), Administrative.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load China administrative information.", e);
        }
    }

}
