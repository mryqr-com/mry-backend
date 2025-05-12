package com.mryqr.common.about;

import com.mryqr.common.ratelimit.MryRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

import static java.time.ZonedDateTime.now;

@Validated
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AboutController {
    private final ZonedDateTime deployTime = now();
    private final Environment environment;
    private final MryRateLimiter mryRateLimiter;

    @GetMapping(value = "/about")
    public QAboutInfo about() {
        mryRateLimiter.applyFor("About", 10);
        String buildTime = environment.getProperty("buildTime");
        String gitRevision = environment.getProperty("gitRevision");
        String gitBranch = environment.getProperty("gitBranch");
        String environment = this.environment.getActiveProfiles()[0];
        String deployTime = this.deployTime.toString();

        return QAboutInfo.builder()
                .buildTime(buildTime)
                .deployTime(deployTime)
                .gitRevision(gitRevision)
                .gitBranch(gitBranch)
                .environment(environment)
                .build();
    }

    @GetMapping("/favicon.ico")
    public void dummyFavicon() {
        //nop
    }

}
