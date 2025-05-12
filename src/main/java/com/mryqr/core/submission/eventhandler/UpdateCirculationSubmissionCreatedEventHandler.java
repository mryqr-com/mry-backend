package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.mryqr.common.domain.user.User.NOUSER;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateCirculationSubmissionCreatedEventHandler extends AbstractDomainEventHandler<SubmissionCreatedEvent> {
    private final AppRepository appRepository;
    private final QrRepository qrRepository;

    @Override
    public int priority() {
        return -10;//由于稍后要通过QR的circulationOptionId计算属性值，因此必须早于其他Handler而执行
    }

    @Override
    protected void doHandle(SubmissionCreatedEvent event) {
        App app = appRepository.cachedById(event.getAppId());
        Optional<String> optionIdOptional = app.circulationStatusAfterSubmission(event.getPageId());
        optionIdOptional.ifPresent(optionId -> {
            QR qr = qrRepository.byId(event.getQrId());
            if (qr.doUpdateCirculationStatus(optionId, NOUSER)) {
                qrRepository.save(qr);
            }
        });
    }
}
