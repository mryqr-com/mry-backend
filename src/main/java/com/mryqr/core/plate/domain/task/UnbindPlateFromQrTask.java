package com.mryqr.core.plate.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.plate.domain.PlateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.common.domain.user.User.NO_USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnbindPlateFromQrTask implements RetryableTask {
    private final PlateRepository plateRepository;

    public void run(String qrId) {
        plateRepository.byQrIdOptional(qrId).ifPresent(plate -> {
            plate.unBind(NO_USER);

            plateRepository.save(plate);
            log.info("Unbound plate for qr[{}].", qrId);
        });
    }

}
