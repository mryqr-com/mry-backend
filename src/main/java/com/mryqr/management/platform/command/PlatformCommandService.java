package com.mryqr.management.platform.command;


import com.mryqr.management.platform.domain.PlatformRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformCommandService {
    private final PlatformRepository platformRepository;

    public void recordQrGeneration() {
        this.platformRepository.recordQrGeneration();
    }
}
