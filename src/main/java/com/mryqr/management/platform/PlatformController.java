package com.mryqr.management.platform;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.management.platform.command.PlatformCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/platform")
public class PlatformController {
    private final PlatformCommandService platformCommandService;
    private final MryRateLimiter mryRateLimiter;

    @PostMapping(value = "/qr-generation-record")
    public void count() {
        mryRateLimiter.applyFor("Platform:qrGeneration", 100);
        platformCommandService.recordQrGeneration();
    }
}
