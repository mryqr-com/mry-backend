package com.mryqr.core.plate.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.user.User.NOUSER;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncPlateGroupFromQrTask implements RepeatableTask {
    private final QrRepository qrRepository;
    private final PlateRepository plateRepository;

    public void run(String qrId) {
        qrRepository.byIdOptional(qrId).ifPresent(qr -> {
            Plate plate = plateRepository.byId(qr.getPlateId());
            plate.syncGroupFromQr(qr, NOUSER);
            plateRepository.save(plate);
            log.info("Updated group id for plate of qr[{}].", qrId);
        });
    }

}
