package com.mryqr.core.plate.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.plate.domain.PlateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.user.User.NOUSER;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnbindPlateFromQrTask implements RepeatableTask {
    private final PlateRepository plateRepository;

    public void run(String qrId) {
        plateRepository.byQrIdOptional(qrId).ifPresent(plate -> {
            plate.unBind(NOUSER);

            plateRepository.save(plate);
            log.info("Unbound plate for qr[{}].", qrId);
        });
    }

}
