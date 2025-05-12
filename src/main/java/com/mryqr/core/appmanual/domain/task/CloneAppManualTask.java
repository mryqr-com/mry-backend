package com.mryqr.core.appmanual.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.appmanual.domain.AppManual;
import com.mryqr.core.appmanual.domain.AppManualFactory;
import com.mryqr.core.appmanual.domain.AppManualRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.mryqr.common.domain.user.User.NO_USER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloneAppManualTask implements RetryableTask {
    private final AppManualRepository appManualRepository;
    private final AppManualFactory appManualFactory;
    private final AppRepository appRepository;

    public void run(String sourceAppId, String destinationAppId) {
        appManualRepository.byAppIdOptional(sourceAppId).ifPresent(source -> {
            if (isNotBlank(source.getContent())) {
                Optional<AppManual> destinationOptional = appManualRepository.byAppIdOptional(destinationAppId);
                if (destinationOptional.isEmpty()) {
                    appRepository.cachedByIdOptional(destinationAppId).ifPresent(app -> {
                        AppManual appManual = appManualFactory.create(app, source.getContent(), NO_USER);
                        appManualRepository.save(appManual);
                        log.info("Cloned app manual from app[{}] to app[{}].", sourceAppId, destinationAppId);
                    });
                }
            }
        });
    }
}
