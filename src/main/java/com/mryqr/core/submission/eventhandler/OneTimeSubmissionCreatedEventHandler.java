package com.mryqr.core.submission.eventhandler;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.OneTimeDomainEventHandler;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_CREATED;
import static com.mryqr.core.common.domain.user.User.NOUSER;

@Slf4j
@Component
@RequiredArgsConstructor
public class OneTimeSubmissionCreatedEventHandler extends OneTimeDomainEventHandler {
    private final AppRepository appRepository;
    private final QrRepository qrRepository;

    @Override
    public int priority() {
        return -1;//由于稍后要通过QR的circulationOptionId计算属性值，因此必须早于其他Handler而执行
    }

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == SUBMISSION_CREATED;
    }

    @Override
    protected void doHandle(DomainEvent domainEvent) {
        SubmissionCreatedEvent event = (SubmissionCreatedEvent) domainEvent;

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
